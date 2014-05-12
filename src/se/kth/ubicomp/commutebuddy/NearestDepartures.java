package se.kth.ubicomp.commutebuddy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import se.kth.ubicomp.slcompanion.model.Departure;
import se.kth.ubicomp.slcompanion.model.Station;
import se.kth.ubicomp.slcompanion.model.WeatherInfo;
import se.kth.ubicomp.slcompanion.util.SlResRobotService;
import se.kth.ubicomp.slcompanion.util.YahooWeather;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class NearestDepartures extends ActionBarActivity implements
		LocationListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private static Location latestLocation;

	private static LocationManager locationManager;

	private static boolean includeBus = false;

	private static boolean largeRadius = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isOnline()) {
			Toast.makeText(this,
					"You need an internet connection for this app",
					Toast.LENGTH_LONG).show();
			return;
		}
		setContentView(R.layout.activity_nearest_departures);
		if (savedInstanceState != null) {
			NearestDepartures.includeBus = savedInstanceState
					.getBoolean("showBuses");
			NearestDepartures.largeRadius = savedInstanceState
					.getBoolean("largeRadius");
			((MenuItem) findViewById(R.id.action_bus)).setChecked(includeBus);
			((MenuItem) findViewById(R.id.action_range))
					.setChecked(largeRadius);
		}

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		latestLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

	}

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (locationManager != null)
			locationManager.removeUpdates(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nearest_departures, menu);
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_bus:
			NearestDepartures.includeBus = !NearestDepartures.includeBus;
			item.setChecked(includeBus);
			break;
		case R.id.action_range:
			NearestDepartures.largeRadius = !NearestDepartures.largeRadius;
			item.setChecked(largeRadius);
			break;
		default:
			break;
		}
		refreshData();
		return super.onOptionsItemSelected(item);
	}

	private void refreshData() {
		PlaceholderFragment fragment = (PlaceholderFragment) getSupportFragmentManager()
				.getFragments().get(0);

		fragment.fetchWeatherInfo();
		fragment.refreshDepartures();

	}

	/**
	 * persist user settings
	 */
	protected void onSaveInstanceState(Bundle instanceState) {
		super.onSaveInstanceState(instanceState);
		instanceState.putBoolean("showBuses", NearestDepartures.includeBus);
		instanceState.putBoolean("largeRadius", NearestDepartures.largeRadius);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private TextView temperatureTextView, conditionTextView;
		private WeatherInfo weatherInfo;

		private static List<Station> stationsNear;

		private ExpandableListAdapter listAdapter;
		private ExpandableListView expListView;
		private List<String> listDataHeader;
		private HashMap<String, List<String>> listDataChild;

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = inflater.inflate(
					R.layout.fragment_nearest_departures, container, false);

			temperatureTextView = (TextView) rootView
					.findViewById(R.id.tempText);
			conditionTextView = (TextView) rootView
					.findViewById(R.id.conditionText);
			temperatureTextView.setText("not available");
			conditionTextView.setText("not available");
			// get the listview
			expListView = (ExpandableListView) rootView
					.findViewById(R.id.lvExp);

			// fetch weather data
			fetchWeatherInfo();
			// fetch SL info
			refreshDepartures();

			return rootView;
		}

		private void prepareListData() {
			listDataHeader = new ArrayList<String>();
			listDataChild = new HashMap<String, List<String>>();

			// Adding data
			int currPos = 0;
			for (Station station : stationsNear) {
				listDataHeader.add(station.getName() + "("
						+ station.getDistance() + ")");
				List<String> departures = new ArrayList<String>();
				if (station.getDepartures() != null
						&& !station.getDepartures().isEmpty()) {
					for (Departure dep : station.getDepartures()) {
						departures.add(dep.getLine() + " - "
								+ dep.getDestination() + ": "
								+ dep.getMinsUntilDeparture());
					}
				} else {
					departures.add("No departures");
				}
				listDataChild.put(listDataHeader.get(currPos), departures);
				currPos++;
			}

		}

		protected class retrieve_weatherTask extends
				AsyncTask<Void, String, String> {

			@Override
			protected String doInBackground(Void... arg0) {
				String location = "906057"; // Stockholm
				YahooWeather weatherProvider = new YahooWeather(location);
				weatherInfo = weatherProvider.fetchWeatherInfo();

				return null;

			}

			protected void onPostExecute(String result) {
				temperatureTextView.setText("Temperature:"
						+ weatherInfo.getTemperature());
				conditionTextView.setText("Condition:"
						+ weatherInfo.getCondition());
				// 32 = sunny
				// 34 = fair
				// 36 = hot
				switch (weatherInfo.getConditionCode()) {
				case 32:
					Toast.makeText(getActivity(),
							"It's sunny - you should use your bike!",
							Toast.LENGTH_SHORT).show();
					break;
				case 34:
					Toast.makeText(getActivity(),
							"It's fair - you should use your bike!",
							Toast.LENGTH_SHORT).show();
					break;
				case 36:
					Toast.makeText(getActivity(),
							"It's hot - you should use your bike!",
							Toast.LENGTH_SHORT).show();
					break;

				default:
					Toast.makeText(
							getActivity(),
							"It's okay if you don't want to bike with this weather ;-)",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}

		public void fetchWeatherInfo() {
			new retrieve_weatherTask().execute();
		}

		private void refreshDepartures() {
			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				new GetStationsTask().execute(location);
			}
		}

		public void setIncludeBus(boolean b) {
			includeBus = b;
			refreshDepartures();
		}

		private class GetStationsTask extends
				AsyncTask<Location, Void, List<Station>> {
			@Override
			protected List<Station> doInBackground(Location... locations) {

				Location locationToPass = latestLocation;
				if (latestLocation.getTime() < locations[0].getTime()) {
					locationToPass = locations[0];
				}

				stationsNear = SlResRobotService.findStationsNear(locations[0],
						includeBus, largeRadius);

				// for each station find next departure:
				for (Station station : stationsNear) {
					int stationId = station.getStationId();

					List<Departure> departures = SlResRobotService
							.getDepartureList(stationId);
					station.setDepartures(departures);
				}

				return stationsNear;
			}

			@Override
			protected void onPostExecute(List<Station> stationsNearcursor) {
				// preparing list data
				prepareListData();

				listAdapter = new ExpandableListAdapter(getActivity(),
						listDataHeader, listDataChild);

				// setting list adapter
				expListView.setAdapter(listAdapter);
				expListView.expandGroup(0);
			}
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		latestLocation = location;

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

}
