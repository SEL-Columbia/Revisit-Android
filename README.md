Facilitator
===========

An android application that facilitates facility selection.

## Usage
The Facilitator application is designed to be used in conjunction with [ODK Collect](http://opendatakit.org/use/collect/). The goal is to streamline the collection of facility information by pulling from a central repository of facility data. Users of the app can quickly select the facility at which they are conducting a survey, reducing manual entry errors and speeding the data entry process. Additionally, users can contribute general facility information (name, sector, location, etc.) and fix incorrect information in order to grow and improve the central repository over time.

## Setup and Build
This project is being developed in Eclipse (ADT), but using gradle for dependency management and builds.

### Install Gradle
Follow the directions here: http://www.gradle.org/installation

### Setup the Project in ADT
Use the Android SDK Manager (in ADT, it's under the Window menu) to install Google Repository and Android Support Repository. Once these have been installed, clone this project and import it as an Android project into your ADT workspace.

If you clean the project through ADT, you'll get tons of errors. This is because we're using gradle to pull in dependencies, so from Eclipse's perspective they are all missing. The build.gradle file includes a task that will copy the dependencies into the libs directory, which will make Eclipse happier. Navigate to the project directory, and run the gradle task:

```
$ gradle copyDeps
```

This is better, but Eclipse is probably still complaining about a few missing dependencies. We need to add the appsupport-v7 library project. Follow the directions here: http://developer.android.com/tools/support-library/setup.html

*Tip: When importing this library from the Extras directory, don't forget to check the "copy Projects into workspace" box. This seems to matter.*

### Build and Deploy the Application
We'll be building and deploying the application from the command line using gradle, NOT through eclipse (though it will likely still work through eclipse once we've used gradle to grab the dependencies). To build and deploy, plug in the development device (or enable an emulated device), and run:

```
$ gradle installDebug
```

After some chugging, it should finish with a success message. If an error occurs, fix the problem and try again.

## Included Libraries
The Facilitator application depends on several open source libraries, outlined here. This list will be updated as these dependencies change, and more information about their implmentation may be added at a later date.

### osmdroid
[osmdroid](https://github.com/osmdroid/osmdroid) is used as the map framework, providing a relatively well documented implementation of Open Street Maps.

### Dagger
[Dagger](http://square.github.io/dagger/) provides [dependency injection](http://en.wikipedia.org/wiki/Dependency_injection).

### Otto
[Otto](square.github.io/otto/) provides the application-wide event bus, used as a light-weight PubSub mechanism.

### Retrofit
[Retrofit](http://square.github.io/retrofit/) provides a nice REST client system, used in conjunction with RoboSpice.

### RoboSpice
[RoboSpice](https://github.com/stephanenicolas/robospice) provides an extra layer of network request niceties (caching, thread management).

### Picasso
[Picasso](square.github.io/picasso/) provides a useful way to load and cache images asyncronously.

### Jackson
[Jackson](http://wiki.fasterxml.com/JacksonHome) makes converting between JSON and POJOs super simple (and fast).

### ButterKnife
[ButterKnife](http://jakewharton.github.io/butterknife/) simplifies injecting views and click handlers into Activities and Fragments.

## Application Notes

### Coordinates
*NOTE: At this time, facility coordinates are in the form `coordinates: [longitude, latitude]`.* This is because the data is being served from a simple node/mongo server, and geospatial indices in mongo expect coordinates in the order (longitude, latitude). In order not to confuse future developers, this sillyness will likely be abstracted out on the server.

### Debugging / Logging
Every activity that subclasses BaseActivity has a `TAG` member that is the class's canonical name (e.g. org.columbia.sel.facilitators.activity.MapActivity). This is useful for debugging on the command line using logcat with grep to filter.

```
$ adb logcat | grep --line-buffered ".*org.columbia.sel.facilitator.*"
```

I'd recommend using [pidcat](https://github.com/JakeWharton/pidcat) or [coloredlogcat](http://jsharkey.org/blog/2009/04/22/modifying-the-android-logcat-stream-for-full-color-debugging/) for a much nicer log viewing experience. For me, pidcat wasn't working consistently, so I used coloredlogcat (renamed logcatc) and added an alias in my .bash_profile for convenience:

```
alias logfac='adb logcat | grep --line-buffered ".*org.columbia.sel.facilitator*" | logcatc'
```
Additionally, an `APP_TAG` member is injected in all activities and the application itself with a value of FacilitatorApplication. You can log to this tag, and filter on it to view only these log entries.

## TODO
- Better project description, purpose
- Offline use
- Image capture
- Basic architecture description
- Documentation
