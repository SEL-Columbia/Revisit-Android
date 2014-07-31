package edu.columbia.sel.revisit.activity;

import javax.inject.Inject;

import edu.columbia.sel.revisit.R;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import edu.columbia.sel.revisit.adapter.SiteArrayAdapter;
import edu.columbia.sel.revisit.api.SitesWithinRetrofitSpiceRequest;
import edu.columbia.sel.revisit.event.DeviceOfflineEvent;
import edu.columbia.sel.revisit.event.SitesLoadedEvent;
import edu.columbia.sel.revisit.event.SiteSelectedEvent;
import edu.columbia.sel.revisit.event.LocationChangedEvent;
import edu.columbia.sel.revisit.event.MapChangedEvent;
import edu.columbia.sel.revisit.fragment.SiteMapFragment;
import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import edu.columbia.sel.revisit.model.JsonFileSiteRepository;
import edu.columbia.sel.revisit.service.LocationService;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * The SiteMapListActivity is the initial Activity displayed when a user
 * enters the application via ODK Collect. It displays a map as well as a list
 * of nearby Sites, allowing the user to select a Site.
 * 
 * @author Jonathan Wohl
 * 
 */
public class SiteMapListActivity extends BaseActivity {

	private Location mMyLocation;

	// Provides the map view and associated functionality
	private SiteMapFragment mMapFragment;

	// Provides the list view, created and defined in this Activity
	private ListFragment mListFragment;

	// Provides the binding between the list of Sites and the ListFragment
	private SiteArrayAdapter mAdapter;

	// TODO: This is probably temporary? Currently used as a flag to indicate
	// whether or not
	// the activity should be finished when starting the next activity.
	private Boolean isLaunchedFromOdk = true;

	// These two both provide user feedback
	private ProgressDialog progressDialog;
	private Toast noSitesToast;

	// Filter by sector
	String mSectorFilter = null;

	// Stores the currently loaded list of Sites
	SiteList mSites;

	// Not currently used
	// private SitesNearRetrofitSpiceRequest sitesNearRequest;

	// RoboSpice request object to handle fetching of sites within the
	// bounds of the map view
	private SitesWithinRetrofitSpiceRequest sitesWithinRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_list);

		Intent i = getIntent();
		String action = i.getAction();
		if (action == Intent.ACTION_MAIN) {
			// launched directly, not from ODK
			isLaunchedFromOdk = false;
		} else if (action == "edu.columbia.sel.revisit.COLLECT") {
			// launched from ODK Collect
			// here we could inspect contents of Intent in case we want to
			// perform any kind of setup based on values from ODK, e.g.
			// filtering on Sector
			Bundle extras = i.getExtras();
			mSectorFilter = extras.getString("sector");
			Log.i(TAG, "------> SECTOR: " + mSectorFilter);
		}

		// Grab a reference to the map fragment.
		FragmentManager fragmentManager = getFragmentManager();
		mMapFragment = (SiteMapFragment) fragmentManager.findFragmentById(R.id.fragment_map);

		// Dynamically create a list fragment and setup onItemClick listener.
		// Could create a ListFragment subclass instead if it's going to be
		// reused?
		mListFragment = (ListFragment) fragmentManager.findFragmentById(R.id.fragment_list);
		mAdapter = new SiteArrayAdapter(this, R.layout.site_list_item);
		mListFragment.setListAdapter(mAdapter);
		ListView listView = mListFragment.getListView();

		// Setup click listener for items in the ListView
		// TODO: look at how AdapterView<?> might be replaced
		OnItemClickListener myListViewClicked = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Site f = (Site) parent.getAdapter().getItem(position);
				// Post an event containing the clicked site
				bus.post(new SiteSelectedEvent(f));
			}
		};
		listView.setOnItemClickListener(myListViewClicked);

		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Loading");
		progressDialog.setMessage("Finding nearby sites...");
		progressDialog.show();

		// As long as we can establish our current location, when we call
		// zoomToMyLocation, the MapChangedEvent
		// will always fire, triggering the reloading of sites. However, if
		// the last known location isn't
		// available, the map will not change and sites won't be fetched.
		// TODO: figure out how to handle this in a more centralized/uniform way
//		 this.zoomToMyLocation();
	}

	@Override
	protected void onStart() {
		super.onStart();
		startService(new Intent(this, LocationService.class));
//		if (mSites != null) {
//			getSpiceManager().execute(sitesWithinRequest, "sites", DurationInMillis.ONE_SECOND,
//					new SitesRequestListener());
//		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maplist_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_mylocation:
			zoomToMyLocation();
			return true;
		case R.id.action_settings:
			 openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Open the SettingsActivity 
	 */
	private void openSettings() {
		Log.i(TAG, "openSettings()");
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}

	/**
	 * When the result is received from the SiteDetailActivity, we simply set the result on this Activity
	 * and finish, thus passing the selected Site data back to ODK.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			setResult(RESULT_OK, data);
			this.finish();
		}
	}

	/**
	 * Center the map on my last known location.
	 */
	private void zoomToMyLocation() {
		Log.i(TAG, "zoomToMyLocation");

		if (mMyLocation == null) {
			Toast.makeText(this, "Current location cannot be determined.", Toast.LENGTH_SHORT).show();
		} else {
			mMapFragment.goToLocation(mMyLocation);
		}
	}

	/**
	 * Click handler for "Add New Site" Button, starts AddSiteActivity
	 * 
	 * @param view
	 */
	public void onAddNewSite(View view) {
		Log.i(TAG, "Begin add new Site...");
		Intent i = new Intent(SiteMapListActivity.this, AddSiteActivity.class);
		startActivityForResult(i, 1);
	}

	/**
	 * Handle SitesLoadedEvent, fired when the sites are ready for
	 * display.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleSitesLoaded(SitesLoadedEvent event) {
		Log.i(TAG, "handleSitesLoaded");

		progressDialog.dismiss();

		mSites = event.getSites();

		if (mSites.size() == 0) {
			// no sites were found, notify user
			if (noSitesToast == null) {
				noSitesToast = Toast.makeText(this, "No sites found in this location.", Toast.LENGTH_LONG);
			}
			noSitesToast.show();
		} else {
			// sites found, hide the toast if it's visible.
			if (noSitesToast != null) {
				noSitesToast.cancel();
			}
		}

		// this reloads the Site list via the SiteArrayAdaptor
		mAdapter.clear();
		mAdapter.addAll(mSites);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Handle SiteSelectedEvent, fired when the user selects a Site from
	 * the list or map, starting the SiteDetailActivity.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleSiteSelected(SiteSelectedEvent event) {
		Log.i(TAG, "handleSiteSelected");
		Site f = event.getSite();
		Intent i = new Intent(SiteMapListActivity.this, SiteDetailActivity.class);
		i.putExtra("site", f);
		startActivityForResult(i, 1);
	}

	/**
	 * Handle MapChangedEvent, fired when the user zooms or scrolls (after
	 * defined delay).
	 * 
	 * When the map is changed, request sites within the new map bounds.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleMapChanged(MapChangedEvent event) {
		Log.i(TAG, "handleMapChanged");

		// We need to convert from E6 lat/lng to degrees
		BoundingBoxE6 bb = event.getBoundingBox();
		double n = (bb.getLatNorthE6() / 1E6);
		double s = (bb.getLatSouthE6() / 1E6);
		double e = (bb.getLonEastE6() / 1E6);
		double w = (bb.getLonWestE6() / 1E6);
		Log.i(TAG, n + ", " + w + ", " + s + ", " + e + ": " + mSectorFilter);

		SiteList facs = mSiteRepository.getSitesWithin(n, s, e, w);
		bus.post(new SitesLoadedEvent(facs));

//		sitesWithinRequest = new SitesWithinRetrofitSpiceRequest(String.valueOf(s), String.valueOf(w),
//				String.valueOf(n), String.valueOf(e), mSectorFilter);
//		getSpiceManager().execute(sitesWithinRequest, "sites", DurationInMillis.ONE_SECOND,
//				new SitesRequestListener());
	}

	/**
	 * Handle LocationChangedEvent, fired when the application detects a new
	 * user location.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleLocationChanged(LocationChangedEvent event) {
		Log.i(TAG, "handleLocationChanged");

		mMyLocation = event.getLocation();

		if (mSites == null) {
			// We only want to zoom to our location if this is the first
			// location change
			this.zoomToMyLocation();
		}
	}

	/**
	 * Handle DeviceOfflineEvent. Currently not used in this view.
	 * 
	 * @param event
	 */
//	@Subscribe
//	public void handleDeviceOfflineEvent(DeviceOfflineEvent event) {
//		Log.i(TAG, "handleDeviceOfflineEvent");
////		Toast.makeText(this, "The device is not currently online.", Toast.LENGTH_SHORT).show();
////		progressDialog.dismiss();
//	}

	// ============================================================================================
	// INNER CLASSES
	// ============================================================================================

	/**
	 * Used by RoboSpice to handle the response from the sites "within"
	 * request.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class SitesRequestListener implements RequestListener<SiteList> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			Toast.makeText(SiteMapListActivity.this, "Failed to load sites.", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final SiteList result) {
			Log.i(TAG, "Sites Loaded: " + result.size());
			bus.post(new SitesLoadedEvent(result));
		}
	}
}
