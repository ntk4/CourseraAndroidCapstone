package org.ntk.mutibo.android.helpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Drawable class that shows up as a diagonal banner on the overlay of a View <br/>
 * 
 * the full example can be found on:
 * <a href="http://flavienlaurent.com/blog/2013/08/14/viewoverlay-when-how-and-for-what-purpose/">this article</a><br/>
 * It's been slightly customized to make the displayed message modifiable
 * 
 */
public class BannerDrawable extends Drawable {

	private static final double SQRT_2 = Math.sqrt(2);
	private final Rect mTextBounds;
	private Paint mPaintFill;
	private Paint mPaintText;
	private String mMessage = "";
	private int mBannerWidth = 50;
	private int mTextSize;
	
	private int offset1 = 50; // higher values move the text top-left on the diagonal banner bounds
	private int offset2 = 0; // higher values move the text bottom-left, under the diagonal bounds

	public BannerDrawable(String message) {
		initPaintFill();
		initPaintText();
		mTextBounds = new Rect();
		setMessage(message);
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}

	private void initPaintFill() {
		mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintFill.setStyle(Paint.Style.FILL);
		mPaintFill.setColor(Color.RED);
	}

	private void initPaintText() {
		mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintText.setStyle(Paint.Style.FILL);
		mPaintText.setColor(Color.WHITE);
		mPaintText.setTextSize(20);
		mPaintText.setShadowLayer(4.0f, 2.0f, 2.0f, Color.BLACK);
	}

	@Override
	public void draw(Canvas canvas) {
		Rect bounds = getBounds();
		if (bounds.isEmpty()) {
			bounds = canvas.getClipBounds();
		}
		float width = bounds.width();

		adaptTextSize((int) (width * 0.9), (int) (mBannerWidth * 0.9));

		float bannerHyp = (float) (mBannerWidth * SQRT_2);

		canvas.translate(0, bounds.centerY() - mBannerWidth);
		canvas.rotate(45, bounds.centerX(), bounds.centerY() - mBannerWidth);
		canvas.drawRect(bounds.left - bannerHyp, bounds.top, bounds.right + bannerHyp, bounds.top + mBannerWidth,
				mPaintFill);

		canvas.drawText(mMessage, bounds.centerX() - mTextBounds.centerX() -offset1,
				mBannerWidth / 2 + mTextBounds.height() / 2 + offset2, mPaintText);
	}

	private void adaptTextSize(float width, int height) {
		if (mTextSize > 0) {
			mPaintText.setTextSize(mTextSize);
			return;
		}
		int textSize = 10;
		int textHeight;
		int textWidth;
		boolean stop = false;
		while (!stop) {
			mTextSize = textSize++;
			mPaintText.setTextSize(mTextSize);
			mPaintText.getTextBounds(mMessage, 0, mMessage.length(), mTextBounds);

			textHeight = mTextBounds.height();
			textWidth = mTextBounds.width();

			stop = textHeight >= height || textWidth >= width;
		}
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}
}