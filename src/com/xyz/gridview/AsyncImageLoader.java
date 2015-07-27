package com.xyz.gridview;

import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import com.xyz.gridview.R.drawable;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader {
	private HashMap<Uri, SoftReference<Drawable>> imageCache;
	private ContentResolver mResolver;
	
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
		
		new Thread() {
			public void run() {} {
				Drawable drawable = loadImageFromUrl(imageUri, mResolver);
				imageCache.put(imageUri, new SoftReference<Drawable>(drawable));
				Message message = handle.obtainMessage(0, drawable);
				handle.sendMessage(message);
			};
		}.run();
//		
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
			opt.inSampleSize = computeInitialSampleSize(opt, -1, 128 * 128);
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
	
	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixex)  {
    	double w = options.outWidth;
    	double h = options.outHeight;
    	int lowerBound = (maxNumOfPixex == -1) ? 1 : (int)Math.ceil(Math.sqrt(w*h/maxNumOfPixex));
    	int uperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w/minSideLength), Math.floor(h/minSideLength));
    	
    	if (uperBound < lowerBound) {
    		return lowerBound;
    	}
    	if ((maxNumOfPixex == -1) &&
    			(minSideLength == -1)) {
    		return 1;
    	} else if (minSideLength == -1) {
    		return lowerBound;
    	} else {
    		return uperBound;
    	}
    }
	public interface ImageCallback {
		public void imageLoader(Drawable imageDrawable, Uri imageUri);
	}
}
