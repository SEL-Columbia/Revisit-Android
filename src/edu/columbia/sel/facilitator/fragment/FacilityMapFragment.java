package edu.columbia.sel.facilitator.fragment;

import java.util.ArrayList;

import edu.columbia.sel.facilitator.R;
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
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import com.squareup.otto.Subscribe;

import edu.columbia.sel.facilitator.activity.SelectOfflineAreaActivity;
import edu.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import edu.columbia.sel.facilitator.event.FacilitySelectedEvent;
import edu.columbia.sel.facilitator.event.MapChangedEvent;
import edu.columbia.sel.facilitator.model.Facility;
import edu.columbia.sel.facilitator.model.FacilityList;
import edu.columbia.sel.facilitator.osm.FacilityOverlayItem;
import edu.columbia.sel.facilitator.resource.FacilityMarker;
import butterknife.ButterKnife;
import butterknife.InjectView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A fragment for displaying the OSM Map with Facilities.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilityMapFragment extends BaseMapFragment {

	// Overlay for known facilities
	private ItemizedOverlay<OverlayItem> mFacilitiesOverlay;
	
	/**
	 * Some setup happens in super class.
	 */
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
    }
	
	/**
	 * Remove the facilities from the map.
	 */
	public void clearFacilitiesFromMap() {
		this.mMapView.getOverlays().remove(this.mFacilitiesOverlay);
	}
	
	/**
	 * Add a marker for each facility in FacilityList argument to the overlay,
	 * and add the overlay to the map. 
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
			
			BitmapDrawable bmd = FacilityMarker.createFacilityMarker(getResources(), String.valueOf(i+1));
			item.setMarker(bmd);
			item.setMarkerHotspot(HotspotPlace.CENTER);
			markers.add(item);
		}
		
		/* OnTapListener for the Markers, shows a simple Toast. */
        this.mFacilitiesOverlay = new ItemizedIconOverlay<OverlayItem>(markers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    
        			@Override
                    public boolean onItemSingleTapUp(final int index,
                            final OverlayItem item) {
        				
        				// Grab the Facility associated with this marker
        				FacilityOverlayItem facItem = (FacilityOverlayItem) item;
        				Log.i(TAG, "CLICKED -------> " + facItem.getFacility().getName());
        				
        				// Post an event containing the clicked facility
        				bus.post(new FacilitySelectedEvent(facItem.getFacility()));
        				
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