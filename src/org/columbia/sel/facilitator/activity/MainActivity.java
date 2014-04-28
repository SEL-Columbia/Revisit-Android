package org.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import org.columbia.sel.facilitator.FacilitatorApplication;
import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.R.id;
import org.columbia.sel.facilitator.R.layout;
import org.columbia.sel.facilitator.R.menu;
import org.columbia.sel.facilitator.event.HttpRequestSuccessEvent;
import org.columbia.sel.facilitator.model.FacilityRepository;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends BaseActivity {
	private static final Object HttpRequestTask = null;

	@Inject LocationManager lm;
	
	@Inject Bus bus;
	
	@Inject FacilityRepository fr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		FacilitatorApplication app = (FacilitatorApplication) getApplicationContext();
		
		// register this class to receive events through the bus. 
		bus.register(this);
		
		Log.i(TAG, "Loading Facilities...");
		
		fr.loadFacilities();
		
//		Log.i("MainActivity", "What's my tag? " + TAG);
//		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//		Log.i(TAG, loc.toString());

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	@Subscribe public void requestSuccess(HttpRequestSuccessEvent event) {
		Log.i(TAG, event.getBody());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
