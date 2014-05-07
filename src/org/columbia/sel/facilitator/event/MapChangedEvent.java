package org.columbia.sel.facilitator.event;

import org.osmdroid.util.BoundingBoxE6;

public class MapChangedEvent {
	BoundingBoxE6 bb;
	
	public MapChangedEvent(BoundingBoxE6 bb) {
		this.bb = bb;
	}
	
	public BoundingBoxE6 getBoundingBox() {
		return this.bb;
	}
	
}
