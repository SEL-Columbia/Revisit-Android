package edu.columbia.sel.revisit.osm;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.bitmap;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import edu.columbia.sel.revisit.R;
import edu.columbia.sel.revisit.resource.SiteMarker;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SiteIconOverlay extends ItemizedOverlay<OverlayItem> {

	private final String TAG = this.getClass().getCanonicalName();

	private List<OverlayItem> mItemList = new ArrayList<OverlayItem>();
	protected OnItemGestureListener<OverlayItem> mOnItemGestureListener;

	private OverlayItem mInDrag;

	private ImageView mDragImage;

	private int xDragTouchOffset;
	private int yDragTouchOffset;
	private int xDragImageOffset;
	private int yDragImageOffset;

	private Activity mContext;

	public SiteIconOverlay(final List<OverlayItem> pList, final Drawable pDefaultMarker,
			final OnItemGestureListener<OverlayItem> pOnItemGestureListener, final ResourceProxy pResourceProxy) {
		super(pDefaultMarker, pResourceProxy);

		this.mItemList = pList;
		this.mOnItemGestureListener = pOnItemGestureListener;
		populate();
	}

	public SiteIconOverlay(final List<OverlayItem> pList,
			final OnItemGestureListener<OverlayItem> pOnItemGestureListener, final ResourceProxy pResourceProxy) {
		this(pList, pResourceProxy.getDrawable(bitmap.marker_default), pOnItemGestureListener, pResourceProxy);

	}

	public SiteIconOverlay(final List<OverlayItem> pList,
			final OnItemGestureListener<OverlayItem> pOnItemGestureListener, final ResourceProxy pResourceProxy,
			final Context context) {
		this(pList, pResourceProxy.getDrawable(bitmap.marker_default), pOnItemGestureListener, pResourceProxy);

		this.mContext = (Activity) context;
		
		mDragImage = (ImageView) mContext.findViewById(R.id.drag);

		// mDragImage = new ImageView(context);
//		Drawable icon = context.getResources().getDrawable(R.drawable.ic_mylocationmarker);
//		mDragImage.setImageDrawable(icon);
		xDragImageOffset = mDragImage.getDrawable().getIntrinsicWidth() / 2;
		yDragImageOffset = mDragImage.getDrawable().getIntrinsicHeight();

	}

	@Override
	protected OverlayItem createItem(int i) {
		return mItemList.get(i);
	}

	@Override
	public int size() {
		if (mItemList != null)
			return mItemList.size();
		else
			return 0;
	}

	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
		Log.i(TAG, "onTouchEvent");

		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();

		final Projection pj = mapView.getProjection();

		boolean result = false;

		Point p = new Point(0, 0);
		Point t = new Point(0, 0);

		if (action == MotionEvent.ACTION_DOWN) {

			Log.i(TAG, "Action Down!");

			for (OverlayItem item : mItemList) {
				// Create a new GeoPoint from pixel coordinates (x, y):
				pj.fromPixels(x, y);

				// Convert the given GeoPoint to onscreen pixel coordinates
				// (GeoPoint, pointOut):
				pj.toPixels(item.getPoint(), p);

				// Log.i(TAG, t.x);
				// Log.i(TAG, p.x);

				// Log.i(TAG, t.y);
				// Log.i(TAG, p.y);

				Drawable marker = item.getDrawable();

				Rect bounds = marker.getBounds();
				Log.i(TAG, "Bounds: " + bounds.toString());
				
				if (hitTest(item, marker, x - p.x, y - p.y)) {
					Log.i(TAG, "Action Down -> IF!");
					result = true;
					mInDrag = item;
					mItemList.remove(item);
					populate();

					xDragTouchOffset = 0;
					yDragTouchOffset = 0;

					setDragImagePosition(x, y);
					mDragImage.setVisibility(View.VISIBLE);

					xDragTouchOffset = x - p.x;
					yDragTouchOffset = y - p.y;
					
					mapView.invalidate();

					break;
				}
			}

		}

		else if (action == MotionEvent.ACTION_MOVE && mInDrag != null) {
			setDragImagePosition(x, y);
			Log.i(TAG, "Action Move!");
			result = true;
		}

		else if (action == MotionEvent.ACTION_UP && mInDrag != null) {
			mDragImage.setVisibility(View.GONE);

			GeoPoint pt = (GeoPoint) pj.fromPixels(x - xDragTouchOffset, y - yDragTouchOffset);
			OverlayItem item = new OverlayItem("New Site", "Description", pt);
			Drawable background = mContext.getResources().getDrawable(R.drawable.ic_mylocationmarker);
			BitmapDrawable bmd = SiteMarker.createSiteMarker(
					mContext.getResources(), "", background);
			item.setMarker(bmd);
			item.setMarkerHotspot(HotspotPlace.BOTTOM_CENTER);
			
			mItemList.add(item);
			populate();

			mInDrag = null;
			result = true;

			pj.fromPixels(x, y);

			if ((x - p.x) == xDragTouchOffset && (y - p.y) == yDragTouchOffset) {
				Log.i(TAG, "Do something here if desired because we didn't move item " + item.getTitle());
			}

			Log.i(TAG, "Action Up: " + pt.toString());
			mapView.invalidate();

		}

		return (result || super.onTouchEvent(event, mapView));
	}

	private void setDragImagePosition(int x, int y) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mDragImage.getLayoutParams();
		lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y - yDragImageOffset - yDragTouchOffset, 0, 0);
		mDragImage.setLayoutParams(lp);
	}

	/**
	 * When the item is touched one of these methods may be invoked depending on
	 * the type of touch.
	 * 
	 * Each of them returns true if the event was completely handled.
	 */
	public static interface OnItemGestureListener<T> {
		public boolean onItemSingleTapUp(final int index, final T item);

		public boolean onItemLongPress(final int index, final T item);
	}
}
