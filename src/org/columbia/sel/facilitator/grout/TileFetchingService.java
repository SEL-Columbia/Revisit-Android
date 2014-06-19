package org.columbia.sel.facilitator.grout;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class TileFetchingService extends Service {
	// Binder given to clients
    private final IBinder mBinder = new TileFetchingBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class TileFetchingBinder extends Binder {
    	TileFetchingService getService() {
            // Return this instance of TileFetchingService so clients can call public methods
            return TileFetchingService.this;
        }
    }

}
