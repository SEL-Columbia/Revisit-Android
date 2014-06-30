package edu.columbia.sel.facilitator.osm;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import edu.columbia.sel.facilitator.model.Facility;
import android.graphics.Canvas;

public class FacilityOverlayItem extends OverlayItem {
	private Facility facility;
	private int index;

	public FacilityOverlayItem(Facility facility, GeoPoint point) {
		super(facility.getName(), facility.getProperties().getType(), point);
		this.facility = facility;
	}

	public FacilityOverlayItem(Facility facility, GeoPoint point, int index) {
		super(facility.getName(), facility.getProperties().getType(), point);
		this.facility = facility;
		this.index = index;
	}

	public Facility getFacility() {
		return facility;
	}
}
