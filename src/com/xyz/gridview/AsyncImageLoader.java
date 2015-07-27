package com.xyz.gridview;

import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.xyz.gridview.R.drawable;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsyncImageLoader {
	private HashMap<Uri, SoftReference<Drawable>> imageCache;
	private ContentResolver mResolver;
	private ExecutorService executorService = Executors.newScheduledThreadPool(5);
	 
	public ContentResolver getmResolver() {
		return mResolver;
	}

	public void setmResolver(ContentResolver mResolver) {
		this.mResolver = mResolver;
	}

	public AsyncImageLoader(ContentResolver resolver) {
		imageCache = new HashMap<Uri, SoftReference<Drawable>>();
		mResolver = resolver;
	}
	
	public Drawable loadDrawable(final Uri imageUri, final ImageCallback imageCallback) {
		if (imageCache.containsKey(imageUri)) {
			SoftReference<Drawable> softReference = imageCache.get(imageUri);
			Drawable drawable = softReference.get();
			if(drawable != null) {
				return drawable;
			}
			
			
		}
		
		final Handler handle = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoader((Drawable) message.obj, imageUri);
			}
		};
		
		
		
//		new Thread() {
//			public void run() {} {
//				Drawable drawable = loadImageFromUrl(imageUri, mResolver);
//				imageCache.put(imageUri, new SoftReference<Drawable>(drawable));
//				Message message = handle.obtainMessage(0, drawable);
//				handle.sendMessage(message);
//			};
//		}.run();
//		
		
		Future<?> test = executorService.submit( new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Drawable drawable = loadImageFromUrl(imageUri, mResolver);
				imageCache.put(imageUri, new SoftReference<Drawable>(drawable));
				Message message = handle.obtainMessage(0, drawable);
				handle.sendMessage(message);
			}
		});
		
		if (!test.isDone()) {
			Log.d("zx", "error");
		}
		
		return null;
	}

	public static Drawable loadImageFromUrl(Uri uri,ContentResolver resolver) {
		
//		
//		
//		
//		Options opt = new BitmapFactory.Options();
//		opt.inJustDecodeBounds = true;
//		
//		
//		
//		
//		
		
		Options opt = new Options();
		FileUtil file = new FileUtil();
		Bitmap bitmap = null;
		byte [] mContent = null;
		try {
			mContent = file.readInputStream(resolver.openInputStream(Uri.parse(uri.toString())));
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(mContent, 0, mContent.length, opt);
//			opt.outWidth = 200;
//			opt.outHeight = 200;
			opt.inSampleSize = computeInitialSampleSize(opt, -1, 230 * 230);
			opt.inJustDecodeBounds = false;
			
			bitmap = file.getBitmapFromBytes(mContent, opt );
			return new BitmapDrawable(null,bitmap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
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
		public void imageLoader(Drawable imageDrawable, Uri imageUri);
	}
	
}
