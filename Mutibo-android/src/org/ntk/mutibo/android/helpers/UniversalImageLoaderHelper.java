package org.ntk.mutibo.android.helpers;

import android.app.Activity;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class UniversalImageLoaderHelper {

//	private static DisplayImageOptions options;
	private static ImageLoader imageLoader;

	public static void init(Activity activity) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity)
				.denyCacheImageMultipleSizesInMemory().memoryCache(new LruMemoryCache(2 * 1024 * 1024))
				.memoryCacheSize(2 * 1024 * 1024).diskCacheExtraOptions(720, 1280, null)
				.diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(100).build();

//		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_launcher) // resource or drawable
//				.showImageForEmptyUri(R.drawable.ic_hot_scores) // resource or drawable
//				.showImageOnFail(R.drawable.ic_hot_scores) // resource or drawable
//				.resetViewBeforeLoading(false) // default
//				// .delayBeforeLoading(1000)
//				.cacheInMemory(false) // default
//				.cacheOnDisk(true) // default
//				.build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
	}

	public static ImageLoader getLoader(Activity activity) {
		if (imageLoader == null)
			init(activity);
		return imageLoader;
	}

}
