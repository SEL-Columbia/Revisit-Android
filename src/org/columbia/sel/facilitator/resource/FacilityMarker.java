package org.columbia.sel.facilitator.resource;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
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
	private static int width = 30;
	private static int height = 30;
	private static int radius = 10;
	private static float textHeight = 12; 
	
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
	 * This is where the icon magic happens.
	 * 
	 * TODO: Probably doesn't make much difference as long as we're not drawing many facilities,
	 * but it would be a bit better to move static calculations out of here so they aren't 
	 * being done on every draw.
	 */
	@Override
	public void draw(Canvas canvas) {
		Log.i(TAG, "\\\\\\\\\\\\\\\\\\     DRAWING     //////////////////");
		mBackgroundPaint.setARGB(200, 18, 74, 255);
		mBackgroundPaint.setStrokeWidth(2);
		mBackgroundPaint.setStyle(Style.FILL);
		mBackgroundPaint.setAntiAlias(true);
		mBackgroundPaint.setShadowLayer(5.0f, 0.0f, 0.0f, Color.BLACK);
		
		mTextPaint.setARGB(255, 255, 255, 255);
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setFakeBoldText(true);
		
		mTextPaint.setTextSize(textHeight);
		
		float markerCenterX = FacilityMarker.width/2;
		float markerCenterY = FacilityMarker.height/2;
		
		canvas.drawCircle(markerCenterX, markerCenterY, FacilityMarker.radius, mBackgroundPaint);
		canvas.drawText(text, markerCenterX, markerCenterY+textHeight/2-2, mTextPaint);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
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
