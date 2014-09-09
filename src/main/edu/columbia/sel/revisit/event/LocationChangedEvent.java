package edu.columbia.sel.revisit.event;

import org.osmdroid.util.BoundingBoxE6;

import android.location.Location;

/**
 * Defines event published when the LocationService has a new location
 * @author Jonathan Wohl
 *
 */
public class LocationChangedEvent {
	Location location;
	
	public LocationChangedEvent(Location location) {
		this.location = location;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
}
