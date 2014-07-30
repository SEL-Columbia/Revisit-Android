package edu.columbia.sel.revisit.activity;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.OnClick;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		Log.i(TAG, "SplashActivity onCreate");

		ButterKnife.inject(this);
		
		bus.register(this);
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
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}
	
	@OnClick(R.id.map_button)
	public void openOfflineMap(View view) {
		Log.i(TAG, "openOfflineMap()");
		Intent i = new Intent(this, SiteMapListActivity.class);
		startActivity(i);
	}
	
	@OnClick(R.id.sync_button)
	public void onSyncSites(View view) {
		SiteList sitesForSync = this.mSiteRepository.getSitesForSync();
		
		if (sitesForSync.size() == 0) {
			Toast toast = Toast.makeText(this, "All Sites are up to date.", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		
		mSyncProgressBar = new ProgressDialog(this);
		mSyncProgressBar.setMax(sitesForSync.size());
		mSyncProgressBar.setCancelable(true);
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
		
//		Toast.makeText(this, "Total Sites Marked for Sync: " + sitesForSync.size(), Toast.LENGTH_SHORT).show();
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
	}
}
