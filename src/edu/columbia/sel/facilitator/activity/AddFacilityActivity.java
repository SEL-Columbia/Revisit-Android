package edu.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import edu.columbia.sel.facilitator.R;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import edu.columbia.sel.facilitator.api.AddFacilityRetrofitSpiceRequest;
import edu.columbia.sel.facilitator.api.FacilitiesWithinRetrofitSpiceRequest;
import edu.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import edu.columbia.sel.facilitator.event.FacilityPlacedEvent;
import edu.columbia.sel.facilitator.event.LocationChangedEvent;
import edu.columbia.sel.facilitator.event.MapChangedEvent;
import edu.columbia.sel.facilitator.fragment.AddFacilityMapFragment;
import edu.columbia.sel.facilitator.model.Facility;
import edu.columbia.sel.facilitator.model.FacilityList;
import edu.columbia.sel.facilitator.service.LocationService;
import android.app.FragmentManager;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * The AddFacilityActivity provides the interface for adding a new Facility.
 * 
 * @author Jonathan Wohl
 * 
 */
public class AddFacilityActivity extends BaseActivity {
	@InjectView(R.id.name)
	EditText mNameEditText;

	@InjectView(R.id.sector)
	Spinner mSectorEditText;

	@InjectView(R.id.type)
	EditText mTypeEditText;

	@InjectView(R.id.location)
	EditText mLocationEditText;

	private boolean mFirstRun = true;

	// Stores the location of the new facility
	private GeoPoint mFacilityGeoPoint;

	// Stores the user's current location
	private Location mMyLocation;

	// Provides the map view and related functionality
	private AddFacilityMapFragment mMapFragment;

	// The POST request that submits the new Facility
	private AddFacilityRetrofitSpiceRequest mAddFacilityRequest;

	// The GET request that retrieves known facilities within the map bounds
	private FacilitiesWithinRetrofitSpiceRequest facilitiesWithinRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_facility);

		// Inject view member variables
		ButterKnife.inject(this);

		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sectors_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mSectorEditText.setAdapter(adapter);

		// Grab a reference to the map fragment.
		FragmentManager fragmentManager = getFragmentManager();
		mMapFragment = (AddFacilityMapFragment) fragmentManager.findFragmentById(R.id.fragment_map);
	}

	@Override
	protected void onStart() {
		super.onStart();
		startService(new Intent(this, LocationService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maplist_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_mylocation:
			zoomToMyLocation();
			return true;
		case R.id.action_settings:
			// openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Center the map on my last known location.
	 */
	private void zoomToMyLocation() {
		Log.i(TAG, "zoomToMyLocation");

		mMyLocation = LocationService.getCurrentLocation();

		if (mMyLocation == null) {
			Toast.makeText(this, "Current location cannot be determined.", Toast.LENGTH_SHORT).show();
		} else {
			mMapFragment.scrollToLocation(mMyLocation);
		}
	}

	/**
	 * Click handler for "Add New Facility" Button.
	 * 
	 * For the moment, I'm leaving the large swath of commented code as
	 * reference. It shows how Jackson and Retrofit are used independent of
	 * RoboSpice.
	 * 
	 * @param view
	 */
	public void onAddNewFacility(View view) {
		Log.i(TAG, "Adding new facility.");

		// TESTING Jackson2 conversion... works.
		// ObjectMapper mapper = new ObjectMapper();
		//
		// try {
		// String json = mapper.writeValueAsString(newFacility);
		// Log.i(TAG, json);
		// } catch (JsonProcessingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// TESTING Retrofit using JacksonConverter... works!
		// JacksonConverter converter = new JacksonConverter(new
		// ObjectMapper());
		// RestAdapter restAdapter = new RestAdapter.Builder()
		// .setConverter(converter)
		// .setEndpoint("http://23.21.86.131:3000/api/v1")
		// .build();
		//
		// FacilitatorApi service = restAdapter.create(FacilitatorApi.class);
		//
		// Facility newFacility = new Facility();
		// newFacility.setName("Testing");
		//
		// service.addFacility(newFacility, new Callback<Facility>() {
		//
		// @Override
		// public void failure(RetrofitError arg0) {
		// // TODO Auto-generated method stub
		// Log.e(TAG, "??????????????????      " + arg0.getMessage());
		// throw arg0;
		// }
		//
		// @Override
		// public void success(Facility facility, Response arg1) {
		// // TODO Auto-generated method stub
		// Log.i(TAG,
		// "((((((((((((((((((( NEW FACILITY ADDED )))))))))))))))))))");
		// Log.i(TAG, facility.getName());
		// }
		//
		// });

		String name = this.mNameEditText.getText().toString();
		String type = this.mTypeEditText.getText().toString();
		String sector = this.mSectorEditText.getSelectedItem().toString();

		if (mFacilityGeoPoint == null || name.equals("") || type.equals("")) {
			Toast.makeText(AddFacilityActivity.this, "Please enter name, type, and location.", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		Facility facility = new Facility();
		facility.setName(name);
		facility.getProperties().setType(type);
		facility.getProperties().setSector(sector);

		facility.getProperties().setCheckins(0);
		facility.getCoordinates().add(mFacilityGeoPoint.getLongitude());
		facility.getCoordinates().add(mFacilityGeoPoint.getLatitude());
		mAddFacilityRequest = new AddFacilityRetrofitSpiceRequest(facility);
		getSpiceManager().execute(mAddFacilityRequest, "addfacility", DurationInMillis.ONE_SECOND,
				new AddFacilityRequestListener());
	}

	/**
	 * onClick handler for button which populates Location field with the user's
	 * current location.
	 * 
	 * @param view
	 */
	public void onPopulateLocationClick(View view) {
//		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location loc = LocationService.getCurrentLocation();
		String locationValue;

		if (loc != null) {
			mFacilityGeoPoint = new GeoPoint(loc);
			locationValue = loc.getLatitude() + ", " + loc.getLongitude();
			mLocationEditText.setText(locationValue);
		} else {
			Toast.makeText(AddFacilityActivity.this, "Current location could not be determined.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * Handle MapChangedEvent, fired when the user zooms or scrolls (after
	 * defined delay).
	 * 
	 * When the map is changed, request facilities within the new map bounds.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleMapChanged(MapChangedEvent event) {
		Log.i(TAG, "handleMapChanged");
		BoundingBoxE6 bb = event.getBoundingBox();
		double n = (bb.getLatNorthE6() / 1E6);
		double s = (bb.getLatSouthE6() / 1E6);
		double e = (bb.getLonEastE6() / 1E6);
		double w = (bb.getLonWestE6() / 1E6);
		Log.i(TAG, n + ", " + w + ", " + s + ", " + e);
		
		// cacheKey uses lat/lng so as to be unique
		String cacheKey = "facs" + n + "," + w + "," + s + "," + e;
		facilitiesWithinRequest = new FacilitiesWithinRetrofitSpiceRequest(String.valueOf(s), String.valueOf(w),
				String.valueOf(n), String.valueOf(e));
		getSpiceManager().execute(facilitiesWithinRequest, cacheKey, DurationInMillis.ONE_SECOND,
				new FacilitiesRequestListener());
	}

	/**
	 * Handle FacilityPlacedEvent, fired when the user places a new Facility on
	 * the map.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleFacilityPlaced(FacilityPlacedEvent event) {
		Log.i(TAG, "handleFacilityPlaced");
		mFacilityGeoPoint = event.getGeoPoint();
		mLocationEditText.setText(mFacilityGeoPoint.getLatitude() + ", " + mFacilityGeoPoint.getLongitude());
	}

	/**
	 * Handle LocationChangedEvent, fired when the application detects a new
	 * user location.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleLocationChanged(LocationChangedEvent event) {
		Log.i(TAG, "handleLocationChanged");

		mMyLocation = event.getLocation();

		// We only want to zoom to our location if this is the first location
		// change
		if (mFirstRun) {
			mFirstRun = false;
			this.zoomToMyLocation();
		}
	}

	// ============================================================================================
	// INNER CLASSES
	// ============================================================================================

	/**
	 * Used by RoboSpice to handle the response for known Facilities.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class FacilitiesRequestListener implements RequestListener<FacilityList> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			Toast.makeText(AddFacilityActivity.this, "Failed to load facilities.", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final FacilityList result) {
			Log.i(TAG, "Facilities Loaded: " + result.size());
			bus.post(new FacilitiesLoadedEvent(result));
		}
	}

	/**
	 * Used by RoboSpice to handle the response for adding a Facility.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class AddFacilityRequestListener implements RequestListener<Facility> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			Toast.makeText(AddFacilityActivity.this, "Failed to add new facility.", Toast.LENGTH_SHORT).show();
		}

		/**
		 * On Success, we finish the activity and start the Detail activity.
		 */
		@Override
		public void onRequestSuccess(final Facility result) {
			Log.i(TAG, "Facility Added!");
			Intent i = new Intent(AddFacilityActivity.this, FacilityDetailActivity.class);
			i.putExtra("facility", result);
			i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
			startActivity(i);
			finish();
		}
	}
}
