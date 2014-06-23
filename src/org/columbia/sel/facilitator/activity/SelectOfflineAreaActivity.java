package org.columbia.sel.facilitator.activity;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.event.LocationChangedEvent;
import org.columbia.sel.facilitator.grout.FetchingProgressEvent;
import org.columbia.sel.facilitator.grout.FetchingStartEvent;
import org.columbia.sel.facilitator.grout.OSMMapTilePackager;
import org.columbia.sel.facilitator.grout.TileFetchingListener;
import org.columbia.sel.facilitator.grout.TileFetchingService;
import org.columbia.sel.facilitator.grout.TileFetchingService.TileFetchingBinder;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.model.FacilityRepository;
import org.columbia.sel.facilitator.service.LocationService;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.OnClick;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
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

	private LocationService mLocationService;
	
	private Location mMyLocation;
	
	private MapView mMapView;
	private MapController mMapCon;
	private OSMMapTilePackager mOsmTP;
	
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
		mMapCon = (MapController) mMapView.getController();
		
		
		mOsmTP = new OSMMapTilePackager();
		mOsmTP.setTileFetchingListener(new TileFetchingListener() {

			@Override
			public void onTileDownloaded() {
				// TODO Auto-generated method stub
//				Log.i(TAG, "------------> onTileDownloaded");
			}

			@Override
			public void onFetchingStart(FetchingStartEvent fse) {
				// TODO Auto-generated method stub
//				Log.i(TAG, "-----------> onFetchingStart: total: " + fse.total );
				mProgressBar.setMax(fse.total);
				mProgressBar.show();
			}

			@Override
			public void onFetchingStop() {
				// TODO Auto-generated method stub
//				Log.i(TAG, "-----------> onFetchingStop");				
			}

			@Override
			public void onFetchingComplete() {
				// TODO Auto-generated method stub
//				Log.i(TAG, "-----------> onFetchingComplete");
				mProgressBar.dismiss();
			}

			@Override
			public void onFetchingProgress(FetchingProgressEvent fpe) {
				// TODO Auto-generated method stub
				Log.i(TAG, "FetchingProgressEvent ---- complete: " + fpe.completed + ", total: " + fpe.total +
						", percent: " + fpe.percent);
//				int percent = Math.round(100*fpe.percent);
				mProgressBar.setProgress(fpe.completed);
			}
		});
		
		this.zoomToMyLocation();
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
//			mService.fetchTiles(n,s,e,w);
		}
		
		// prepare for a progress bar dialog
		mProgressBar = new ProgressDialog(this);
		mProgressBar.setCancelable(true);
		mProgressBar.setMessage("Downloading Map Tiles...");
		mProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressBar.setProgress(0);
		
		mOsmTP.setBoundingBox(bb);
		mOsmTP.run();
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
        public void onServiceConnected(ComponentName className,
                IBinder service) {
        	Log.i(TAG, "------------> SERVICE CONNECTED");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
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
	 * Handle LocationChangedEvent, fired when the application detects a new user location.
	 * 
	 * @param event
	 */
	@Subscribe public void handleLocationChanged(LocationChangedEvent event) {
		Log.i(TAG, "handleLocationChanged");
		mMyLocation = event.getLocation();
		this.zoomToMyLocation();			
		
	}
}
