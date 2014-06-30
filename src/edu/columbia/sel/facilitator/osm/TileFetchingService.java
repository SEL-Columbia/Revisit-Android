package edu.columbia.sel.facilitator.osm;

import edu.columbia.sel.facilitator.grout.OSMTileFetcher;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TileFetchingService extends Service {
	public String TAG = this.getClass().getCanonicalName();
	
	// Binder given to clients
    private final IBinder mBinder = new TileFetchingBinder();
    private Boolean mIsRunning = false;
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "-----------------> onBind()");
		return mBinder;
	}

	// ===========================================================
	// Getters/Setters
	// ===========================================================
    
    public Boolean getIsRunning() {
    	return mIsRunning;
    }
    
    public void setIsRunning(Boolean pIsRunning) {
    	mIsRunning = pIsRunning;
    }
    
	// ===========================================================
	// Methods
	// ===========================================================

	public void fetchTiles(Double north, Double south, Double east, Double west) {
		Log.i(TAG, "-----------------> fetchTiles");
		if (!mIsRunning) {
			OSMTileFetcher osmTP = new OSMTileFetcher(north, south, east, west);
			osmTP.run();
			mIsRunning = true;
		}
	}
	

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class TileFetchingBinder extends Binder {
		public TileFetchingService getService() {
			Log.i(TAG, "-----------------> getService()");
			
			// Return this instance of TileFetchingService so clients can call public methods
			return TileFetchingService.this;
		}
		
	}


}
