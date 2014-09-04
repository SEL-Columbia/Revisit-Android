package edu.columbia.sel.revisit.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.inject.Inject;

import edu.columbia.sel.revisit.R;

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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import edu.columbia.sel.grout.Grout;
import edu.columbia.sel.grout.TileFetchingListener;
import edu.columbia.sel.grout.event.FetchingErrorEvent;
import edu.columbia.sel.grout.event.FetchingProgressEvent;
import edu.columbia.sel.grout.event.FetchingStartEvent;
import edu.columbia.sel.grout.util.DeleterListener;
import edu.columbia.sel.revisit.api.SitesWithinRetrofitSpiceRequest;
import edu.columbia.sel.revisit.event.DeviceOfflineEvent;
import edu.columbia.sel.revisit.event.LocationChangedEvent;
import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import edu.columbia.sel.revisit.resource.PhotoManager;
import edu.columbia.sel.revisit.resource.util.BitmapUtils;
import edu.columbia.sel.revisit.service.LocationService;
import butterknife.ButterKnife;
import butterknife.OnClick;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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

	private BoundingBoxE6 mBoundingBox;
	private boolean mDoDownload = false;
	
	private Picasso mPicasso;
	private ArrayList<CustomTarget> mImageDownloadTargets;

	private boolean mDoZoomToMyLocation = true;

	// RoboSpice request object to handle fetching of Sites within the
	// bounds of the map view
	private SitesWithinRetrofitSpiceRequest mSitesWithinRequest;

	private ProgressDialog mProgressBar;
	private ProgressDialog mDeleteProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_offline);

		Log.i("SelectOfflineAreaActivity", "_+_+_+_+_+_+_+_+_+_+_ THE TAG");

		// Injection for views and onclick handlers
		ButterKnife.inject(this);
		
		mPicasso = Picasso.with(this);

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

		mGrout = new Grout();
		mGrout.setThreadCount(10);
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

				// go get the Sites
				fetchSites();

				if (!mIsRunning) {
					return;
				}

				// Alert the user that the download has completed successfully.
				AlertDialog.Builder builder = new AlertDialog.Builder(SelectOfflineAreaActivity.this);
				builder.setMessage(
						"The region you selected has been downloaded. You can now use this application offline.")
						.setTitle("Success!");
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
						SelectOfflineAreaActivity.this.finish();
					}
				});
				builder.setNeutralButton("View Offline Map", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent i = new Intent(SelectOfflineAreaActivity.this, SiteMapListActivity.class);
						startActivity(i);
						SelectOfflineAreaActivity.this.finish();
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
				if (!mIsRunning) {
					return;
				}

				if (fee.cause == FetchingErrorEvent.ALREADY_RUNNING) {
					Toast.makeText(SelectOfflineAreaActivity.this, "Download in progress. Please wait.",
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
	protected void onResume() {
		super.onResume();
		// TODO: this doesn't work.
		// mMyLocation = LocationService.getCurrentLocation();
		// // Log.i(TAG, "mMyLocation ------- " + mMyLocation.toString());
		//
		//
		// // mMyLocation =
		// lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		// if (mMyLocation != null) {
		// this.zoomToMyLocation();
		// }
	}
	
	/**
	 * Perform a bit of clean up -- clear out lingering targets.
	 */
	@Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
        	if (mImageDownloadTargets != null) {        	
	            for (CustomTarget target : mImageDownloadTargets) {
	            	mPicasso.cancelRequest(target);            	
	            }
            	mImageDownloadTargets.clear();
            	mImageDownloadTargets = null;            	
            }
        }
    }

	// @Override
	// public void onBackPressed() {
	//
	// }

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
		case R.id.action_mylocation:
			zoomToMyLocation();
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

	private void fetchSites() {
		Log.i(TAG, "00000000000000--     Fetch those Facs.");
		Double n = (mBoundingBox.getLatNorthE6() / 1E6);
		Double s = (mBoundingBox.getLatSouthE6() / 1E6);
		Double e = (mBoundingBox.getLonEastE6() / 1E6);
		Double w = (mBoundingBox.getLonWestE6() / 1E6);
		mSitesWithinRequest = new SitesWithinRetrofitSpiceRequest(String.valueOf(s), String.valueOf(w),
				String.valueOf(n), String.valueOf(e));
		getSpiceManager()
				.execute(mSitesWithinRequest, "sites", DurationInMillis.ONE_SECOND, new SitesRequestListener());
	}

	private void fetchPhotos(SiteList sites) {
		if (sites == null) {
			return;
		}
		
		this.mImageDownloadTargets = new ArrayList<CustomTarget>();
		
		for (final Site s : sites) {
			ArrayList<String> urls = (ArrayList<String>) s.getProperties().getPhotoUrls();
			for (int i = 0; i < urls.size(); i++) {
				final String url = urls.get(i);
				final int pos = i;
				if (url != null) {
					Log.i(TAG, "downloading image: " + url);
					CustomTarget target = new CustomTarget(url, s, pos);
					mImageDownloadTargets.add(target);
					mPicasso.load(url).into(target);
				}
			}
		}
	}

	@OnClick(R.id.download_button)
	public void download(View view) {
		Log.i(TAG, "------------> download initiated");
		// prepare a progress bar dialog
		mProgressBar = new ProgressDialog(this);
		mProgressBar.setCancelable(false);
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
	// @OnClick(R.id.clear_button)
	// public void clearOfflineTiles(View view) {
	// clearOfflineTiles();
	// }

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

	private void zoomToMyLocation() {
		if (mMyLocation == null) {
			Toast.makeText(this, "Current location cannot be determined.", Toast.LENGTH_SHORT).show();
		} else {
			Log.i(TAG, "zoomToMyLocation ----> " + mMyLocation.toString());
			this.zoomToLocation(mMyLocation);
		}
	}

	private void zoomToLocation(Location loc) {
		Log.i(TAG, "zoomToLocation");

		if (loc == null) {
			throw new RuntimeException("Location can not be null.");
		}

		final GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		// mMapCon.animateTo(point);

		// HACK! See https://github.com/osmdroid/osmdroid/issues/22
		// TODO: This should be removed when osmdroid is updated.
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			public void run() {
				mMapCon.animateTo(point);
			}
		});

		// mMapCon.setCenter(point);
	}

	/**
	 * Handle LocationChangedEvent, fired when the application detects a new
	 * user location.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleLocationChanged(LocationChangedEvent event) {
		Log.i(TAG, "handleLocationChanged - SELECT OFFLINE AREA ACTIVITY");

		// always set the current location so that the user can zoom back to it
		mMyLocation = event.getLocation();

		// but only automatically zoom to the user's location when the activity
		// first loads.
		if (this.mDoZoomToMyLocation) {
			this.zoomToMyLocation();
			mDoZoomToMyLocation = false;
		}

	}

	/**
	 * Handle LocationChangedEvent, fired when the application detects a new
	 * user location.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleDeviceOfflineEvent(DeviceOfflineEvent event) {
		Log.i(TAG, "handleDeviceOfflineEvent");

		// Alert the user that the download has completed successfully.
		AlertDialog.Builder builder = new AlertDialog.Builder(SelectOfflineAreaActivity.this);
		builder.setMessage("Please connect to a network and try again.").setTitle("Device Offline");
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User clicked OK button
				SelectOfflineAreaActivity.this.finish();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();

		// Toast.makeText(this, "The device is not currently online.",
		// Toast.LENGTH_SHORT).show();
	}

	// ============================================================================================
	// INNER CLASSES
	// ============================================================================================

	/**
	 * Used by RoboSpice to handle the response from the sites "within" request.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class SitesRequestListener implements RequestListener<SiteList> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			Toast.makeText(SelectOfflineAreaActivity.this, "Failed to load sites.", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final SiteList result) {
			Log.i(TAG, "00000000000 Sites Loaded: " + result.size());
			mSiteRepository.persistSites(result);

			fetchPhotos(result);
		}
	}
	
	public class CustomTarget implements Target {

		private String url;
		private Site site;
		private int index = -1;
		
		public CustomTarget(String url, Site site, int index) {
			this.url = url;
			this.site = site;
			this.index = index;
		}
		
		@Override
		public void onBitmapFailed(Drawable arg0) {
			Log.i(TAG, "image failed to download: " + url);
			Toast.makeText(SelectOfflineAreaActivity.this, "Error downloading Site photo.",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onBitmapLoaded(Bitmap photo, LoadedFrom from) {
			Log.i(TAG, "image downloaded: " + url);
			String dirPath = Environment.getExternalStorageDirectory().toString() + File.separator
					+ "revisit" + File.separator + "photos" + File.separator + site.get_id();
			File dir = new File(dirPath);
			if (!dir.mkdirs()) {
				Log.e(TAG, "Directory might already exist.");
			}
			String path = dirPath + File.separator + index + ".jpg";
			
			BitmapUtils.saveBitmapToFile(photo, path);
			releaseTargetReference();
		}

		@Override
		public void onPrepareLoad(Drawable arg0) {
			// TODO Auto-generated method stub

		}
		
		// release the target reference so it can be GC'd
		private void releaseTargetReference() {
            if (index != -1)
                mImageDownloadTargets.remove(index);
        }
	}
}
