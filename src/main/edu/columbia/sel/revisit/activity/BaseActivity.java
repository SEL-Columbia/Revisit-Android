package edu.columbia.sel.revisit.activity;

import javax.inject.Inject;

import com.octo.android.robospice.SpiceManager;
import com.squareup.otto.Bus;

import edu.columbia.sel.revisit.RevisitApplication;
import edu.columbia.sel.revisit.annotation.ForLogging;
import edu.columbia.sel.revisit.model.JsonFileSiteRepository;
import edu.columbia.sel.revisit.service.LocationService;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;

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
public abstract class BaseActivity extends Activity {
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
	
	// This is used to ensure we don't attempt to open dialogs if the activity is in the background.
	protected boolean mIsRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// For the moment, let's only worry about Portrait orientation.
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Set the tag to the class name of the instance.
		TAG = this.getClass().getCanonicalName();

		// Perform injection so that when this call returns all dependencies
		// will be available for use.
		((RevisitApplication) getApplication()).inject(this);

		bus.register(this);
	}
	
	@Override
    protected void onStart() {
		if (!mSpiceManager.isStarted()) {
			mSpiceManager.start(this);	
		}
        super.onStart();

		mIsRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
		mIsRunning = false;
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
		mSiteRepository.persistInMemorySitesToDisk();
		
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
