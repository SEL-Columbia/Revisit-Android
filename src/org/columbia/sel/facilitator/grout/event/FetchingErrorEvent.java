package org.columbia.sel.facilitator.grout.event;

public class FetchingErrorEvent {
	public static final int INVALID_REGION = 1;
	public static final int MAX_REGION_SIZE_EXCEEDED = 2;
	public static final int ALREADY_RUNNING = 3;
	
	public int cause;
	
	public FetchingErrorEvent(int cause) {
		this.cause = cause;
	}
}
