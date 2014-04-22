package org.columbia.sel.facilitator;

import java.util.Map;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import org.columbia.sel.facilitator.model.Facility;

import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class FacilityListActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facility_list);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Toast.makeText(getApplication(), "Fetching Facilities...", Toast.LENGTH_SHORT).show();
		new HttpRequestTask().execute();
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
			new HttpRequestTask().execute();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_facility_list,
					container, false);
			return rootView;
		}
	}
	
	/**
	 * An internal class for handling of the asyncronous REST service interaction.
	 * @author jmw
	 */
	private class HttpRequestTask extends AsyncTask<Void, Void, Map> {
        @Override
        protected Map doInBackground(Void... params) {
            try {
//                final String url = "http://rest-service.guides.spring.io/greeting";
                final String url = "http://10.88.0.108:3000/api/v1/Facilities/53557c1fbb69d3f28ac05979";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
//                Facility facility = restTemplate.getForObject(url, Facility.class);
                Map<String,Object> facility = restTemplate.getForObject(url, Map.class);
                return facility;
            } catch (Exception e) {
                Log.e("FacilityListActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Map facility) {
            TextView facilityIdText = (TextView) findViewById(R.id.id_value);
            TextView facilityNameText = (TextView) findViewById(R.id.name_value);
//            facilityIdText.setText(facility.getId());
            facilityIdText.setText((String) facility.get("_id"));
//            facilityNameText.setText(facility.getName());
            facilityNameText.setText((String) facility.get("name"));
            Log.e("FacilityListActivity", facility.toString());
        }

    }

}
