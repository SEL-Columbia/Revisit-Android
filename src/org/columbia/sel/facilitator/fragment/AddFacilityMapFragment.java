package org.columbia.sel.facilitator.fragment;

import java.util.ArrayList;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.activity.MapActivity;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.event.FacilityPlacedEvent;
import org.columbia.sel.facilitator.event.FacilitySelectedEvent;
import org.columbia.sel.facilitator.event.MapChangedEvent;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.resource.FacilityMarker;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A fragment for displaying the OSM Map with Facilities.
 * 
 * @author jmw
 * 
 */
public class AddFacilityMapFragment extends BaseMapFragment {

//	@InjectView (R.id.facilities_map) MapView mMapView;
	
	private Overlay mEventOverlay;
	private Overlay mFacilityOverlay;

	// In the case of this map, markers will always contain only a single item.
	private ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		mMapCon.setZoom(17);
		
		this.setupMapEventOverlay();
		
		return view;
    }

	public void setupMapEventOverlay() {
		this.mEventOverlay = new Overlay(mResourceProxy) {
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
				float x = event.getX();
				float y = event.getY();
				Projection projection = mapView.getProjection();
				GeoPoint tappedGeoPoint = (GeoPoint) projection.fromPixels(x, y);
				bus.post(new FacilityPlacedEvent(tappedGeoPoint));
				addFacilityToMap(tappedGeoPoint);
				return true;
			}

			@Override
			protected void draw(Canvas arg0, MapView arg1, boolean arg2) {
				// TODO Auto-generated method stub
				// Nothing to draw
			}
		};
		
		// Add the overlays to the map
		this.mMapView.getOverlays().add(this.mEventOverlay);
		this.mMapView.invalidate();
	}

	public void clearFacilitiesFromMap() {
		this.mMapView.getOverlays().remove(this.mFacilityOverlay);
	}

	public void addFacilityToMap(GeoPoint point) {
		Log.i(TAG, "addFacilitiesToMap");
		
		this.mMapView.getOverlays().remove(this.mFacilityOverlay);
		
		OverlayItem item = new OverlayItem("New Facility", "Description", point);

		BitmapDrawable bmd = FacilityMarker.createFacilityMarker(
				getResources(), "+");
		item.setMarker(bmd);
		item.setMarkerHotspot(HotspotPlace.CENTER);
		markers.clear();
		markers.add(item);
		
		/* OnTapListener for the Markers, shows a simple Toast. */
        this.mFacilityOverlay = new ItemizedIconOverlay<OverlayItem>(markers,
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
}
