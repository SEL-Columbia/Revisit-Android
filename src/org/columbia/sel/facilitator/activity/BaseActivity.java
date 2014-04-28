package org.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import org.columbia.sel.facilitator.FacilitatorApplication;

import com.squareup.otto.Bus;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * BaseActivity contains a few core features that should be included in all
 * Activities. Namely:
 * 
 * - setup of Dependency Injection
 * - setup of Logging TAG (class name of instance)
 * 
 * @author jmw
 *
 */
public abstract class BaseActivity extends ActionBarActivity {
	// TAG for logging
	protected String TAG;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the tag to the class name of the instance.
		 TAG = this.getClass().getSimpleName();
		
		// Perform injection so that when this call returns all dependencies
		// will be available for use.
		((FacilitatorApplication) getApplication()).inject(this);
	}
}
