package org.columbia.sel.facilitator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import org.columbia.sel.facilitator.model.Facility;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class FacilityListActivity extends ListActivity {

	public ArrayAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
		setListAdapter(mAdapter);
	}

	@Override
	public void onStart() {
		super.onStart();
		Toast.makeText(getApplication(), "Fetching Facilities...",
				Toast.LENGTH_SHORT).show();
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
	 * An internal class for handling of the asyncronous REST service
	 * interaction.
	 * 
	 * @author jmw
	 */
	private class HttpRequestTask extends AsyncTask<Void, Void, Facility[]> {
		@Override
		protected Facility[] doInBackground(Void... params) {
			try {
				// final String url =
				// "http://rest-service.guides.spring.io/greeting";
				final String url = "http://10.88.0.108:3000/api/v1/Facilities/";
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(
						new MappingJackson2HttpMessageConverter());
				
				Facility[] facilities = restTemplate.getForObject(url,
						Facility[].class);

				return facilities;
			} catch (Exception e) {
				Log.e("FacilityListActivity", e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Facility[] facilities) {
			Log.i("FacilityListActivity", ""+facilities.length);
			Log.i("FacilityListActivity", ""+facilities[0].getName());
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
	private class FacilityArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public FacilityArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

}
