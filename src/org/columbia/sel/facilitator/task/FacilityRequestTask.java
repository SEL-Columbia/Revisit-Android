package org.columbia.sel.facilitator.task;

import java.util.List;

import javax.inject.Inject;

import org.columbia.sel.facilitator.FacilitatorApplication;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.event.HttpRequestSuccessEvent;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.squareup.otto.Bus;

import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * An internal class for handling of the asyncronous REST service
 * interaction.
 * 
 * @author jmw
 */
public class FacilityRequestTask extends AsyncTask<Void, Void, FacilityList> {
	private String TAG = this.getClass().getCanonicalName();
	
	@Inject Bus bus;
	
	@Inject LocationManager lm;
	
	private Double lat, lng, rad;
	
	private String url;
	
	private FacilitatorApplication app;

	@Inject
	public FacilityRequestTask() {
		super();
		Log.i(TAG, "-=-=-=-=- HttpRequestTask() -=-=-=-=-");
	}
	
	public FacilityRequestTask(String url, Double lat, Double lng, Double rad) {
		super();
		
		this.url = url;
		this.lat = lat;
		this.lng = lng;
		this.rad = rad;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setLat(Double lat) {
		this.lat = lat;
	}
	
	public void setLng(Double lng) {
		this.lng = lng;
	}
	
	public void setRad(Double rad) {
		this.rad = rad;
	}

	@Override
	protected FacilityList doInBackground(Void... params) {
		try {
			final String url = this.url + "?lat="
					+ this.lat + "&lng=" + this.lng + "&rad=" + this.rad;

			Log.e(TAG, url);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new MappingJackson2HttpMessageConverter());

			FacilityList facilities = restTemplate.getForObject(url,
					FacilityList.class);

			return facilities;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}

		return null;
	}

	@Override
	protected void onPostExecute(FacilityList facilities) {
		Log.i(TAG, "" + facilities.size());
//		bus.post(new HttpRequestSuccessEvent(facilities.toString()));
		bus.post(new FacilitiesLoadedEvent(facilities));
	}

}