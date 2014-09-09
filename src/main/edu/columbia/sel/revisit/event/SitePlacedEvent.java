package edu.columbia.sel.revisit.event;

import org.osmdroid.util.GeoPoint;

/**
 * Defines event published when the user places a new facility on the map.
 * 
 * @author Jonathan Wohl
 *
 */
public class SitePlacedEvent {
	private GeoPoint geopoint;
	
	public SitePlacedEvent(GeoPoint tappedGeoPoint) {
		this.geopoint = tappedGeoPoint;
	}
	
	public GeoPoint getGeoPoint() {
		return geopoint;
	}
}
