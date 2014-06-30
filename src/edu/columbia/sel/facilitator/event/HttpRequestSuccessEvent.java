package edu.columbia.sel.facilitator.event;

/**
 * Event published when an HttpRequestTask completes.
 * 
 * @author Jonathan Wohl
 *
 * @deprecated
 */
public class HttpRequestSuccessEvent {
	private String body;
	
	public HttpRequestSuccessEvent(String body) {
		this.body = body;
	}
	
	public String getBody() {
		return body;
	}
}