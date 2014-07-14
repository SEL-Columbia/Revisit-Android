package edu.columbia.sel.revisit.service;

import javax.inject.Inject;

import com.squareup.otto.Bus;

import edu.columbia.sel.revisit.RevisitApplication;
import edu.columbia.sel.revisit.event.DeviceOfflineEvent;
import edu.columbia.sel.revisit.event.LocationChangedEvent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * A service that manages location information. Largely based on info here:
 * 
 * http://developer.android.com/guide/topics/location/strategies.html
 * 
 * @author Jonathan Wohl
 * 
 */
public class LocationService extends Service implements LocationListener {
	public String TAG = this.getClass().getCanonicalName();

	boolean isGPSEnabled = false;

	boolean isNetworkEnabled = false;

	final static long MIN_TIME_INTERVAL = 60 * 1000L;

	private static Location mCurrentLocation;

	@Inject
	Bus bus;

	@Inject
	LocationManager mLocationManager;

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 0;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "LocationService created");
		((RevisitApplication) getApplication()).inject(this);
		if (!this.locationProvidersEnabled()) {
			Toast.makeText(this, "Cannot get location: No provider enabled.", Toast.LENGTH_SHORT).show();
			throw new RuntimeException("Cannot get location: No provider enabled.");
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// For time consuming an long tasks you can launch a new thread here...
		Log.i(TAG, "LocationService started");
		bus.register(this);
		this.start();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "LocationService destroyed");
		bus.unregister(this);
		this.stopLocationRequests();
	}

	/**
	 * Check if the device has a network connection
	 * 
	 * @return
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public void start() {
		if (isOnline()) {
			try {
				Location tempLocation;
				
				isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

				isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

				if (isNetworkEnabled) {
					if (mLocationManager != null) {
						mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d(TAG, "Network Provider Enabled");
						tempLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (tempLocation != null && isBetterLocation(tempLocation, mCurrentLocation))
							Log.i(TAG, "-------> NETWORK: " + tempLocation.toString());
							mCurrentLocation = tempLocation;
					}
				}

				if (isGPSEnabled) {
					if (mLocationManager != null) {
						mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
						mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d(TAG, "GPS Provider Enabled");
						tempLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (tempLocation != null && isBetterLocation(tempLocation, mCurrentLocation))
							Log.i(TAG, "-------> GPS: " + tempLocation.toString());
							mCurrentLocation = tempLocation;
					}
				}
//				bus.post(new LocationChangedEvent(mCurrentLocation));
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
			}
		} else {
			// The device is offline. Let's see if we have a last known location, and post it along with the 
			// DeviceOfflineEvent.
			Log.e(TAG, "ERROR: Device not online.");
			mCurrentLocation = getLastKnownLocation();
			if (mCurrentLocation != null) {
				bus.post(new LocationChangedEvent(mCurrentLocation));
			}
			bus.post(new DeviceOfflineEvent());
		}
	}
	
	private Location getLastKnownLocation() {
		Location tempNetworkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location tempGpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		// if we have last known locs from both providers, see which is better and return it
		if (tempNetworkLocation != null && tempGpsLocation != null) {
			if (isBetterLocation(tempNetworkLocation, tempGpsLocation)) {
				return tempNetworkLocation;
			} else {
				return tempGpsLocation;
			}
		}
		
		// if we only have one of the two, return the one we have
		if (tempNetworkLocation != null) {
			return tempNetworkLocation;
		} else if (tempGpsLocation != null) {
			return tempGpsLocation;
		}
		
		return null;
	}

	/**
	 * Stop location updates.
	 */
	public void stopLocationRequests() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(LocationService.this);
		}
	}

	/**
	 * Determine whether or not the necessary location providers are enabled.
	 * 
	 * @return
	 */
	public boolean locationProvidersEnabled() {

		// get GPS enabled status
		isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// get Network enabled status
		isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// if either GPS or Network is enabled, return true
		return isGPSEnabled || isNetworkEnabled;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.i(TAG,
				"----------> New Location from " + location.getProvider() + ": " + location.getLatitude() + ", "
						+ location.getLongitude());
		if (location != null && isBetterLocation(location, this.mCurrentLocation)) {
			this.mCurrentLocation = location;
			bus.post(new LocationChangedEvent(location));
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * Return the most recent and accurate location.
	 * 
	 * @return
	 */
	public static Location getCurrentLocation() {
		return mCurrentLocation;
	}

	/**
	 * Check if the new location is 'better' than the current best location.
	 * 
	 * Note: This code is taken directly from the Android docs (url above).
	 * 
	 * @param location
	 * @param currentBestLocation
	 * @return
	 */
	private boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > MIN_TIME_INTERVAL;
		boolean isSignificantlyOlder = timeDelta < -MIN_TIME_INTERVAL;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location,
		// use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must
			// be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether two providers are the same.
	 * */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
