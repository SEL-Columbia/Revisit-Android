package org.columbia.sel.facilitator.activity;

import java.util.ArrayList;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.adapter.FacilityArrayAdapter;
import org.columbia.sel.facilitator.api.FacilitiesNearRetrofitSpiceRequest;
import org.columbia.sel.facilitator.api.FacilitiesWithinRetrofitSpiceRequest;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.event.FacilitySelectedEvent;
import org.columbia.sel.facilitator.event.MapChangedEvent;
import org.columbia.sel.facilitator.fragment.FacilityMapFragment;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.model.FacilityRepository;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FacilityMapListActivity extends BaseActivity {
	
	@Inject FacilityRepository fr;
	
	@Inject LocationManager lm;
	
	private Location mMyLocation;
	
	private FacilityMapFragment mMapFragment;
	private ListFragment mListFragment;
	private FacilityArrayAdapter mAdapter;
	
	private Boolean isLaunchedFromOdk = true;
	
	private ProgressDialog progressDialog;
	private Toast noFacilitiesToast;
	
	private FacilitiesNearRetrofitSpiceRequest facilitiesNearRequest;
	private FacilitiesWithinRetrofitSpiceRequest facilitiesWithinRequest;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_list);
		
		// This is just to test the incoming data passed from ODK, leaving it here
		// for reference.
		Intent i = getIntent();
		String action = i.getAction();
		if (action == Intent.ACTION_MAIN) {
			// launched directly, not from ODK
			isLaunchedFromOdk = false;
		} else if (action == "org.columbia.sel.facilitator.COLLECT") {
			// launched from ODK Collect
			// here we could inspect contents of Intent in case we want to 
			// perform any kind of setup based on values from ODK
		}
		
		// Grab a reference to the map fragment.
		FragmentManager fragmentManager = getFragmentManager();
		mMapFragment = (FacilityMapFragment) fragmentManager.findFragmentById(R.id.fragment_map);
		
		// Dynamically create a list fragment and setup onItemClick listener. 
		// Could create a ListFragment subclass instead?
		mListFragment = (ListFragment) fragmentManager.findFragmentById(R.id.fragment_list);
		mAdapter = new FacilityArrayAdapter(this, R.layout.facility_list_item);
		mListFragment.setListAdapter(mAdapter);
		ListView listView = mListFragment.getListView();
		OnItemClickListener myListViewClicked = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Facility f = (Facility) parent.getAdapter().getItem(position);
				// Post an event containing the clicked facility
				bus.post(new FacilitySelectedEvent(f));
			}
		};
		listView.setOnItemClickListener(  myListViewClicked );
		
		this.setupLocationListener();
		this.zoomToMyLocation();
		
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Loading");
		progressDialog.setMessage("Finding nearby facilities...");
		progressDialog.show();
		
		// TODO: Do we need to perform an initial call to fetch facilities or will the MapChangedEvent always fire?
		
//		facilitiesNearRequest = new FacilitiesNearRetrofitSpiceRequest(String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()), "10");
//		facilitiesWithinRequest = new FacilitiesWithinRetrofitSpiceRequest(String.valueOf(s), String.valueOf(w), String.valueOf(n), String.valueOf(e));
		
//		fr.loadFacilities(loc);
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
	 * Handle FacilitiesLoadedEvent, fired when the facilities are ready for display.
	 * @param event
	 */
	@Subscribe public void handleFacilitiesLoaded(FacilitiesLoadedEvent event) {
		Log.i(TAG, "handleFacilitiesLoaded");
		
		progressDialog.dismiss();
		
		FacilityList facilities = event.getFacilities();
		
		if (facilities.size() == 0) {
			// no facilities were found, notify user
			if (noFacilitiesToast == null) {
				noFacilitiesToast = Toast.makeText(this, "No facilites found in this location.", Toast.LENGTH_LONG);
			}
			noFacilitiesToast.show();
		} else {
			// facilities found, hide the toast if it's visible.
			if (noFacilitiesToast != null) {
				noFacilitiesToast.cancel();
			}
		}
		
		// this reloads the facility list via the FacilityArrayAdaptor
		mAdapter.clear();
		mAdapter.addAll(event.getFacilities());
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Handle FacilitySelectedEvent, fired when the user selects a facility from the
	 * list or map.
	 * @param event
	 */
	@Subscribe public void handleFacilitySelected(FacilitySelectedEvent event) {
		Log.i(TAG, "handleFacilitySelected");
		Facility f = event.getFacility();
		Intent i = new Intent(FacilityMapListActivity.this, FacilityDetailActivity.class);
		i.putExtra("facility", f);
		i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		startActivity(i);
		if (isLaunchedFromOdk) {
			this.finish();			
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
	
	
	// ============================================================================================
    // INNER CLASSES
    // ============================================================================================

    public final class FacilitiesRequestListener implements RequestListener<FacilityList> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
        	Log.e(TAG, spiceException.toString());
            Toast.makeText(FacilityMapListActivity.this, "Failed to load facilities.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(final FacilityList result) {
            Log.i(TAG, "Facilities Loaded: " + result.size());
            bus.post(new FacilitiesLoadedEvent(result));
        }
    }
}
