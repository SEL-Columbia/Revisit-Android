package edu.columbia.sel.revisit.model;

public interface ISiteRepository {
	
	/**
	 * Persists the list of Sites currently in memory.
	 * @param sites
	 */
	public boolean persistSites();
	
	/**
	 * Persists a list of Sites.
	 * @param sites
	 */
	public boolean persistSites(SiteList sites);
	
	/**
	 * Persists an individual Site.
	 * @param site
	 */
	public boolean persistSite(Site site, boolean markForSync);
	
	/**
	 * Add a new Site, marking for sync.
	 */
	public boolean addSite(Site site);
	
	/**
	 * Update a Site, marking for sync.
	 */
	public boolean updateSite(Site site);
	
	/**
	 * Save a site to memory without marking for sync.
	 * @param site
	 * @return
	 */
	public boolean saveSite(Site site);
	
	/**
	 * Gets all of the persisted Sites.
	 * @return
	 */
	public SiteList getSites();
	
	/**
	 * Gets all of the persisted Sites within the specific lat/lng bounds.
	 * @param n
	 * @param s
	 * @param e
	 * @param w
	 * @return
	 */
	public SiteList getSitesWithin(double n, double s, double e, double w);
	
	/**
	 * Gets an individual Site by server-assigned ID.
	 * @param _id
	 * @return
	 */
	public Site getSiteById(String _id);
	
	/**
	 * Gets all of the Sites requesting to be synced to the server.
	 * @return
	 */
	public SiteList getSitesForSync();
	
	/**
	 * Sends local changes to Sites to the remote server.
	 * @return
	 */
	public boolean syncSites();

	/**
	 * Delete a Site.
	 */
	public boolean deleteSite(Site site);
	
}
