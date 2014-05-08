package org.columbia.sel.facilitator.activity;

import java.util.ArrayList;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.adapter.FacilityArrayAdapter;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.event.FacilitySelectedEvent;
import org.columbia.sel.facilitator.fragment.FacilityMapFragment;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.model.FacilityRepository;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import com.squareup.otto.Subscribe;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_list);
		
		Intent i = getIntent();
		if (i != null) {
			Bundle extras = i.getExtras();
			Log.i(TAG, "bundle up -------> " + extras.get("facilityName"));
		}
		
		FragmentManager fragmentManager = getFragmentManager();
		mMapFragment = (FacilityMapFragment) fragmentManager.findFragmentById(R.id.fragment_map);
		
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
		
		fr.loadFacilities(loc);
		
		// TODO: This doesn't belong here - figure out how to register the FR on its own. 
//		bus.register(fr);
	}
	
	/**
	 * TODO: Move location listening to its own class, use application events to communicate changes
	 */
	private void setupLocationListener() {
		Log.i(TAG, "setupLocationListener");
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	Log.i(TAG, "=============> LOCATION UPDATED: " + location.toString());
		    	mMyLocation = location;
		    	
//		    	mMapFragment.zoomToLocation(location);
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
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	
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
	
	@Subscribe public void handleFacilitiesLoaded(FacilitiesLoadedEvent event) {
		Log.i(TAG, "handleFacilitiesLoaded");
		
		FacilityList facilities = event.getFacilities();
		
		mAdapter.clear();
		mAdapter.addAll(event.getFacilities());
		mAdapter.notifyDataSetChanged();
	}
	
	@Subscribe public void handleFacilitySelected(FacilitySelectedEvent event) {
		Log.i(TAG, "handleFacilitySelected");
		Facility f = event.getFacility();
		Intent i = new Intent(FacilityMapListActivity.this, FacilityDetailActivity.class);
		i.putExtra("facility", f);
		i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		startActivity(i);
		this.finish();
	}
}
