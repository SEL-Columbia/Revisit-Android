Facilitator
===========

An android application that facilitates facility selection.

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
- Basic architecture description
- Documentation
