package edu.columbia.sel.revisit.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;

import javax.inject.Inject;

import edu.columbia.sel.revisit.R;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import edu.columbia.sel.revisit.api.UpdateSiteRetrofitSpiceRequest;
import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.resource.util.BitmapUtils;
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
	// @InjectView(R.id.detail_image_progress)
	// ProgressBar mSiteImageProgressView;

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
		int red = getResources().getColor(R.color.sel_red);
		int blue = getResources().getColor(R.color.sel_blue);
		int green = getResources().getColor(R.color.sel_green);
		int orange = getResources().getColor(R.color.sel_orange);

		Log.i(TAG, "sector -----------> " + sector + " <-----------");

		if (sector.equals("health")) {
			Log.i(TAG, " -----------> health! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.ic_health);
			mSiteVisitsView.setTextColor(red);
		} else if (sector.equals("energy")) {
			Log.i(TAG, " -----------> energy! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.ic_energy);
			mSiteVisitsView.setTextColor(green);
		} else if (sector.equals("education")) {
			Log.i(TAG, " -----------> education! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.ic_education);
			mSiteVisitsView.setTextColor(orange);
		} else if (sector.equals("water")) {
			Log.i(TAG, " -----------> water! <-----------");
			this.mSiteSectorIconView.setImageResource(R.drawable.ic_water);
			mSiteVisitsView.setTextColor(blue);
		}
		// this.mSiteImageView.invalidate();
		this.mSiteTypeView.setText(site.getProperties().getType());
		this.mSiteSectorView.setText(sector);
		// this.mSiteLocationView.setText("Location: " +
		// site.getCoordinates().get(1) + ", " + site.getCoordinates().get(0));
		this.mSiteVisitsView.setText(site.getProperties().getVisits() + " Visits");
	}

	/**
	 * If there is a storage directory for this Site, grab the files therein
	 * and add them to the image Pager. 
	 */
	private void displayImages() {
		if (mStorageDir.isDirectory() && mStorageDir.listFiles().length > 0) {
			// presumably we have images of this place.
			File[] listing = mStorageDir.listFiles();
			
			for (File child : listing) {
				// images.add(child.getAbsolutePath());
				addImageToPager(child.getAbsolutePath(), false);
			}
			mImagePager.setCurrentItem(0, false);
			mNoImagesTextView.setVisibility(View.GONE);
			mImagePager.setVisibility(View.VISIBLE);
		} else {
			Log.i(TAG, "No photos found for this Site.");
			mNoImagesTextView.setVisibility(View.VISIBLE);
			mImagePager.setVisibility(View.GONE);
		}

	}
	
	/**
	 * Add a single image view to the pager view.
	 * @param path
	 */
	private void addImageToPager(String path, Boolean setToCurrent) {
		ImageView imageView = new ImageView(this);
		int padding = 0;
		imageView.setPadding(padding, padding, padding, padding);
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		
		Log.i(TAG, "Adding Image: " + path);
		
		int pageIndex = mPagerAdapter.prependView(imageView, path);
		mPagerAdapter.notifyDataSetChanged();
		
		if (setToCurrent) {
			mImagePager.setCurrentItem (pageIndex, false);	
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
				Log.e(TAG, ex.toString());
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult() : " + requestCode + ", " + resultCode);
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			
			// Immediately resize the image
			// TODO: since this is happening one pop at a time it shouldn't be too slow, but might be.
			BitmapUtils.reduceBitmapInPlace(mCurrentPhotoPath, 800, 800);
			
			// Success, add the image to the pager 
			mNoImagesTextView.setVisibility(View.GONE);
			mImagePager.setVisibility(View.VISIBLE);
			this.addImageToPager(mCurrentPhotoPath, true);
			
			// and store the path on the object to be used for syncing
			ArrayList<String> newImages;			
			if (!mSite.getAdditionalProperties().containsKey("newImages")) {
				mSite.setAdditionalProperty("newImages", new ArrayList<String>());
			}
			newImages = (ArrayList<String>) mSite.getAdditionalProperties().get("newImages");
			newImages.add(mCurrentPhotoPath);
			
			mSite.setRequestSync(true);
		}
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
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
		Log.i(TAG, "Timestamp: " + timeStamp);
		String imageFileName = "Site_" + mSite.get_id() + "_" + timeStamp;

//		File image = File.createTempFile(imageFileName, /* prefix */
//				".jpg", /* suffix */
//				mStorageDir /* directory */
//		);
		
		File image = new File(mStorageDir, imageFileName + ".jpg");

		Log.i(TAG, "image.getAbsolutePath() : " + image.getAbsolutePath());
		
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

		// list to hold the ImageViews that will be paged
		private ArrayList<View> mImageViews = new ArrayList<View>();
		
		// list to hold the paths for each image
		private ArrayList<String> mImagePaths = new ArrayList<String>();

		@Override
		public int getItemPosition(Object object) {
			int index = mImageViews.indexOf(object);
			if (index == -1)
				return POSITION_NONE;
			else
				return index;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView v = (ImageView) mImageViews.get(position);
	        loadImage(v, mImagePaths.get(position));
	        container.addView(v);
			return v;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mImageViews.get(position));
		}

		@Override
		public int getCount() {
			return mImageViews.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ImageView) object);
		}
		
		/**
		 * Add view to the front of the list.
		 * @param v
		 * @return
		 */
		public int prependView(View v, String path) {
			return addView(v, path, 0);
		}
		
		/**
		 * Add {view} to be loaded with file at {path} into position {position} within ViewPager
		 * Not used by ViewPager directly.
		 * @param v
		 * @param path
		 * @param position
		 * @return
		 */
		public int addView(View v, String path, int position) {
			mImageViews.add(position, v);
			mImagePaths.add(position, path);
			return position;
		}

		/**
		 * Used by external components to remove a view {v} from {pager}.
		 * Not used by ViewPager directly.
		 * @param pager
		 * @param v
		 * @return
		 */
		public int removeView(ViewPager pager, View v) {
			return removeView(pager, mImageViews.indexOf(v));
		}

		/**
		 * Used by external components to remove a view at {position} from {pager}.
		 * Not used by ViewPager directly.
		 * @param pager
		 * @param position
		 * @return
		 */
		public int removeView(ViewPager pager, int position) {
			// ViewPager doesn't have a delete method; the closest is to set the adapter
			// again. When doing so, it deletes all its views. Then we can delete the view
			// from from the adapter and finally set the adapter to the pager again. Note
			// that we set the adapter to null before removing the view from "views" - that's
			// because while ViewPager deletes all its views, it will call destroyItem which
			// will in turn cause a null pointer ref.
			pager.setAdapter(null);
			mImageViews.remove(position);
			pager.setAdapter(this);

			return position;
		}

		/**
		 * Used by external components that want to access the view at {position}
		 * from the ViewPager. Not used by ViewPager directly.
		 * @param position
		 * @return
		 */
		public View getView(int position) {
			return mImageViews.get(position);
		}

		/**
		 * Load the image at {path} into {imageView} with Picasso.
		 * 
		 * TODO: add placeholders
		 * 
		 * @param imageView
		 * @param path
		 */
		private void loadImage(ImageView imageView, String path) {
			Log.i(TAG, "loading image: " + path);
			Picasso.with(SiteDetailActivity.this)
			    .load(new File(path))
			    .transform(getFitHeightTransformation())
//			    .placeholder(R.drawable.user_placeholder)
//			    .error(R.drawable.user_placeholder_error)
			    .into(imageView, new Callback() {
			    	
			    	@Override
			    	public void onSuccess() {
			    		Log.i(TAG, "Successfully loaded image.");
			    	}

					@Override
					public void onError() {
						// TODO Auto-generated method stub
						Log.i(TAG, "Gall darnit!");
					}
			    });
		}
		
		/**
		 * Custom transform to fit the image to the height of the view without
		 * distorting.
		 * @return
		 */
		private Transformation getFitHeightTransformation() {
			Transformation transformation = new Transformation() {

                @Override public Bitmap transform(Bitmap source) {
                    int targetHeight = mImagePager.getHeight();

                    double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                    int targetWidth = (int) (targetHeight / aspectRatio);
                    Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                    if (result != source) {
                        // Same bitmap is returned if sizes are the same
                        source.recycle();
                    }
                    return result;
                }

                @Override public String key() {
                    return "transformation" + " desiredWidth";
                }
            };
            
            return transformation;
		}
	}
}
