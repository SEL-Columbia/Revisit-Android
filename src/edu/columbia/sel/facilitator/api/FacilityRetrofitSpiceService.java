package edu.columbia.sel.facilitator.api;

import com.octo.android.robospice.retrofit.RetrofitJackson2SpiceService;

/**
 * Defines the RoboSpice Service which actually handles the management of requests.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilityRetrofitSpiceService extends RetrofitJackson2SpiceService {

	private final static String BASE_URL = "http://23.21.86.131:3000/api/v1";

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(FacilitatorApi.class);
    }

    @Override
    protected String getServerUrl() {
        return BASE_URL;
    }
}
