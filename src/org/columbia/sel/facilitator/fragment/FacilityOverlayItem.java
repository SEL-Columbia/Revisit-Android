package org.columbia.sel.facilitator.fragment;

import org.columbia.sel.facilitator.model.Facility;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class FacilityOverlayItem extends OverlayItem {
	private Facility facility;
	
	public FacilityOverlayItem(Facility facility, GeoPoint point) {
		super(facility.name, facility.properties.get("type"), point);
		this.facility = facility;
	}
	
	public Facility getFacility() {
		return facility;
	}
}
