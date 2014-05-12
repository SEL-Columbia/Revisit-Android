package org.columbia.sel.facilitator.resource;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class FacilityMarker extends BitmapDrawable {
	private String TAG = this.getClass().getCanonicalName();
	private Paint mBackgroundPaint;
	private Paint mTextPaint;
	private String text;
	
	public FacilityMarker(Resources resources, Bitmap bm, String text) {
		super(resources, bm);
		this.mBackgroundPaint = new Paint();
		this.mTextPaint = new Paint();
		this.text = text;
	}
	
	@Override
	public void draw(Canvas canvas) {
		Log.i(TAG, "\\\\\\\\\\\\\\\\\\     DRAWING     ///////////////////");
		super.draw(canvas);
		mBackgroundPaint.setARGB(255, 0, 0, 255);
		mBackgroundPaint.setStrokeWidth(2);
		mBackgroundPaint.setStyle(Style.FILL);
		mTextPaint.setARGB(255, 0, 0, 0);
		mTextPaint.setStrokeWidth(1);
		canvas.drawCircle(0, 0, 10, mBackgroundPaint);
		canvas.drawText(text, 0, 0, mTextPaint);
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
