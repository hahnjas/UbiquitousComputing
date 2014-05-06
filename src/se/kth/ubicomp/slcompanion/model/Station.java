/**
 * 
 */
package se.kth.ubicomp.slcompanion.model;

import java.util.List;

/**
 * @author jasperh
 *
 */
public class Station {

	private String name;
	
	private int stationId;
	
	private int nextDeparture;
	
	private String distance;
	
	private String types;
	
	private List<Departure> departures;
	

	public Station(String name, int stationId,String distance, String types) {
		super();
		this.name = name;
		this.stationId = stationId;
		this.distance = distance;
		this.types = types;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNextDeparture() {
		return nextDeparture;
	}

	public void setNextDeparture(int nextDeparture) {
		this.nextDeparture = nextDeparture;
	}

	public List<Departure> getDepartures() {
		return departures;
	}

	public void setDepartures(List<Departure> departures) {
		this.departures = departures;
	}

	public int getStationId() {
		return stationId;
	}

	public String getDistance() {
		return distance;
	}


	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	
	
}
