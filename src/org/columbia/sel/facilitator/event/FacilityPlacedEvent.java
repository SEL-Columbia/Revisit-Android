package org.columbia.sel.facilitator.event;

import org.osmdroid.util.GeoPoint;

public class FacilityPlacedEvent {
	private GeoPoint geopoint;
	
	public FacilityPlacedEvent(GeoPoint tappedGeoPoint) {
		this.geopoint = tappedGeoPoint;
	}
	
	public GeoPoint getGeoPoint() {
		return geopoint;
	}
}
