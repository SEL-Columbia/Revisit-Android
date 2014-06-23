package org.columbia.sel.facilitator.activity;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.grout.OSMMapTilePackager;
import org.columbia.sel.facilitator.grout.TileFetchingService;
import org.columbia.sel.facilitator.grout.TileFetchingService.TileFetchingBinder;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.model.FacilityRepository;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import butterknife.ButterKnife;
import butterknife.OnClick;
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

/**
 * TODO: THIS ACTIVITY WAS AN INITIAL TEST, NOT CURRENTLY USED
 * 
 * @author Jonathan Wohl
 *
 */
public class SelectOfflineAreaActivity extends BaseActivity {
	
	@Inject FacilityRepository fr;
	
	@Inject LocationManager lm;
	
	private Location mMyLocation;
	
	private MapView mMapView;
	private MapController mMapCon;
	private ItemizedOverlay<OverlayItem> mMyLocationOverlay;
	private DefaultResourceProxyImpl mResourceProxy;
	
	TileFetchingService mService;
    boolean mBound = false;
	
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
		
		mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		
		this.setupLocationListener();
		
		this.zoomToMyLocation();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		// Bind to TileFetchingService
        Intent intent = new Intent(this, TileFetchingService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
	
	@OnClick(R.id.download_button)
	public void download(View view) {
		Log.i("SelectOfflineAreaActivity", "++++++++++++ clicked");
		if (mBound) {
			
			BoundingBoxE6 bb = mMapView.getBoundingBox();
			Double n = (bb.getLatNorthE6() / 1E6);
			Double s = (bb.getLatSouthE6() / 1E6);
			Double e = (bb.getLonEastE6() / 1E6);
			Double w = (bb.getLonWestE6() / 1E6);
			
			mService.fetchTiles(n,s,e,w);
		}
		
//		OSMMapTilePackager osmTP = new OSMMapTilePackager(n, s, e, w);
//		osmTP.run();
//		String tempDir = Environment.getExternalStorageDirectory().toString() + "/osmdroid/tiles/Mapnik";
//		String[] args = {"-u", "http://otile1.mqcdn.com/tiles/1.0.0/map/%d/%d/%d.png", "-t", tempDir, "-zmin", "4", "-zmax", "12", "-n", n, "-s", s, "-e", e, "-w", w, "-fa", ".tile"};
//		tp.start(args);
	}
	
	
	
	private void setupLocationListener() {
		Log.i(TAG, "setupLocationListener");
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	Log.i(TAG, "=============> LOCATION UPDATED: " + location.toString());
		    	mMyLocation = location;
		    	zoomToLocation(location);
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {
		    	Log.i(TAG, "=============> STATUS CHANGES: " + provider);
		    }

		    public void onProviderEnabled(String provider) {
		    	Log.i(TAG, "=============> PROVIDER ENABLED: " + provider);
		    }

		    public void onProviderDisabled(String provider) {
		    	Log.i(TAG, "=============> PROVIDER DISABLED: " + provider);
		    }
		  };

		// Register the listener with the Location Manager to receive location updates
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	
	private void zoomToMyLocation() {
		Log.i(TAG, "zoomToMyLocation");
		
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if (loc == null) {
			loc = new Location(LocationManager.GPS_PROVIDER);
			loc.setLatitude(41.0);
			loc.setLongitude(-79.0);
		}

		this.zoomToLocation(loc);
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
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	TileFetchingBinder binder = (TileFetchingBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
