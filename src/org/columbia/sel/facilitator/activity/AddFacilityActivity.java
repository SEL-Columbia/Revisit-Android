package org.columbia.sel.facilitator.activity;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.activity.FacilityMapListActivity.FacilitiesRequestListener;
import org.columbia.sel.facilitator.api.AddFacilityRetrofitSpiceRequest;
import org.columbia.sel.facilitator.api.FacilitiesNearRetrofitSpiceRequest;
import org.columbia.sel.facilitator.api.FacilitiesWithinRetrofitSpiceRequest;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.event.MapChangedEvent;
import org.columbia.sel.facilitator.fragment.FacilityMapFragment;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;

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

public class AddFacilityActivity extends BaseActivity {
	@InjectView (R.id.name) EditText mNameEditText;
	@InjectView (R.id.type) EditText mTypeEditText;
	@InjectView (R.id.location) EditText mLocationEditText;
	
	@Inject LocationManager lm;
	
	private Location mFacilityLocation;
	private Location mMyLocation;
	
	private FacilityMapFragment mMapFragment;
	
	private AddFacilityRetrofitSpiceRequest mAddFacilityRequest;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_facility);
		
		ButterKnife.inject(this);
		
		// This is just to test the incoming data passed from ODK, leaving it here
		// for reference.
		Intent i = getIntent();
		String action = i.getAction();
		
		// Grab a reference to the map fragment.
		FragmentManager fragmentManager = getFragmentManager();
		mMapFragment = (FacilityMapFragment) fragmentManager.findFragmentById(R.id.fragment_map);
		
		this.setupLocationListener();
		this.zoomToMyLocation();
		
	}
	
	@Override
    protected void onStart() {
        super.onStart();
//        getSpiceManager().execute(facilitiesWithinRequest, "facilities", DurationInMillis.ONE_SECOND, new FacilitiesRequestListener());
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
//	            openSettings();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * TODO: Consider moving location listening to its own class, use event bus to communicate changes.
	 */
	private void setupLocationListener() {
		Log.i(TAG, "setupLocationListener");
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	Log.i(TAG, "=============> LOCATION UPDATED: " + location.toString());
		    	mMyLocation = location;
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {
		    	Log.i(TAG, "=============> STATUS CHANGES: " + provider);
		    }

		    public void onProviderEnabled(String provider) {
		    	Log.i(TAG, "=============> PROVIDER ENABLED: " + provider);
		    }

		    public void onProviderDisabled(String provider) {
		    	Log.i(TAG, "=============> PROVIDER DISABLED: " + provider);
		    }
		  };

		// Register the listener with the Location Manager to receive location updates
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
	}
	
	/**
	 * Center the map on my last known location.
	 * TODO: Remove the hard-coded defaults.
	 */
	private void zoomToMyLocation() {
		Log.i(TAG, "zoomToMyLocation");
		
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if (loc == null) {
			loc = new Location(LocationManager.GPS_PROVIDER);
			loc.setLatitude(40.73614);
			loc.setLongitude(-73.98354);
		}

		mMapFragment.zoomToLocation(loc);
	}
	
	/**
	 * Click handler for "Add New Facility" Button
	 * @param view
	 */
	public void onAddNewFacility(View view) {
		Log.i(TAG, "Adding new facility.");
		
		String name = this.mNameEditText.getText().toString();
		String type = this.mTypeEditText.getText().toString();

		HashMap<String, String> props = new HashMap<String, String>();
		props.put("type", type);
		
		// TODO: This should come from somewhere real... dropdown in the add page?
		props.put("sector", "health");
		
		ArrayList<Double> coords = new ArrayList<Double>();
		coords.add(mFacilityLocation.getLongitude());
		coords.add(mFacilityLocation.getLatitude());
		
		Facility facility = new Facility();
		facility.name = name;
		mAddFacilityRequest = new AddFacilityRetrofitSpiceRequest(facility);
		getSpiceManager().execute(mAddFacilityRequest, "addfacility", DurationInMillis.ONE_SECOND, new FacilitiesRequestListener());
	}
	
	public void onPopulateLocationClick(View view) {
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		String locationValue;
		
		if (loc != null) {
			mFacilityLocation = loc;
			locationValue = loc.getLatitude() + ", " + loc.getLongitude();
			mLocationEditText.setText(locationValue);
		} else {
			Toast.makeText(AddFacilityActivity.this, "Current location could not be determined.", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Handle MapChangedEvent, fired when the user zooms or scrolls (after defined delay).
	 * @param event
	 */
	@Subscribe public void handleMapChanged(MapChangedEvent event) {
		Log.i(TAG, "handleMapChanged");
	}
	
	
	// ============================================================================================
    // INNER CLASSES
    // ============================================================================================

    public final class FacilitiesRequestListener implements RequestListener<Facility> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
        	Log.e(TAG, spiceException.toString());
            Toast.makeText(AddFacilityActivity.this, "Failed to addfacility.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(final Facility result) {
            Log.i(TAG, "Facility Added!");
            Toast.makeText(AddFacilityActivity.this, result.name + " added!!!", Toast.LENGTH_SHORT).show();
//            bus.post(new FacilitiesLoadedEvent(result));
        }
    }
}
