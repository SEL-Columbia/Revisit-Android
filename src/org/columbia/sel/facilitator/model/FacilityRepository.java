package org.columbia.sel.facilitator.model;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.columbia.sel.facilitator.FacilitatorApplication;
import org.columbia.sel.facilitator.annotation.ForApplication;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.event.MapChangedEvent;
import org.columbia.sel.facilitator.task.HttpRequestTask;
import org.osmdroid.util.GeoPoint;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import dagger.ObjectGraph;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

@Singleton
public class FacilityRepository {
	private String TAG = this.getClass().getCanonicalName();
	
	@Inject LocationManager lm;
	
	@Inject Bus bus;
	
	@Inject @ForApplication FacilitatorApplication app;
	
	private FacilityList mFacilities;
	
	@Inject
	public FacilityRepository() {
		Log.i(TAG, "constructing...");
	}
	
	public void loadFacilities() {
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		this.loadFacilities(loc);
	}
	
	public void loadFacilities(Location loc) {
		Log.i(TAG, "loading facilities...");
		if (lm != null) {
			Log.i(TAG, "using lm");
			if (loc == null) {
				loc = new Location(LocationManager.GPS_PROVIDER);
				loc.setLatitude(41.0);
				loc.setLongitude(-79.0);
				Log.i(TAG, "-------- " + loc.toString());
			} else {				
				Log.i(TAG, "+++++++++ " + loc.toString());
			}
			
			String url = "http://ec2-54-86-63-25.compute-1.amazonaws.com/api/test/facilities/geowithin";
			
			ObjectGraph og = app.getObjectGraph();
			
			HttpRequestTask req = og.get(HttpRequestTask.class);
			
			req.setUrl(url);
			req.setLat(loc.getLatitude());
			req.setLng(loc.getLongitude());
			req.setRad(0.5);
			
			req.execute();			
		} else {
			Log.i(TAG, "lm null.");
		}
	}
	
	public FacilityList getFacilities() {
		return mFacilities;
	}
	
	@Subscribe public void handleMapChanged(MapChangedEvent event) {
		Log.i(TAG, "Map Changed Location!");
		GeoPoint gp = event.getBoundingBox().getCenter();
		Location loc = new Location(LocationManager.GPS_PROVIDER);
		loc.setLatitude(gp.getLatitude());
		loc.setLongitude(gp.getLongitude());
		this.loadFacilities(loc);
	}
}
