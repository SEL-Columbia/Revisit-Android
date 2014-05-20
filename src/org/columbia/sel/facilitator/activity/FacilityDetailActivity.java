package org.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityRepository;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
	
	// The current facility being viewed
	Facility facility;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facility_detail);

		// Injection for views and onclick handlers
		ButterKnife.inject(this);
		
		// Facility is received upon creation in the Intent
		Intent i = this.getIntent();
		facility = i.getParcelableExtra("facility");
		this.displayFacility(facility);
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
		Intent i = new Intent();
		i.putExtra("facility_name", facility.getName());
		i.putExtra("facility_sector", facility.getProperties().getSector());
		i.putExtra("facility_type", facility.getProperties().getType());
		i.putExtra("facility_latitude", facility.getCoordinates().get(1));
		i.putExtra("facility_longitude", facility.getCoordinates().get(0));
		i.putExtra("facility_location", facility.getCoordinates().get(1) + ", " + facility.getCoordinates().get(0));
		this.setResult(RESULT_OK, i);
		this.finish();
	}
}
