package edu.columbia.sel.revisit;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.squareup.otto.Bus;

import android.app.Application;
import android.content.Intent;
import dagger.ObjectGraph;
import edu.columbia.sel.revisit.annotation.ForLogging;
import edu.columbia.sel.revisit.di.DIModule;
import edu.columbia.sel.revisit.service.LocationService;

/**
 * Main Application class.
 * @author Jonathan Wohl
 *
 */
public class RevisitApplication extends Application {
	// TAG for logging
	private final String TAG = this.getClass().getCanonicalName();

	// Dependency Injection Object Graph
	private ObjectGraph graph;
	
	@Inject @ForLogging String APP_TAG;

	/**
	 * Get a List of DI modules
	 * @return
	 */
	protected List<?> getModules() {
		return Arrays.asList(new DIModule(this));
	}

	/**
	 * Inject an object into the object graph.
	 * @param object
	 */
	public void inject(Object object) {
		graph.inject(object);
	}

	/**
	 * Application initialization. Creates the DI object graph.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		graph = ObjectGraph.create(getModules().toArray());
		
		graph.inject(this);
	}
	
	/**
	 * Cleanup. TODO: do we need to ensure all subscriptions are unregistered from Bus?
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
		Intent serviceIntent = new Intent(this, LocationService.class);
		stopService(serviceIntent);
	}
	
	/**
	 * Retrieve the Dependency Injection object graph.
	 * @return
	 */
    public ObjectGraph getObjectGraph() {
        return graph;
    }
}