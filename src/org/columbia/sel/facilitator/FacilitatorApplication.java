package org.columbia.sel.facilitator;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.columbia.sel.facilitator.annotation.ForLogging;
import org.columbia.sel.facilitator.di.DIModule;

import android.app.Application;
import android.location.LocationManager;

import dagger.ObjectGraph;

public class FacilitatorApplication extends Application {
	// TAG for logging
	private final String TAG = this.getClass().getCanonicalName();

	// Dependency Injection Object Graph
	private ObjectGraph graph;
	
	@Inject LocationManager lm;
	
	@Inject @ForLogging String APP_TAG;

	/**
	 * Get a List of DI modules
	 * @return
	 */
	protected List<?> getModules() {
		return Arrays.asList(new DIModule(this));
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
		
		graph.inject(this);
	}
	
    public ObjectGraph getObjectGraph() {
        return graph;
    }
}