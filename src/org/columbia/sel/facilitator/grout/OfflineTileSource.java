package org.columbia.sel.facilitator.grout;

import java.io.File;
import java.io.InputStream;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * An implementation of
 * {@link org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase}
 */
public class OfflineTileSource extends OnlineTileSourceBase {

	private final String TAG = this.getClass().getCanonicalName();
	
	public OfflineTileSource(final String aName, final string aResourceId, final int aZoomMinLevel,
			final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
			final String[] aBaseUrl) {
		super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
		Log.i(TAG, ">>>>>>>>>>>> OFFLINE TILE SOURCE <<<<<<<<<<<<");
	}

	@Override
	public String getTileURLString(final MapTile aTile) {
		return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY() + mImageFilenameEnding;
	}

	@Override
	public Drawable getDrawable(final String aFilePath) {
		Log.i(TAG, "getDrawable, String");
		try {
			// default implementation will load the file as a bitmap and create
			// a BitmapDrawable from it
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			BitmapPool.getInstance().applyReusableOptions(bitmapOptions);
			final Bitmap bitmap = BitmapFactory.decodeFile(aFilePath, bitmapOptions);
			Log.i(TAG, ">>>>>>>   " + bitmap.toString());
			if (bitmap != null) {
				return new ReusableBitmapDrawable(bitmap);
			} else {
				// if we couldn't load it then it's invalid - delete it
				try {
					new File(aFilePath).delete();
				} catch (final Throwable e) {
					// logger.error("Error deleting invalid file: " + aFilePath,
					// e);
				}
			}
		} catch (final OutOfMemoryError e) {
			Log.i(TAG, ">>>>>>>   " + e.getMessage());
			// logger.error("OutOfMemoryError loading bitmap: " + aFilePath);
			System.gc();
		}
		return null;
	}

	@Override
	public String getTileRelativeFilenameString(final MapTile tile) {
		final StringBuilder sb = new StringBuilder();
		sb.append(pathBase());
		sb.append('/');
		sb.append(tile.getZoomLevel());
		sb.append('/');
		sb.append(tile.getX());
		sb.append('/');
		sb.append(tile.getY());
		sb.append(imageFilenameEnding());
		return sb.toString();
	}

	@Override
	public Drawable getDrawable(final InputStream aFileInputStream) throws LowMemoryException {
		Log.i(TAG, "getDrawable, InputStream");
		try {
			// default implementation will load the file as a bitmap and create
			// a BitmapDrawable from it
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			BitmapPool.getInstance().applyReusableOptions(bitmapOptions);
			final Bitmap bitmap = BitmapFactory.decodeStream(aFileInputStream, null, bitmapOptions);
			Log.i(TAG, ">>>>>>>   " + bitmap.toString());
			if (bitmap != null) {
				return new ReusableBitmapDrawable(bitmap);
			}
		} catch (final OutOfMemoryError e) {
			// logger.error("OutOfMemoryError loading bitmap");
			Log.i(TAG, ">>>>>>>   " + e.getMessage());
			System.gc();
			throw new LowMemoryException(e);
		}
		return null;
	}
}