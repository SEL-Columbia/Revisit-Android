package org.columbia.sel.facilitator.event;

import org.columbia.sel.facilitator.model.FacilityList;

/**
 * Defines event published when Facilities have successfully been loaded.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilitiesLoadedEvent {
	private FacilityList facilities;
	
	public FacilitiesLoadedEvent(FacilityList facilities) {
		this.facilities = facilities;
	}
	
	public FacilityList getFacilities() {
		return facilities;
	}
}
