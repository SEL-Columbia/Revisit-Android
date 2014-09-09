package edu.columbia.sel.revisit.event;

import edu.columbia.sel.revisit.model.SiteList;

/**
 * Defines event published when Sites have successfully been loaded.
 * 
 * @author Jonathan Wohl
 *
 */
public class SitesLoadedEvent {
	private SiteList mSites;
	
	public SitesLoadedEvent(SiteList sites) {
		this.mSites = sites;
	}
	
	public SiteList getSites() {
		return mSites;
	}
}
