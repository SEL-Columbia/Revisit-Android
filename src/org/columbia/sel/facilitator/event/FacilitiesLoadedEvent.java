package org.columbia.sel.facilitator.event;

import org.columbia.sel.facilitator.model.FacilityList;

public class FacilitiesLoadedEvent {
	private FacilityList facilities;
	
	public FacilitiesLoadedEvent(FacilityList facilities) {
		this.facilities = facilities;
	}
	
	public FacilityList getFacilities() {
		return facilities;
	}
}
