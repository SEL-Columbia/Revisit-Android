package edu.columbia.sel.revisit.api;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import edu.columbia.sel.revisit.model.SiteList;

/**
 * Defines RoboSpice Request for fetching Sites near a given point lat,lng in range rad.
 * 
 * @author Jonathan Wohl
 *
 */
public class SitesNearRetrofitSpiceRequest extends RetrofitSpiceRequest<SiteList, RevisitApi> {
	
	private String lat;
    private String lng;
    private String rad;

    public SitesNearRetrofitSpiceRequest(String lat, String lng, String rad) {
        super(SiteList.class, RevisitApi.class);
        this.lat = lat;
        this.lng = lng;
        this.rad = rad;
    }

    @Override
    public SiteList loadDataFromNetwork() {
        return ((RevisitApi) getService()).sitesNear(lat, lng, rad);
    }
}
