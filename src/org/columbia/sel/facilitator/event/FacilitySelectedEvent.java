package org.columbia.sel.facilitator.event;

import org.columbia.sel.facilitator.model.Facility;

public class FacilitySelectedEvent {
	private Facility facility;
	
	public FacilitySelectedEvent(Facility facility) {
		this.facility = facility;
	}
	
	public Facility getFacility() {
		return this.facility;
	}
}
