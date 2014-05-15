package org.columbia.sel.facilitator.event;

import org.columbia.sel.facilitator.model.Facility;

/**
 * Defines event published when the user selects a facility from the list or map.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilitySelectedEvent {
	private Facility facility;
	
	public FacilitySelectedEvent(Facility facility) {
		this.facility = facility;
	}
	
	public Facility getFacility() {
		return this.facility;
	}
}
