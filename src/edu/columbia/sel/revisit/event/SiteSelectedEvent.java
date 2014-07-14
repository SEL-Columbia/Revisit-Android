package edu.columbia.sel.revisit.event;

import edu.columbia.sel.revisit.model.Site;

/**
 * Defines event published when the user selects a Site from the list or map.
 * 
 * @author Jonathan Wohl
 *
 */
public class SiteSelectedEvent {
	private Site site;
	
	public SiteSelectedEvent(Site site) {
		this.site = site;
	}
	
	public Site getSite() {
		return this.site;
	}
}
