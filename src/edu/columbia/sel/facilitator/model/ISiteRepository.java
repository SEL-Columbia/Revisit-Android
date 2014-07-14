package edu.columbia.sel.facilitator.model;

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
	public boolean persistSites(FacilityList sites);
	
	/**
	 * Persists an individual Site.
	 * @param site
	 */
	public boolean persistSite(Facility site, boolean markForSync);
	
	/**
	 * Add a new Site, marking for sync.
	 */
	public boolean addSite(Facility site);
	
	/**
	 * Update a Site, marking for sync.
	 */
	public boolean updateSite(Facility site);
	
	/**
	 * Save a site to memory without marking for sync.
	 * @param site
	 * @return
	 */
	public boolean saveSite(Facility site);
	
	/**
	 * Gets all of the persisted Sites.
	 * @return
	 */
	public FacilityList getSites();
	
	/**
	 * Gets all of the persisted Sites within the specific lat/lng bounds.
	 * @param n
	 * @param s
	 * @param e
	 * @param w
	 * @return
	 */
	public FacilityList getSitesWithin(double n, double s, double e, double w);
	
	/**
	 * Gets an individual Site by server-assigned ID.
	 * @param _id
	 * @return
	 */
	public Facility getSiteById(String _id);
	
	/**
	 * Gets all of the Sites requesting to be synced to the server.
	 * @return
	 */
	public FacilityList getSitesForSync();
	
	/**
	 * Sends local changes to Sites to the remote server.
	 * @return
	 */
	public boolean syncSites();

	/**
	 * Delete a Site.
	 */
	public boolean deleteSite(Facility site);
	
}
