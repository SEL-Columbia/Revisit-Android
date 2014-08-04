package edu.columbia.sel.revisit.resource;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
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
public class SiteMarker extends Drawable {
	private String TAG = this.getClass().getCanonicalName();
	private Paint mTextPaint;
	private String text;
	private int width = 30;
	private int height = 30;
	private static float textHeight = 18; 
	
	public SiteMarker(Resources resources, String text) {
		this.mTextPaint = new Paint();
		this.text = text;
	}

	public SiteMarker(Resources resources, String text, int w, int h) {
		this.mTextPaint = new Paint();
		this.text = text;
		this.width = w;
		this.height = h;
	}
	
	/**
	 * Factory method for generating new SiteMarkers, using the passed in drawable as a background.
	 * @param resources
	 * @param text
	 * @return BitmapDrawable
	 */
	public static BitmapDrawable createSiteMarker(Resources resources, String text, Drawable background) {
		// used to draw text onto canvas
		SiteMarker sm = new SiteMarker(resources, text, background.getIntrinsicWidth(), background.getIntrinsicHeight());

		// Output bitmap and canvas
		Bitmap bmpOverlay = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvasOverlay = new Canvas(bmpOverlay);
		
		// Convert background to bitmap
		Bitmap bmpBackground = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Config.ARGB_8888); 
	    Canvas canvas = new Canvas(bmpBackground);
	    background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
	    background.draw(canvas);
	    
	    // Convert text to bitmap
	    Bitmap bmpText = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Config.ARGB_8888); 
	    Canvas canvasText = new Canvas(bmpText);
	    sm.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
	    sm.draw(canvasText);
	    
	    // Overlay two components
	    canvasOverlay.drawBitmap(bmpBackground, new Matrix(), null);
	    canvasOverlay.drawBitmap(bmpText, new Matrix(), null);
	    
	    return new BitmapDrawable(resources, bmpOverlay);
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

		mTextPaint.setARGB(255, 255, 255, 255);
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setFakeBoldText(true);
		mTextPaint.setTextSize(textHeight);
		
		float markerCenterX = this.width/2;
		float markerCenterY = this.height/2 - 2;
		
		canvas.drawText(text, markerCenterX, markerCenterY, mTextPaint);
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
