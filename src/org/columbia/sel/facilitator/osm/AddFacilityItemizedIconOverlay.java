package org.columbia.sel.facilitator.osm;

import java.util.List;

import javax.inject.Inject;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import com.squareup.otto.Bus;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;

/**
 * A custom ItemizedOverlay in order to handle tap events and draw new facility.
 * 
 * TODO: Where to unregister bus? Application termination? It's currently in onDetach, 
 * but it's unclear if this always gets called.
 * 
 * TODO: THIS IS NOT CURRENTLY USED. An anonymous inner class is used instead.
 * 
 * @author Jonathan Wohl
 */
public class AddFacilityItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem> {
	private String TAG = this.getClass().getCanonicalName();

	public AddFacilityItemizedIconOverlay(List<OverlayItem> pList, Drawable pDefaultMarker,
			OnItemGestureListener<OverlayItem> pOnItemGestureListener,
			ResourceProxy pResourceProxy) {
		super(pList, pDefaultMarker, pOnItemGestureListener, pResourceProxy);
	}
	
	public AddFacilityItemizedIconOverlay(List<OverlayItem> pList,
			OnItemGestureListener<OverlayItem> pOnItemGestureListener,
			ResourceProxy pResourceProxy) {
		super(pList, pOnItemGestureListener, pResourceProxy);
	}
	
	@Override
	public void onDetach(final MapView mapView) {

    }
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
		float x = event.getX();
		float y = event.getY();
		Projection projection = mapView.getProjection();
		GeoPoint tappedGeoPoint = (GeoPoint) projection.fromPixels(x, y);
		double lat = tappedGeoPoint.getLatitude();
		double lng = tappedGeoPoint.getLongitude();
		Log.i(TAG, "Map Single Tapped at: " + lat + ", " + lng);
		return true;
	}
	
	/*
	 * All of this stuff below was copied directly from a post about creating draggable markers.
	 * Might be useful if we decide to go that route. 
	 */
	
//	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
//
//	    final int action = event.getAction();
//	    final int x = (int) event.getX();
//	    final int y = (int) event.getY();
//
//	    final Projection pj = mapView.getProjection();
//
//	    boolean result = false;
//	    //Object TAG;
//	    //Log.d(TAG, "onTouchEvent entered");
//
//	    System.out.println("onTouchEvent!");        
//	    Point p = new Point(0,0);
//	    Point t = new Point(0,0);
//
//	    //System.out.print(MotionEvent.ACTION_DOWN);
//	    //System.out.print(action);
//
//	    if (action == MotionEvent.ACTION_DOWN) {
//
//	        System.out.println("Action Down!");
//
//	        for (OverlayItem item : mOverlays) {
//
//	            // Create a new GeoPoint from pixel coordinates (x, y, pointReuse):
//	            //pj.fromMapPixels(x, y, t);
//
//	            // Convert the given GeoPoint to onscreen pixel coordinates (GeoPoint, pointOut):
//	            pj.toPixels(item.getPoint(), p);
//
//	            //System.out.println(t.x);
//	            //System.out.println(p.x);
//
//	            //System.out.println(t.y);
//	            //System.out.println(p.y);
//
//	            defaultMarker = item.getDrawable();
//
//	            if (hitTest(item, defaultMarker, x - p.x, y - p.y)) {
//	                System.out.println("Action Down -> IF!");
//	                result = true;
//	                inDrag = item;
//	                mOverlays.remove(inDrag);
//	                populate();
//
//	                xDragTouchOffset = 0;
//	                yDragTouchOffset = 0;
//
//	                setDragImagePosition(x, y);
//	                dragImage.setVisibility(View.VISIBLE);
//
//	                xDragTouchOffset = t.x - p.x;
//	                yDragTouchOffset = t.y - p.y;
//
//	                break;
//	            }
//	        }
//
//	    } 
//
//	    else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
//	        //dragImage.setVisibility(View.VISIBLE);
//	        setDragImagePosition(x, y);
//	        System.out.println("Action Move!");
//	        result = true;
//	    } 
//
//	    else if (action == MotionEvent.ACTION_UP && inDrag != null) {
//	        dragImage.setVisibility(View.GONE);
//
//	        GeoPoint pt = (GeoPoint) pj.fromPixels(x - xDragTouchOffset, y - yDragTouchOffset);
//	        OverlayItem toDrop = new OverlayItem(inDrag.getTitle(),
//	                inDrag.getSnippet(), pt);
//
//	        mOverlays.add(toDrop);
//	        populate();
//
//	        inDrag = null;
//	        result = true; 
//
//	        pj.fromMapPixels(x, y, t);
//
//	        if((t.x - p.x) == xDragTouchOffset && (t.y - p.y) == yDragTouchOffset) {
//	            System.out.println ("Do something here if desired because we didn't move item " + toDrop.getTitle() );
//	        }
//
//	        System.out.println("Action Up!");
//
//	    }
//	    System.out.print(inDrag);
//	    return (result || super.onTouchEvent(event, mapView));
//	}
//
//	private void setDragImagePosition(int x, int y) {
//	    RelativeLayout.LayoutParams lp=
//	            (RelativeLayout.LayoutParams)dragImage.getLayoutParams();
//	    lp.setMargins(x-xDragImageOffset-xDragTouchOffset,
//	            y-yDragImageOffset-yDragTouchOffset, 0, 0);
//	    dragImage.setLayoutParams(lp);
//	    }

}
