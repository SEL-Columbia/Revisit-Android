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

import com.squareup.otto.Subscribe;

import edu.columbia.sel.facilitator.event.LocationChangedEvent;
import edu.columbia.sel.facilitator.grout.Grout;
import edu.columbia.sel.facilitator.grout.TileFetchingListener;
import edu.columbia.sel.facilitator.grout.event.FetchingErrorEvent;
import edu.columbia.sel.facilitator.grout.event.FetchingProgressEvent;
import edu.columbia.sel.facilitator.grout.event.FetchingStartEvent;
import edu.columbia.sel.facilitator.grout.util.DeleterListener;
import edu.columbia.sel.facilitator.model.FacilityList;
import edu.columbia.sel.facilitator.osm.TileFetchingService;
import edu.columbia.sel.facilitator.osm.TileFetchingService.TileFetchingBinder;
import edu.columbia.sel.facilitator.service.LocationService;
import butterknife.ButterKnife;
import butterknife.OnClick;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
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
	private Grout mGrout;

	TileFetchingService mService;
	boolean mBound = false;
	
	boolean mDoDownload = false;

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
		mGrout.setDestinationFile("OfflineTiles.gemf");
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
				
				// Alert the user that the download has completed successfully.
				AlertDialog.Builder builder = new AlertDialog.Builder(SelectOfflineAreaActivity.this);
				builder.setMessage("Success!")
				       .setTitle("The region you selected has been downloaded. You can now use this application offline.");
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

		mGrout.setBoundingBox(bb);
		mDoDownload = true;
		clearOfflineTiles();
//		mGrout.run();
	}

	@OnClick(R.id.clear_button)
	public void clearOfflineTiles(View view) {
		clearOfflineTiles();
	}
	
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
//	@OnClick(R.id.count_button)
//	public void countOfflineTiles(View view) {
//		int numTiles = mOsmTP.countCachedTiles();
//		Toast.makeText(this, "Total Tiles Cached: " + numTiles, Toast.LENGTH_SHORT).show();
//	}
//
//	@OnClick(R.id.zip_button)
//	public void createZip(View view) {
//		mOsmTP.setDestinationFile("OfflineTiles.zip");
//		mOsmTP.createZipFile();
//		// Toast.makeText(this, "Total Tiles Cached: " + numTiles,
//		// Toast.LENGTH_SHORT).show();
//	}
//
//	@OnClick(R.id.gemf_button)
//	public void createGemf(View view) {
//		mOsmTP.setDestinationFile("OfflineTiles.gemf");
//		mOsmTP.createGemfFile();
//		// Toast.makeText(this, "Total Tiles Cached: " + numTiles,
//		// Toast.LENGTH_SHORT).show();
//	}

	@OnClick(R.id.map_button)
	public void gotoOfflineMap(View view) {
		Intent i = new Intent(this, FacilityMapListActivity.class);
		finish();
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
