package edu.columbia.sel.revisit.api;

import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import edu.columbia.sel.revisit.model.Site;

/**
 * Defines RoboSpice Request for adding a new Site.
 * 
 * @author Jonathan Wohl
 *
 */
public class UpdateSiteRetrofitSpiceRequest extends RetrofitSpiceRequest<Site, RevisitApi> {
	private String TAG = this.getClass().getCanonicalName();
	
	private Site site;

    public UpdateSiteRetrofitSpiceRequest(Site site) {
        super(Site.class, RevisitApi.class);
        this.site = site;
    }

    @Override
    public Site loadDataFromNetwork() {
    	Log.i(TAG, "PUTing a new Site");
    	return ((RevisitApi) getService()).updateSite(site, site.get_id());
    }
}
