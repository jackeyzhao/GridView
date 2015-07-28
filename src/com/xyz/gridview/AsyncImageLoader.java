package com.xyz.gridview;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsyncImageLoader {
	private HashMap<String, SoftReference<Drawable>> imageCache;
	private ContentResolver mResolver;
	private ExecutorService executorService = Executors.newScheduledThreadPool(5);
	 
	public ContentResolver getmResolver() {
		return mResolver;
	}

	public void setmResolver(ContentResolver mResolver) {
		this.mResolver = mResolver;
	}

	public AsyncImageLoader(ContentResolver resolver) {
		imageCache = new HashMap<String, SoftReference<Drawable>>();
		mResolver = resolver;
	}
	public Drawable loadDrawable(String imageUrl) {
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if(drawable != null) {
				return drawable;
			}
			
			
		} 
		return null;
	}
	
	public Drawable loadDrawable(final String imageUrl, final ImageCallback imageCallback) {
//		if (imageCache.containsKey(imageUrl)) {
//			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
//			Drawable drawable = softReference.get();
//			if(drawable != null) {
//				return drawable;
//			}
//			
//			
//		}
		
		final Handler handle = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoader((Drawable) message.obj, imageUrl);
			}
		};
		
		
		
		
		executorService.submit( new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Drawable drawable = loadImageFromUrl(imageUrl);
				imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
				Message message = handle.obtainMessage(0, drawable);
				handle.sendMessage(message);
			}
		});
		
		
		return null;
	}

	public static Drawable loadImageFromUrl(String url) {
		

		
		Options opt = new Options();

		Bitmap bitmap = null;
		byte [] mContent = null;
		
			
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(url, opt);
//			opt.outWidth = 96;
//			opt.outHeight = 96;
			opt.inSampleSize = computeInitialSampleSize(opt, -1, 128 * 128);
			opt.inJustDecodeBounds = false;

			bitmap = BitmapFactory.decodeFile(url, opt);
			return new BitmapDrawable(null,bitmap);
		
		
		
		
	}
	
	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels < 0) ? 1 : (int) Math.ceil(Math.sqrt(w
				* h / maxNumOfPixels));
		int upperBound = (minSideLength < 0) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if (maxNumOfPixels < 0 && minSideLength < 0) {
			return 1;
		} else if (minSideLength < 0) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
	public interface ImageCallback {
		public void imageLoader(Drawable imageDrawable, String url);
	}
	
}
