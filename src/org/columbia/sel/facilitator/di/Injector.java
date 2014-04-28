package org.columbia.sel.facilitator.di;

import dagger.ObjectGraph;

public interface Injector {

	/**
	 * Get a reference to the object graph.
	 * @return
	 */
	public ObjectGraph getObjectGraph();
	
	/**
     * Allow passed object to inject things.
     * @param object
     */
    public void inject(Object object);

}
