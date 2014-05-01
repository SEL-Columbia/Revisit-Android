package org.columbia.sel.facilitator.di;

import android.content.Context;
import android.location.LocationManager;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import org.columbia.sel.facilitator.FacilitatorApplication;
import org.columbia.sel.facilitator.activity.BaseActivity;
import org.columbia.sel.facilitator.activity.FacilityDetailActivity;
import org.columbia.sel.facilitator.activity.FacilityListActivity;
import org.columbia.sel.facilitator.activity.MainActivity;
import org.columbia.sel.facilitator.activity.MapActivity;
import org.columbia.sel.facilitator.annotation.ForApplication;
import org.columbia.sel.facilitator.annotation.ForLogging;
import org.columbia.sel.facilitator.model.FacilityRepository;
import org.columbia.sel.facilitator.task.HttpRequestTask;

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
		BaseActivity.class,
		MainActivity.class,
		FacilityListActivity.class,
		MapActivity.class,
		FacilityDetailActivity.class,
		HttpRequestTask.class,
		FacilityRepository.class
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
}