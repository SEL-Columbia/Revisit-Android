package edu.columbia.sel.facilitator;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.squareup.otto.Bus;

import android.app.Application;
import android.content.Intent;
import dagger.ObjectGraph;
import edu.columbia.sel.facilitator.annotation.ForLogging;
import edu.columbia.sel.facilitator.di.DIModule;
import edu.columbia.sel.facilitator.model.FacilityRepository;
import edu.columbia.sel.facilitator.service.LocationService;

/**
 * Main Application class.
 * @author Jonathan Wohl
 *
 */
public class FacilitatorApplication extends Application {
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
	 * Cleanup. At the moment, we need to unregister the FacilityRepository here
	 * because it has no lifecycle methods of it's own.
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
		FacilityRepository fr = graph.get(FacilityRepository.class);
		Bus bus = graph.get(Bus.class);
		bus.unregister(fr);
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