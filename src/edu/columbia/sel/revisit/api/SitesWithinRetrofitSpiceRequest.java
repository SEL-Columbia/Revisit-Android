package edu.columbia.sel.revisit.api;

import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import edu.columbia.sel.revisit.model.SiteList;

/**
 * Defines RoboSpice Request for fetching Sites within a given bounding box.
 * 
 * @author Jonathan Wohl
 *
 */
public class SitesWithinRetrofitSpiceRequest extends RetrofitSpiceRequest<SiteList, RevisitApi> {
	
	private String slat;
    private String wlng;
    private String nlat;
    private String elng;
    private String sector = null;

    public SitesWithinRetrofitSpiceRequest(String slat, String wlng, String nlat, String elng, String sector) {
        super(SiteList.class, RevisitApi.class);
        this.slat = slat;
        this.wlng = wlng;
        this.nlat = nlat;
        this.elng = elng;
        this.sector = sector;
    }
    
    public SitesWithinRetrofitSpiceRequest(String slat, String wlng, String nlat, String elng) {
        super(SiteList.class, RevisitApi.class);
        this.slat = slat;
        this.wlng = wlng;
        this.nlat = nlat;
        this.elng = elng;
    }

    @Override
    public SiteList loadDataFromNetwork() {
    	if (sector != null) {
    		Log.i("edu.columbia.sel.revisit", sector);    		
    	}
        return ((RevisitApi) getService()).sitesWithin(slat, wlng, nlat, elng, sector);
    }
}
