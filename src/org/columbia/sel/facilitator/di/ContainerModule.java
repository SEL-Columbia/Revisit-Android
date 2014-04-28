package org.columbia.sel.facilitator.di;

import android.content.Context;
import android.location.LocationManager;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import org.columbia.sel.facilitator.FacilitatorApplication;
import org.columbia.sel.facilitator.activity.BaseActivity;
import org.columbia.sel.facilitator.activity.MainActivity;
import org.columbia.sel.facilitator.annotation.ForApplication;
import org.columbia.sel.facilitator.model.FacilityRepository;
import org.columbia.sel.facilitator.tasks.HttpRequestTask;

import com.squareup.otto.Bus;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module(
	library = true,
	injects = {
		BaseActivity.class, 
		MainActivity.class, 
		HttpRequestTask.class,
		FacilityRepository.class
	}
)
public class ContainerModule {
	private final FacilitatorApplication application;

	public ContainerModule(FacilitatorApplication application) {
		this.application = application;
	}

	/**
	 * Allow the application context to be injected but require that it be
	 * annotated with {@link ForApplication @Annotation} to explicitly
	 * differentiate it from an activity context.
	 */
	@Provides
	@Singleton
	@ForApplication
	FacilitatorApplication provideApplicationContext() {
		return application;
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