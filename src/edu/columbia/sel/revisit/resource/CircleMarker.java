package edu.columbia.sel.revisit.resource;

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

/**
 * A Drawable for use as a marker on the Site map.
 * 
 * @author Jonathan Wohl
 *
 */
public class CircleMarker extends Drawable {
	private String TAG = this.getClass().getCanonicalName();
	private Paint mBackgroundPaint;
	private Paint mTextPaint;
	private String text;
	private int backgroundColor = Color.argb(200, 18, 74, 255);
	private static int width = 30;
	private static int height = 30;
	private static int radius = 10;
	private static float textHeight = 12; 
	
	public CircleMarker(Resources resources, String text) {
		this.mBackgroundPaint = new Paint();
		this.mTextPaint = new Paint();
		this.text = text;
	}
	
	/**
	 * Factory method for generating new SiteMarkers, using the passed in drawable as a background.
	 * @param resources
	 * @param text
	 * @return BitmapDrawable
	 */
	public static BitmapDrawable createSiteMarker(Resources resources, String text, Drawable background) {
		CircleMarker sm = new CircleMarker(resources, text);
		Bitmap bmp = Bitmap.createBitmap(CircleMarker.width, CircleMarker.height, Config.ARGB_8888); 
	    Canvas canvas = new Canvas(bmp);
	    sm.setBounds(0, 0, CircleMarker.width, CircleMarker.height);
	    sm.draw(canvas);
	    return new BitmapDrawable(resources, bmp);
	}
	
	
	/**
	 * Factory method for generating new SiteMarkers.
	 * @param resources
	 * @param text
	 * @return BitmapDrawable
	 */
	public static BitmapDrawable createSiteMarker(Resources resources, String text) {
		CircleMarker sm = new CircleMarker(resources, text);
		Bitmap bmp = Bitmap.createBitmap(CircleMarker.width, CircleMarker.height, Config.ARGB_8888); 
	    Canvas canvas = new Canvas(bmp);
	    sm.setBounds(0, 0, CircleMarker.width, CircleMarker.height);
	    sm.draw(canvas);
	    return new BitmapDrawable(resources, bmp);
	}
	
	/**
	 * Factory method for generating new SiteMarkers.
	 * @param resources
	 * @param text
	 * @return BitmapDrawable
	 */
	public static BitmapDrawable createSiteMarker(Resources resources, String text, int backgroundColor) {
		CircleMarker sm = new CircleMarker(resources, text);
		Bitmap bmp = Bitmap.createBitmap(CircleMarker.width, CircleMarker.height, Config.ARGB_8888); 
	    Canvas canvas = new Canvas(bmp);
	    sm.setBounds(0, 0, CircleMarker.width, CircleMarker.height);
	    sm.backgroundColor = backgroundColor;
	    sm.draw(canvas);
	    return new BitmapDrawable(resources, bmp);
	}
	
	/**
	 * This is where the icon magic happens.
	 * 
	 * TODO: Probably doesn't make much difference as long as we're not drawing many Sites,
	 * but it would be a bit better to move static calculations out of here so they aren't 
	 * being done on every draw.
	 */
	@Override
	public void draw(Canvas canvas) {
		Log.i(TAG, "\\\\\\\\\\\\\\\\\\     DRAWING     //////////////////");
		mBackgroundPaint.setColor(backgroundColor);
		mBackgroundPaint.setStrokeWidth(2);
		mBackgroundPaint.setStyle(Style.FILL);
		mBackgroundPaint.setAntiAlias(true);
		mBackgroundPaint.setShadowLayer(5.0f, 0.0f, 0.0f, Color.BLACK);
		
		mTextPaint.setARGB(255, 255, 255, 255);
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setFakeBoldText(true);
		
		mTextPaint.setTextSize(textHeight);
		
		float markerCenterX = CircleMarker.width/2;
		float markerCenterY = CircleMarker.height/2;
		
		canvas.drawCircle(markerCenterX, markerCenterY, CircleMarker.radius, mBackgroundPaint);
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
