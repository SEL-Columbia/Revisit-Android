package org.columbia.sel.facilitator.resource;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class FacilityMarker extends Drawable {
	private String TAG = this.getClass().getCanonicalName();
	private Paint mBackgroundPaint;
	private Paint mTextPaint;
	private String text;
	private static int width = 20;
	private static int height = 20;
	
	public FacilityMarker(Resources resources, String text) {
		this.mBackgroundPaint = new Paint();
		this.mTextPaint = new Paint();
		this.text = text;
	}
	
	/**
	 * Factory method for generating new FacilityMarkers.
	 * @param resources
	 * @param text
	 * @return BitmapDrawable
	 */
	public static BitmapDrawable createFacilityMarker(Resources resources, String text) {
		FacilityMarker fm = new FacilityMarker(resources, text);
		Bitmap bmp = Bitmap.createBitmap(FacilityMarker.width, FacilityMarker.height, Config.ARGB_8888); 
	    Canvas canvas = new Canvas(bmp);
	    fm.setBounds(0, 0, FacilityMarker.width, FacilityMarker.height);
	    fm.draw(canvas);
	    return new BitmapDrawable(resources, bmp);
	}
	
	/**
	 * This is where the magic happens.
	 */
	@Override
	public void draw(Canvas canvas) {
		Log.i(TAG, "\\\\\\\\\\\\\\\\\\     DRAWING     ///////////////////");
		mBackgroundPaint.setARGB(230, 18, 74, 255);
		mBackgroundPaint.setStrokeWidth(2);
		mBackgroundPaint.setStyle(Style.FILL);
//		mBackgroundPaint.setFilterBitmap(true);
		mBackgroundPaint.setAntiAlias(true);
//		mBackgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setARGB(255, 255, 255, 255);
//		mTextPaint.setStrokeWidth(3);
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setFakeBoldText(true);
		
		float textHeight = (float) FacilityMarker.width/2;
		mTextPaint.setTextSize(textHeight);
		
		float markerCenterX = FacilityMarker.width/2;
		float markerCenterY = FacilityMarker.height/2;
		
		canvas.drawCircle(markerCenterX, markerCenterY, markerCenterX, mBackgroundPaint);
		canvas.drawText(text, markerCenterX, markerCenterY+textHeight/2, mTextPaint);
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return PixelFormat.OPAQUE;
	}

	@Override
	public void setAlpha(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setColorFilter(ColorFilter arg0) {
		// TODO Auto-generated method stub
		
	}

}
