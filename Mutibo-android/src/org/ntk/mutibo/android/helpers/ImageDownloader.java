package org.ntk.mutibo.android.helpers;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
	private static final String TAG = "ImageDownloader";
	private ImageView bmImage;

	public ImageDownloader() {
	}

	public ImageDownloader setImageView(ImageView bmImage) {
		this.bmImage = bmImage;
		return this; // for chained calls
	}

	protected Bitmap doInBackground(String... urls) {
		String url = urls[0];
		try {
			// BitmapFactory.Options options = new BitmapFactory.Options();
			// options.inJustDecodeBounds = true;
			// options.inSampleSize = calculateInSampleSize(options, bmImage.getWidth(), bmImage.getHeight());
			// // BitmapFactory.decodeResource(getResources(), R.id.myimage, options);
			// // int imageHeight = options.outHeight;
			// // int imageWidth = options.outWidth;
			// // String imageType = options.outMimeType;
			// InputStream in = new java.net.URL(url).openStream();
			// mIcon = BitmapFactory.decodeStream(in, new Rect(0, 0, bmImage.getWidth(), bmImage.getHeight()), options);
			InputStream in = null;
			try {
				final int IMAGE_MAX_SIZE = 500000; // 1.2MP
				in = new java.net.URL(url).openStream();

				// Decode image size
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(in, null, o);
				in.close();

				int scale = 1;
				while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
					scale++;
				}
				Log.d(TAG, "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

				Bitmap b = null;
				in = new java.net.URL(url).openStream();
				if (scale > 1) {
					scale--;
					// scale to max possible inSampleSize that still yields an image
					// larger than target
					o = new BitmapFactory.Options();
					o.inSampleSize = scale;
					b = BitmapFactory.decodeStream(in, null, o);

					// resize to desired dimensions
					int height = b.getHeight();
					int width = b.getWidth();
					Log.d(TAG, "1th scale operation dimenions - width: " + width + ", height: " + height);

					double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
					double x = (y / height) * width;

					Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, (int) y, true);
					b.recycle();
					b = scaledBitmap;

					System.gc();
				} else {
					b = BitmapFactory.decodeStream(in);
				}
				in.close();

				Log.d(TAG, "bitmap size - width: " + b.getWidth() + ", height: " + b.getHeight());

				return b;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return null;
			}
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
		} catch (OutOfMemoryError e) {
			Log.e("OutOfMemoryError", e.getMessage()); // pointless to catch it, the app will terminate anyway
		}
		return null;
	}

	protected void onPostExecute(Bitmap result) {
		if (bmImage != null && result != null)
			bmImage.setImageBitmap(result);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}