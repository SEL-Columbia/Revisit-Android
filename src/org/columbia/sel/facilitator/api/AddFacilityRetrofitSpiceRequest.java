package org.columbia.sel.facilitator.api;

import org.columbia.sel.facilitator.model.Facility;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class AddFacilityRetrofitSpiceRequest extends RetrofitSpiceRequest<Facility, FacilitatorApi> {
	private String TAG = this.getClass().getCanonicalName();
	
	private Facility facility;

    public AddFacilityRetrofitSpiceRequest(Facility facility) {
        super(Facility.class, FacilitatorApi.class);
        this.facility = facility;
    }

    @Override
    public Facility loadDataFromNetwork() {
    	Log.i(TAG, "POSTing a new Facility");
        ((FacilitatorApi) getService()).addFacility(facility, new Callback<Facility>() {

			@Override
			public void failure(RetrofitError arg0) {
				// TODO Auto-generated method stub
				Log.e(TAG, "??????????????????      " + arg0.getMessage());
				throw arg0;
			}

			@Override
			public void success(Facility arg0, Response arg1) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        Facility fac = new Facility();
        fac.name = "Nope";
        return fac;
    }
}
