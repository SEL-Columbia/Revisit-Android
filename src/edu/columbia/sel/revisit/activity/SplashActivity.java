package edu.columbia.sel.revisit.activity;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.OnClick;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import edu.columbia.sel.revisit.R;
import edu.columbia.sel.revisit.event.DeviceOfflineEvent;
import edu.columbia.sel.revisit.event.SiteSyncErrorEvent;
import edu.columbia.sel.revisit.event.SiteSyncSuccessEvent;
import edu.columbia.sel.revisit.event.SitesSyncCompleteEvent;
import edu.columbia.sel.revisit.model.SiteList;

public class SplashActivity extends BaseActivity {
//	@InjectView (R.id.goto_offline_activity) Button mGotoOfflineButton;
	
	private ProgressDialog mSyncProgressBar;
	
	private boolean mSitesNeedSync = false;
	private int mNumSitesForSync = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		Log.i(TAG, "SplashActivity onCreate");

		ButterKnife.inject(this);
		
		bus.register(this);
	}
	
	/**
	 * When the activity resumes, check if there are sites to be uploaded, and if there is a network connection.
	 * If both, alert the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		SiteList sitesForSync = this.mSiteRepository.getSitesForSync();
		mNumSitesForSync = sitesForSync.size();
		mSitesNeedSync = mNumSitesForSync != 0;
		
		if (isNetworkAvailable()) {
			// Sites are ready for syncing... maybe alert the user now?
			this.invalidateOptionsMenu();
		}
	}
	
	@OnClick(R.id.select_offline)
	public void openSelectOffline(View v) {
		Log.i(TAG, "gotoSelectOffline()");
		Intent i = new Intent(this, SelectOfflineAreaActivity.class);
		startActivity(i);
	}
	
	@OnClick(R.id.settings)
	public void openSettings(View v) {
		Log.i(TAG, "openSettings()");
		openSettings();
	}
	
	@OnClick(R.id.map_button)
	public void openOfflineMap(View view) {
		Log.i(TAG, "openOfflineMap()");
		Intent i = new Intent(this, SiteMapListActivity.class);
		startActivity(i);
	}
	
	@OnClick(R.id.tour_button)
	public void openTour(View view) {
		Log.i(TAG, "openTour()");
		Toast toast = Toast.makeText(this, "Tour coming soon...", Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.splash_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);

	    // If there are no sites that need syncing, hide the action bar icon		
 		if (!mSitesNeedSync || !this.isNetworkAvailable()) {
 			menu.findItem(R.id.action_upload_sites).setVisible(false);
 		}
 		
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_settings:
			openSettings();
			return true;
		case R.id.action_upload_sites:
			syncSites();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Check whether the network is available.
	 * @return
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	/**
	 * Open the settings activity
	 */
	public void openSettings() {
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}
	
	/**
	 * Sync local site data with server
	 */
	public void syncSites() {
		if (!mSitesNeedSync) {
			Toast toast = Toast.makeText(this, "All Sites are up to date.", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		
		mSyncProgressBar = new ProgressDialog(this);
		mSyncProgressBar.setMax(mNumSitesForSync);
		mSyncProgressBar.setCancelable(false);
		mSyncProgressBar.setCanceledOnTouchOutside(false);
		mSyncProgressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel Upload",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SplashActivity.this.mSiteRepository.cancelSync();
					}

				});

		mSyncProgressBar.setMessage("Uploading Sites...");
		mSyncProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mSyncProgressBar.setProgress(0);
		mSyncProgressBar.show();
		
		this.mSiteRepository.syncSites();
	}
	
	/**
	 * Handle SiteSyncSuccessEvent, update progress
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleSiteSyncSuccessEvent(SiteSyncSuccessEvent event) {
		Log.i(TAG, "handleSiteSyncSuccessEvent");
		mSyncProgressBar.setProgress(mSyncProgressBar.getProgress() + 1);
	}
	
	/**
	 * Handle SiteSyncErrorEvent: close progress dialog, alert user to problem
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleSiteSyncErrorEvent(SiteSyncErrorEvent event) {
		Log.i(TAG, "handleSiteSyncErrorEvent");
		mSyncProgressBar.dismiss();
		Toast toast = Toast.makeText(this, "There has been an problem uploading the Site data.", Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	/**
	 * Handle SitesSyncCompleteEvent: all site data uplaoded, close the dialog
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleSitesSyncCompleteEvent(SitesSyncCompleteEvent event) {
		Log.i(TAG, "handleSitesSyncCompleteEvent");
		mSyncProgressBar.dismiss();
		Toast toast = Toast.makeText(this, "All Sites are up to date.", Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		
		// reset local sync state vars and invalidate menu
		this.mSitesNeedSync = false;
		this.mNumSitesForSync = 0;
		this.invalidateOptionsMenu();
	}
}
