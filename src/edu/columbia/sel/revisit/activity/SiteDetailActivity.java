package edu.columbia.sel.revisit.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Queue;

import javax.inject.Inject;

import edu.columbia.sel.revisit.R;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import edu.columbia.sel.revisit.api.UpdateSiteRetrofitSpiceRequest;
import edu.columbia.sel.revisit.model.Site;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * The SiteDetailActivity shows all details of a single Site for review before
 * selecting the Site.
 * 
 * @author Jonathan Wohl
 * 
 */
public class SiteDetailActivity extends BaseActivity {

	// Use ButterKnife to inject views
//	@InjectView(R.id.detail_image_progress)
//	ProgressBar mSiteImageProgressView;
	
	@InjectView(R.id.detail_image_frame)
	LinearLayout mImageLayout;
	
	@InjectView(R.id.image_pager)
	ViewPager mImagePager;
	
	@InjectView(R.id.detail_site_sector)
	TextView mSiteSectorView;

	@InjectView(R.id.no_images_text)
	TextView mNoImagesTextView;
	
	@InjectView(R.id.site_sector_icon)
	ImageView mSiteSectorIconView;
	
	@InjectView(R.id.detail_site_type)
	TextView mSiteTypeView;
	
	@InjectView(R.id.detail_site_visits)
	TextView mSiteVisitsView;

	private static final int REQUEST_TAKE_PHOTO = 1;

	// The current facility being viewed
	Site mSite;

	// Path to the storage dir
	File mStorageDir;

	// Absolute URI to the current photo (i.e. the photo just taken)
	String mCurrentPhotoUri;

	// Path to the current photo (i.e. the photo just taken)
	String mCurrentPhotoPath;

	ImagePagerAdapter mPagerAdapter;

	Boolean mLayoutComplete = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_site_detail);

		// add 'back' button to go to parent (FacilityMapListActivity)
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Injection for views and onclick handlers
		ButterKnife.inject(this);

		// Setup image pager
		mPagerAdapter = new ImagePagerAdapter();
		mImagePager.setAdapter(mPagerAdapter);

		// Facility is received upon creation in the Intent
		Intent i = this.getIntent();
		mSite = i.getParcelableExtra("site");

		// TODO: this will break for locally added sites at present due to the
		// get_id()
		setStorageDir("revisit" + File.separator + "photos" + File.separator + mSite.get_id());

		mImageLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mImageLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				displaySite(mSite);
				// if (!mLayoutComplete) {
				// mLayoutComplete = true;
				// }
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.details_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Populate the views with the Site info.
	 * 
	 * @param site
	 */
	private void displaySite(Site site) {
		// load images
		this.displayImages();

		// TODO: set activity title instead;
		// this.mSiteNameView.setText(site.getName());
		this.setTitle(site.getName());

		String sector = site.getProperties().getSector();

		Log.i(TAG, "sector -----------> " + sector + " <-----------");

		if (sector.equals("health")) {
			Log.i(TAG, " -----------> health! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.ic_health);
		} else if (sector.equals("energy")) {
			Log.i(TAG, " -----------> energy! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.ic_energy);
		} else if (sector.equals("education")) {
			Log.i(TAG, " -----------> education! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.ic_education);
		} else if (sector.equals("water")) {
			Log.i(TAG, " -----------> water! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.ic_water);
		}
		// this.mSiteImageView.invalidate();
		this.mSiteTypeView.setText(site.getProperties().getType());
		this.mSiteSectorView.setText(sector);
		// this.mSiteLocationView.setText("Location: " +
		// site.getCoordinates().get(1) + ", " + site.getCoordinates().get(0));
		this.mSiteVisitsView.setText(site.getProperties().getVisits() + " Visits");
	}

	private void displayImages() {
		// Picasso is a nifty library for downloading and caching images.
		if (mStorageDir.isDirectory() && mStorageDir.listFiles().length > 0) {
			// presumably we have images of this place.
			File[] listing = mStorageDir.listFiles();
			mPagerAdapter.clearImages();
			
//			ArrayList<String> images = new ArrayList<String>();
			
			for (File child : listing) {
//				images.add(child.getAbsolutePath());
				mPagerAdapter.addImage(child.getAbsolutePath());
			}
//			mPagerAdapter.reverseImages();
//			Collections.reverse(images);
//			mPagerAdapter.setImages(images);
			mPagerAdapter.notifyDataSetChanged();
			mNoImagesTextView.setVisibility(View.GONE);
			mImagePager.setVisibility(View.VISIBLE);
		} else {
			Log.i(TAG, "No photos found for this Site.");
			mNoImagesTextView.setVisibility(View.VISIBLE);
			mImagePager.setVisibility(View.GONE);
		}

	}

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File

			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult() : " + requestCode + ", " + resultCode);
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			// The Intent is empty, but we can assume success...
			// setPic();
			// galleryAddPic();
			this.displayImages();
		}
		// File img = new File(mCurrentPhotoUri);
		// sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
		// Uri.fromFile(img)));
	}

	/**
	 * Set the storage root dir for photos
	 * 
	 * @param dirName
	 */
	public void setStorageDir(String dirName) {
		File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + dirName);
		if (!file.mkdirs()) {
			Log.e(TAG, "Directory not created");
		}
		mStorageDir = file;
	}

	/**
	 * Generate a file into which the Camera App will save the photo.
	 * 
	 * @return
	 * @throws IOException
	 */
	private File createImageFile() throws IOException {
		// Create an image file name
		Calendar c = Calendar.getInstance();
		String timeStamp = new SimpleDateFormat("yyMMddHHmmss").format(c.getTime());
		Log.i(TAG, "Timestamp: " + timeStamp);
		String imageFileName = "Site_" + mSite.get_id() + "_" + timeStamp + "_";

		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				mStorageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		Log.i(TAG, "image.getAbsolutePath() : " + image.getAbsolutePath());
		mCurrentPhotoUri = "file:" + image.getAbsolutePath();
		mCurrentPhotoPath = image.getAbsolutePath();

		return image;
	}

	/**
	 * Add the photo to the android gallery as well as to our app.
	 */
	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	/**
	 * Scale the photo appropriately for better memory management
	 * 
	 * @param path
	 * @return
	 */
	private Bitmap preparePhoto(String path) {
		Log.i(TAG, "preparePic() : " + path);
		// Get the dimensions of the View
		int targetW = mImagePager.getWidth();
		int targetH = mImagePager.getHeight();

		String uri = "file:" + path;
		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(uri, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		Log.i(TAG, "widths: " + targetW + ", " + targetH + ", " + photoW + ", " + photoH);

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
		return bitmap;
	}

	@OnClick(R.id.capture_image)
	public void captureImage() {
		dispatchTakePictureIntent();
	}

	@OnClick(R.id.finish_button)
	public void submit() {

		int visits = mSite.getProperties().getVisits();
		visits += 1;
		mSite.getProperties().setVisits(visits);

		// JsonFileSiteRepository sr = new JsonFileSiteRepository(this);
		mSiteRepository.updateSite(mSite);

		Intent i = new Intent();
		i.putExtra("site_name", mSite.getName());
		i.putExtra("site_sector", mSite.getProperties().getSector());
		i.putExtra("site_type", mSite.getProperties().getType());
		i.putExtra("site_latitude", mSite.getCoordinates().get(1));
		i.putExtra("site_longitude", mSite.getCoordinates().get(0));
		i.putExtra("site_location", mSite.getCoordinates().get(1) + ", " + mSite.getCoordinates().get(0));
		setResult(RESULT_OK, i);
		finish();
	}

	private class ImagePagerAdapter extends PagerAdapter {
		private ArrayList<String> mImages = new ArrayList<String>();
//		private ArrayDeque<String> mImages = new ArrayDeque<String>();

		public void reverseImages() {
			Collections.reverse(mImages);
		}
		
		public void clearImages() {
			mImages.clear();
		}
		
		public void prependImage(String first) {
			mImages.add(0, first);
		}
		
		public void addImage(String path) {
			mImages.add(path);
		}

		public void removeImage(String path) {
			mImages.remove(path);
		}

		@Override
		public int getCount() {
			return mImages.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ImageView) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Context context = SiteDetailActivity.this;
			ImageView imageView = new ImageView(context);
			int padding = 0;
			imageView.setPadding(padding, padding, padding, padding);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			if (mImages.get(position) != null) {
				Bitmap bitmap = prepareImage(mImages.get(position));
				// Bitmap bitmap =
				// BitmapFactory.decodeFile(mImages.get(position));
				imageView.setImageBitmap(bitmap);
			}
			((ViewPager) container).addView(imageView, position);
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}

		/**
		 * TODO: We should cache these rather than having them generated on
		 * every swipe
		 * 
		 * @param path
		 * @return
		 */
		private Bitmap prepareImage(String path) {
			Log.i(TAG, "preparePic() : " + path);
			// Get the dimensions of the View
			int targetW = mImagePager.getWidth();
			int targetH = mImagePager.getHeight();

			String uri = "file:" + path;
			// Get the dimensions of the bitmap
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			Log.i(TAG, "widths: " + targetW + ", " + targetH + ", " + photoW + ", " + photoH);

			// Determine how much to scale down the image
			int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

			// Decode the image file into a Bitmap sized to fill the View
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
			return bitmap;
		}
	}
}
