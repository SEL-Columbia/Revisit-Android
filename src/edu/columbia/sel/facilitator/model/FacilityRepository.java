package edu.columbia.sel.facilitator.model;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.squareup.otto.Bus;

import dagger.ObjectGraph;
import edu.columbia.sel.facilitator.FacilitatorApplication;
import edu.columbia.sel.facilitator.annotation.ForApplication;
import edu.columbia.sel.facilitator.task.FacilityRequestTask;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Data Provider layer for Facilities.
 * 
 * TODO: THIS IS NOT CURRENTLY USED. It has been replaced by RoboSpice/Retrofit.
 * 
 * @author Jonathan Wohl
 *
 */
@Singleton
public class FacilityRepository {
	private String TAG = this.getClass().getCanonicalName();
	
	@Inject LocationManager lm;
	
	@Inject @ForApplication FacilitatorApplication app;
	
	private FacilityList mFacilities;
	
	/**
	 * Injectable constructor. 
	 * 
	 * Note: Bus argument is injected upon construction via the DIModule's provideBus method.
	 * @param bus
	 */
	@Inject
	public FacilityRepository(Bus bus) {
		Log.i(TAG, "constructing...");
		// TODO - where to unregister? The app onTerminate()?
		bus.register(this);
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
			
			String url = "http://23.21.86.131:3000/api/test/facilities/geowithin";
			
			ObjectGraph og = app.getObjectGraph();
			
			FacilityRequestTask req = og.get(FacilityRequestTask.class);
			
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
	
//	@Subscribe public void handleMapChanged(MapChangedEvent event) {
//		Log.i(TAG, "Map Changed Location!");
//		GeoPoint gp = event.getBoundingBox().getCenter();
//		Location loc = new Location(LocationManager.GPS_PROVIDER);
//		loc.setLatitude(gp.getLatitude());
//		loc.setLongitude(gp.getLongitude());
//		this.loadFacilities(loc);
//	}
}