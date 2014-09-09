package edu.columbia.sel.revisit.di;

import android.content.Context;
import android.location.LocationManager;
import dagger.Module;
import dagger.Provides;
import edu.columbia.sel.revisit.RevisitApplication;
import edu.columbia.sel.revisit.activity.AddSiteActivity;
import edu.columbia.sel.revisit.activity.BaseActivity;
import edu.columbia.sel.revisit.activity.SiteDetailActivity;
import edu.columbia.sel.revisit.activity.SiteMapListActivity;
import edu.columbia.sel.revisit.activity.SelectOfflineAreaActivity;
import edu.columbia.sel.revisit.activity.SplashActivity;
import edu.columbia.sel.revisit.annotation.ForApplication;
import edu.columbia.sel.revisit.annotation.ForLogging;
import edu.columbia.sel.revisit.api.SiteRetrofitSpiceService;
import edu.columbia.sel.revisit.fragment.AddSiteMapFragment;
import edu.columbia.sel.revisit.fragment.BaseFragment;
import edu.columbia.sel.revisit.fragment.BaseMapFragment;
import edu.columbia.sel.revisit.fragment.SiteMapFragment;
import edu.columbia.sel.revisit.model.JsonFileSiteRepository;
import edu.columbia.sel.revisit.service.LocationService;

import javax.inject.Singleton;

import com.octo.android.robospice.SpiceManager;
import com.squareup.otto.Bus;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module(
	library = true,
	complete = false,
	injects = {
		RevisitApplication.class,
		
		// Activities	
		BaseActivity.class,
		SplashActivity.class,
		SelectOfflineAreaActivity.class,
		SiteDetailActivity.class,
		SiteMapListActivity.class,
		AddSiteActivity.class,
		
		// Fragments
		BaseFragment.class,
		BaseMapFragment.class,
		SiteMapFragment.class,
		AddSiteMapFragment.class,
		
		// Other Components
//		SiteRequestTask.class,
		LocationService.class,
		JsonFileSiteRepository.class
	}
)
public class DIModule {
	private final RevisitApplication application;
	
	private final String TAG = "RevisitApplication";

	public DIModule(RevisitApplication application) {
		this.application = application;
	}

	/**
	 * Allow the application context to be injected but require that it be
	 * annotated with {@link ForApplication @Annotation} to explicitly
	 * differentiate it from an activity context.
	 * @return RevisitApplication
	 */
	@Provides
	@Singleton
	@ForApplication
	RevisitApplication provideApplicationContext() {
		return application;
	}
	
	/**
	 * Allow the application logging TAG to be injected,
	 * but require that it be annotated with {@link ForLogging @Annotation}.
	 * @return String
	 */
	@Provides
	@ForLogging
	String provideApplicationTag() {
		return TAG;
	}

	@Provides
	@Singleton
	Bus provideBus() {
		return new Bus();
	}

	@Provides
	@Singleton
	LocationManager provideLocationManager() {
		return (LocationManager) application.getSystemService(LOCATION_SERVICE);
	}
	
	@Provides
	@Singleton
	SpiceManager provideSpiceManager() {
		return new SpiceManager(SiteRetrofitSpiceService.class);
	}
	
//	@Provides
//	@Singleton
//	JsonFileSiteRepository provideSiteRepository(Bus bus) {
//		JsonFileSiteRepository
//	}
}