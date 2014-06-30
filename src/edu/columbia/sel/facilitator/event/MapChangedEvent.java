package edu.columbia.sel.facilitator.event;

import org.osmdroid.util.BoundingBoxE6;

/**
 * Defines event published when the user changes the map view box (e.g. zoom or scroll)
 * @author Jonathan Wohl
 *
 */
public class MapChangedEvent {
	BoundingBoxE6 bb;
	
	public MapChangedEvent(BoundingBoxE6 bb) {
		this.bb = bb;
	}
	
	public BoundingBoxE6 getBoundingBox() {
		return this.bb;
	}
	
}
