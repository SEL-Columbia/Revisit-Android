package edu.columbia.sel.facilitator.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileSystemSiteRepository implements ISiteRepository {

	private final String TAG = this.getClass().getCanonicalName();

	private String mFilePath = "locations.json";
	private Context mContext;

	public FileSystemSiteRepository(Context context) {
		this.mContext = context;
	}

	public FileSystemSiteRepository(Context context, String filePath) {
		this.mContext = context;
		this.mFilePath = filePath;
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String mFilePath) {
		this.mFilePath = mFilePath;
	}

	//
	// Repository Interface
	//

	@Override
	public void saveSites(FacilityList sites) {
		Log.i(TAG, "Saving Sites...");
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(sites);
			FileOutputStream fos = this.mContext.openFileOutput(this.mFilePath, Context.MODE_PRIVATE);
			fos.write(json.getBytes());
			fos.close();
			Log.i(TAG, "Sites Saved.");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public FacilityList getSites() {
		Log.i(TAG, "Getting Sites...");
		ObjectMapper mapper = new ObjectMapper();
		try {
			FileInputStream fis = mContext.openFileInput(this.mFilePath);
			FacilityList sites = mapper.readValue(fis, FacilityList.class);
			fis.close();
			Log.i(TAG, "Sites Gotten.");
			return sites;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// If we're here, the sites couldn't be loaded from the file... return
		// an empty list.
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
				if (f.get_id() == _id) {
					return f;
				}
			}
		}
		return null;
	}

	@Override
	public void saveSite(Facility site) {
		FacilityList fullList = this.getSites();

		// since we're saving this site, let's assume for the moment that it
		// will need to be synced to the server too
		site.setRequestSync(true);

		// If the site doesn't have a server-supplied id, add it to the list and
		// save the list to disk
		if (site.get_id() == null) {
			fullList.add(site);
			this.saveSites(fullList);
		}
		// otherwise the site already on the server, so replace it locally with
		// the new site then save the list to disk
		else {
			// if the site already exists in the local list, remove it
			for (Facility f : fullList) {
				if (f.get_id() == site.get_id()) {
					fullList.remove(f);
					break;
				}
			}
			fullList.add(site);
		}
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
}
