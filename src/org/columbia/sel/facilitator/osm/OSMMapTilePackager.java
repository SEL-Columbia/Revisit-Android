// Created by plusminus on 9:22:20 PM - Mar 5, 2009
package org.columbia.sel.facilitator.osm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.osmdroid.util.GEMFFile;

import android.util.Log;

public class OSMMapTilePackager {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int THREADCOUNT_DEFAULT = 2;
	private static boolean FORCE = false;
	private String TAG = this.getClass().getCanonicalName();

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public OSMMapTilePackager() {
		Log.i(TAG, "++++++++++++ Creating Tile Packager");
	}

	public void start(final String[] args) {
		if (args == null || args.length == 0) {
			printUsageAndExit();
		}

		/* Parsing will only start if this variable was set. */
		FORCE = false;
		String serverURL = null;
		String destinationFile = null;
		String tempFolder = null;
		String fileAppendix = "";
		Double north = null;
		Double south = null;
		Double east = null;
		Double west = null;
		Integer maxzoom = null;
		int minzoom = 0;
		int threadCount = THREADCOUNT_DEFAULT;

		try {
			for (int i = 0; i < args.length; i += 2) {
				if (args[i].equals("-u")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						serverURL = args[i + 1];
					}
				} else if (args[i].equals("-force")) {
					i--;
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						FORCE = true;
					}
				} else if (args[i].equals("-d")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						destinationFile = args[i + 1];
					}
				} else if (args[i].equals("-fa")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						fileAppendix = args[i + 1];
					}
				} else if (args[i].equals("-nthreads")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						threadCount = Integer.parseInt(args[i + 1]);
					}
				} else if (args[i].equals("-zmin")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						minzoom = Integer.parseInt(args[i + 1]);
					}
				} else if (args[i].equals("-zmax")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						maxzoom = Integer.parseInt(args[i + 1]);
					}
				} else if (args[i].equals("-t")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						tempFolder = args[i + 1];
					}
				} else if (args[i].equals("-n")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						north = Double.parseDouble(args[i + 1]);
					}
				} else if (args[i].equals("-s")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						south = Double.parseDouble(args[i + 1]);
					}
				} else if (args[i].equals("-e")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						east = Double.parseDouble(args[i + 1]);
					}
				} else if (args[i].equals("-w")) {
					if (i >= args.length) {
						printUsageAndExit();
					} else {
						west = Double.parseDouble(args[i + 1]);
					}
				}
			}
		} catch (final NumberFormatException nfe) {
			printUsageAndExit();
		}

		if (tempFolder == null) {
			printUsageAndExit();
		}

		if (serverURL == null && !new File(tempFolder).exists()) {
			printUsageAndExit();
		}

		if (north == null || south == null || east == null || west == null) {
			printUsageAndExit();
		}

		run(serverURL, destinationFile, tempFolder, threadCount, fileAppendix, minzoom, maxzoom, north, south, east,
				west);
	}

	private void run(final String pServerURL, final String pDestinationFile, final String pTempFolder,
			final int pThreadCount, final String pFileAppendix, final int pMinZoom, final int pMaxZoom,
			final double pNorth, final double pSouth, final double pEast, final double pWest) {

		if (pServerURL != null) {
			Log.i(TAG, "--------------------------- DELETING TILES");
			Util.deleteDirectory(new File(pTempFolder));

			Log.i(TAG, "---------------------------");
			final int expectedFileCount = runFileExpecter(pMinZoom, pMaxZoom, pNorth, pSouth, pEast, pWest);

			Log.i(TAG, "---------------------------");
			runDownloading(pServerURL, pTempFolder, pThreadCount, pFileAppendix, pMinZoom, pMaxZoom, pNorth, pSouth,
					pEast, pWest);

			Log.i(TAG, "---------------------------");
			runFileExistenceChecker(expectedFileCount, pTempFolder, pMinZoom, pMaxZoom, pNorth, pSouth, pEast, pWest);
		}

		if (pDestinationFile != null) {
			Log.i(TAG, "---------------------------");
			if (pDestinationFile.endsWith(".zip")) {
				runZipToFile(pTempFolder, pDestinationFile);
			} else if (pDestinationFile.endsWith(".gemf")) {
				runCreateGEMFFile(pTempFolder, pDestinationFile);
			} else {
				runCreateDb(pTempFolder, pDestinationFile);
			}

			Log.i(TAG, "---------------------------");

			if (pServerURL != null) {
				runCleanup(pTempFolder);
			}
		}

		Log.i(TAG, "---------------------------");
	}

	private void runFileExistenceChecker(final int pExpectedFileCount, final String pTempFolder, final int pMinZoom,
			final int pMaxZoom, final double pNorth, final double pSouth, final double pEast, final double pWest) {

		// abortIfUserIsNotSure("This will check if the actual filecount is the same as the expected ("
		// + pExpectedFileCount + ").");

		/* Quickly count files in the tempFolder. */
		Log.i(TAG, "Counting existing files ...");
		final int actualFileCount = FolderFileCounter.getTotalRecursiveFileCount(new File(pTempFolder));
		if (pExpectedFileCount == actualFileCount) {
			Log.i(TAG, " done.");
		} else {
			Log.i(TAG, " FAIL!");
			// abortIfUserIsNotSure("Reason: Actual files:" + actualFileCount +
			// "    Expected: " + pExpectedFileCount + ". Proceed?");
		}
	}

	private void printUsageAndExit() {
		String message = "Usage:\n" + "-u\t[OSM-style tile URL: http://_URL_/%d/%d/%d.png]\n"
				+ "-t\t[Temporary Folder]\n" + "-d\t[Destination-file: C:\\mappack.zip]\n"
				+ "-zmin\t[Minimum zoomLevel to download. Default: 0]\n" + "-zmax\t[Maximum zoomLevel to download]\n"
				+ "-fa\t[Filename-Appendix. Default: \"\"]\n" + "-n\t[North Latitude]\n" + "-s\t[South Latitude]\n"
				+ "-e\t[East Longitude]\n" + "-w\t[West Longitude]\n"
				+ "-nthreads\t[Number of Download-Threads. Default: 2]\n";
		Log.i(TAG, message);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	private void runCreateGEMFFile(final String pTempFolder, final String pDestinationFile) {
		try {
			Log.i(TAG, "Creating GEMF archive from " + pTempFolder + " to " + pDestinationFile + " ...");
			final List<File> sourceFolders = new ArrayList<File>();
			sourceFolders.add(new File(pTempFolder));
			final GEMFFile file = new GEMFFile(pDestinationFile, sourceFolders);
			Log.i(TAG, " done.");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void runZipToFile(final String pTempFolder, final String pDestinationFile) {
		try {
			Log.i(TAG, "Zipping files to " + pDestinationFile + " ...");
			FolderZipper.zipFolderToFile(new File(pDestinationFile), new File(pTempFolder));
			Log.i(TAG, " done.");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void runCreateDb(final String pTempFolder, final String pDestinationFile) {
		try {
			Log.i(TAG, "Putting files into db : " + pDestinationFile + " ...");
			DbCreator.putFolderToDb(new File(pDestinationFile), new File(pTempFolder));
			Log.i(TAG, " done.");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void runCleanup(final String pTempFolder) {
		// abortIfUserIsNotSure("This will delete the temp folder: " +
		// pTempFolder + " !");

		/* deleteDirecto */
		Log.i(TAG, "Deleting temp folder ...");
		FolderDeleter.deleteFolder(new File(pTempFolder));
		Log.i(TAG, " done.");
	}

	private void runDownloading(final String pBaseURL, final String pTempFolder, final int pThreadCount,
			final String pFileAppendix, final int pMinZoom, final int pMaxZoom, final double pNorth,
			final double pSouth, final double pEast, final double pWest) {
		final String pTempBaseURL = pTempFolder + File.separator + "%d" + File.separator + "%d" + File.separator + "%d"
				+ pBaseURL.substring(pBaseURL.lastIndexOf('.'))
				+ pFileAppendix.replace(File.separator + File.separator, File.separator);

		final DownloadManager dm = new DownloadManager(pBaseURL, pTempBaseURL, pThreadCount);

		/* For each zoomLevel. */
		for (int z = pMinZoom; z <= pMaxZoom; z++) {
			final OSMTileInfo upperLeft = Util.getMapTileFromCoordinates(pNorth, pWest, z);
			final OSMTileInfo lowerRight = Util.getMapTileFromCoordinates(pSouth, pEast, z);

			Log.i(TAG, "ZoomLevel: " + z + " ");
			for (int x = upperLeft.x; x <= lowerRight.x; x++) {
				for (int y = upperLeft.y; y <= lowerRight.y; y++) {
					dm.add(new OSMTileInfo(x, y, z));
				}
			}
			try {
				dm.waitEmpty();
				Log.i(TAG, " done.");
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			Log.i(TAG, "Awaiting termination of all threads ...");
			dm.waitFinished();
			Log.i(TAG, " done.");
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private int runFileExpecter(final int pMinZoom, final int pMaxZoom, final double pNorth, final double pSouth,
			final double pEast, final double pWest) {
		/* Calculate file-count. */
		int fileCnt = 0;
		for (int z = pMinZoom; z <= pMaxZoom; z++) {
			final OSMTileInfo upperLeft = Util.getMapTileFromCoordinates(pNorth, pWest, z);
			final OSMTileInfo lowerRight = Util.getMapTileFromCoordinates(pSouth, pEast, z);

			final int dx = lowerRight.x - upperLeft.x + 1;
			final int dy = lowerRight.y - upperLeft.y + 1;
			fileCnt += dx * dy;
		}

		// abortIfUserIsNotSure("This will download: " + fileCnt +
		// " Maptiles!");

		return fileCnt;
	}

	private void abortIfUserIsNotSure(final String message) {
		if (FORCE) {
			return;
		}

		Log.i(TAG, message);
		Log.i(TAG, "Are you sure? [Y/N] ?: ");
		try {
			// java.awt.Toolkit.getDefaultToolkit().beep();
		} catch (final Throwable t) { /* Ignore */
		}

		final String line = new Scanner(System.in).nextLine().toUpperCase().trim();

		if (!line.equals("Y") && !line.equals("YES")) {
			System.err.println("User aborted.");
			System.exit(0);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
