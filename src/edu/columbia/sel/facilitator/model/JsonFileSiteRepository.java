package edu.columbia.sel.facilitator.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import edu.columbia.sel.facilitator.FacilitatorApplication;
import edu.columbia.sel.facilitator.annotation.ForApplication;
import edu.columbia.sel.facilitator.api.AddFacilityRetrofitSpiceRequest;
import edu.columbia.sel.facilitator.api.UpdateFacilityRetrofitSpiceRequest;

/**
 * TODO: check External Storage availability before attempting read/write
 * 
 * @author Jonathan Wohl
 * 
 */
@Singleton
public class JsonFileSiteRepository implements ISiteRepository {

	private final String TAG = this.getClass().getCanonicalName();

	private final String DEFAULT_DIR = Environment.getExternalStorageDirectory().toString() + File.separator
			+ "revisit";

	@Inject
	SpiceManager mSpiceManager;

	@Inject
	@ForApplication
	FacilitatorApplication mContext;

	private String mFilePath = "locations.json";
	private FacilityList mSites;
	private File mRootDir;
	
	private int mNumTotalSitesForSync;
	private int mNumSitesRemainingForSync;

	@Inject
	public JsonFileSiteRepository() {
		mRootDir = new File(DEFAULT_DIR);
		mRootDir.mkdirs();
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String mFilePath) {
		this.mFilePath = mFilePath;
	}

	/**
	 * Get the list of sites. If the in-memory list is present, use it, otherwise pull from disk and stick it in
	 * memory for future access.
	 */
	@Override
	public FacilityList getSites() {
		Log.i(TAG, "Getting Sites...");

		// If the sites are in memory, return them
		if (this.mSites != null) {
			Log.i(TAG, "...From MEMORY.");
			return mSites;
		}

		// Sites not yet in memory, try to fetch them from disk
		ObjectMapper mapper = new ObjectMapper();
		try {
			File file = new File(mRootDir, mFilePath);
			FileInputStream fis = new FileInputStream(file);
			FacilityList sites = mapper.readValue(fis, FacilityList.class);
			fis.close();
			Log.i(TAG, "...From DISK.");
			// Sites should not be null here, but just as an extra safety precaution
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
		return new FacilityList();
	}

	@Override
	public FacilityList getSitesWithin(double n, double s, double e, double w) {
		// TODO Auto-generated method stub
		FacilityList fullList = this.getSites();
		Log.i(TAG, "Here's the full list count: " + fullList.size());
		Log.i(TAG, "n: " + n + ", s: " + s + ", e: " + e + ", w: " + w);
		FacilityList filteredList = new FacilityList();
		if (fullList != null) {
			for (Facility f : fullList) {
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
	public Facility getSiteById(String _id) {
		FacilityList fullList = this.getSites();
		if (fullList != null) {
			for (Facility f : fullList) {
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
	public boolean persistSites() {
		Log.i(TAG, "Persisting Sites...");
		if (mSites != null) {
			return false;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(mSites);
			File file = new File(mRootDir, mFilePath);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(json.getBytes());
			fos.close();
			Log.i(TAG, "Sites Persisted.");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		// save this to memory within this instance for faster access
		// this.mSites = sites;
		return true;
	}

	/**
	 * Saves the supplied list of facilities to disk, and replaces in-memory list.
	 */
	@Override
	public boolean persistSites(FacilityList sites) {
		Log.i(TAG, "Persisting Sites...");
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(sites);
			File file = new File(mRootDir, mFilePath);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(json.getBytes());
			fos.close();
			Log.i(TAG, sites.size() + " Sites Persisted.");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		// replace the in-memory list of Sites with the newly persisted list
		this.mSites = sites;
		return true;
	}

	/**
	 * Persist a site, defaulting to markForSync = true
	 * @param site
	 * @return
	 */
	public boolean persistSite(Facility site) {
		return this.persistSite(site, true);
	}
	
	@Override
	public boolean persistSite(Facility site, boolean markForSync) {
		FacilityList fullList = this.getSites();

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
	public FacilityList getSitesForSync() {
		FacilityList fullList = this.getSites();
		FacilityList filteredList = new FacilityList();
		if (fullList != null) {
			for (Facility f : fullList) {
				if (f.getRequestSync()) {
					filteredList.add(f);
				}
			}
		}
		return filteredList;
	}

	/**
	 * Adds a site to the in-memory Facility list, and marks for sync.
	 */
	@Override
	public boolean addSite(Facility site) {
		Log.i(TAG, "addSite");
		// If the site has a server-generated _id, it shouldn't be added again.
		if (site.get_id() != null) {
			return false;
		}
		FacilityList fullList = this.getSites();
		site.setRequestSync(true);
		fullList.add(site);
		return true;
	}

	/**
	 * Updates a site in the central in-memory store, marking it for sync request.
	 */
	@Override
	public boolean updateSite(Facility site) {
		Log.i(TAG, "updateSite");
		FacilityList fullList = this.getSites();
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
	public boolean saveSite(Facility site) {
		Log.i(TAG, "saveSite");
		FacilityList fullList = this.getSites();
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
	 * Perform the syncing of the marked sites with the server. Sites with no server-generated _id field are added,
	 * while sites with an existing _id are updated.
	 * 
	 * Requests are tracked manually via the success callbacks in the listeners. Once all requests have completed, the
	 * onSyncComplete() method is called, which persists the updated list of Sites.
	 */
	@Override
	public boolean syncSites() {
		FacilityList sitesForSync = this.getSitesForSync();
		this.mNumTotalSitesForSync = sitesForSync.size();
		this.mNumSitesRemainingForSync = this.mNumTotalSitesForSync;
		if (this.mNumTotalSitesForSync == 0) {
			// no sync required, we consider this a success
			return true;
		}
		for (Facility f : sitesForSync) {
			if (f.get_id() == null) {
				Log.i(TAG, "                 ---> ADDING FACILITY.");
				AddFacilityRetrofitSpiceRequest addFacilityRequest = new AddFacilityRetrofitSpiceRequest(f);
				// Note that cache key (second arg) includes uuid in order to avoid returning cached result
				mSpiceManager.execute(addFacilityRequest, "addfacility"+f.getUuid(), DurationInMillis.ONE_SECOND,
						new AddFacilityRequestListener());
			} else {
				Log.i(TAG, "                 ---> UPDATING FACILITY: " + f.getName());
				UpdateFacilityRetrofitSpiceRequest updateFacilityRequest = new UpdateFacilityRetrofitSpiceRequest(f);
				// Note that cache key (second arg) includes _id in order to avoid returning cached result
				mSpiceManager.execute(updateFacilityRequest, "updatefacility"+f.get_id(), DurationInMillis.ONE_SECOND,
						new UpdateFacilityRequestListener());
			}
		}
		return false;
	}

	@Override
	public boolean deleteSite(Facility site) {
		// TODO Auto-generated method stub
		FacilityList sites = this.getSites();
		return sites.remove(site);
	}
	
	/**
	 * Called when the all Sites marked for sync have been successfully synced.
	 */
	public void onSyncComplete() {
		Log.i(TAG, "Sync is Complete.");
		this.persistSites();
	}
	
	/**
	 * Called when a single site finishes syncing.
	 */
	public void updateSyncStatus() {
		this.mNumSitesRemainingForSync -= 1;
		if (this.mNumSitesRemainingForSync == 0) {
			this.onSyncComplete();
		}
	}

	/**
	 * Used by RoboSpice to handle the response for adding a Facility.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class AddFacilityRequestListener implements RequestListener<Facility> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			// Toast.makeText(SelectOfflineAreaActivity.this,
			// "Failed to add new facility.", Toast.LENGTH_SHORT).show();
		}

		/**
		 * On Success, we finish the activity and start the Detail activity.
		 */
		@Override
		public void onRequestSuccess(final Facility site) {
			Log.i(TAG, "                 ---> Facility Added: " + site.getName());
			saveSite(site);
			updateSyncStatus();
		}
	}

	/**
	 * Used by RoboSpice to handle the response for adding a Facility.
	 * 
	 * @author Jonathan Wohl
	 * 
	 */
	public final class UpdateFacilityRequestListener implements RequestListener<Facility> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Log.e(TAG, spiceException.toString());
			// Toast.makeText(SelectOfflineAreaActivity.this,
			// "Failed to add new facility.", Toast.LENGTH_SHORT).show();
		}

		/**
		 * On Success, we finish the activity and start the Detail activity.
		 */
		@Override
		public void onRequestSuccess(final Facility site) {
			Log.i(TAG, "                 ---> Facility Updated: " + site.getName());
			saveSite(site);
			updateSyncStatus();
		}
	}
}
