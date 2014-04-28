package org.columbia.sel.facilitator.model;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.columbia.sel.facilitator.FacilitatorApplication;
import org.columbia.sel.facilitator.annotation.ForApplication;
import org.columbia.sel.facilitator.di.Injector;
import org.columbia.sel.facilitator.tasks.HttpRequestTask;

import dagger.ObjectGraph;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

@Singleton
public class FacilityRepository {
	
	@Inject LocationManager lm;
	
	@Inject @ForApplication FacilitatorApplication app;
	
	private FacilityList facilities;
	
	@Inject
	public FacilityRepository() {
		Log.i("FacilityRepository", "constructing...");
		
	}
	
	public void loadFacilities() {
		Log.i("FacilityRepository", "loading facilities...");
		if (lm != null) {
			Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			if (loc == null) {
				loc = new Location(LocationManager.GPS_PROVIDER);
				loc.setLatitude(41.0);
				loc.setLongitude(-79.0);
			}
			String url = "http://fac.wohllabs.com/api/test/facilities/geowithin";
			
			ObjectGraph og = app.getObjectGraph();
			
			HttpRequestTask req = og.get(HttpRequestTask.class);
			
			req.setUrl(url);
			req.setLat(loc.getLatitude());
			req.setLng(loc.getLongitude());
			req.setRad(100.0);
			
			req.execute();			
		}
	}


}
