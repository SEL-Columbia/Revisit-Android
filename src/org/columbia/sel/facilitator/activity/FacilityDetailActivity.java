package org.columbia.sel.facilitator.activity;

import javax.inject.Inject;

import org.columbia.sel.facilitator.R;
import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityRepository;

import butterknife.ButterKnife;
import butterknife.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class FacilityDetailActivity extends BaseActivity {
	
	// Use ButterKnife to inject views
	@InjectView (R.id.detail_facility_name) TextView mFacilityNameView;
	@InjectView (R.id.detail_facility_sector) TextView mFacilitySectorView;
	@InjectView (R.id.detail_facility_type) TextView mFacilityTypeView;

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
		this.mFacilityNameView.setText(facility.name);
		this.mFacilitySectorView.setText((String)facility.properties.get("type"));
		this.mFacilityTypeView.setText((String)facility.properties.get("type"));
	}
}
