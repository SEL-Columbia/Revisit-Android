package edu.columbia.sel.revisit.resource.util;

import java.io.File;
import java.io.FileOutputStream;

import edu.columbia.sel.revisit.resource.PhotoManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

public class BitmapUtils {

	private static final String TAG = BitmapUtils.class.toString(); 
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	/**
	 * Synchronously save a bitmap to file - does not use a separate thread.
	 * @param bm
	 * @param path
	 */
	public static void saveBitmapToFileSync(final Bitmap bm, final String path) {

		File file = new File(path);
		try {
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			bm.compress(CompressFormat.JPEG, 75, out);
			out.close();
			Log.i(TAG, "Bitmap Saved!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "ERROR SAVING BITMAP: " + e.toString());
		}

//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				File file = new File(path);
//				try {
//					file.createNewFile();
//					FileOutputStream out = new FileOutputStream(file);
//					bm.compress(CompressFormat.JPEG, 75, out);
//					out.close();
//					Log.i(TAG, "Bitmap Saved!");
//				} catch (Exception e) {
//					e.printStackTrace();
//					Log.e(TAG, "ERROR SAVING BITMAP: " + e.toString());
//				}
//			}
//
//		}).start();

	}
	
	/**
	 * Save bitmap to file using a separate worker thread.
	 * @param bm
	 * @param path
	 */
	public static void saveBitmapToFile(final Bitmap bm, final String path) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				File file = new File(path);
				try {
					file.createNewFile();
					FileOutputStream out = new FileOutputStream(file);
					bm.compress(CompressFormat.JPEG, 75, out);
					out.close();
					Log.i(TAG, "Bitmap Saved!");
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "ERROR SAVING BITMAP: " + e.toString());
				}
			}

		}).start();
		
	}

	/**
	 * Essentially like mkdir -p
	 * @param dirName
	 * @return
	 */
	public static boolean createDirs(String dirName) {
		File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + dirName);
		if (!file.mkdirs()) {
			Log.i(TAG, "Directory not created, may already exist.");
			return false;
		}
		return true;
	}

	/**
	 * Get a bitmap of reduced dimensions.
	 * 
	 * @param filePath
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap reduceBitmap(String filePath, int maxWidth, int maxHeight) {

		BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
		bmpFactoryOptions.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmpFactoryOptions);
		
		int fullWidth = bmpFactoryOptions.outWidth;
		int fullHeight = bmpFactoryOptions.outHeight;
		
		Log.i(TAG, "fullWidth: " + fullWidth);
		Log.i(TAG, "fullHeight: " + fullHeight);
		
		float targetWidth = maxWidth;
		float targetHeight = maxHeight;

		float widthToHeightRatio = (float) fullWidth / fullHeight;
		
		Log.i(TAG, "widthToHeightRatio: " + widthToHeightRatio);
		
		if (widthToHeightRatio > 1) {
			// fit width
			targetHeight = maxWidth / widthToHeightRatio;
		} else {
			targetWidth = maxHeight * widthToHeightRatio;
		}
		
		int targetWidthInt = Math.round(targetWidth);
		int targetHeightInt = Math.round(targetHeight);
		
		Log.i(TAG, "targetWidthInt: " + targetWidthInt + ", targetHeightInt: " + targetHeightInt);
		
		bmpFactoryOptions.inSampleSize = calculateInSampleSize(bmpFactoryOptions, targetWidthInt, targetHeightInt);
		Log.i(TAG, "inSampleSize: " + bmpFactoryOptions.inSampleSize);
		
		bmpFactoryOptions.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(filePath, bmpFactoryOptions);
		Bitmap result = Bitmap.createScaledBitmap(bitmap, targetWidthInt, targetHeightInt, false);
        if (result != bitmap) {
            // Same bitmap is returned if sizes are the same
            bitmap.recycle();
        }
        return result;
	}
	
	/**
	 * Reduce a bitmap and resave it in place of the previous bitmap.
	 * @param filePath
	 * @param width
	 * @param height
	 */
	public static void reduceBitmapInPlace(String filePath, int width, int height) {
		// reduce the bitmap
		Bitmap reducedBitmap = reduceBitmap(filePath, width, height);
		
		// delete the original file
		File curFile = new File(filePath);
		curFile.delete();
		
		saveBitmapToFileSync(reducedBitmap, filePath);
	}
}
