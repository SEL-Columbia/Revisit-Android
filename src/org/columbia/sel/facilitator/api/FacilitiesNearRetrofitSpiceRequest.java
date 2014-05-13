package org.columbia.sel.facilitator.api;

import java.util.List;

import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;

import roboguice.util.temp.Ln;
import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class FacilitiesNearRetrofitSpiceRequest extends RetrofitSpiceRequest<FacilityList, FacilitatorApi> {
	private String TAG = this.getClass().getCanonicalName();
	
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
