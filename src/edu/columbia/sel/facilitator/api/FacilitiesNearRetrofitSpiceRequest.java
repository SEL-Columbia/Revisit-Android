package edu.columbia.sel.facilitator.api;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import edu.columbia.sel.facilitator.model.FacilityList;

/**
 * Defines RoboSpice Request for fetching facilities near a given point lat,lng in range rad.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilitiesNearRetrofitSpiceRequest extends RetrofitSpiceRequest<FacilityList, FacilitatorApi> {
	
	private String lat;
    private String lng;
    private String rad;

    public FacilitiesNearRetrofitSpiceRequest(String lat, String lng, String rad) {
        super(FacilityList.class, FacilitatorApi.class);
        this.lat = lat;
        this.lng = lng;
        this.rad = rad;
    }

    @Override
    public FacilityList loadDataFromNetwork() {
        return ((FacilitatorApi) getService()).facilitiesNear(lat, lng, rad);
    }
}
