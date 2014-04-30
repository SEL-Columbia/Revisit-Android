package org.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import org.columbia.sel.facilitator.FacilitatorApplication;
import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.R.id;
import org.columbia.sel.facilitator.R.layout;
import org.columbia.sel.facilitator.R.menu;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.adapter.FacilityArrayAdapter;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
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
import android.widget.ListView;
import android.os.Build;

public class MainActivity extends BaseActivity {
	private static final Object HttpRequestTask = null;

	@Inject LocationManager lm;
	
	@Inject Bus bus;
	
	@Inject FacilityRepository fr;
	
	public FacilityArrayAdapter mAdapter;
	
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
				
		// register this class to receive events through the bus. 
		bus.register(this);
		
		Log.i(TAG, "Loading Facilities...");
		
		
		listView = new ListView(this);
		mAdapter = new FacilityArrayAdapter(this, R.layout.facility_list_item);
		listView.setAdapter(mAdapter);
		
		setContentView(listView);

//		if (savedInstanceState == null) {
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		fr.loadFacilities();
	}
	
//	@Override
//	protected void onResume() {
//		fr.loadFacilities();
//	}

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
	
	/**
	 * Handle event indicating that the Facilities have been loaded successfully.
	 * @param event
	 */
	@Subscribe public void requestSuccess(FacilitiesLoadedEvent event) {
		FacilityList facilities = event.getFacilities();
		
		for (Facility facility: facilities) {
			Log.i(TAG, facility.name);
		}
		
		mAdapter.clear();
		mAdapter.addAll(event.getFacilities());
		mAdapter.notifyDataSetChanged();
	}

}
