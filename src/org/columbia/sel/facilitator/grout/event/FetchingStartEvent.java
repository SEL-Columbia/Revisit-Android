package org.columbia.sel.facilitator.grout.event;

public class FetchingStartEvent {
	public int total;
	
	public FetchingStartEvent() {
	}
	
	public FetchingStartEvent(int pTotal) {
		total = pTotal;
	}
}
