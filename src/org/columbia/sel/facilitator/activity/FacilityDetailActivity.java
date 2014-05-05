package org.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityRepository;

import butterknife.ButterKnife;
import butterknife.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class FacilityDetailActivity extends BaseActivity {
	
	// Use ButterKnife to inject views
	@InjectView (R.id.detail_image) ImageView mFacilityImageView;
	@InjectView (R.id.detail_facility_name) TextView mFacilityNameView;
	@InjectView (R.id.detail_facility_sector) TextView mFacilitySectorView;
	@InjectView (R.id.detail_facility_type) TextView mFacilityTypeView;
	@InjectView (R.id.detail_facility_location) TextView mFacilityLocationView;
	@InjectView (R.id.detail_facility_checkins) TextView mFacilityCheckinsView;

	// Inject facility repo
	@Inject FacilityRepository fr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facility_detail);

		// Injection for views and onclick handlers
		ButterKnife.inject(this);
		
		Intent i = this.getIntent();
		Facility facility = i.getParcelableExtra("facility");
		this.displayFacility(facility);
	}

	private void displayFacility(Facility facility) {
		this.mFacilityImageView.setImageResource(R.drawable.facility);				
		this.mFacilityNameView.setText(facility.name);
		this.mFacilitySectorView.setText(facility.properties.get("type"));
		this.mFacilityTypeView.setText(facility.properties.get("type"));
		this.mFacilityLocationView.setText(facility.coordinates.get(0) + ", " + facility.coordinates.get(1));
		this.mFacilityCheckinsView.setText(facility.properties.get("checkins"));
	}
}
