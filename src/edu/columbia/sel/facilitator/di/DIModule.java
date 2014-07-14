package edu.columbia.sel.facilitator.di;

import android.content.Context;
import android.location.LocationManager;
import dagger.Module;
import dagger.Provides;
import edu.columbia.sel.facilitator.FacilitatorApplication;
import edu.columbia.sel.facilitator.activity.AddFacilityActivity;
import edu.columbia.sel.facilitator.activity.BaseActivity;
import edu.columbia.sel.facilitator.activity.FacilityDetailActivity;
import edu.columbia.sel.facilitator.activity.FacilityListActivity;
import edu.columbia.sel.facilitator.activity.FacilityMapListActivity;
import edu.columbia.sel.facilitator.activity.MainActivity;
import edu.columbia.sel.facilitator.activity.SelectOfflineAreaActivity;
import edu.columbia.sel.facilitator.annotation.ForApplication;
import edu.columbia.sel.facilitator.annotation.ForLogging;
import edu.columbia.sel.facilitator.api.FacilityRetrofitSpiceService;
import edu.columbia.sel.facilitator.fragment.AddFacilityMapFragment;
import edu.columbia.sel.facilitator.fragment.BaseFragment;
import edu.columbia.sel.facilitator.fragment.BaseMapFragment;
import edu.columbia.sel.facilitator.fragment.FacilityMapFragment;
import edu.columbia.sel.facilitator.model.FacilityRepository;
import edu.columbia.sel.facilitator.model.JsonFileSiteRepository;
import edu.columbia.sel.facilitator.osm.AddFacilityItemizedIconOverlay;
import edu.columbia.sel.facilitator.service.LocationService;
import edu.columbia.sel.facilitator.task.FacilityRequestTask;

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
		FacilitatorApplication.class,
		
		// Activities
		BaseActivity.class,
		MainActivity.class,
		FacilityListActivity.class,
		SelectOfflineAreaActivity.class,
		FacilityDetailActivity.class,
		FacilityMapListActivity.class,
		AddFacilityActivity.class,
		
		// Fragments
		BaseFragment.class,
		BaseMapFragment.class,
		FacilityMapFragment.class,
		AddFacilityMapFragment.class,
		
		// Other Components
		FacilityRequestTask.class,
		FacilityRepository.class,
		AddFacilityItemizedIconOverlay.class,
		LocationService.class,
		JsonFileSiteRepository.class
	}
)
public class DIModule {
	private final FacilitatorApplication application;
	
	private final String TAG = "FacilitatorApplication";

	public DIModule(FacilitatorApplication application) {
		this.application = application;
	}

	/**
	 * Allow the application context to be injected but require that it be
	 * annotated with {@link ForApplication @Annotation} to explicitly
	 * differentiate it from an activity context.
	 * @return FacilitatorApplication
	 */
	@Provides
	@Singleton
	@ForApplication
	FacilitatorApplication provideApplicationContext() {
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
		return new SpiceManager(FacilityRetrofitSpiceService.class);
	}
}