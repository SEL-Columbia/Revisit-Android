package edu.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import com.octo.android.robospice.SpiceManager;
import com.squareup.otto.Bus;

import edu.columbia.sel.facilitator.FacilitatorApplication;
import edu.columbia.sel.facilitator.annotation.ForLogging;
import edu.columbia.sel.facilitator.api.FacilityRetrofitSpiceService;
import edu.columbia.sel.facilitator.model.ISiteRepository;
import edu.columbia.sel.facilitator.model.JsonFileSiteRepository;
import edu.columbia.sel.facilitator.service.LocationService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * BaseActivity contains a few core features that should be included in all
 * Activities. Namely:
 * 
 * - setup of Dependency Injection - setup of Logging TAG (class name of
 * instance)
 * 
 * @author Jonathan Wohl
 * 
 */
public abstract class BaseActivity extends ActionBarActivity {
	// TAG for logging
	protected String TAG;

	// All activities should have access to the Event Bus
	@Inject
	Bus bus;

	// All activities should have access to the APP_TAG for logging
	@Inject
	@ForLogging
	String APP_TAG;
	
	// All activites should have access to the SpiceManager for network requests
	@Inject
	SpiceManager mSpiceManager;
	
	@Inject
	JsonFileSiteRepository mSiteRepository;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the tag to the class name of the instance.
		TAG = this.getClass().getCanonicalName();

		// Perform injection so that when this call returns all dependencies
		// will be available for use.
		((FacilitatorApplication) getApplication()).inject(this);

		bus.register(this);
	}
	
	@Override
    protected void onStart() {
		if (!mSpiceManager.isStarted()) {
			mSpiceManager.start(this);	
		}
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

	@Override
	protected void onPause() {
		super.onPause();
		// onPause is called before the following activity's onStart, so we stop the Service here
		// in case the next activity want's to start it again (calling stopService in onStop is a problem
		// because onStop gets called AFTER the next activity has started).
		stopService(new Intent(this, LocationService.class));
		if (mSpiceManager.isStarted()) {
			mSpiceManager.shouldStop();
		}
		
		// we should persist our in-memory Site list
		mSiteRepository.persistSites();
		
		bus.unregister(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		bus.register(this);
	}
	
    protected SpiceManager getSpiceManager() {
        return mSpiceManager;
    }
}
