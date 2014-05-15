package org.columbia.sel.facilitator.fragment;

import org.columbia.sel.facilitator.R;
import org.osmdroid.views.MapController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.ButterKnife;

/**
 * NOTE: This class is currently unused. An android ListFragment is being used directly 
 * without subclassing in FacilityMapListActivity.
 * 
 * If we decide to break the List view into it's own Activity (like the FacilityListActivity),
 * we should use this Fragment subclass.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilityListFragment extends BaseFragment {
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
	
		ListView listView = new ListView(this.getActivity());
		return listView;
    }
}