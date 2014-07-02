package edu.columbia.sel.facilitator.api;

import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import edu.columbia.sel.facilitator.model.FacilityList;

/**
 * Defines RoboSpice Request for fetching facilities near a given point lat,lng in range rad.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilitiesWithinRetrofitSpiceRequest extends RetrofitSpiceRequest<FacilityList, FacilitatorApi> {
	
	private String slat;
    private String wlng;
    private String nlat;
    private String elng;
    private String sector = null;

    public FacilitiesWithinRetrofitSpiceRequest(String slat, String wlng, String nlat, String elng, String sector) {
        super(FacilityList.class, FacilitatorApi.class);
        this.slat = slat;
        this.wlng = wlng;
        this.nlat = nlat;
        this.elng = elng;
        this.sector = sector;
    }
    
    public FacilitiesWithinRetrofitSpiceRequest(String slat, String wlng, String nlat, String elng) {
        super(FacilityList.class, FacilitatorApi.class);
        this.slat = slat;
        this.wlng = wlng;
        this.nlat = nlat;
        this.elng = elng;
    }

    @Override
    public FacilityList loadDataFromNetwork() {
    	if (sector != null) {
    		Log.i("edu.columbia.sel.facilitator", sector);    		
    	}
        return ((FacilitatorApi) getService()).facilitiesWithin(slat, wlng, nlat, elng, sector);
    }
}