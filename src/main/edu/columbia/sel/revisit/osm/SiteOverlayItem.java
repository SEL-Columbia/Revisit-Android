package edu.columbia.sel.revisit.osm;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import edu.columbia.sel.revisit.model.Site;
import android.graphics.Canvas;

public class SiteOverlayItem extends OverlayItem {
	private Site site;
	private int index;

	public SiteOverlayItem(Site site, GeoPoint point) {
		super(site.getName(), site.getProperties().getType(), point);
		this.site = site;
	}

	public SiteOverlayItem(Site site, GeoPoint point, int index) {
		super(site.getName(), site.getProperties().getType(), point);
		this.site = site;
		this.index = index;
	}

	public Site getSite() {
		return site;
	}
}
