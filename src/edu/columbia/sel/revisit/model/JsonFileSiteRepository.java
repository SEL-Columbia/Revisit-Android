package edu.columbia.sel.revisit.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Bus;

import edu.columbia.sel.revisit.RevisitApplication;
import edu.columbia.sel.revisit.annotation.ForApplication;
import edu.columbia.sel.revisit.api.AddSitePhotoRetrofitSpiceRequest;
import edu.columbia.sel.revisit.api.AddSiteRetrofitSpiceRequest;
import edu.columbia.sel.revisit.api.UpdateSiteRetrofitSpiceRequest;
import edu.columbia.sel.revisit.event.ExternalStorageNotReadableEvent;
import edu.columbia.sel.revisit.event.ExternalStorageNotWritableEvent;
import edu.columbia.sel.revisit.event.SiteSyncErrorEvent;
import edu.columbia.sel.revisit.event.SiteSyncSuccessEvent;
import edu.columbia.sel.revisit.event.SitesSyncCompleteEvent;

/**
 * 
 * @author Jonathan Wohl
 * 
 */
@Singleton
public class JsonFileSiteRepository implements ISiteRepository {

	private final String TAG = this.getClass().getCanonicalName();

	private final String DEFAULT_DIR = "revisit";

	@Inject
	SpiceManager mSpiceManager;

	// @Inject
	Bus mBus;

	@Inject
	@ForApplication
	RevisitApplication mContext;

	private String mFilePath = "locations.json";
	private SiteList mSites;
	private File mStorageDir;

	private int mNumTotalSitesForSync;
	private int mNumSitesRemainingForSync;

	/**
	 * Injectable constructor. Receives bus via DIModule's @Provide of the event
	 * bus.
	 * 
	 * @param bus
	 */
	@Inject
	public JsonFileSiteRepository(Bus bus) {
		mBus = bus;

		if (!this.isExternalStorageWritable()) {
			mBus.post(new ExternalStorageNotWritableEvent());
			return;
		}

		if (!this.isExternalStorageReadable()) {
			mBus.post(new ExternalStorageNotReadableEvent());
			return;
		}

		this.setStorageDir(DEFAULT_DIR);
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String mFilePath) {
		this.mFilePath = mFilePath;
	}

	public void setFilterString(String filter) {
	}

	/**
	 * Get the list of sites. If the in-memory list is present, use it,
	 * otherwise pull from disk and stick it in memory for future access.
	 */
	@Override
	public SiteList getSites() {
		Log.i(TAG, "Getting Sites...");

		// If the sites are in memory, return them
		if (this.mSites != null) {
			Log.i(TAG, "...From MEMORY.");
			return mSites;
		}

		// Sites not yet in memory, try to fetch them from disk
		ObjectMapper mapper = new ObjectMapper();
		try {
			File file = new File(mStorageDir, mFilePath);
			FileInputStream fis = new FileInputStream(file);
			SiteList sites = mapper.readValue(fis, SiteList.class);
			fis.close();
			Log.i(TAG, "...From DISK.");
			// Sites should not be null here, but just as an extra safety
			// precaution
			if (sites != null) {
				this.mSites = sites;
				return sites;
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// If we're here, the sites couldn't be loaded from memory or disk...
		// return an empty list.
		Log.i(TAG, "...No Sites Found On Device.");
		return new SiteList();
	}

	@Override
	public SiteList getSitesWithin(double n, double s, double e, double w) {
		// TODO Auto-generated method stub
		SiteList fullList = this.getSites();
		Log.i(TAG, "Here's the full list count: " + fullList.size());
		Log.i(TAG, "n: " + n + ", s: " + s + ", e: " + e + ", w: " + w);
		SiteList filteredList = new SiteList();
		if (fullList != null) {
			for (Site f : fullList) {

				Log.i(TAG, "Site Coords: " + f.getCoordinates().toString());
				if (s <= f.getCoordinates().get(1) && f.getCoordinates().get(1) <= n) {
					// we're within lats
					if (w <= f.getCoordinates().get(0) && f.getCoordinates().get(0) <= e) {
						// we're within lngs
						filteredList.add(f);
					}
				}
			}
		}
		return filteredList;
	}

	@Override
	public Site getSiteById(String _id) {
		SiteList fullList = this.getSites();
		if (fullList != null) {
			for (Site f : fullList) {
				if (f.get_id() != null && f.get_id().equals(_id)) {
					return f;
				}
			}
		}
		return null;
	}

	/**
	 * Persists the current in-memory list of sites to disk.
	 */
	@Override
	public boolean persistInMemorySitesToDisk() {
		Log.i(TAG, "Persisting Sites...");
		boolean success = true;

		if (!this.isExternalStorageWritable()) {
			mBus.post(new ExternalStorageNotWritableEvent());
			success = false;
		}

		if (mSites != null) {
			success = false;
		}
		
		pojoToFile(mSites);
	
		return success;
	}

	/**
	 * Saves the supplied list of Sites to disk, and replaces in-memory list.
	 */
	@Override
	public boolean persistSites(SiteList sites) {
		Log.i(TAG, "Persisting Sites...");
		boolean success = true;

		if (!this.isExternalStorageWritable()) {
			mBus.post(new ExternalStorageNotWritableEvent());
			success = false;
		}
		
		pojoToFile(sites);

		// replace the in-memory list of Sites with the newly persisted list
		this.mSites = sites;
		return success;
	}

	/**
	 * Performs the actual serialization and saving to disk.
	 * @param o
	 * @return
	 */
	public boolean pojoToFile(Object o) {
		boolean success = true;
		
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(mStorageDir, mFilePath);
		try {
			String json = mapper.writeValueAsString(o);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(json.getBytes());
			fos.close();
			Log.i(TAG, ((ArrayList) o).size() + " Sites Persisted.");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			success = false;
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
		// TODO: HACK! This is to refresh the Android File Transfer
		// application's list... doesn't seem to work consistently
		mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
		
		return success;
	}

	/**
	 * Persist a site, defaulting to markForSync = true
	 * 
	 * @param site
	 * @return
	 */
	public boolean persistSite(Site site) {
		return this.persistSite(site, true);
	}

	@Override
	public boolean persistSite(Site site, boolean markForSync) {
		if (!this.isExternalStorageWritable()) {
			mBus.post(new ExternalStorageNotWritableEvent());
			return false;
		}

		SiteList fullList = this.getSites();

		site.setRequestSync(markForSync);

		// is the site already in our list?
		if (fullList.indexOf(site) != -1) {
			// yes, replace it with the specified Site
			fullList.replace(site);
		} else {
			// no, add it to the list of Sites
			fullList.add(site);
		}

		// persist the entire list to disk
		return this.persistSites(fullList);
	}

	@Override
	public SiteList getSitesForSync() {
		SiteList fullList = this.getSites();
		SiteList filteredList = new SiteList();
		if (fullList != null) {
			for (Site f : fullList) {
				if (f.getRequestSync()) {
					filteredList.add(f);
				}
			}
		}
		return filteredList;
	}

	/**
	 * Adds a site to the in-memory Site list, and marks for sync.
	 */
	@Override
	public boolean addSite(Site site) {
		Log.i(TAG, "addSite");
		// If the site has a server-generated _id, it shouldn't be added again.
		if (site.get_id() != null) {
			return false;
		}
		SiteList fullList = this.getSites();
		site.setRequestSync(true);
		fullList.add(site);
		return true;
	}

	/**
	 * Updates a site in the central in-memory store, marking it for sync
	 * request.
	 */
	@Override
	public boolean updateSite(Site site) {
		Log.i(TAG, "updateSite");
		SiteList fullList = this.getSites();
		// is the site already in our list?
		if (fullList.indexOf(site) != -1) {
			Log.i(TAG, "Replacing Site...");
			// yes, replace it with the specified Site
			site.setRequestSync(true);
			fullList.replace(site);
			return true;
		}
		Log.i(TAG, "Site not found, not updating...");
		return false;
	}

	/**
	 * Saves the passed site to central in-memory store.
	 */
	@Override
	public boolean saveSite(Site site) {
		Log.i(TAG, "saveSite");
		SiteList fullList = this.getSites();
		// is the site already in our list?
		if (fullList.indexOf(site) != -1) {
			Log.i(TAG, "Replacing Site...");
			// yes, replace it with the specified Site
			fullList.replace(site);
			return true;
		}
		Log.i(TAG, "Site not found, not saving...");
		return false;
	}

	/**
	 * Perform the syncing of the marked sites with the server. Sites with no
	 * server-generated _id field are added, while sites with an existing _id
	 * are updated.
	 * 
	 * Requests are tracked manually via the success callbacks in the listeners.
	 * Once all requests have completed, the onSyncComplete() method is called,
	 * which persists the updated list of Sites.
	 */
	@Override
	public boolean syncSites() {
		SiteList sitesForSync = this.getSitesForSync();
		this.mNumTotalSitesForSync = sitesForSync.size();
		this.mNumSitesRemainingForSync = this.mNumTotalSitesForSync;
		if (this.mNumTotalSitesForSync == 0) {
			// no sync required, we consider this a success
			return true;
		}
		for (Site s : sitesForSync) {
			if (s.get_id() == null) {
				Log.i(TAG, "                 ---> ADDING SITE: " + s.getName());
				AddSiteRetrofitSpiceRequest addSiteRequest = new AddSiteRetrofitSpiceRequest(s);
				// Note that cache key (second arg) includes uuid in order to
				// avoid returning cached result
				mSpiceManager.execute(addSiteRequest, "addsite" + s.getUuid(), DurationInMillis.ONE_SECOND,
						new AddSiteRequestListener());
			} else {
				Log.i(TAG, "                 ---> UPDATING SITE: " + s.getName());
				UpdateSiteRetrofitSpiceRequest updateSiteRequest = new UpdateSiteRetrofitSpiceRequest(s);
				// Note that cache key (second arg) includes _id in order to
				// avoid returning cached result
				mSpiceManager.execute(updateSiteRequest, "updatesite" + s.get_id(), DurationInMillis.ONE_SECOND,
						new UpdateSiteRequestListener());

				ArrayList<String> newImages = (ArrayList<String>) s.getAdditionalProperties().get("newImages");
				
				if (newImages != null) {
					for (String path : newImages) {
						Log.i(TAG, "//////    about to upload photo: " + path);
						File photo = new File(path);
						AddSitePhotoRetrofitSpiceRequest addPhotoRequest = new AddSitePhotoRetrofitSpiceRequest(photo, s.get_id());
						mSpiceManager.execute(addPhotoRequest, "addphoto" + path, DurationInMillis.ONE_SECOND, new AddSitePhotoRequestListener());
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean deleteSite(Site site) {
		// TODO Auto-generated method stub
		SiteList sites = this.getSites();
		return sites.remove(site);
	}

	/**
	 * Check if external storage is writable.
	 * 
	 * TODO: Consider moving this to a utility class
	 * 
	 * @return boolean
	 */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Check if external storage is at least readable.
	 * 
	 * TODO: Consider moving this to a utility class
	 * 
	 * @return boolean
	 */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public void setStorageDir(String dirName) {
		File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + dirName);
		if (!file.mkdirs()) {
			Log.e(TAG, "Directory not created");
		}
		mStorageDir = file;
	}

	public File getStorageDir() {
		return mStorageDir;
	}

	/**
	 * Called when the all Sites marked for sync have been successfully synced.
	 */
	public void onSyncComplete() {
		Log.i(TAG, "Sync is Complete.");
		this.persistInMemorySitesToDisk();
		mBus.post(new SitesSyncCompleteEvent());
	}

	/**
	 * Called when a single site finishes syncing.
	 */
	public void updateSyncStatus() {
		this.mNumSitesRemainingForSync -= 1;
		mBus.post(new SiteSyncSuccessEvent());
		if (this.mNumSitesRemainingForSync == 0) {
			this.onSyncComplete();
		}
	}

	/**
	 * Cancel all pending sync requests.
	 */
	public void cancelSync() {
		mSpiceManager.cancelAllRequests();
	}

	/**
	 * INTERNAL CLASSES
	 */

	/**
	 * Used by RoboSpice to handle the response for adding a Site.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class AddSiteRequestListener implements RequestListener<Site> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			mBus.post(new SiteSyncErrorEvent());
			// Toast.makeText(SelectOfflineAreaActivity.this,
			// "Failed to add new Site.", Toast.LENGTH_SHORT).show();
		}

		/**
		 * On Success, we finish the activity and start the Detail activity.
		 */
		@Override
		public void onRequestSuccess(final Site site) {
			Log.i(TAG, "                 ---> Site Added: " + site.getName());
			saveSite(site);
			updateSyncStatus();
		}
	}

	/**
	 * Used by RoboSpice to handle the response for adding a Site.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class UpdateSiteRequestListener implements RequestListener<Site> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			mBus.post(new SiteSyncErrorEvent());
			// Toast.makeText(SelectOfflineAreaActivity.this,
			// "Failed to add new Site.", Toast.LENGTH_SHORT).show();
		}

		/**
		 * On Success, we finish the activity and start the Detail activity.
		 */
		@Override
		public void onRequestSuccess(final Site site) {
			Log.i(TAG, "                 ---> Site Updated: " + site.getName());
			saveSite(site);
			updateSyncStatus();
		}
	}
	
	
	/**
	 * Used by RoboSpice to handle the response for adding a Site.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class AddSitePhotoRequestListener implements RequestListener<Site> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
//			mBus.post(new SiteSyncErrorEvent());
			// Toast.makeText(SelectOfflineAreaActivity.this,
			// "Failed to add new Site.", Toast.LENGTH_SHORT).show();
		}

		/**
		 * On Success, we finish the activity and start the Detail activity.
		 */
		@Override
		public void onRequestSuccess(final Site site) {
			Log.i(TAG, "                 ---> Photo Uploaded for site: " + site.getName());
			saveSite(site);
			updateSyncStatus();
		}
	}
}
