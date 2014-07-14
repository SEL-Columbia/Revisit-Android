package edu.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import edu.columbia.sel.facilitator.R;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.ResourceProxy;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import edu.columbia.sel.facilitator.api.AddFacilityRetrofitSpiceRequest;
import edu.columbia.sel.facilitator.api.FacilitiesWithinRetrofitSpiceRequest;
import edu.columbia.sel.facilitator.api.UpdateFacilityRetrofitSpiceRequest;
import edu.columbia.sel.facilitator.event.LocationChangedEvent;
import edu.columbia.sel.facilitator.grout.Grout;
import edu.columbia.sel.facilitator.grout.TileFetchingListener;
import edu.columbia.sel.facilitator.grout.event.FetchingErrorEvent;
import edu.columbia.sel.facilitator.grout.event.FetchingProgressEvent;
import edu.columbia.sel.facilitator.grout.event.FetchingStartEvent;
import edu.columbia.sel.facilitator.grout.util.DeleterListener;
import edu.columbia.sel.facilitator.model.Facility;
import edu.columbia.sel.facilitator.model.FacilityList;
import edu.columbia.sel.facilitator.model.JsonFileSiteRepository;
import edu.columbia.sel.facilitator.service.LocationService;
import butterknife.ButterKnife;
import butterknife.OnClick;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * TODO: THIS ACTIVITY WAS AN INITIAL TEST, NOT CURRENTLY USED
 * 
 * @author Jonathan Wohl
 * 
 */
public class SelectOfflineAreaActivity extends BaseActivity {

	@Inject
	LocationManager lm;

	private Location mMyLocation;

	private MapView mMapView;
	private MapController mMapCon;
	private Grout mGrout;

	BoundingBoxE6 mBoundingBox;
	boolean mDoDownload = false;

	// RoboSpice request object to handle fetching of facilities within the
	// bounds of the map view
	private FacilitiesWithinRetrofitSpiceRequest mFacilitiesWithinRequest;

	ProgressDialog mProgressBar;
	ProgressDialog mDeleteProgressBar;

	// TODO each activity probably doesn't need a reference to the facilities?
	// Centralize in FacilityRepository or the like?
	private FacilityList facilities;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_offline);

		Log.i("SelectOfflineAreaActivity", "_+_+_+_+_+_+_+_+_+_+_ THE TAG");

		// Injection for views and onclick handlers
		ButterKnife.inject(this);

		mMapView = (MapView) this.findViewById(R.id.mapview);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);

		mMapView.getController().setZoom(12);
		OnlineTileSourceBase MAPQUESTOSM = new XYTileSource("MapquestOSM", ResourceProxy.string.mapquest_osm, 0, 18,
				256, ".jpg", new String[] { "http://otile1.mqcdn.com/tiles/1.0.0/map/",
						"http://otile2.mqcdn.com/tiles/1.0.0/map/", "http://otile3.mqcdn.com/tiles/1.0.0/map/",
						"http://otile4.mqcdn.com/tiles/1.0.0/map/" });
		mMapView.setTileSource(MAPQUESTOSM);
		mMapCon = (MapController) mMapView.getController();

		// TODO: remove this.
		mMyLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if (mMyLocation != null) {
			this.zoomToMyLocation();
		}

		mGrout = new Grout();
		mGrout.setMaxzoom(17);
		// For now we're just using raw tiles on the file system... seems
		// fastest, and the archives
		// aren't working with the jpgs fetched (as .png files) from the tile
		// servers.
		// mGrout.setDestinationFile("OfflineTiles.gemf");
		mGrout.setDeleterListener(new DeleterListener() {

			@Override
			public void onDeleteComplete() {
				mDeleteProgressBar.dismiss();
				if (mDoDownload) {
					mGrout.run();
					mDoDownload = false;
				}
			}

			@Override
			public void onDeleteStart() {
			}

			@Override
			public void onDeleteError() {
			}

		});
		mGrout.setTileFetchingListener(new TileFetchingListener() {

			@Override
			public void onTileDownloaded() {
				// Log.i(TAG, "------------> onTileDownloaded");
			}

			@Override
			public void onFetchingStart(FetchingStartEvent fse) {
				Log.i(TAG, "-----------> onFetchingStart: total: " + fse.total);
				mProgressBar.setMax(fse.total);
				mProgressBar.show();
			}

			@Override
			public void onFetchingStop() {
				Log.i(TAG, "-----------> onFetchingStop");
			}

			@Override
			public void onFetchingComplete() {
				mProgressBar.dismiss();

				// go get the facilities
				fetchFacilities();

				// Alert the user that the download has completed successfully.
				AlertDialog.Builder builder = new AlertDialog.Builder(SelectOfflineAreaActivity.this);
				builder.setMessage(
						"The region you selected has been downloaded. You can now use this application offline.")
						.setTitle("Success!");
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}

			@Override
			public void onFetchingProgress(FetchingProgressEvent fpe) {
				Log.i(TAG, "FetchingProgressEvent ---- complete: " + fpe.completed + ", total: " + fpe.total
						+ ", percent: " + fpe.percent);

				mProgressBar.setProgress(fpe.completed);
			}

			@Override
			public void onFetchingError(FetchingErrorEvent fee) {
				// TODO Auto-generated method stub
				if (fee.cause == FetchingErrorEvent.ALREADY_RUNNING) {
					Toast.makeText(SelectOfflineAreaActivity.this, "Region download in progress. Please wait.",
							Toast.LENGTH_SHORT).show();
				}
				if (fee.cause == FetchingErrorEvent.INVALID_REGION) {
					Toast.makeText(SelectOfflineAreaActivity.this, "The selected region is invalid.",
							Toast.LENGTH_SHORT).show();
				}
				if (fee.cause == FetchingErrorEvent.MAX_REGION_SIZE_EXCEEDED) {
					Toast.makeText(SelectOfflineAreaActivity.this, "Please select a smaller region.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "------------> onStart()");

		// start location service
		startService(new Intent(this, LocationService.class));
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "------------> onStop()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.offline_select_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
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

	private void fetchFacilities() {
		Log.i(TAG, "00000000000000--     Fetch those Facs.");
		Double n = (mBoundingBox.getLatNorthE6() / 1E6);
		Double s = (mBoundingBox.getLatSouthE6() / 1E6);
		Double e = (mBoundingBox.getLonEastE6() / 1E6);
		Double w = (mBoundingBox.getLonWestE6() / 1E6);
		mFacilitiesWithinRequest = new FacilitiesWithinRetrofitSpiceRequest(String.valueOf(s), String.valueOf(w),
				String.valueOf(n), String.valueOf(e));
		getSpiceManager().execute(mFacilitiesWithinRequest, "facilities", DurationInMillis.ONE_SECOND,
				new FacilitiesRequestListener());
	}

	@OnClick(R.id.download_button)
	public void download(View view) {
		Log.i(TAG, "------------> download initiated");
		// prepare a progress bar dialog
		mProgressBar = new ProgressDialog(this);
		mProgressBar.setCancelable(true);
		mProgressBar.setCanceledOnTouchOutside(false);
		mProgressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel Download",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mGrout.cancel();
					}

				});
		// mProgressBar.setButton(DialogInterface.BUTTON_POSITIVE,
		// "Continue in Background", new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// mProgressBar.dismiss();
		// }
		//
		// });

		mProgressBar.setMessage("Downloading Map Tiles...");
		mProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressBar.setProgress(0);

		// Set additional params for Grout
		mBoundingBox = mMapView.getBoundingBox();
		mGrout.setBoundingBox(mBoundingBox);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		mGrout.setServerURL(sharedPref.getString("tile_server_url", "http://otile1.mqcdn.com/tiles/1.0.0/map/"));
		mDoDownload = true;

		// For our purpose, we want to clear offline tiles first, then start the
		// new download, so we use the
		// onDeleteComplete listener to start the new download rather than
		// kicking of the download here.
		// mGrout.run();
		clearOfflineTiles();
	}

	/**
	 * Public click handler for clearing offline tiles, not to be confused with
	 * the private method called therein.
	 * 
	 * @param view
	 */
	@OnClick(R.id.clear_button)
	public void clearOfflineTiles(View view) {
		clearOfflineTiles();
	}

	/**
	 * Show the Delete progress dialog, then begin clearing the tiles.
	 */
	private void clearOfflineTiles() {
		mDeleteProgressBar = new ProgressDialog(this);
		mDeleteProgressBar.setCancelable(true);
		mDeleteProgressBar.setCanceledOnTouchOutside(false);
		mDeleteProgressBar.setTitle("Deleting Tiles...");
		mDeleteProgressBar.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface arg0) {
				mGrout.clearOfflineTiles();
			}
		});
		mDeleteProgressBar.show();
	}

	@OnClick(R.id.sync_button)
	public void syncFacilities(View view) {
//		JsonFileSiteRepository sr = new JsonFileSiteRepository(this);
//		sr.syncSites();
//		for (Facility f : facs) {
//			if (f.get_id() == null) {
//				Log.i(TAG, "                 ---> ADDING FACILITY.");
//				AddFacilityRetrofitSpiceRequest addFacilityRequest = new AddFacilityRetrofitSpiceRequest(f);
//				getSpiceManager().execute(addFacilityRequest, "addfacility", DurationInMillis.ONE_SECOND,
//						new AddFacilityRequestListener());
//			} else {
//				Log.i(TAG, "                 ---> UPDATING FACILITY.");
//				UpdateFacilityRetrofitSpiceRequest updateFacilityRequest = new UpdateFacilityRetrofitSpiceRequest(f);
//				getSpiceManager().execute(updateFacilityRequest, "updatefacility", DurationInMillis.ONE_SECOND,
//						new UpdateFacilityRequestListener());
//			}
//		}
		FacilityList sitesForSync = this.mSiteRepository.getSitesForSync();
		Toast.makeText(this, "Total Facilities Marked for Sync: " + sitesForSync.size(), Toast.LENGTH_SHORT).show();
		this.mSiteRepository.syncSites();
	}

	//
	// @OnClick(R.id.zip_button)
	// public void createZip(View view) {
	// mOsmTP.setDestinationFile("OfflineTiles.zip");
	// mOsmTP.createZipFile();
	// // Toast.makeText(this, "Total Tiles Cached: " + numTiles,
	// // Toast.LENGTH_SHORT).show();
	// }
	//
	// @OnClick(R.id.gemf_button)
	// public void createGemf(View view) {
	// mOsmTP.setDestinationFile("OfflineTiles.gemf");
	// mOsmTP.createGemfFile();
	// // Toast.makeText(this, "Total Tiles Cached: " + numTiles,
	// // Toast.LENGTH_SHORT).show();
	// }

	@OnClick(R.id.map_button)
	public void gotoOfflineMap(View view) {
		Intent i = new Intent(this, FacilityMapListActivity.class);
		// finish();
		startActivity(i);
	}

	private void zoomToMyLocation() {
		if (mMyLocation == null) {
			Toast.makeText(this, "Current location cannot be determined.", Toast.LENGTH_SHORT).show();
		} else {
			this.zoomToLocation(mMyLocation);
		}

	}

	private void zoomToLocation(Location loc) {
		Log.i(TAG, "zoomToLocation");

		if (loc == null) {
			throw new RuntimeException("Location can not be null.");
		}

		GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		mMapCon.animateTo(point);
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
		this.zoomToMyLocation();

	}

	// ============================================================================================
	// INNER CLASSES
	// ============================================================================================

	/**
	 * Used by RoboSpice to handle the response from the facilities "within"
	 * request.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class FacilitiesRequestListener implements RequestListener<FacilityList> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			Toast.makeText(SelectOfflineAreaActivity.this, "Failed to load facilities.", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final FacilityList result) {
			Log.i(TAG, "00000000000 Facilities Loaded: " + result.size());
//			JsonFileSiteRepository sr = new JsonFileSiteRepository(SelectOfflineAreaActivity.this);
			mSiteRepository.persistSites(result);
		}
		// bus.post(new FacilitiesLoadedEvent(result));
	}

	/**
	 * Used by RoboSpice to handle the response for adding a Facility.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class AddFacilityRequestListener implements RequestListener<Facility> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			Toast.makeText(SelectOfflineAreaActivity.this, "Failed to add new facility.", Toast.LENGTH_SHORT).show();
		}

		/**
		 * On Success, we finish the activity and start the Detail activity.
		 */
		@Override
		public void onRequestSuccess(final Facility facility) {
			Log.i(TAG, "Facility Added!");
			facility.setRequestSync(false);
			Toast.makeText(SelectOfflineAreaActivity.this, facility.getName() + " saved to server.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * Used by RoboSpice to handle the response for adding a Facility.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class UpdateFacilityRequestListener implements RequestListener<Facility> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			Toast.makeText(SelectOfflineAreaActivity.this, "Failed to add new facility.", Toast.LENGTH_SHORT).show();
		}

		/**
		 * On Success, we finish the activity and start the Detail activity.
		 */
		@Override
		public void onRequestSuccess(final Facility facility) {
			facility.setRequestSync(false);
			Toast.makeText(SelectOfflineAreaActivity.this, facility.getName() + " saved to server.", Toast.LENGTH_SHORT)
					.show();
		}
	}
}
