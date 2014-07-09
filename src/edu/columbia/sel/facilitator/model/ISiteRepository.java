package edu.columbia.sel.facilitator.model;

public interface ISiteRepository {
	
	/**
	 * Persists a list of sites.
	 * @param sites
	 */
	public void saveSites(FacilityList sites);
	
	/**
	 * Persists an individual site.
	 * @param site
	 */
	public void saveSite(Facility site);
	
	/**
	 * Gets all of the persisted sites.
	 * @return
	 */
	public FacilityList getSites();
	
	/**
	 * Gets all of the persisted sites within the specific lat/lng bounds.
	 * @param n
	 * @param s
	 * @param e
	 * @param w
	 * @return
	 */
	public FacilityList getSitesWithin(double n, double s, double e, double w);
	
	/**
	 * Gets an individual site by server-assigned ID.
	 * @param _id
	 * @return
	 */
	public Facility getSiteById(String _id);
	
	/**
	 * Gets all of the sites requesting to be synced to the server.
	 * @return
	 */
	public FacilityList getSitesForSync();
	
}
