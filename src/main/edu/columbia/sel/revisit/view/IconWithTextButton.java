package edu.columbia.sel.revisit.view;

import edu.columbia.sel.revisit.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

/**
 * An IconWithTextButton lets you create a button with an icon at the beginning of the text.
 * 
 * Makes use of the regular android:text and android:drawableStart att 
 * 
 * @author Jonathan Wohl
 * 
 */
public class IconWithTextButton extends Button {
	protected String TAG = this.getClass().getCanonicalName();

	private Drawable mIcon;
	private String mText;

	public IconWithTextButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		int iconId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "drawableStart", 0);
		mIcon = this.getResources().getDrawable(iconId);

		int textId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "text", 0);
		mText = this.getResources().getString(textId);

		Log.i(TAG, ">>>> Icon: " + mIcon.toString());
		Log.i(TAG, ">>>> Text: " + mText);

		if (mIcon != null) {
			Log.i(TAG, "We have an Icon.");
			String buttonText = mText;
			Spannable buttonLabel = new SpannableString("  " + buttonText);
			// This assumes a square icon:
			mIcon.setBounds(0, -2, this.getLineHeight(), this.getLineHeight());
			buttonLabel.setSpan(new ImageSpan(mIcon), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			this.setText(buttonLabel);
			super.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		} else {
			Log.i(TAG, "NOOOOO Icon.");
		}
	}

	@Override
	protected void onDraw(Canvas canvasObject) {
		super.onDraw(canvasObject);
	}

}
