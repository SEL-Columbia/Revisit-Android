package edu.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import edu.columbia.sel.facilitator.R;
import retrofit.converter.JacksonConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import edu.columbia.sel.facilitator.activity.AddFacilityActivity.AddFacilityRequestListener;
import edu.columbia.sel.facilitator.api.AddFacilityRetrofitSpiceRequest;
import edu.columbia.sel.facilitator.api.FacilitiesWithinRetrofitSpiceRequest;
import edu.columbia.sel.facilitator.api.UpdateFacilityRetrofitSpiceRequest;
import edu.columbia.sel.facilitator.model.Facility;
import edu.columbia.sel.facilitator.model.FacilityRepository;
import edu.columbia.sel.facilitator.model.FileSystemSiteRepository;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The FacilityDetailActivity shows all details of a single Facility for review
 * before selecting the Facility.
 * 
 * @author Jonathan Wohl
 *
 */
public class FacilityDetailActivity extends BaseActivity {
	
	// Use ButterKnife to inject views
	@InjectView (R.id.detail_image_progress) ProgressBar mFacilityImageProgressView;
	@InjectView (R.id.detail_image) ImageView mFacilityImageView;
	@InjectView (R.id.detail_facility_name) TextView mFacilityNameView;
	@InjectView (R.id.detail_facility_sector) TextView mFacilitySectorView;
	@InjectView (R.id.facility_sector_icon) ImageView mFacilitySectorIconView;
	@InjectView (R.id.detail_facility_type) TextView mFacilityTypeView;
	@InjectView (R.id.detail_facility_location) TextView mFacilityLocationView;
	@InjectView (R.id.detail_facility_checkins) TextView mFacilityCheckinsView;

	// Inject facility repo
	@Inject FacilityRepository fr;
	
	// The GET request that retrieves known facilities within the map bounds
	private UpdateFacilityRetrofitSpiceRequest mUpdateFacilityRequest;
	
	// The current facility being viewed
	Facility mFacility;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facility_detail);
		
		// add 'back' button to go to parent (FacilityMapListActivity)
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Injection for views and onclick handlers
		ButterKnife.inject(this);
		
		// Facility is received upon creation in the Intent
		Intent i = this.getIntent();
		mFacility = i.getParcelableExtra("facility");
		this.displayFacility(mFacility);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.maplist_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            NavUtils.navigateUpFromSameTask(this);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * Populate the views with the Facility info.
	 * @param facility
	 */
	private void displayFacility(Facility facility) {
		// Picasso is a nifty library for downloading and caching images.
		Picasso.with(this)
			.load("http://sel.columbia.edu/wp-content/uploads/2013/05/sharedSolar.jpg")
			.into(this.mFacilityImageView, new Callback() {

				@Override
				public void onError() {
					Log.e(TAG, "error loading image");
				}

				@Override
				public void onSuccess() {
					mFacilityImageProgressView.setVisibility(View.INVISIBLE);
					mFacilityImageView.setVisibility(View.VISIBLE);
				}
				
			});
		
		this.mFacilityNameView.setText(facility.getName());
		
		String sector = facility.getProperties().getSector();
		
		Log.i(TAG, "sector -----------> " + sector + " <-----------");
		
		if (sector.equals("health")) {
			Log.i(TAG, " -----------> health! <-----------");
			this.mFacilitySectorIconView.setImageResource(R.drawable.hospital);				
		} else if (sector.equals("power")) {
			Log.i(TAG, " -----------> power! <-----------");
			this.mFacilitySectorIconView.setImageResource(R.drawable.power);
		} else if (sector.equals("education")) {
			Log.i(TAG, " -----------> education! <-----------");
			this.mFacilitySectorIconView.setImageResource(R.drawable.education);
		}
		this.mFacilityImageView.invalidate();
		this.mFacilityTypeView.setText(facility.getProperties().getType());
		this.mFacilitySectorView.setText(sector.toUpperCase());
		this.mFacilityLocationView.setText("Location: " + facility.getCoordinates().get(1) + ", " + facility.getCoordinates().get(0));
		this.mFacilityCheckinsView.setText("Checkins: " + facility.getProperties().getCheckins());
	}
	
	@OnClick(R.id.finish_button)
	public void submit() {
		
		int checkins = mFacility.getProperties().getCheckins();
		checkins += 1;
		mFacility.getProperties().setCheckins(checkins);
		
		FileSystemSiteRepository sr = new FileSystemSiteRepository(this);
		sr.saveSite(mFacility);
		
		Intent i = new Intent();
		i.putExtra("facility_name", mFacility.getName());
		i.putExtra("facility_sector", mFacility.getProperties().getSector());
		i.putExtra("facility_type", mFacility.getProperties().getType());
		i.putExtra("facility_latitude", mFacility.getCoordinates().get(1));
		i.putExtra("facility_longitude", mFacility.getCoordinates().get(0));
		i.putExtra("facility_location", mFacility.getCoordinates().get(1) + ", " + mFacility.getCoordinates().get(0));
		setResult(RESULT_OK, i);
		finish();
	}
	
//	
//	/**
//	 * Used by RoboSpice to handle the response for adding a Facility.
//	 * @author Jonathan Wohl
//	 *
//	 */
//	public final class UpdateFacilityRequestListener implements
//			RequestListener<Facility> {
//
//		@Override
//		public void onRequestFailure(SpiceException spiceException) {
//			Log.e(TAG, spiceException.toString());
//			Toast.makeText(FacilityDetailActivity.this, "Failed to add new facility.",
//					Toast.LENGTH_SHORT).show();
//		}
//
//		/**
//		 * On Success, we finish the activity and start the Detail activity.
//		 */
//		@Override
//		public void onRequestSuccess(final Facility facility) {
//			Intent i = new Intent();
//			i.putExtra("facility_name", facility.getName());
//			i.putExtra("facility_sector", facility.getProperties().getSector());
//			i.putExtra("facility_type", facility.getProperties().getType());
//			i.putExtra("facility_latitude", facility.getCoordinates().get(1));
//			i.putExtra("facility_longitude", facility.getCoordinates().get(0));
//			i.putExtra("facility_location", facility.getCoordinates().get(1) + ", " + facility.getCoordinates().get(0));
//			setResult(RESULT_OK, i);
//			finish();
//		}
//	}
}
