package org.columbia.sel.facilitator.event;

public class HttpRequestSuccessEvent {
	private String body;
	
	public HttpRequestSuccessEvent(String body) {
		this.body = body;
	}
	
	public String getBody() {
		return body;
	}
}