package org.columbia.sel.facilitator.api;

import java.util.List;

import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;

import roboguice.util.temp.Ln;
import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class FacilityRetrofitSpiceRequest extends RetrofitSpiceRequest {
	private String TAG = this.getClass().getCanonicalName();
	
	private String lat;
    private String lng;
    private String rad;

    public FacilityRetrofitSpiceRequest(String lat, String lng, String rad) {
        super(FacilityList.class, FacilitatorApi.class);
        this.lat = lat;
        this.lng = lng;
        this.rad = rad;
    }

    @Override
    public FacilityList loadDataFromNetwork() {
        Log.i(TAG, "+++++++ Loading from Network +++++++");
        return ((FacilitatorApi) getService()).facilitiesNear(lat, lng, rad);
    }
}
