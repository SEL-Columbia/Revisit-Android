package edu.columbia.sel.revisit.adapter;

import edu.columbia.sel.revisit.R;
import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * FacilityArrayAdapter is used to populate the ListView of Sites.
 * 
 * @author Jonathan Wohl
 *
 */
public class SiteArrayAdapter extends ArrayAdapter<Site> {
	
	private SiteList mSites;
	
	// We need a constructor that accepts a SiteList
	public SiteArrayAdapter(Context context, int viewId, SiteList sites) {
		super(context, viewId, sites);
		this.mSites = sites;
	}
	
	public SiteArrayAdapter(Context context, int viewId) {
		super(context, viewId);
	}
	
	/**
	 * Here is where we populate each and return each row in the list's view.
	 * @return View  
	 */
	@Override
	public View getView(int pos, View itemView, ViewGroup parent) {
		if (itemView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = inflater.inflate(R.layout.site_list_item, null);
		}
		
		Site f = getItem(pos);
		
		String sector = (String) f.getProperties().getSector();
		
		ImageView icon = (ImageView) itemView.findViewById(R.id.site_type_icon);
		if (sector.equals("health")) {
			icon.setImageResource(R.drawable.hospital);				
		} else if (sector.equals("power")) {
			icon.setImageResource(R.drawable.power);
		} else if (sector.equals("education")) {
			icon.setImageResource(R.drawable.education);
		}
		
		TextView index = (TextView) itemView.findViewById(R.id.site_list_item_index);
		index.setText(String.valueOf(pos+1));
		
		TextView title = (TextView) itemView.findViewById(R.id.site_list_item_title);
		title.setText(f.getName());
		
		TextView desc = (TextView) itemView.findViewById(R.id.site_list_item_description);
		desc.setText("checkins: " + f.getProperties().getCheckins());
		
		return itemView;
		
	}
}
