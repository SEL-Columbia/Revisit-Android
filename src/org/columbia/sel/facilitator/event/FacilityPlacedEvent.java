package org.columbia.sel.facilitator.event;

import org.osmdroid.util.GeoPoint;

/**
 * Defines event published when the user places a new facility on the map.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilityPlacedEvent {
	private GeoPoint geopoint;
	
	public FacilityPlacedEvent(GeoPoint tappedGeoPoint) {
		this.geopoint = tappedGeoPoint;
	}
	
	public GeoPoint getGeoPoint() {
		return geopoint;
	}
}
