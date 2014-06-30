package edu.columbia.sel.facilitator.api;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import edu.columbia.sel.facilitator.model.Facility;

/**
 * Defines RoboSpice Request for adding a new Facility.
 * 
 * @author Jonathan Wohl
 *
 */
public class UpdateFacilityRetrofitSpiceRequest extends RetrofitSpiceRequest<Facility, FacilitatorApi> {
	private String TAG = this.getClass().getCanonicalName();
	
	private Facility facility;

    public UpdateFacilityRetrofitSpiceRequest(Facility facility) {
        super(Facility.class, FacilitatorApi.class);
        this.facility = facility;
    }

    @Override
    public Facility loadDataFromNetwork() {
    	Log.i(TAG, "PUTing a new Facility");
    	return ((FacilitatorApi) getService()).updateFacility(facility, facility.get_id());
    }
}
