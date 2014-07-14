package edu.columbia.sel.revisit.osm;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.bitmap;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class SiteIconOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mItemList = new ArrayList<OverlayItem>();
	protected OnItemGestureListener<OverlayItem> mOnItemGestureListener;

	public SiteIconOverlay(ArrayList<OverlayItem> pList,
			Drawable pDefaultMarker,
			OnItemGestureListener<OverlayItem> pOnItemGestureListener,
			ResourceProxy pResourceProxy) {

		super(pDefaultMarker, pResourceProxy);

		this.mItemList = pList;
		this.mOnItemGestureListener = pOnItemGestureListener;
		populate();
	}

	public SiteIconOverlay(ArrayList<OverlayItem> pList,
			OnItemGestureListener<OverlayItem> pOnItemGestureListener,
			ResourceProxy pResourceProxy) {
		
		this(pList, pResourceProxy.getDrawable(bitmap.marker_default),
				pOnItemGestureListener, pResourceProxy);
	}

	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	public void addOverlay(OverlayItem aOverlayItem) {
		mItemList.add(aOverlayItem);
		populate();
	}

	public void removeOverlay(OverlayItem aOverlayItem) {
		mItemList.remove(aOverlayItem);
		populate();
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
