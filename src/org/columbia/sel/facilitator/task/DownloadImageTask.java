package org.columbia.sel.facilitator.task;

import org.springframework.util.support.Base64.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

//show The Image
//new DownloadImageTask((ImageView) findViewById(R.id.imageView1))
//         .execute("http://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png");
//}
//
//public void onClick(View v) {
// startActivity(new Intent(this, IndexActivity.class));
// finish();
//
//}

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private String TAG = this.getClass().getCanonicalName();
	
	ImageView bmImage;

	public DownloadImageTask(ImageView bmImage) {
		this.bmImage = bmImage;
	}

	protected Bitmap doInBackground(String... urls) {
		String urldisplay = urls[0];
		Bitmap mIcon11 = null;
		try {
			InputStream in = (InputStream) new java.net.URL(urldisplay)
					.openStream();
			mIcon11 = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		return mIcon11;
	}

	protected void onPostExecute(Bitmap result) {
		bmImage.setImageBitmap(result);
	}
}