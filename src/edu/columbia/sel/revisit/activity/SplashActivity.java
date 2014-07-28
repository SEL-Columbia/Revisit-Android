package edu.columbia.sel.revisit.activity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import edu.columbia.sel.revisit.R;

public class SplashActivity extends BaseActivity {
//	@InjectView (R.id.goto_offline_activity) Button mGotoOfflineButton;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		Log.i(TAG, "SplashActivity onCreate");

		ButterKnife.inject(this);

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
}
