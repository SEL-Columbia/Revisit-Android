package org.columbia.sel.facilitator;

import java.util.Arrays;
import java.util.List;

import org.columbia.sel.facilitator.di.ContainerModule;
import org.columbia.sel.facilitator.model.*;
import org.columbia.sel.facilitator.tasks.HttpRequestTask;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.google.android.gms.location.LocationClient;

import dagger.ObjectGraph;

public class FacilitatorApplication extends Application {
	// TAG for logging
	private final String TAG = this.getClass().getSimpleName();

	// Application context for use in activities, if necessary
	private static Context context;

	// Dependency Injection Object Graph
	private ObjectGraph graph;

	// Collection of Facility POJOs, populated via HttpRequestTask
	// (RestTemplate)
	private FacilityList facilities;

	// Location management
	private Location currentLocation;
	LocationManager locationManager;
	LocationListener locationListener;

	public static Context getAppContext() {
		return FacilitatorApplication.context;
	}

	/**
	 * Get a List of DI modules
	 * @return
	 */
	protected List<?> getModules() {
		return Arrays.asList(new ContainerModule(this));
	}

	/**
	 * Used to bootstrap classes that want to use DI graph? 
	 * @param object
	 */
	public void inject(Object object) {
		graph.inject(object);
	}

	/**
	 * Application initialization
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		graph = ObjectGraph.create(getModules().toArray());
	}

	public void onResume() {

	}
	
    public ObjectGraph getObjectGraph() {
        return graph;
    }
	
	public void setLocation(Location location) {
		Log.i(TAG, "setting location");
		Log.i(TAG, location.toString());
		currentLocation = location;
	}
}