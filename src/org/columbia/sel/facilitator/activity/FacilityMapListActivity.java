package org.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.adapter.FacilityArrayAdapter;
import org.columbia.sel.facilitator.api.FacilitiesWithinRetrofitSpiceRequest;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.event.FacilitySelectedEvent;
import org.columbia.sel.facilitator.event.LocationChangedEvent;
import org.columbia.sel.facilitator.event.MapChangedEvent;
import org.columbia.sel.facilitator.fragment.FacilityMapFragment;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.model.FacilityRepository;
import org.columbia.sel.facilitator.service.LocationService;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

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

/**
 * The FacilityMapListActivity is the initial Activity displayed when a user enters the 
 * application via ODK Collect. It displays a map as well as a list of nearby facilities,
 * allowing the user to select a Facility.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilityMapListActivity extends BaseActivity {
	
	@Inject FacilityRepository fr;
	
	@Inject LocationManager lm;
	
	private LocationService mLocationService;
	
	private Location mMyLocation;
	
	// Provides the map view and associated functionality
	private FacilityMapFragment mMapFragment;
	
	// Provides the list view, created and defined in this Activity
	private ListFragment mListFragment;
	
	// Provides the binding between the list of Facilities and the ListFragment
	private FacilityArrayAdapter mAdapter;
	
	// TODO: This is probably temporary? Currently used as a flag to indicate whether or not
	// the activity should be finished when starting the next activity.
	private Boolean isLaunchedFromOdk = true;
	
	// These two both provide user feedback
	private ProgressDialog progressDialog;
	private Toast noFacilitiesToast;
	
	// The provider that's being used
	// TODO: This should be part of an application-wide location service
	private String mProvider = LocationManager.NETWORK_PROVIDER;
	
	// Filter by sector
	String sectorFilter = null;
	
	// Flag indicating whether this is the first load
	// TODO: this probably isn't necessary... figure out a better way.
	private boolean mFirstRun = true;
	
	// Stores the currently loaded list of facilitites
	FacilityList mFacilities;
	
	// Not currently used
//	private FacilitiesNearRetrofitSpiceRequest facilitiesNearRequest;
	
	// RoboSpice request object to handle fetching of facilities within the bounds of the map view
	private FacilitiesWithinRetrofitSpiceRequest facilitiesWithinRequest;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_list);
		
		Intent i = getIntent();
		String action = i.getAction();
		if (action == Intent.ACTION_MAIN) {
			// launched directly, not from ODK
			isLaunchedFromOdk = false;
		} else if (action == "org.columbia.sel.facilitator.COLLECT") {
			// launched from ODK Collect
			// here we could inspect contents of Intent in case we want to 
			// perform any kind of setup based on values from ODK, e.g. filtering on Sector
			Bundle extras = i.getExtras();
			sectorFilter = extras.getString("sector");
			Log.i(TAG, "------> SECTOR: " + sectorFilter);
		}
		
		// Grab a reference to the map fragment.
		FragmentManager fragmentManager = getFragmentManager();
		mMapFragment = (FacilityMapFragment) fragmentManager.findFragmentById(R.id.fragment_map);
		
		// Dynamically create a list fragment and setup onItemClick listener. 
		// Could create a ListFragment subclass instead if it's going to be reused?
		mListFragment = (ListFragment) fragmentManager.findFragmentById(R.id.fragment_list);
		mAdapter = new FacilityArrayAdapter(this, R.layout.facility_list_item);
		mListFragment.setListAdapter(mAdapter);
		ListView listView = mListFragment.getListView();
		
		// Setup click listener for items in the ListView
		// TODO: look at how AdapterView<?> might be replaced
		OnItemClickListener myListViewClicked = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Facility f = (Facility) parent.getAdapter().getItem(position);
				// Post an event containing the clicked facility
				bus.post(new FacilitySelectedEvent(f));
			}
		};
		listView.setOnItemClickListener(myListViewClicked);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Loading");
		progressDialog.setMessage("Finding nearby facilities...");
		progressDialog.show();
		
		// As long as we can establish our current location, when we call zoomToMyLocation, the MapChangedEvent 
		// will always fire, triggering the reloading of facilities. However, if the last known location isn't 
		// available, the map will not change and facilities won't be fetched.
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, LocationService.class));
        if (mFacilities != null) {
        	getSpiceManager().execute(facilitiesWithinRequest, "facilities", DurationInMillis.ONE_SECOND, new FacilitiesRequestListener());        	
        }
    }
	
	@Override
	protected void onResume() {
		super.onResume();
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
	 * Center the map on my last known location.
	 */
	private void zoomToMyLocation() {
		Log.i(TAG, "zoomToMyLocation");

		if (mMyLocation == null) {
			Toast.makeText(this, "Current location cannot be determined.", Toast.LENGTH_SHORT).show();
		} else {
			mMapFragment.goToLocation(mMyLocation);			
		}
	}

	
	/**
	 * Click handler for "Add New Facility" Button, starts AddFacilityActivity
	 * @param view
	 */
	public void onAddNewFacility(View view) {
		Log.i(TAG, "Begin add new Facility...");
		Intent i = new Intent(FacilityMapListActivity.this, AddFacilityActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		startActivity(i);
		if (isLaunchedFromOdk) {
			this.finish();
		}
	}
	
	/**
	 * Handle FacilitiesLoadedEvent, fired when the facilities are ready for display.
	 * @param event
	 */
	@Subscribe public void handleFacilitiesLoaded(FacilitiesLoadedEvent event) {
		Log.i(TAG, "handleFacilitiesLoaded");
		
		progressDialog.dismiss();
		
		mFacilities = event.getFacilities();
		
		if (mFacilities.size() == 0) {
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
		mAdapter.addAll(mFacilities);
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Handle FacilitySelectedEvent, fired when the user selects a facility from the
	 * list or map, starting the FacilityDetailActivity.
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
		
		// We need to convert from E6 lat/lng to degrees
		BoundingBoxE6 bb = event.getBoundingBox();
		double n = (bb.getLatNorthE6() / 1E6);
		double s = (bb.getLatSouthE6() / 1E6);
		double e = (bb.getLonEastE6() / 1E6);
		double w = (bb.getLonWestE6() / 1E6);
		
		Log.i(TAG, n + ", " + w + ", " + s + ", " + e + ": " + sectorFilter);
		
		facilitiesWithinRequest = new FacilitiesWithinRetrofitSpiceRequest(String.valueOf(s), String.valueOf(w), String.valueOf(n), String.valueOf(e), sectorFilter);
		getSpiceManager().execute(facilitiesWithinRequest, "facilities", DurationInMillis.ALWAYS_EXPIRED, new FacilitiesRequestListener());
	}
	
	
	/**
	 * Handle LocationChangedEvent, fired when the application detects a new user location.
	 * 
	 * @param event
	 */
	@Subscribe public void handleLocationChanged(LocationChangedEvent event) {
		Log.i(TAG, "handleLocationChanged");
		
		mMyLocation = event.getLocation();
		
		if (mFirstRun) {
			// We only want to zoom to our location if this is the first location change
			mFirstRun = false;
			this.zoomToMyLocation();			
		} else {
			// It's not the first run, so let's just reload the Facilities
//			mMapFragment.reloadFacilities();
//			MapView mapView = mMapFragment.getMapView();
//			BoundingBoxE6 bb = mapView.getBoundingBox();
//			bus.post(new MapChangedEvent(bb));
//			getSpiceManager().execute(facilitiesWithinRequest, "facilities", DurationInMillis.ONE_SECOND, new FacilitiesRequestListener());
		}
	}
	
	
	// ============================================================================================
    // INNER CLASSES
    // ============================================================================================


	/**
	 * Used by RoboSpice as to handle the response from the facilities "within" request.
	 * @author Jonathan Wohl
	 *
	 */
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
