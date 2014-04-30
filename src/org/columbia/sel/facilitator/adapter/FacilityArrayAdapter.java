package org.columbia.sel.facilitator.adapter;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.model.FacilityList;
import org.columbia.sel.facilitator.model.Facility;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Example of custom adaptor - will need to look at how best to do this.
 * @author jmw
 *
 */
public class FacilityArrayAdapter extends ArrayAdapter<Facility> {
	
	private FacilityList facilities;
	
	public FacilityArrayAdapter(Context context, int viewId, FacilityList facilities) {
		super(context, viewId, facilities);
		this.facilities = facilities;
	}

	public FacilityArrayAdapter(Context context, int viewId) {
		super(context, viewId);
	}
	
	@Override
	public View getView(int pos, View itemView, ViewGroup parent) {
		if (itemView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = inflater.inflate(R.layout.facility_list_item, null);
		}
		
		Facility f = getItem(pos);
		
		String type = (String) f.properties.get("type");
		
		ImageView icon = (ImageView) itemView.findViewById(R.id.facility_type_icon);
		if (type.equals("health")) {
			icon.setImageResource(R.drawable.hospital);				
		} else if (type.equals("power")) {
			icon.setImageResource(R.drawable.power);
		} else if (type.equals("education")) {
			icon.setImageResource(R.drawable.education);
		}
		
		TextView title = (TextView) itemView.findViewById(R.id.facility_list_item_title);
		title.setText(f.name);
		
		TextView desc = (TextView) itemView.findViewById(R.id.facility_list_item_description);
		desc.setText("checkins: " + f.properties.get("checkins"));
		
//		TextView created = (TextView) itemView.findViewById(R.id.facility_list_created);
//		created.setText("Facility added: " + f.createdAt.toString());
		
//		Log.i(TAG, f.properties.get("type")+"");
//		Log.i(TAG, f.name);
		
		return itemView;
		
	}
}
