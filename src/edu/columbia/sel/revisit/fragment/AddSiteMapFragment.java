package edu.columbia.sel.revisit.fragment;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import com.squareup.otto.Subscribe;

import edu.columbia.sel.revisit.R;
import edu.columbia.sel.revisit.event.SitesLoadedEvent;
import edu.columbia.sel.revisit.event.SitePlacedEvent;
import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import edu.columbia.sel.revisit.osm.SiteOverlayItem;
import edu.columbia.sel.revisit.resource.SiteMarker;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A fragment for displaying the OSM Map with Sites.
 * 
 * @author Jonathan Wohl
 * 
 */
public class AddSiteMapFragment extends BaseMapFragment {
	
	// Overlay that only handles events, no drawing 
	private Overlay mEventOverlay;
	
	// Overlay that displays the new Site being added
	private Overlay mSiteOverlay;
	
	// Overlay that displays the known sites
	private ItemizedIconOverlay<OverlayItem> mSitesOverlay;

	// In the case of this map, markers will always contain only a single item.
	private ArrayList<OverlayItem> mNewSiteMarkers = new ArrayList<OverlayItem>();

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		mMapCon.setZoom(17);
		
		this.setupMapEventOverlay();
		
//		this.placeNewSite
		
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
				bus.post(new SitePlacedEvent(tappedGeoPoint));
				addNewSiteToMap(tappedGeoPoint);
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
	 * for the new Site being added.
	 * @param point
	 */
	public void addNewSiteToMap(GeoPoint point) {
		Log.i(TAG, "addNewSiteToMap");
		
		this.mMapView.getOverlays().remove(this.mSiteOverlay);
		
		OverlayItem item = new OverlayItem("New Site", "Description", point);

		Drawable background = this.getResources().getDrawable(R.drawable.ic_mylocationmarker);
		BitmapDrawable bmd = SiteMarker.createSiteMarker(
				getResources(), "", background);
		item.setMarker(bmd);
		item.setMarkerHotspot(HotspotPlace.CENTER);
		mNewSiteMarkers.clear();
		mNewSiteMarkers.add(item);
		
		/* OnTapListener for the Markers, shows a simple Toast. */
        this.mSiteOverlay = new ItemizedIconOverlay<OverlayItem>(mNewSiteMarkers,
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
        this.refreshNewSiteOverlay();
	}
	
	public void refreshNewSiteOverlay() {
		this.mMapView.getOverlays().remove(this.mSiteOverlay);
		this.mMapView.getOverlays().add(this.mSiteOverlay);
        this.mMapView.invalidate();
	}
	
	/**
	 * Clears the overlay containing known Sites.
	 */
	public void clearSitesFromMap() {
		this.mMapView.getOverlays().remove(this.mSitesOverlay);
	}
	
	/**
	 * Adds the markers to an overlay for the known Sites.
	 * @param sites
	 */
	public void addSitesToMap(SiteList sites) {
		Log.i(TAG, "addSitesToMap");
		
		// List of markers
		ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();
		
		// Create a marker for each site
		int arraySize = sites.size();
		for (int i = 0; i < arraySize; i ++) {
			Site site = sites.get(i);
			Log.i(TAG, site.getCoordinates().get(1) + ", " + site.getCoordinates().get(0));
			GeoPoint point = new GeoPoint(site.getCoordinates().get(1), site.getCoordinates().get(0));
			SiteOverlayItem item = new SiteOverlayItem(site, point, i);
			Drawable background = this.getResources().getDrawable(R.drawable.ic_location_green);
			BitmapDrawable bmd = SiteMarker.createSiteMarker(getResources(), "", background);
			item.setMarker(bmd);
			item.setMarkerHotspot(HotspotPlace.CENTER);
			markers.add(item);
		}
		
		// OnTapListener for the known site markers, shows a simple Toast displaying the name
        this.mSitesOverlay = new ItemizedIconOverlay<OverlayItem>(markers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    
        			@Override
                    public boolean onItemSingleTapUp(final int index,
                            final OverlayItem item) {
        				
        				// Haptic response... added because it's hard to tell exactly when your finger
        				// hits the marker.
        				Vibrator myVib = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
        				myVib.vibrate(50);
        				
        				// Grab the Site associated with this marker
        				SiteOverlayItem facItem = (SiteOverlayItem) item;
        				Log.i(TAG, "CLICKED -------> " + facItem.getSite().getName());
        				
        				Toast.makeText(getActivity(), facItem.getSite().getName(), Toast.LENGTH_SHORT).show();
        				
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
        this.mMapView.getOverlays().add(this.mSitesOverlay);
        this.mMapView.invalidate();
        
        this.refreshNewSiteOverlay();
	}
	
	/**
	 * When the known Sites have loaded, clear the map then draw them. 
	 * @param event
	 */
	@Subscribe public void handleSitesLoaded(SitesLoadedEvent event) {
		Log.i(TAG, "handleSitesLoaded");
		this.clearSitesFromMap();
		this.addSitesToMap(event.getSites());
	}
}
