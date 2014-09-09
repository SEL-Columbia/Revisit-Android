package edu.columbia.sel.revisit.api;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import edu.columbia.sel.revisit.model.Site;

/**
 * Defines RoboSpice Request for adding a new Site.
 * 
 * @author Jonathan Wohl
 *
 */
public class AddSiteRetrofitSpiceRequest extends RetrofitSpiceRequest<Site, RevisitApi> {
	private String TAG = this.getClass().getCanonicalName();
	
	private Site site;

    public AddSiteRetrofitSpiceRequest(Site site) {
        super(Site.class, RevisitApi.class);
        this.site = site;
    }

    @Override
    public Site loadDataFromNetwork() {
    	Log.i(TAG, "POSTing a new Site");
    	return ((RevisitApi) getService()).addSite(site);
    }
}
