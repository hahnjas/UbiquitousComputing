package se.kth.ubicomp.slcompanion.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.kth.ubicomp.slcompanion.model.Departure;
import se.kth.ubicomp.slcompanion.model.Station;
import android.database.MatrixCursor;
import android.location.Location;
import android.text.TextUtils;

public class SlResRobotService {

	private static final String ENDPOINT = "https://api.trafiklab.se/samtrafiken/resrobot/";
	private static final String APIURL = "https://api.trafiklab.se/samtrafiken/resrobotstops/";
	private static final String CARRIER_ID = "275";
	private static final String RANGE = "1500"; //range around position for which stations are looked up

	
	/**
	 * Retrieve a list of upcoming departures for a given location
	 * @param des
	 * @return
	 */
	public static List<Departure> getDepartureList(int stationId) {

		List<Departure> departures = new ArrayList<Departure>();
		
		try {
			URL url = new URL(APIURL + "GetDepartures.json"
					+ "?apiVersion=2.1&key=" + ApiKeys.RESROBOT_KEY
					+ "&coordSys=WGS84" + "&locationId=" + stationId
					+ "&timeSpan=120");

			JSONArray data = new JSONObject(Utils.fetchURL(url, "ISO-8859-1"))
					.getJSONObject("getdeparturesresult").getJSONArray(
							"departuresegment");

			MatrixCursor cursor = new MatrixCursor(new String[] { "_id",
					"direction", "number", "time", "type" }, data.length());
			for (int i = 0; i < data.length(); i++) {
				JSONObject o = data.getJSONObject(i);

				Object motObject = o.getJSONObject("segmentid").opt("mot");
				Object carrierObject = o.getJSONObject("segmentid").opt(
						"carrier");
				JSONObject departureTimeObject = o.getJSONObject("departure");

				//ectract info
				
					String timeDiffInMin = Utils.getTimeDiffInMin(((JSONObject) departureTimeObject).getString("datetime"));
					String lineNumber = ((JSONObject) carrierObject).getString("number");
					String direction = o.getString("direction");
					
					departures.add(new Departure(direction, lineNumber, timeDiffInMin));
					
					cursor.addRow(new Object[] {
							i,
							direction,
							lineNumber,
							timeDiffInMin,
							translateDisplayType(((JSONObject) motObject).getString("@displaytype")), });
			}
			return departures;
		} catch (Exception e) {
			// ignore Exception handling for now.
			e.printStackTrace();
		} 
		return departures;
	}


	public static MatrixCursor getDestinationList(String stationId) {
		try {
			URL url = new URL(APIURL + "GetDepartures.json"
					+ "?apiVersion=2.1&key=" + ApiKeys.RESROBOT_KEY
					+ "&coordSys=WGS84" + "&locationId=" + stationId
					+ "&timeSpan=120");

			JSONArray data = new JSONObject(Utils.fetchURL(url, "ISO-8859-1"))
					.getJSONObject("getdeparturesresult").getJSONArray(
							"departuresegment");

			MatrixCursor cursor = new MatrixCursor(new String[] { "_id",
					"direction", "number", "time", "type" }, data.length());
			for (int i = 0; i < data.length(); i++) {
				JSONObject o = data.getJSONObject(i);

				Object motObject = o.getJSONObject("segmentid").opt("mot");
				Object carrierObject = o.getJSONObject("segmentid").opt(
						"carrier");
				JSONObject departureTimeObject = o.getJSONObject("departure");
				
				
				
				cursor.addRow(new Object[] {
						i,
						o.getString("direction"),
						((JSONObject) carrierObject).getString("number"),
						Utils.getTimeDiffInMin(((JSONObject) departureTimeObject).getString("datetime")),
						translateDisplayType(((JSONObject) motObject).getString("@displaytype")), });
			}
			return cursor;
		} catch (Exception e) {
			// ignore Exception handling for now.
			e.printStackTrace();
		} 
		return null;
	}

	
	/**
	 * Retrieve nearby stations based on GPS coordinates
	 * 
	 * @param location
	 * @param includeBus
	 * @return
	 */
	public static List<Station> findStationsNear(Location location, boolean includeBus) {
		MatrixCursor cursor = null;
		List<Station> stations = new ArrayList<Station>();
		try {
			URL url = new URL(ENDPOINT + "StationsInZone.json"
					+ "?apiVersion=2.1&key=" + ApiKeys.RESROBOT_SOKRESA_KEY
					+ "&coordSys=WGS84&radius="+ RANGE + "&centerX="
					+ location.getLongitude() + "&centerY="
					+ location.getLatitude());

			JSONArray data = new JSONObject(Utils.fetchURL(url, "ISO-8859-1"))
					.getJSONObject("stationsinzoneresult").getJSONArray(
							"location");

			cursor = new MatrixCursor(new String[] { "_id",
					"name", "lgn", "lat","distance", "types" }, data.length());
			for (int i = 0; i < data.length(); i++) {
				JSONObject o = data.getJSONObject(i);

				Object producer = o.getJSONObject("producerlist").opt(
						"producer");
				List<Object> producerList = Utils
						.jsonMaybeArrayToList(producer);
				boolean foundProducer = false;
				for (Object p : producerList) {
					if (((JSONObject) p).getString("@id").equals(CARRIER_ID)) {
						foundProducer = true;
						break;
					}
				}
				if (!foundProducer) {
					continue;
				}

				Location l = new Location(ENDPOINT);
				l.setLongitude(o.getDouble("@x"));
				l.setLatitude(o.getDouble("@y"));
				String distance = String
				        .format("%.0fm", location.distanceTo(l));

				Object transport = o.getJSONObject("transportlist").opt(
						"transport");
				List<Object> transportList = Utils.jsonMaybeArrayToList(transport);
				Set<String> typesList = new TreeSet<String>();
				for (int j = 0; j < transportList.size(); j++) {
				    typesList.add(translateDisplayType(((JSONObject) transportList.get(j)).getString("@displaytype")));
				}
				if (!includeBus && typesList.size() == 1 && typesList.toArray()[0].equals("B")) {
				    continue;
				}
				String types = TextUtils.join(", ", typesList);

				String name = o.getString("name");
				int stationId = o.getInt("@id");
				stations.add(new Station(name, stationId, distance, types));
				cursor.addRow(new Object[] { stationId,
						name, l.getLongitude(), l.getLatitude(), distance, types });
			}
			return stations;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stations;
	}

	protected static String translateDisplayType(String type) {
	    if (type.equals("U")) {
	        return "T";
	    }
	    else if (type.equals("T")) {
	        return "U";
	    }
	    return type;
	}

}
