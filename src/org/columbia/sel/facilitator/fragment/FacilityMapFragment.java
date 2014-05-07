package org.columbia.sel.facilitator.fragment;

import java.util.ArrayList;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.activity.MapActivity;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.event.FacilitySelectedEvent;
import org.columbia.sel.facilitator.event.MapChangedEvent;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A fragment for displaying the OSM Map with Facilities.
 * @author jmw
 *
 */
public class FacilityMapFragment extends BaseFragment {

	@InjectView (R.id.facilities_map) MapView mMapView;
	
	private MapController mMapCon;
	private ItemizedOverlay<OverlayItem> mMyLocationOverlay;
	private DefaultResourceProxyImpl mResourceProxy;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_map, container, false); 
		
		// Inflate the layout for this fragment
		ButterKnife.inject(this, view);

		mMapCon = (MapController) mMapView.getController();
		mMapView.getController().setZoom(15);
		
		mResourceProxy = new DefaultResourceProxyImpl(this.getActivity().getApplication());
		
		this.setupMapEvents();
		
		return view;
    }
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
	}
	
	public void zoomToLocation(Location loc) {
		Log.i(TAG, "zoomToLocation");
		
		if (loc == null) {
			throw new RuntimeException("Location can not be null.");
		}

		GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		mMapCon.animateTo(point);
	}
	
	public void setupMapEvents() {
		// Enable touch controls
		mMapView.setMultiTouchControls(true);
		mMapView.setBuiltInZoomControls(true);
		
		// Set map event listeners
		mMapView.setMapListener(new DelayedMapListener(new MapListener() {  
		    public boolean onZoom(final ZoomEvent e) {
		        //do something
		    	BoundingBoxE6 bb = mMapView.getBoundingBox();
		        bus.post(new MapChangedEvent(bb));
		        return true;
		    }

		    public boolean onScroll(final ScrollEvent e) {
		        Log.i(TAG, e.toString());
		        BoundingBoxE6 bb = mMapView.getBoundingBox();
		        bus.post(new MapChangedEvent(bb));
		        return true;
		    }
		    }, 1000 ));
	}
	
	public void addFacilitiesToMap(FacilityList facilities) {
		Log.i(TAG, "addFacilitiesToMap");
		
		// List of markers
		ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();
		
		// Create a marker for each facilitiy
		for (Facility facility: facilities) {
			Log.i(TAG, facility.coordinates.get(0) + ", " + facility.coordinates.get(1));
			GeoPoint point = new GeoPoint(facility.coordinates.get(0), facility.coordinates.get(1));

			// Casting to OverlayItem, we'll need to cast back in the event handlers.
			markers.add((OverlayItem) new FacilityOverlayItem(facility, point));
		}
		
		/* OnTapListener for the Markers, shows a simple Toast. */
        this.mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(markers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    
        			@Override
                    public boolean onItemSingleTapUp(final int index,
                            final OverlayItem item) {
        				
        				// Grab the Facility associated with this marker
        				FacilityOverlayItem facItem = (FacilityOverlayItem) item;
        				Log.i(TAG, "CLICKED -------> " + facItem.getFacility().name);
        				
        				// Post an event containing the clicked facility
        				bus.post(new FacilitySelectedEvent(facItem.getFacility()));
        				
        				// handled true
                        return true;
                    }
                    
                    @Override
                    public boolean onItemLongPress(final int index,
                            final OverlayItem item) {
//                        Toast.makeText(
//                        		MapActivity.this, 
//                                item.getSnippet() ,Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    
                }, mResourceProxy);
        
        // Add the overlays to the map
        this.mMapView.getOverlays().add(this.mMyLocationOverlay);
        this.mMapView.invalidate();
	}
	
	@Subscribe public void handleFacilitiesLoaded(FacilitiesLoadedEvent event) {
		Log.i(TAG, "handleFacilitiesLoaded");
		addFacilitiesToMap(event.getFacilities());
	}
}
