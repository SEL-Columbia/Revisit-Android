package org.columbia.sel.facilitator.fragment;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.event.MapChangedEvent;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A base fragment for displaying an OSM Map.
 * 
 * @author Jonathan Wohl
 *
 */
public class BaseMapFragment extends BaseFragment {
	
	@InjectView (R.id.facilities_map) MapView mMapView;
	
	protected MapController mMapCon;
	
	protected DefaultResourceProxyImpl mResourceProxy;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_map, container, false); 
		
		// Inflate the layout for this fragment
		ButterKnife.inject(this, view);

		mMapCon = (MapController) mMapView.getController();
		
		mMapCon.setZoom(15);
		
		mResourceProxy = new DefaultResourceProxyImpl(this.getActivity().getApplication());
		
		this.setupMapEvents();
		
		return view;
    }
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
	}
	
	public MapView getMapView() {
		return mMapView;
	}
	
	/**
	 * Scroll the map to the specified location.
	 * @param loc
	 */
	public void scrollToLocation(Location loc) {
		Log.i(TAG, "scrollToLocation");
		
		if (loc == null) {
			throw new RuntimeException("Location can not be null.");
		}

		GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		mMapCon.animateTo(point);
	}
	
	/**
	 * Change the map to the specified location immediately.
	 * @param loc
	 */
	public void goToLocation(Location loc) {
		Log.i(TAG, "scrollToLocation");
		
		if (loc == null) {
			throw new RuntimeException("Location can not be null.");
		}

		GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		mMapCon.setCenter(point);
	}
	
	/**
	 * Setup basic map events such as scrolling and zooming. Publishes MapChangedEvent events
	 * when the user changes the map view.
	 */
	public void setupMapEvents() {
		// Enable touch controls
		mMapView.setMultiTouchControls(true);
		mMapView.setBuiltInZoomControls(true);
		
		// Set map event listeners
		mMapView.setMapListener(new DelayedMapListener(new MapListener() {  
		    public boolean onZoom(final ZoomEvent e) {
		    	Log.i(TAG, e.toString());
		    	postMapChangedEvent();
		        return true;
		    }

		    public boolean onScroll(final ScrollEvent e) {
		        Log.i(TAG, e.toString());
		        postMapChangedEvent();
		        return true;
		    }
		    }, 250 ));
	}
	
	public void postMapChangedEvent() {
		BoundingBoxE6 bb = mMapView.getBoundingBox();
        bus.post(new MapChangedEvent(bb));
	}
}
