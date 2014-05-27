package org.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.api.AddFacilityRetrofitSpiceRequest;
import org.columbia.sel.facilitator.api.FacilitiesWithinRetrofitSpiceRequest;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.event.FacilityPlacedEvent;
import org.columbia.sel.facilitator.event.MapChangedEvent;
import org.columbia.sel.facilitator.fragment.AddFacilityMapFragment;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

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
import android.widget.EditText;
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
	
	@InjectView(R.id.type)
	EditText mTypeEditText;
	
	@InjectView(R.id.location)
	EditText mLocationEditText;

	@Inject
	LocationManager lm;

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

		// Grab a reference to the map fragment.
		FragmentManager fragmentManager = getFragmentManager();
		mMapFragment = (AddFacilityMapFragment) fragmentManager
				.findFragmentById(R.id.fragment_map);

		this.setupLocationListener();
		this.zoomToMyLocation();

	}

	@Override
	protected void onStart() {
		super.onStart();
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
	 * TODO: Consider moving location listening to its own class, use event bus
	 * to communicate changes.
	 */
	private void setupLocationListener() {
		Log.i(TAG, "setupLocationListener");
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Log.i(TAG,
						"=============> LOCATION UPDATED: "
								+ location.toString());
				mMyLocation = location;
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.i(TAG, "=============> STATUS CHANGES: " + provider);
			}

			public void onProviderEnabled(String provider) {
				Log.i(TAG, "=============> PROVIDER ENABLED: " + provider);
			}

			public void onProviderDisabled(String provider) {
				Log.i(TAG, "=============> PROVIDER DISABLED: " + provider);
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10,
				locationListener);
	}

	/**
	 * Center the map on my last known location.
	 */
	private void zoomToMyLocation() {
		Log.i(TAG, "zoomToMyLocation");

		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (loc == null) {
			Toast.makeText(this, "Current location cannot be determined.", Toast.LENGTH_SHORT).show();
		} else {
			mMapFragment.scrollToLocation(loc);			
		}

	}

	/**
	 * Click handler for "Add New Facility" Button.
	 * 
	 * For the moment, I'm leaving the large swath of commented code as reference. It shows
	 * how Jackson and Retrofit are used independent of RoboSpice.
	 * 
	 * @param view
	 */
	public void onAddNewFacility(View view) {
		Log.i(TAG, "Adding new facility.");

		
		// TESTING Jackson2 conversion... works.
//		ObjectMapper mapper = new ObjectMapper();
//		
//		try {
//			String json = mapper.writeValueAsString(newFacility);
//			Log.i(TAG, json);
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// TESTING Retrofit using JacksonConverter... works!
//		JacksonConverter converter = new JacksonConverter(new ObjectMapper());
//		RestAdapter restAdapter = new RestAdapter.Builder()
//				.setConverter(converter)
//				.setEndpoint("http://23.21.86.131:3000/api/v1")
//				.build();
//
//		FacilitatorApi service = restAdapter.create(FacilitatorApi.class);
//
//		Facility newFacility = new Facility();
//		newFacility.setName("Testing");
//		
//		service.addFacility(newFacility, new Callback<Facility>() {
//
//			@Override
//			public void failure(RetrofitError arg0) {
//				// TODO Auto-generated method stub
//				Log.e(TAG, "??????????????????      " + arg0.getMessage());
//				throw arg0;
//			}
//
//			@Override
//			public void success(Facility facility, Response arg1) {
//				// TODO Auto-generated method stub
//				Log.i(TAG,
//						"((((((((((((((((((( NEW FACILITY ADDED )))))))))))))))))))");
//				Log.i(TAG, facility.getName());
//			}
//
//		});
		

		String name = this.mNameEditText.getText().toString();
		String type = this.mTypeEditText.getText().toString();
		
		if(mFacilityGeoPoint == null || name.equals("") || type.equals("")) {
			Toast.makeText(AddFacilityActivity.this, "Please enter name, type, and location.", Toast.LENGTH_SHORT).show();
			return;
		}

		 
		Facility facility = new Facility();
		facility.setName(name);
		facility.getProperties().setType(type);
		
		// TODO: THIS IS FAKED. Should we use a drop-down for sector?
		facility.getProperties().setSector("health");
		
		facility.getProperties().setCheckins(0);
		facility.getCoordinates().add(mFacilityGeoPoint.getLongitude());
		facility.getCoordinates().add(mFacilityGeoPoint.getLatitude());
		mAddFacilityRequest = new AddFacilityRetrofitSpiceRequest(facility);
		getSpiceManager().execute(mAddFacilityRequest, "addfacility", DurationInMillis.ONE_SECOND, new AddFacilityRequestListener());
	}

	/**
	 * onClick handler for button which populates Location field with the user's current location.
	 * @param view
	 */
	public void onPopulateLocationClick(View view) {
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		String locationValue;

		if (loc != null) {
			mFacilityGeoPoint = new GeoPoint(loc);
			locationValue = loc.getLatitude() + ", " + loc.getLongitude();
			mLocationEditText.setText(locationValue);
		} else {
			Toast.makeText(AddFacilityActivity.this,
					"Current location could not be determined.",
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Handle MapChangedEvent, fired when the user zooms or scrolls (after defined delay).
	 * 
	 * When the map is changed, request facilities within the new map bounds.
	 * 
	 * @param event
	 */
	@Subscribe public void handleMapChanged(MapChangedEvent event) {
		Log.i(TAG, "handleMapChanged");
		BoundingBoxE6 bb = event.getBoundingBox();
		double n = (bb.getLatNorthE6() / 1E6);
		double s = (bb.getLatSouthE6() / 1E6);
		double e = (bb.getLonEastE6() / 1E6);
		double w = (bb.getLonWestE6() / 1E6);
		Log.i(TAG, n + ", " + w + ", " + s + ", " + e);
		facilitiesWithinRequest = new FacilitiesWithinRetrofitSpiceRequest(String.valueOf(s), String.valueOf(w), String.valueOf(n), String.valueOf(e));
		getSpiceManager().execute(facilitiesWithinRequest, "facilities", DurationInMillis.ONE_SECOND, new FacilitiesRequestListener());
	}
	
	/**
	 * Handle FacilityPlacedEvent, fired when the user places a new Facility on the map.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleFacilityPlaced(FacilityPlacedEvent event) {
		Log.i(TAG, "handleFacilityPlaced");
		mFacilityGeoPoint = event.getGeoPoint();
		mLocationEditText.setText(mFacilityGeoPoint.getLatitude() + ", " + mFacilityGeoPoint.getLongitude());
	}

	// ============================================================================================
	// INNER CLASSES
	// ============================================================================================

	/**
	 * Used by RoboSpice to handle the response for known Facilities.
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
	 * @author Jonathan Wohl
	 *
	 */
	public final class AddFacilityRequestListener implements
			RequestListener<Facility> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			Toast.makeText(AddFacilityActivity.this, "Failed to add new facility.",
					Toast.LENGTH_SHORT).show();
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
