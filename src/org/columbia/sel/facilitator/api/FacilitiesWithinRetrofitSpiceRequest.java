package org.columbia.sel.facilitator.api;

import org.columbia.sel.facilitator.model.FacilityList;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

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

    public FacilitiesWithinRetrofitSpiceRequest(String slat, String wlng, String nlat, String elng) {
        super(FacilityList.class, FacilitatorApi.class);
        this.slat = slat;
        this.wlng = wlng;
        this.nlat = nlat;
        this.elng = elng;
    }

    @Override
    public FacilityList loadDataFromNetwork() {
        return ((FacilitatorApi) getService()).facilitiesWithin(slat, wlng, nlat, elng);
    }
}
