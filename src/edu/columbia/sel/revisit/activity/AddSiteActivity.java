package edu.columbia.sel.revisit.activity;

import edu.columbia.sel.revisit.R;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import edu.columbia.sel.revisit.api.AddSiteRetrofitSpiceRequest;
import edu.columbia.sel.revisit.api.SitesWithinRetrofitSpiceRequest;
import edu.columbia.sel.revisit.event.SitesLoadedEvent;
import edu.columbia.sel.revisit.event.SitePlacedEvent;
import edu.columbia.sel.revisit.event.LocationChangedEvent;
import edu.columbia.sel.revisit.event.MapChangedEvent;
import edu.columbia.sel.revisit.fragment.AddSiteMapFragment;
import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import edu.columbia.sel.revisit.service.LocationService;
import android.animation.LayoutTransition;
import android.animation.LayoutTransition.TransitionListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
//import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
/**
 * The AddSiteActivity provides the interface for adding a new Site.
 * 
 * @author Jonathan Wohl
 * 
 */
public class AddSiteActivity extends BaseActivity {
	@InjectView(R.id.name)
	EditText mNameEditText;

	@InjectView(R.id.sector)
	Spinner mSectorEditText;

	@InjectView(R.id.type)
	EditText mTypeEditText;

	@InjectView(R.id.accept_button)
	Button mAcceptButton;
	
	@InjectView(R.id.root_layout)
	LinearLayout mRootLayout;

	@InjectView(R.id.add_header_text_wrap)
	LinearLayout mAddHeaderText;

	@InjectView(R.id.map_wrap)
	RelativeLayout mMapWrapView;

	@InjectView(R.id.add_properties)
	RelativeLayout mAddPropertiesView;

	@InjectView(R.id.add_button)
	Button mSubmitButton;

//	@InjectView(R.id.location)
//	EditText mLocationEditText;

	private boolean mFirstRun = true;

	// Stores the location of the new Site
	private GeoPoint mSiteGeoPoint;

	// Stores the user's current location
	private Location mMyLocation;

	// Provides the map view and related functionality
	private AddSiteMapFragment mMapFragment;
	
	// The center of the map
	private GeoPoint mMapCenter;
	
	// State variable to determine whether the 'Accept location' button has been pressed
	private boolean mLocationAccepted = false;

	// The POST request that submits the new Site
//	private AddSiteRetrofitSpiceRequest mAddSiteRequest;

	// The GET request that retrieves known Sites within the map bounds
//	private SitesWithinRetrofitSpiceRequest mSitesWithinRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_site);

		// add 'back' button to go to parent (SiteMapListActivity)
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Inject view member variables
		ButterKnife.inject(this);
		
		LayoutTransition lt = mRootLayout.getLayoutTransition();
		lt.addTransitionListener(new TransitionListener() {

			@Override
			public void endTransition(LayoutTransition arg0, ViewGroup arg1, View arg2, int arg3) {
				// TODO Auto-generated method stub
				if (mMapCenter != null) {
					mMapFragment.goToLocation(mMapCenter);					
				}
			}

			@Override
			public void startTransition(LayoutTransition arg0, ViewGroup arg1, View arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
		});
				
		
//		mAddPropertiesView.setVisibility(View.INVISIBLE);

		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sectors_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mSectorEditText.setAdapter(adapter);

		// Grab a reference to the map fragment.
		FragmentManager fragmentManager = getFragmentManager();
		mMapFragment = (AddSiteMapFragment) fragmentManager.findFragmentById(R.id.fragment_map);
		
		this.zoomToMyLocation();
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		startService(new Intent(this, LocationService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_mylocation:
			zoomToMyLocation();
			return true;
		case R.id.action_settings:
			// openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Center the map on my last known location.
	 */
	private void zoomToMyLocation() {
		Log.i(TAG, "zoomToMyLocation");

		mMyLocation = LocationService.getCurrentLocation();

		if (mMyLocation == null) {
			Toast.makeText(this, "Current location cannot be determined.", Toast.LENGTH_SHORT).show();
		} else {
			mMapFragment.scrollToLocation(mMyLocation);
			// the mMyLocation member gets set in zoomToMyLocation
			GeoPoint gp = new GeoPoint(mMyLocation);
			mMapFragment.addNewSiteToMap(gp);
			bus.post(new SitePlacedEvent(gp));
		}
	}
	
	/**
	 * Handler for user click on the "Accept location" button.
	 * @param view
	 */
	@OnClick(R.id.accept_button)
	public void onAcceptLocation(View view) {
		Log.i(TAG, "onAcceptLocation");
		
		this.mLocationAccepted = true;
		
		int padding_in_dp = 20;
		final float scale = getResources().getDisplayMetrics().density;
		int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
		mMapWrapView.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

//		ValueAnimator va = ValueAnimator.ofInt(padding_in_px, 0);
//		va.setDuration(1000);
//		va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//			public void onAnimationUpdate(ValueAnimator animation) {
//				Integer value = (Integer) animation.getAnimatedValue();
//				Log.i(TAG, "updating value: " + value);
//				mMapWrapView.setPadding(value.intValue(), 0, value.intValue(), value.intValue());
//				mMapWrapView.invalidate();
//			}
//		});
//		va.start();
		
		mMapCenter = (GeoPoint) this.mMapFragment.getMapView().getMapCenter();
//		Animation slideInAnim = AnimationUtils.loadAnimation(this, R.anim.add_properties_show);
//		mAddPropertiesView.startAnimation(slideInAnim);
		mAddPropertiesView.setAlpha(0);
		mAddPropertiesView.setVisibility(View.VISIBLE);
		mAddPropertiesView.animate().alpha(1).setDuration(500);
		
		mAddHeaderText.setVisibility(View.GONE);
		mAcceptButton.setVisibility(View.GONE);
		mSubmitButton.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Override the back button press so that we can return to the previous state
	 * within the activity.
	 */
	@Override
	public void onBackPressed() {
		if (this.mLocationAccepted) {
			returnToAccept();
		} else {
			super.onBackPressed();
		}
	}
	
	/**
	 * Return to the map-only pin dropping view.
	 */
	public void returnToAccept() {
		Log.i(TAG, "onBackToAccept");
		
		mLocationAccepted = false;
		
		mMapCenter = (GeoPoint) this.mMapFragment.getMapView().getMapCenter();
		int padding_in_dp = 20;
	    final float scale = getResources().getDisplayMetrics().density;
	    int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
		mMapWrapView.setPadding(padding_in_px, 0, padding_in_px, padding_in_px);
		mAddPropertiesView.setVisibility(View.GONE);
		mAddHeaderText.setVisibility(View.VISIBLE);
		mAcceptButton.setVisibility(View.VISIBLE);
		mSubmitButton.setVisibility(View.GONE);
	}

	/**
	 * Click handler for "Add New Site" Button.
	 * 
	 * For the moment, I'm leaving the large swath of commented code as
	 * reference. It shows how Jackson and Retrofit are used independent of
	 * RoboSpice.
	 * 
	 * @param view
	 */
	@OnClick(R.id.add_button)
	public void onAddNewSite(View view) {
		Log.i(TAG, "Adding new site.");

		// TESTING Jackson2 conversion... works.
		// ObjectMapper mapper = new ObjectMapper();
		//
		// try {
		// String json = mapper.writeValueAsString(newSite);
		// Log.i(TAG, json);
		// } catch (JsonProcessingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// TESTING Retrofit using JacksonConverter... works!
		// JacksonConverter converter = new JacksonConverter(new
		// ObjectMapper());
		// RestAdapter restAdapter = new RestAdapter.Builder()
		// .setConverter(converter)
		// .setEndpoint("http://23.21.86.131:3000/api/v1")
		// .build();
		//
		// RevisitApi service = restAdapter.create(RevisitApi.class);
		//
		// Site newSite = new Site();
		// newSite.setName("Testing");
		//
		// service.addSite(newSite, new Callback<Site>() {
		//
		// @Override
		// public void failure(RetrofitError arg0) {
		// // TODO Auto-generated method stub
		// Log.e(TAG, "??????????????????      " + arg0.getMessage());
		// throw arg0;
		// }
		//
		// @Override
		// public void success(Site site, Response arg1) {
		// // TODO Auto-generated method stub
		// Log.i(TAG,
		// "((((((((((((((((((( NEW SITE ADDED )))))))))))))))))))");
		// Log.i(TAG, site.getName());
		// }
		//
		// });

		String name = this.mNameEditText.getText().toString();
		String type = this.mTypeEditText.getText().toString();
		String sector = "";
		
		Object sectorItem = this.mSectorEditText.getSelectedItem();
		if (sectorItem != null) {
			sector = sectorItem.toString();			
		}

		if (mSiteGeoPoint == null || name.equals("") || type.equals("") || sector.equals("")) {
			Toast.makeText(AddSiteActivity.this, "Please enter name, sector, and type.", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		Site site = new Site();
		site.setName(name);
		site.getProperties().setType(type);
		site.getProperties().setSector(sector);

		site.getProperties().setVisits(0);
		site.getCoordinates().add(mSiteGeoPoint.getLongitude());
		site.getCoordinates().add(mSiteGeoPoint.getLatitude());
//		JsonFileSiteRepository sr = new JsonFileSiteRepository(this);
		mSiteRepository.addSite(site);
		
		Intent i = new Intent(AddSiteActivity.this, SiteDetailActivity.class);
		i.putExtra("site", site);
		i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		startActivity(i);
		finish();
		
//		mAddSiteRequest = new AddSiteRetrofitSpiceRequest(site);
//		getSpiceManager().execute(mAddSiteRequest, "addsite", DurationInMillis.ONE_SECOND,
//				new AddSiteRequestListener());
	}

	/**
	 * onClick handler for button which populates Location field with the user's
	 * current location.
	 * 
	 * @param view
	 */
	public void onPopulateLocationClick(View view) {
		// Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location loc = LocationService.getCurrentLocation();
//		String locationValue;

		if (loc != null) {
			mSiteGeoPoint = new GeoPoint(loc);
//			locationValue = loc.getLatitude() + ", " + loc.getLongitude();
//			mLocationEditText.setText(locationValue);
		} else {
			Toast.makeText(AddSiteActivity.this, "Current location could not be determined.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * Handle MapChangedEvent, fired when the user zooms or scrolls (after
	 * defined delay).
	 * 
	 * When the map is changed, request Sites within the new map bounds.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleMapChanged(MapChangedEvent event) {
		Log.i(TAG, "handleMapChanged");
		BoundingBoxE6 bb = event.getBoundingBox();
		double n = (bb.getLatNorthE6() / 1E6);
		double s = (bb.getLatSouthE6() / 1E6);
		double e = (bb.getLonEastE6() / 1E6);
		double w = (bb.getLonWestE6() / 1E6);
		Log.i(TAG, n + ", " + w + ", " + s + ", " + e);

		SiteList facs = mSiteRepository.getSitesWithin(n, s, e, w);
		bus.post(new SitesLoadedEvent(facs));
	}

	/**
	 * Handle SitePlacedEvent, fired when the user places a new Site on
	 * the map.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleSitePlaced(SitePlacedEvent event) {
		Log.i(TAG, "handleSitePlaced");
		mSiteGeoPoint = event.getGeoPoint();
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

		// We only want to zoom to our location if this is the first location
		// change
		if (mFirstRun) {
			mFirstRun = false;
			this.zoomToMyLocation();
		}
	}
}
