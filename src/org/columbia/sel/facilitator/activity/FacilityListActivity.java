package org.columbia.sel.facilitator.activity;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.R.drawable;
import org.columbia.sel.facilitator.R.id;
import org.columbia.sel.facilitator.R.layout;
import org.columbia.sel.facilitator.R.menu;
import org.columbia.sel.facilitator.model.Facility;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.app.ListActivity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

/**
 * The FacilityListActivity displays a list of facilities pulled from the API.
 * @author jmw
 *
 */
public class FacilityListActivity extends ListActivity implements
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener {
	
	@Inject LocationManager locationManager;
	
	// TAG for logging
	private final String TAG = this.getClass().getCanonicalName();

	// A container class for a collection of Facility POJOs
	static class FacilityList extends ArrayList<Facility> {}
	
	public FacilityArrayAdapter mAdapter;
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new FacilityArrayAdapter();
		
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		
		setListAdapter(mAdapter);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.facility_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
//			new HttpRequestTask().execute();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * An internal class for handling of the asyncronous REST service
	 * interaction.
	 * 
	 * @author jmw
	 */
	private class HttpRequestTask extends AsyncTask<Void, Void, List<Facility>> {
		private Double lat, lng, rad;
		
		public HttpRequestTask(Double lat, Double lng, Double rad) {
			super();
			
			this.lat = lat;
			this.lng = lng;
			this.rad = rad;	
		}
		
		@Override
		protected List<Facility> doInBackground(Void... params) {
			try {
				// final String url =
				// "http://rest-service.guides.spring.io/greeting";
				final String url = "http://fac.wohllabs.com/api/test/facilities/geowithin?lat=" 
						+ this.lat + "&lng=" + this.lng + "&rad=" + this.rad;
				
				Log.i(TAG, url);
				
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(
						new MappingJackson2HttpMessageConverter());
				
				List<Facility> facilities = restTemplate.getForObject(url,
						FacilityList.class);

				return facilities;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(List<Facility> facilities) {
			Log.i(TAG, ""+facilities.size());
//			Log.i(TAG, ""+facilities[0].getName());
			mAdapter.clear();
			mAdapter.addAll(facilities);
			mAdapter.notifyDataSetChanged();
		}

	}

	/**
	 * Example of custom adaptor - will need to look at how best to do this.
	 * @author jmw
	 *
	 */
	private class FacilityArrayAdapter extends ArrayAdapter<Facility> {
		
		public FacilityArrayAdapter() {
			super(getApplicationContext(), 0);
		}
		
		@Override
		public View getView(int pos, View baseView, ViewGroup parent) {
			if (baseView == null) {
				baseView = getLayoutInflater().inflate(R.layout.facility_list_item, null);
			}
			
			Facility f = getItem(pos);
			
			String type = (String) f.properties.get("type");
			
			ImageView icon = (ImageView) baseView.findViewById(R.id.facility_type_icon);
			if (type.equals("health")) {
				icon.setImageResource(R.drawable.hospital);				
			} else if (type.equals("power")) {
				icon.setImageResource(R.drawable.power);
			} else if (type.equals("education")) {
				icon.setImageResource(R.drawable.education);
			}
			
			TextView title = (TextView) baseView.findViewById(R.id.facility_list_item_title);
			title.setText(f.name);
			
			TextView desc = (TextView) baseView.findViewById(R.id.facility_list_item_description);
			desc.setText("checkins: " + f.properties.get("checkins"));
			
//			TextView created = (TextView) baseView.findViewById(R.id.facility_list_created);
//			created.setText("Facility added: " + f.createdAt.toString());
			
			Log.i(TAG, f.properties.get("type")+"");
			Log.i(TAG, f.name);
			
			return baseView;
			
		}
	}
	
	
	@Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
        Facility f = (Facility) getListView().getItemAtPosition(position);
        Log.i(TAG, "clicked " + f.name);
    }
	
	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "GPS Connected", Toast.LENGTH_SHORT).show();;
		mCurrentLocation = mLocationClient.getLastLocation();
		new HttpRequestTask(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1.0).execute();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

}
