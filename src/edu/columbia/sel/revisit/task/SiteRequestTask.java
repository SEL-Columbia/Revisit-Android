package edu.columbia.sel.revisit.task;

import java.util.List;

import javax.inject.Inject;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.squareup.otto.Bus;

import edu.columbia.sel.revisit.RevisitApplication;
import edu.columbia.sel.revisit.event.SitesLoadedEvent;
import edu.columbia.sel.revisit.event.HttpRequestSuccessEvent;
import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * An internal class for handling of the asyncronous REST service
 * interaction.
 * 
 * TODO: This Class is not used.
 * 
 * @author jmw
 */
public class SiteRequestTask extends AsyncTask<Void, Void, SiteList> {
	private String TAG = this.getClass().getCanonicalName();
	
	@Inject Bus bus;
	
	@Inject LocationManager lm;
	
	private Double lat, lng, rad;
	
	private String url;
	
	private RevisitApplication app;

	@Inject
	public SiteRequestTask() {
		super();
		Log.i(TAG, "-=-=-=-=- HttpRequestTask() -=-=-=-=-");
	}
	
	public SiteRequestTask(String url, Double lat, Double lng, Double rad) {
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
	protected SiteList doInBackground(Void... params) {
		try {
			final String url = this.url + "?lat="
					+ this.lat + "&lng=" + this.lng + "&rad=" + this.rad;

			Log.e(TAG, url);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new MappingJackson2HttpMessageConverter());

			SiteList sites = restTemplate.getForObject(url,
					SiteList.class);

			return sites;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}

		return null;
	}

	@Override
	protected void onPostExecute(SiteList sites) {
		Log.i(TAG, "" + sites.size());
//		bus.post(new HttpRequestSuccessEvent(sites.toString()));
		bus.post(new SitesLoadedEvent(sites));
	}

}