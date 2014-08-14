package edu.columbia.sel.revisit.api;

import java.io.File;

import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;
import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import edu.columbia.sel.revisit.model.Site;

/**
 * Defines RoboSpice Request for adding a new Site.
 * 
 * @author Jonathan Wohl
 *
 */
public class AddSitePhotoRetrofitSpiceRequest extends RetrofitSpiceRequest<Site, RevisitApi> {
	private String TAG = this.getClass().getCanonicalName();
	
	private String siteId;
	private File photo;

    public AddSitePhotoRetrofitSpiceRequest(File photo, String siteId) {
        super(Site.class, RevisitApi.class);
        this.photo = photo;
        this.siteId = siteId;
    }

    @Override
    public Site loadDataFromNetwork() {
    	Log.i(TAG, "Uploading new photo.");
    	TypedFile tf = new TypedFile("application/octet-stream", photo);
    	return ((RevisitApi) getService()).uploadPhoto(tf, siteId);
    }
}
