package edu.columbia.sel.revisit.adapter;

import java.util.ArrayList;

import edu.columbia.sel.revisit.R;
import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * FacilityArrayAdapter is used to populate the ListView of Sites.
 * 
 * @author Jonathan Wohl
 * 
 */
public class SiteArrayAdapter extends ArrayAdapter<Site> implements Filterable {
	private final String TAG = this.getClass().getCanonicalName();
	private final Object mLock = new Object();
	private SiteList mSites;
	private SiteList mSitesFiltered;
	private SitesFilter mFilter;

	/**
	 * Constructor that initializes this object with a SiteList.
	 * 
	 * Note: Because fetching the sites happens asynchronously, we don't
	 * currently use this constructor... instead we use the addSites() method to
	 * add the Sites once they have been loaded.
	 * 
	 * @param context
	 * @param viewId
	 * @param sites
	 */
	public SiteArrayAdapter(Context context, int viewId, SiteList sites) {
		super(context, viewId, sites);
		this.mSitesFiltered = sites;
		this.mSites = sites;
	}

	public SiteArrayAdapter(Context context, int viewId) {
		super(context, viewId);
	}

	/**
	 * Overridden in order to implement filtering
	 */
	@Override
	public int getCount() {
		if (mSitesFiltered != null) {
			return mSitesFiltered.size();
		}
		return 0;
	}

	/**
	 * Overridden in order to implement filtering
	 */
	@Override
	public Site getItem(int pos) {
		if (mSitesFiltered != null) {
			return mSitesFiltered.get(pos);
		}
		return null;
	}

	/**
	 * Overridden in order to implement filtering
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Used as a replacement for ArrayAdapter's 'addAll' so that we can
	 * implement filtering.
	 * 
	 * @param sites
	 */
	public void addSites(SiteList sites) {
		this.mSitesFiltered = sites;
		this.mSites = sites;
		this.addAll(sites);
	}

	/**
	 * Here is where we populate each and return each row in the list's view.
	 * 
	 * @return View
	 */
	@Override
	public View getView(int pos, View itemView, ViewGroup parent) {
		if (itemView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = inflater.inflate(R.layout.site_list_item, null);
		}

		// Pulling from the mSitesFiltered
		Site f = getItem(pos);

		String sector = (String) f.getProperties().getSector();

		int red = this.getContext().getResources().getColor(R.color.sel_red);
		int blue = this.getContext().getResources().getColor(R.color.sel_blue);
		int green = this.getContext().getResources().getColor(R.color.sel_green);
		int orange = this.getContext().getResources().getColor(R.color.sel_orange);

		TextView index = (TextView) itemView.findViewById(R.id.site_list_item_index);
		index.setText(String.valueOf(pos + 1));

		TextView title = (TextView) itemView.findViewById(R.id.site_list_item_title);
		title.setText(f.getName());

		TextView desc = (TextView) itemView.findViewById(R.id.site_list_item_description);
		desc.setText(f.getProperties().getVisits() + " Visits");

		ImageView icon = (ImageView) itemView.findViewById(R.id.site_type_icon);
		if (sector.equals("health")) {
			icon.setImageResource(R.drawable.ic_health);
			index.setTextColor(red);
			desc.setTextColor(red);
		} else if (sector.equals("energy")) {
			icon.setImageResource(R.drawable.ic_energy);
			// text already green
			index.setTextColor(green);
			desc.setTextColor(green);
		} else if (sector.equals("education")) {
			icon.setImageResource(R.drawable.ic_education);
			index.setTextColor(orange);
			desc.setTextColor(orange);
		} else if (sector.equals("water")) {
			icon.setImageResource(R.drawable.ic_water);
			index.setTextColor(blue);
			desc.setTextColor(blue);
		}

		return itemView;

	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new SitesFilter();
		}
		return mFilter;
	}

	public class SitesFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// Initiate our results object
			FilterResults results = new FilterResults();
			final SiteList filteredList = new SiteList();

			if (constraint == null || constraint.length() == 0) {
				Log.i(TAG, "filtering, no constraint");
				synchronized (mLock) {
					results.values = mSites;
					results.count = mSites.size();
				}
			} else {
				Log.i(TAG, "filtering: " + constraint);
				// Compare lower case strings
				String constraintLower = constraint.toString().toLowerCase();
				// Local to here so we're not changing actual array
				final int count = mSites.size();
				for (int i = 0; i < count; i++) {
					final Site site = mSites.get(i);
					final String itemName = site.getName().toString().toLowerCase();
					// First match against the whole, non-split value
					if (itemName.startsWith(constraintLower)) {
						filteredList.add(site);
					} else {
						// Here we'll test against the tokenized name
						final String[] words = itemName.split(" ");
						final int wordCount = words.length;
						for (int k = 0; k < wordCount; k++) {
							if (words[k].startsWith(constraintLower)) {
								filteredList.add(site);
								break;
							}
						}
					}
				}
				// Set and return
				results.values = filteredList;
				results.count = filteredList.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mSitesFiltered = (SiteList) results.values;

			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

	}
}
