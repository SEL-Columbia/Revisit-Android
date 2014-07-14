package edu.columbia.sel.revisit.fragment;

import java.util.ArrayList;

import edu.columbia.sel.revisit.R;

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

import edu.columbia.sel.revisit.activity.SelectOfflineAreaActivity;
import edu.columbia.sel.revisit.event.SitesLoadedEvent;
import edu.columbia.sel.revisit.event.SiteSelectedEvent;
import edu.columbia.sel.revisit.event.MapChangedEvent;
import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import edu.columbia.sel.revisit.osm.SiteOverlayItem;
import edu.columbia.sel.revisit.resource.SiteMarker;
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
 * A fragment for displaying the OSM Map with Site.
 * 
 * @author Jonathan Wohl
 *
 */
public class SiteMapFragment extends BaseMapFragment {

	// Overlay for known site
	private ItemizedOverlay<OverlayItem> mSitesOverlay;
	
	/**
	 * Some setup happens in super class.
	 */
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
    }
	
	/**
	 * Remove the Sites from the map.
	 */
	public void clearSitesFromMap() {
		this.mMapView.getOverlays().remove(this.mSitesOverlay);
	}
	
	/**
	 * Add a marker for each site in SiteList argument to the overlay,
	 * and add the overlay to the map. 
	 * @param sites
	 */
	public void addSitesToMap(SiteList sites) {
		Log.i(TAG, "addSitesToMap");
		
		// List of markers
		ArrayList<OverlayItem> markers = new ArrayList<OverlayItem>();
		
		// Create a marker for each Site
		int arraySize = sites.size();
		for (int i = 0; i < arraySize; i ++) {
			Site site = sites.get(i);
			Log.i(TAG, site.getCoordinates().get(1) + ", " + site.getCoordinates().get(0));
			GeoPoint point = new GeoPoint(site.getCoordinates().get(1), site.getCoordinates().get(0));
			SiteOverlayItem item = new SiteOverlayItem(site, point, i);
			
			BitmapDrawable bmd = SiteMarker.createSiteMarker(getResources(), String.valueOf(i+1));
			item.setMarker(bmd);
			item.setMarkerHotspot(HotspotPlace.CENTER);
			markers.add(item);
		}
		
		/* OnTapListener for the Markers, shows a simple Toast. */
        this.mSitesOverlay = new ItemizedIconOverlay<OverlayItem>(markers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    
        			@Override
                    public boolean onItemSingleTapUp(final int index,
                            final OverlayItem item) {
        				
        				// Grab the S associated with this marker
        				SiteOverlayItem facItem = (SiteOverlayItem) item;
        				Log.i(TAG, "CLICKED -------> " + facItem.getSite().getName());
        				
        				// Post an event containing the clicked Site
        				bus.post(new SiteSelectedEvent(facItem.getSite()));
        				
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
        this.mMapView.getOverlays().add(this.mSitesOverlay);
        this.mMapView.invalidate();
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
