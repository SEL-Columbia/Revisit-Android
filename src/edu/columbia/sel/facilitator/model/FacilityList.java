package edu.columbia.sel.facilitator.model;

import java.util.ArrayList;

import android.util.Log;

/**
 * Stores a collection of Facility objects.
 * 
 * @author Jonathan Wohl
 * 
 */
public class FacilityList extends ArrayList<Facility> {

	/**
	 * Used by Serializable interface
	 * 
	 * http://stackoverflow.com/questions/285793/what-is-a-serialversionuid-and-
	 * why-should-i-use-it
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Our overridden equals() method on the Facility object makes this possible.
	 * @param site
	 */
	public void replace(Facility site) {
		// is the site already in our list?
		int index = this.indexOf(site); 
		if (index != -1) {
			// yes
			this.remove(site);
			this.add(index, site);
		}
	}
	
	/**
	 * Add a check to avoid duplicates.
	 */
	public boolean safeAdd(Facility site) {
		// is the site already in our list?
		if (this.indexOf(site) != -1) {
			// yes, don't add it again.
			return false;
		}
		
		super.add(site);
		return true;
	}

}
