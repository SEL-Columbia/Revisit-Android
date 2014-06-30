package org.columbia.sel.facilitator.activity;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.event.LocationChangedEvent;
import org.columbia.sel.facilitator.grout.OSMTileFetcher;
import org.columbia.sel.facilitator.grout.TileFetchingListener;
import org.columbia.sel.facilitator.grout.TileFetchingService;
import org.columbia.sel.facilitator.grout.TileFetchingService.TileFetchingBinder;
import org.columbia.sel.facilitator.grout.event.FetchingErrorEvent;
import org.columbia.sel.facilitator.grout.event.FetchingProgressEvent;
import org.columbia.sel.facilitator.grout.event.FetchingStartEvent;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.service.LocationService;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.ResourceProxy;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.OnClick;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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
	private OSMTileFetcher mOsmTP;

	TileFetchingService mService;
	boolean mBound = false;

	ProgressDialog mProgressBar;

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
		OnlineTileSourceBase MAPQUESTOSM = new XYTileSource("MapquestOSM",
                ResourceProxy.string.mapquest_osm, 0, 18, 256, ".jpg", new String[] {
                                "http://otile1.mqcdn.com/tiles/1.0.0/map/",
                                "http://otile2.mqcdn.com/tiles/1.0.0/map/",
                                "http://otile3.mqcdn.com/tiles/1.0.0/map/",
                                "http://otile4.mqcdn.com/tiles/1.0.0/map/" });
		mMapView.setTileSource(MAPQUESTOSM);
		mMapCon = (MapController) mMapView.getController();

		// TODO: remove this.
		mMyLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		mOsmTP = new OSMTileFetcher();
		mOsmTP.setTileFetchingListener(new TileFetchingListener() {

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

		// Bind to TileFetchingService
		Intent intent = new Intent(this, TileFetchingService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "------------> onStop()");
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@OnClick(R.id.download_button)
	public void download(View view) {
		Log.i(TAG, "------------> download initiated");
		BoundingBoxE6 bb = mMapView.getBoundingBox();
		Double n = (bb.getLatNorthE6() / 1E6);
		Double s = (bb.getLatSouthE6() / 1E6);
		Double e = (bb.getLonEastE6() / 1E6);
		Double w = (bb.getLonWestE6() / 1E6);

		if (mBound) {
			// mService.fetchTiles(n,s,e,w);
		}

		// prepare a progress bar dialog
		mProgressBar = new ProgressDialog(this);
		mProgressBar.setCancelable(true);
		mProgressBar.setCanceledOnTouchOutside(false);
		mProgressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel Download",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO: Stop download.
						mOsmTP.cancel();
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

		mOsmTP.setBoundingBox(bb);
		mOsmTP.run();
	}
	
	@OnClick(R.id.clear_button)
	public void clearOfflineTiles(View view) {
		mOsmTP.clearOfflineTiles();
	}
	
	@OnClick(R.id.count_button)
	public void countOfflineTiles(View view) {
		int numTiles = mOsmTP.countCachedTiles();
		Toast.makeText(this, "Total Tiles Cached: " + numTiles, Toast.LENGTH_SHORT).show();
	}
	
	@OnClick(R.id.zip_button)
	public void createZip(View view) {
		mOsmTP.setDestinationFile("OfflineTiles.zip");
		mOsmTP.createZipFile();
//		Toast.makeText(this, "Total Tiles Cached: " + numTiles, Toast.LENGTH_SHORT).show();
	}
	
	@OnClick(R.id.gemf_button)
	public void createGemf(View view) {
		mOsmTP.setDestinationFile("OfflineTiles.gemf");
		mOsmTP.createGemfFile();
//		Toast.makeText(this, "Total Tiles Cached: " + numTiles, Toast.LENGTH_SHORT).show();
	}
	
	@OnClick(R.id.map_button)
	public void gotoOfflineMap(View view) {
		Intent i = new Intent(this, FacilityMapListActivity.class);
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

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i(TAG, "------------> SERVICE CONNECTED");
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			TileFetchingBinder binder = (TileFetchingBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.i(TAG, "------------> SERVICE DISCONNECTED");
			mBound = false;
		}
	};

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
}
