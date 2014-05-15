package org.columbia.sel.facilitator.activity;

import java.util.ArrayList;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.R.id;
import org.columbia.sel.facilitator.R.layout;
import org.columbia.sel.facilitator.R.menu;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
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

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * TODO: THIS ACTIVITY WAS AN INITIAL TEST, NOT CURRENTLY USED
 * 
 * @author Jonathan Wohl
 *
 */
public class MapActivity extends BaseActivity {
	
	@Inject FacilityRepository fr;
	
	@Inject LocationManager lm;
	
	private Location mMyLocation;
	
	private MapView mMapView;
	private MapController mMapCon;
	private ItemizedOverlay<OverlayItem> mMyLocationOverlay;
	private DefaultResourceProxyImpl mResourceProxy;
	
	// TODO each activity probably doesn't need a reference to the facilities?
	// Centralize in FacilityRepository or the like?
	private FacilityList facilities;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		Log.i(APP_TAG, "_+_+_+_+_+_+_+_+_+_+_ THE TAG");
		
		
		mMapView = (MapView) this.findViewById(R.id.mapview);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);

		mMapView.getController().setZoom(15);
		mMapCon = (MapController) mMapView.getController();
		
		mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		
		this.setupLocationListener();
		
		this.zoomToMyLocation();
		
//		fr.loadFacilities();
	}
	
	private void setupLocationListener() {
		Log.i(TAG, "setupLocationListener");
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	Log.i(TAG, "=============> LOCATION UPDATED: " + location.toString());
		    	mMyLocation = location;
		    	zoomToLocation(location);
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
			loc.setLatitude(41.0);
			loc.setLongitude(-79.0);
		}

		this.zoomToLocation(loc);
	}
	
	private void zoomToLocation(Location loc) {
		Log.i(TAG, "zoomToLocation");
		
		if (loc == null) {
			throw new RuntimeException("Location can not be null.");
		}

		GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		mMapCon.animateTo(point);
	}
	
	private void addFacilitiesToMap(FacilityList facilities) {
		Log.i(TAG, "addFacilitiesToMap");
		
		// List of markers
		ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();
		
		// Create a marker for each facilitiy
		for (Facility facility: facilities) {
			Log.i(TAG, facility.getCoordinates().get(0) + ", " + facility.getCoordinates().get(1));
			GeoPoint point = new GeoPoint(facility.getCoordinates().get(0), facility.getCoordinates().get(1));
			markers.add(new OverlayItem(facility.getName(), "SampleDescription", point));
		}
		
		/* OnTapListener for the Markers, shows a simple Toast. */
        this.mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(markers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    
        			@Override
                    public boolean onItemSingleTapUp(final int index,
                            final OverlayItem item) {
                        Toast.makeText(
                                MapActivity.this,
                                item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true; // We 'handled' this event.
                    }
                    
                    @Override
                    public boolean onItemLongPress(final int index,
                            final OverlayItem item) {
                        Toast.makeText(
                        		MapActivity.this, 
                                item.getSnippet() ,Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    
                }, mResourceProxy);
        
        // Add the overlays to the map
        this.mMapView.getOverlays().add(this.mMyLocationOverlay);
        mMapView.invalidate();
	}
	
	@Subscribe public void handleFacilitiesLoaded(FacilitiesLoadedEvent event) {
		Log.i(TAG, "handleFacilitiesLoaded");
		addFacilitiesToMap(event.getFacilities());
	}
}
