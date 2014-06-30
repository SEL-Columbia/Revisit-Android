// Created by plusminus on 10:15:51 PM - Mar 5, 2009
package org.columbia.sel.facilitator.grout.util;

import java.io.File;

import org.columbia.sel.facilitator.grout.OSMTileInfo;

import android.util.Log;

public class TileUtils {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * For a description see:
	 * 
	 * @see http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames For a
	 *      code-description see:
	 * @see http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames#
	 *      compute_bounding_box_for_tile_number
	 * @param aLat
	 *            latitude to get the {@link OSMTileInfo} for.
	 * @param aLon
	 *            longitude to get the {@link OSMTileInfo} for.
	 * @return The {@link OSMTileInfo} providing 'x' 'y' and 'z'(oom) for the
	 *         coordinates passed.
	 */
	public static OSMTileInfo getMapTileFromCoordinates(final double aLat, final double aLon, final int zoom) {
		final int y = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1
				/ Math.cos(aLat * Math.PI / 180))
				/ Math.PI)
				/ 2 * (1 << zoom));
		final int x = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));

		return new OSMTileInfo(x, y, zoom);
	}
	
	/**
	 * Recursively delete a directory and its contents. Returns true on succes, false otherwise.
	 * @param dir
	 * @return boolean
	 */
	public static boolean deleteDirectory(File dir) {
		if (dir.exists()) {
			File[] files = dir.listFiles();
			if (files == null) {
				return true;
			}
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (dir.delete());
	}
	
	/**
	 * Returns the recursive total number of files in the specified directory.
	 * @return int
	 */
	public static int getTotalRecursiveFileCount(final File dir){
		int tmpCnt = 0;
		if (!dir.exists()) {
			return tmpCnt;
		}
		final File[] children = dir.listFiles();
		for(final File c : children){
			if(c.isDirectory()){
				tmpCnt += getTotalRecursiveFileCount(c);
			}else{
				tmpCnt++;
			}
		}
		return tmpCnt;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
