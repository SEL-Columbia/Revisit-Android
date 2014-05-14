package org.columbia.sel.facilitator.activity;

import org.columbia.sel.facilitator.FacilitatorApplication;
import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.R.drawable;
import org.columbia.sel.facilitator.R.id;
import org.columbia.sel.facilitator.R.layout;
import org.columbia.sel.facilitator.R.menu;
import org.columbia.sel.facilitator.adapter.FacilityArrayAdapter;
import org.columbia.sel.facilitator.event.FacilitiesLoadedEvent;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.model.FacilityRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * The FacilityListActivity displays a list of facilities pulled from the API.
 * @author jmw
 *
 */
public class FacilityListActivity extends BaseActivity {
	
	@Inject FacilityRepository fr;

	// TAG for logging
	private final String TAG = this.getClass().getCanonicalName();
	
	public FacilityArrayAdapter mAdapter;
	
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		
		listView = new ListView(this);
		mAdapter = new FacilityArrayAdapter(this, R.layout.facility_list_item);
		listView.setAdapter(mAdapter);
		
		OnItemClickListener myListViewClicked = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Facility f = (Facility) parent.getAdapter().getItem(position);
				Intent i = new Intent(FacilityListActivity.this, FacilityDetailActivity.class);
				i.putExtra("facility", f);
				startActivity(i);
			}
		};
		
		listView.setOnItemClickListener(  myListViewClicked );
		
		setContentView(listView);
	}
	

	@Override
	public void onStart() {
		super.onStart();
		
		// load facilities
		fr.loadFacilities();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.facility_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Event handler for Facilities Loaded event.
	 * @param event
	 */
	@Subscribe public void facilitiesLoadedHandler(FacilitiesLoadedEvent event) {
		Log.i(TAG, "EVENT HANDLED - facs loaded.");
		
		FacilityList facilities = event.getFacilities();
		
		for (Facility facility: facilities) {
			Log.i(TAG, facility.getName());
		}
		
		mAdapter.clear();
		mAdapter.addAll(event.getFacilities());
		mAdapter.notifyDataSetChanged();
	}

}
