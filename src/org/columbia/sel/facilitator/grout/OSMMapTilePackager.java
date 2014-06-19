// Created by plusminus on 9:22:20 PM - Mar 5, 2009
package org.columbia.sel.facilitator.grout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GEMFFile;

import android.os.Environment;
import android.util.Log;

public class OSMMapTilePackager {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int DEFAULT_THREADCOUNT = 2;
	private static final String DEFAULT_SERVER_URL = "http://otile1.mqcdn.com/tiles/1.0.0/map/%d/%d/%d.png";
	private static final String DEFAULT_ROOT_DIR = Environment.getExternalStorageDirectory().toString() + "/osmdroid/tiles";
	private static final int DEFAULT_MAX_TILES = 10000;
	private static boolean FORCE = false;
	private final String TAG = this.getClass().getCanonicalName();

	// ===========================================================
	// Fields
	// ===========================================================

	// String[] args = {"-u",
	// "http://otile1.mqcdn.com/tiles/1.0.0/map/%d/%d/%d.png", "-t", fileDest,
	// "-zmin", "4", "-zmax", "12", "-n", n, "-s", s, "-e", e, "-w", w, "-fa",
	// ".tile"};

	private String mServerURL = DEFAULT_SERVER_URL;
	private String mRootDownloadDir = DEFAULT_ROOT_DIR;
	private String mDestinationFile = null;
	private String mTempFolder = "Mapnik";
	private String mFileAppendix = ".tile";
	private Double mNorth = null;
	private Double mSouth = null;
	private Double mEast = null;
	private Double mWest = null;
	private Integer mMaxZoom = 16;
	private int mMinZoom = 8;
	private int mThreadCount = DEFAULT_THREADCOUNT;
	private int mMaxTiles = DEFAULT_MAX_TILES;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OSMMapTilePackager() {
		Log.i(TAG, "++++++++++++ Creating Tile Packager");
	}

	public OSMMapTilePackager(Double north, Double south, Double east, Double west) {
		this.mNorth = north;
		this.mSouth = south;
		this.mEast = east;
		this.mWest = west;
	}

	public OSMMapTilePackager(BoundingBoxE6 bb) {
		this.mNorth = (bb.getLatNorthE6() / 1E6);
		this.mSouth = (bb.getLatSouthE6() / 1E6);
		this.mEast = (bb.getLonEastE6() / 1E6);
		this.mWest = (bb.getLonWestE6() / 1E6);
	}

	/**
	 * Check if the specified area is suitable for download. TODO: determine
	 * some maximum values for number of tiles.
	 * 
	 * @return
	 */
	public Boolean isValidRegionForDownload() {

		// check bounds
		if (mNorth == null || mSouth == null || mEast == null || mWest == null) {
			return false;
		}

		// check that expected num tiles < max
		int maxExpected = this.getExpectedFileCount(mMinZoom, mMaxZoom, mNorth, mSouth, mEast, mWest);
		if (maxExpected > mMaxTiles) {
			return false;
		}

		return true;
	}

	public String getServerURL() {
		return mServerURL;
	}

	public void setServerURL(String serverURL) {
		this.mServerURL = serverURL;
	}

	public String getDestinationFile() {
		return mDestinationFile;
	}

	public void setDestinationFile(String destinationFile) {
		this.mDestinationFile = destinationFile;
	}

	public String getTempFolder() {
		return mTempFolder;
	}

	public void setTempFolder(String tempFolder) {
		this.mTempFolder = tempFolder;
	}

	public String getmRootDownloadDir() {
		return mRootDownloadDir;
	}

	public void setmRootDownloadDir(String mRootDownloadDir) {
		this.mRootDownloadDir = mRootDownloadDir;
	}

	public String getFileAppendix() {
		return mFileAppendix;
	}

	public void setFileAppendix(String fileAppendix) {
		this.mFileAppendix = fileAppendix;
	}

	public Double getNorth() {
		return mNorth;
	}

	public void setNorth(Double north) {
		this.mNorth = north;
	}

	public Double getSouth() {
		return mSouth;
	}

	public void setSouth(Double south) {
		this.mSouth = south;
	}

	public Double getEast() {
		return mEast;
	}

	public void setEast(Double east) {
		this.mEast = east;
	}

	public Double getWest() {
		return mWest;
	}

	public void setWest(Double west) {
		this.mWest = west;
	}

	public Integer getMaxzoom() {
		return mMaxZoom;
	}

	public void setMaxzoom(Integer maxzoom) {
		this.mMaxZoom = maxzoom;
	}

	public int getMinzoom() {
		return mMinZoom;
	}

	public void setMinzoom(int minzoom) {
		this.mMinZoom = minzoom;
	}

	public int getThreadCount() {
		return mThreadCount;
	}

	public void setThreadCount(int threadCount) {
		this.mThreadCount = threadCount;
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
		int threadCount = DEFAULT_THREADCOUNT;

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

	public void run() {
		// remove previously cached files
		String fullTempPath = this.mRootDownloadDir + "/" + this.mTempFolder;
		String fullDestinationFilePath = this.mRootDownloadDir + "/" + this.mDestinationFile;

		Log.i(TAG, "----------------------- DELETING TILES in " + fullTempPath);
		Util.deleteDirectory(new File(fullTempPath));

		// If region is valid for download, do it.
		if (isValidRegionForDownload()) {
			Log.i(TAG, "----------------------- DOWNLOADING TILES in " + this.mTempFolder);
			downloadTiles(mServerURL, fullTempPath, mThreadCount, mFileAppendix, mMinZoom, mMaxZoom, mNorth, mSouth,
					mEast, mWest);
		}

		if (mDestinationFile != null) {
			if (mDestinationFile.endsWith(".zip")) {
				Log.i(TAG, "----------------------- ZIPPING TILES in " + this.mTempFolder);
				runZipToFile(fullTempPath, fullDestinationFilePath);
			} else if (mDestinationFile.endsWith(".gemf")) {
				runCreateGEMFFile(fullTempPath, fullDestinationFilePath);
			} else {
				runCreateDb(fullTempPath, fullDestinationFilePath);
			}

			Log.i(TAG, "---------------------------");

			if (mServerURL != null) {
				runCleanup(fullTempPath);
			}
		}

	}

	private void run(final String pServerURL, final String pDestinationFile, final String pTempFolder,
			final int pThreadCount, final String pFileAppendix, final int pMinZoom, final int pMaxZoom,
			final double pNorth, final double pSouth, final double pEast, final double pWest) {

		if (pServerURL != null) {
			Log.i(TAG, "--------------------------- DELETING TILES in " + pTempFolder);
			Util.deleteDirectory(new File(pTempFolder));

			Log.i(TAG, "---------------------------");
			final int expectedFileCount = getExpectedFileCount(pMinZoom, pMaxZoom, pNorth, pSouth, pEast, pWest);

			Log.i(TAG, "---------------------------");
			downloadTiles(pServerURL, pTempFolder, pThreadCount, pFileAppendix, pMinZoom, pMaxZoom, pNorth, pSouth,
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

	private void downloadTiles(final String pBaseURL, final String pTempFolder, final int pThreadCount,
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

	/**
	 * Given a min/max zoom and a bounding box, how many tiles will we be
	 * downloading?
	 * 
	 * @param pMinZoom
	 * @param pMaxZoom
	 * @param pNorth
	 * @param pSouth
	 * @param pEast
	 * @param pWest
	 * @return int Expected number of images.
	 */
	private int getExpectedFileCount(final int pMinZoom, final int pMaxZoom, final double pNorth, final double pSouth,
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

		return fileCnt;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
