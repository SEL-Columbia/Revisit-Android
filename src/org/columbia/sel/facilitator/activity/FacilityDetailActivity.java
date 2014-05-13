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
	
	Facility facility;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facility_detail);

		// Injection for views and onclick handlers
		ButterKnife.inject(this);
		
		Intent i = this.getIntent();
		facility = i.getParcelableExtra("facility");
		this.displayFacility(facility);
	}

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
		
		this.mFacilityNameView.setText(facility.name);
		
		String sector = facility.properties.get("sector");
		
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
		this.mFacilityTypeView.setText(facility.properties.get("type"));
		this.mFacilitySectorView.setText(facility.properties.get("sector").toUpperCase());
		this.mFacilityLocationView.setText("Location: " + facility.coordinates.get(1) + ", " + facility.coordinates.get(0));
		this.mFacilityCheckinsView.setText("Checkins: " + facility.properties.get("checkins"));
	}
	
	@OnClick(R.id.finish_button)
	public void submit() {
		Intent i = new Intent();
		i.putExtra("facilityName", facility.name);
		i.putExtra("facilityType", facility.properties.get("type"));
		i.putExtra("facilityLocation", facility.coordinates.get(1) + ", " + facility.coordinates.get(0));
		this.setResult(RESULT_OK, i);
		this.finish();
	}
}
