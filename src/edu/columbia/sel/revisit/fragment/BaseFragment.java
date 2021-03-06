package edu.columbia.sel.revisit.fragment;

import javax.inject.Inject;

import com.squareup.otto.Bus;

import edu.columbia.sel.revisit.RevisitApplication;
import android.app.Fragment;
import android.os.Bundle;

/**
 * A base Fragment for use within the Facilitators app.
 * 
 * Fragment-common code should go here (Injections, for instance).
 * 
 * @author Jonathan Wohl
 *
 */
public class BaseFragment extends Fragment {
	// TAG for logging
	protected String TAG;
	
	// All fragments should have access to the Event Bus
	@Inject
	Bus bus;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the tag to the class name of the instance.
		TAG = this.getClass().getCanonicalName();

		((RevisitApplication) this.getActivity().getApplication()).inject(this);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		bus.register(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		bus.unregister(this);
	}
}
