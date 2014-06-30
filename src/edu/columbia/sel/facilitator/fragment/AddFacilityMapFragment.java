package edu.columbia.sel.facilitator.fragment;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import com.squareup.otto.Subscribe;

import edu.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import edu.columbia.sel.facilitator.event.FacilityPlacedEvent;
import edu.columbia.sel.facilitator.model.Facility;
import edu.columbia.sel.facilitator.model.FacilityList;
import edu.columbia.sel.facilitator.osm.FacilityOverlayItem;
import edu.columbia.sel.facilitator.resource.FacilityMarker;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A fragment for displaying the OSM Map with Facilities.
 * 
 * @author Jonathan Wohl
 * 
 */
public class AddFacilityMapFragment extends BaseMapFragment {
	
	// Overlay that only handles events, no drawing 
	private Overlay mEventOverlay;
	
	// Overlay that displays the new Facility being added
	private Overlay mFacilityOverlay;
	
	// Overlay that displays the known facilities
	private ItemizedIconOverlay<OverlayItem> mFacilitiesOverlay;

	// In the case of this map, markers will always contain only a single item.
	private ArrayList<OverlayItem> newFacilityMarkers = new ArrayList<OverlayItem>();

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		mMapCon.setZoom(17);
		
		this.setupMapEventOverlay();
		
		return view;
    }

	/**
	 * Adds the overlay that handles user events on the map.
	 */
	public void setupMapEventOverlay() {
		this.mEventOverlay = new Overlay(mResourceProxy) {
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
				int x = (int) event.getX();
				int y = (int) event.getY();
				Projection projection = mapView.getProjection();
				GeoPoint tappedGeoPoint = (GeoPoint) projection.fromPixels(x, y);
				bus.post(new FacilityPlacedEvent(tappedGeoPoint));
				addNewFacilityToMap(tappedGeoPoint);
				return true;
			}

			@Override
			protected void draw(Canvas arg0, MapView arg1, boolean arg2) {
				// TODO Auto-generated method stub
				// Nothing to draw, but this method is required by abstract class Overlay
			}
		};
		
		// Add the overlays to the map
		this.mMapView.getOverlays().add(this.mEventOverlay);
		this.mMapView.invalidate();
	}

	/**
	 * Called when the user single taps the map, this method creates the overlay and marker
	 * for the new facility being added.
	 * @param point
	 */
	public void addNewFacilityToMap(GeoPoint point) {
		Log.i(TAG, "addFacilitiesToMap");
		
		this.mMapView.getOverlays().remove(this.mFacilityOverlay);
		
		OverlayItem item = new OverlayItem("New Facility", "Description", point);

		BitmapDrawable bmd = FacilityMarker.createFacilityMarker(
				getResources(), "+", Color.argb(200, 18, 74, 255));
		item.setMarker(bmd);
		item.setMarkerHotspot(HotspotPlace.CENTER);
		newFacilityMarkers.clear();
		newFacilityMarkers.add(item);
		
		/* OnTapListener for the Markers, shows a simple Toast. */
        this.mFacilityOverlay = new ItemizedIconOverlay<OverlayItem>(newFacilityMarkers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    
        			@Override
                    public boolean onItemSingleTapUp(final int index,
                            final OverlayItem item) {

        				// handled true
                        return true;
                    }
                    
                    @Override
                    public boolean onItemLongPress(final int index,
                            final OverlayItem item) {
                    	// Currently not doing anything with long press...
                        return false;
                    }
                    
                }, mResourceProxy);
        
        // Add the overlays to the map
        this.mMapView.getOverlays().add(this.mFacilityOverlay);
        this.mMapView.invalidate();
	}
	
	/**
	 * Clears the overlay containing known facilities.
	 */
	public void clearFacilitiesFromMap() {
		this.mMapView.getOverlays().remove(this.mFacilitiesOverlay);
	}
	
	/**
	 * Adds the markers to an overlay for the known facilities.
	 * @param facilities
	 */
	public void addFacilitiesToMap(FacilityList facilities) {
		Log.i(TAG, "addFacilitiesToMap");
		
		// List of markers
		ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();
		
		// Create a marker for each facility
		int arraySize = facilities.size();
		for (int i = 0; i < arraySize; i ++) {
			Facility facility = facilities.get(i);
			Log.i(TAG, facility.getCoordinates().get(1) + ", " + facility.getCoordinates().get(0));
			GeoPoint point = new GeoPoint(facility.getCoordinates().get(1), facility.getCoordinates().get(0));
			FacilityOverlayItem item = new FacilityOverlayItem(facility, point, i);
			BitmapDrawable bmd = FacilityMarker.createFacilityMarker(getResources(), "", Color.argb(127, 18, 255, 74));
			item.setMarker(bmd);
			item.setMarkerHotspot(HotspotPlace.CENTER);
			markers.add(item);
		}
		
		// OnTapListener for the known facility markers, shows a simple Toast displaying the name
        this.mFacilitiesOverlay = new ItemizedIconOverlay<OverlayItem>(markers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    
        			@Override
                    public boolean onItemSingleTapUp(final int index,
                            final OverlayItem item) {
        				
        				// Haptic response... added because it's hard to tell exactly when your finger
        				// hits the marker.
        				Vibrator myVib = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
        				myVib.vibrate(50);
        				
        				// Grab the Facility associated with this marker
        				FacilityOverlayItem facItem = (FacilityOverlayItem) item;
        				Log.i(TAG, "CLICKED -------> " + facItem.getFacility().getName());
        				
        				Toast.makeText(getActivity(), facItem.getFacility().getName(), Toast.LENGTH_SHORT).show();
        				
                        return true;
                    }
                    
                    @Override
                    public boolean onItemLongPress(final int index,
                            final OverlayItem item) {
                    	// Currently not doing anything with long press...
                        return false;
                    }
                    
                }, mResourceProxy);
        
        // Add the overlays to the map
        this.mMapView.getOverlays().add(this.mFacilitiesOverlay);
        this.mMapView.invalidate();
	}
	
	/**
	 * When the known facilities have loaded, clear the map then draw them. 
	 * @param event
	 */
	@Subscribe public void handleFacilitiesLoaded(FacilitiesLoadedEvent event) {
		Log.i(TAG, "handleFacilitiesLoaded");
		this.clearFacilitiesFromMap();
		this.addFacilitiesToMap(event.getFacilities());
	}
}
