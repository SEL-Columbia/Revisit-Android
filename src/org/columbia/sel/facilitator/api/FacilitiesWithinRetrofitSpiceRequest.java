package org.columbia.sel.facilitator.api;

import java.util.List;

import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;

import roboguice.util.temp.Ln;
import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class FacilitiesWithinRetrofitSpiceRequest extends RetrofitSpiceRequest<FacilityList, FacilitatorApi> {
	private String TAG = this.getClass().getCanonicalName();
	
	private String swlat;
    private String swlng;
    private String nelat;
    private String nelng;

    public FacilitiesWithinRetrofitSpiceRequest(String swlat, String swlng, String nelat, String nelng) {
        super(FacilityList.class, FacilitatorApi.class);
        this.swlat = swlat;
        this.swlng = swlng;
        this.nelat = nelat;
        this.nelng = nelng;
    }

    @Override
    public FacilityList loadDataFromNetwork() {
        return ((FacilitatorApi) getService()).facilitiesWithin(swlat, swlng, nelat, nelng);
    }
}
