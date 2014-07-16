package edu.columbia.sel.revisit.activity;

import javax.inject.Inject;

import edu.columbia.sel.revisit.R;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import edu.columbia.sel.revisit.api.UpdateSiteRetrofitSpiceRequest;
import edu.columbia.sel.revisit.model.Site;
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

/**
 * The SiteDetailActivity shows all details of a single Site for review
 * before selecting the Site.
 * 
 * @author Jonathan Wohl
 *
 */
public class SiteDetailActivity extends BaseActivity {
	
	// Use ButterKnife to inject views
	@InjectView (R.id.detail_image_progress) ProgressBar mSiteImageProgressView;
	@InjectView (R.id.detail_image) ImageView mSiteImageView;
	@InjectView (R.id.detail_site_name) TextView mSiteNameView;
	@InjectView (R.id.detail_site_sector) TextView mSiteSectorView;
	@InjectView (R.id.site_sector_icon) ImageView mSiteSectorIconView;
	@InjectView (R.id.detail_site_type) TextView mSiteTypeView;
	@InjectView (R.id.detail_site_location) TextView mSiteLocationView;
	@InjectView (R.id.detail_site_visits) TextView mSiteVisitsView;
	
	// The GET request that retrieves known sites within the map bounds
//	private UpdateSiteRetrofitSpiceRequest mUpdateSiteRequest;
	
	// The current facility being viewed
	Site mSite;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_site_detail);
		
		// add 'back' button to go to parent (FacilityMapListActivity)
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Injection for views and onclick handlers
		ButterKnife.inject(this);
		
		// Facility is received upon creation in the Intent
		Intent i = this.getIntent();
		mSite = i.getParcelableExtra("site");
		this.displaySite(mSite);
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
	 * Populate the views with the Site info.
	 * @param site
	 */
	private void displaySite(Site site) {
		// Picasso is a nifty library for downloading and caching images.
		Picasso.with(this)
			.load("http://sel.columbia.edu/wp-content/uploads/2013/05/sharedSolar.jpg")
			.into(this.mSiteImageView, new Callback() {

				@Override
				public void onError() {
					Log.e(TAG, "error loading image");
				}

				@Override
				public void onSuccess() {
					mSiteImageProgressView.setVisibility(View.INVISIBLE);
					mSiteImageView.setVisibility(View.VISIBLE);
				}
				
			});
		
		this.mSiteNameView.setText(site.getName());
		
		String sector = site.getProperties().getSector();
		
		Log.i(TAG, "sector -----------> " + sector + " <-----------");
		
		if (sector.equals("health")) {
			Log.i(TAG, " -----------> health! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.hospital);				
		} else if (sector.equals("power")) {
			Log.i(TAG, " -----------> power! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.power);
		} else if (sector.equals("education")) {
			Log.i(TAG, " -----------> education! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.education);
		}
		this.mSiteImageView.invalidate();
		this.mSiteTypeView.setText(site.getProperties().getType());
		this.mSiteSectorView.setText(sector.toUpperCase());
		this.mSiteLocationView.setText("Location: " + site.getCoordinates().get(1) + ", " + site.getCoordinates().get(0));
		this.mSiteVisitsView.setText("Visits: " + site.getProperties().getVisits());
	}
	
	@OnClick(R.id.finish_button)
	public void submit() {
		
		int visits = mSite.getProperties().getVisits();
		visits += 1;
		mSite.getProperties().setVisits(visits);
		
//		JsonFileSiteRepository sr = new JsonFileSiteRepository(this);
		mSiteRepository.updateSite(mSite);
		
		Intent i = new Intent();
		i.putExtra("site_name", mSite.getName());
		i.putExtra("site_sector", mSite.getProperties().getSector());
		i.putExtra("site_type", mSite.getProperties().getType());
		i.putExtra("site_latitude", mSite.getCoordinates().get(1));
		i.putExtra("site_longitude", mSite.getCoordinates().get(0));
		i.putExtra("site_location", mSite.getCoordinates().get(1) + ", " + mSite.getCoordinates().get(0));
		setResult(RESULT_OK, i);
		finish();
	}
}
