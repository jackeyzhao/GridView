package com.xyz.gridview;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.xyz.gridview.AsyncImageLoader.ImageCallback;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.SpannableString;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;



public class HomeActivity extends FragmentActivity implements MultiChoiceModeListener, LoaderCallbacks<Cursor> {

    private GridView mGridView;

    private TextView mActionText;
    private static final int MENU_SELECT_ALL = 0;
    private static final int MENU_UNSELECT_ALL = MENU_SELECT_ALL + 1;
    private Map<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
    private Map<Integer, String >mUrlMap = new HashMap<Integer, String>(); 
    private int count = 0;

    private static final String[] STORE_IMAGES = {
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media._ID,
        
	};
    private GridViewCursorAdapter mGridCursorAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);

        mGridCursorAdapter = new GridViewCursorAdapter(this,
        		R.layout.grid_item,
        		null,
        		STORE_IMAGES,
        		new int[] {R.id.img_view, R.id.select},
        		0);
//        mGridCursorAdapter.setViewBinder(new ImageLocationBinder());
        
        mGridView.setAdapter(mGridCursorAdapter);
        getSupportLoaderManager().initLoader(0, null, this);
        
        mGridView.setMultiChoiceModeListener(this);
    }

    /** Override MultiChoiceModeListener start **/
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        View v = LayoutInflater.from(this).inflate(R.layout.actionbar_layout,
                null);
        mActionText = (TextView) v.findViewById(R.id.action_text);
        mActionText.setText(formatString(mGridView.getCheckedItemCount()));
        mode.setCustomView(v);
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        menu.getItem(MENU_SELECT_ALL).setEnabled(
                mGridView.getCheckedItemCount() != mGridView.getCount());
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case R.id.menu_select:
            for (int i = 0; i < mGridView.getCount(); i++) {
                mGridView.setItemChecked(i, true);
                mSelectMap.put(i, true);
            }
            break;
        case R.id.menu_unselect:
            for (int i = 0; i < mGridView.getCount(); i++) {
                mGridView.setItemChecked(i, false);
                mSelectMap.clear();
            }
            break;
        case R.id.menu_confirm:
        	
        	Intent data = new Intent();
        	Bundle bundle = new Bundle();
        	
        	String[] array = new String[mSelectMap.size()];
        	
        	Iterator iter = mSelectMap.entrySet().iterator();
        	int i = 0;
        	while ( iter.hasNext()) {
        		Map.Entry entry = (Map.Entry) iter.next();
        		int key = (Integer) entry.getKey();
        		array[i] = mUrlMap.get(key);
        		i++;
        	}
        	
        	
        	bundle.putStringArray("need_upload", array);
        	data.putExtra("needUpload", bundle);
//        	bundle.put
        	setResult(1002, data);
        	finish();
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // TODO Auto-generated method stub
        mGridCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
            long id, boolean checked) {
        // TODO Auto-generated method stub
        mActionText.setText(formatString(mGridView.getCheckedItemCount()));
        if (checked) {
        	mSelectMap.put(position, checked);
        } else {
        	mSelectMap.remove(position);
        }
//        mGridView.findViewById(position);
        mode.invalidate();
    }

    /** Override MultiChoiceModeListener end **/

    private String formatString(int count) {
        return String.format(getString(R.string.selection), count);
    }

   
    private class GridViewCursorAdapter extends SimpleCursorAdapter {

    	private AsyncImageLoader asyncImageLoader;
		public GridViewCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			// TODO Auto-generated constructor stub
			asyncImageLoader = new AsyncImageLoader(getContentResolver());
		}
    	
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
//			return mImgIds.length;
			return count;
//			return 10;
		}
		
	
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
//			Log.e("getItemId", "positon:"+ position);
			return position;
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			GridItem item;
			
			String tag = mUrlMap.get(position);
			if (convertView == null) {
				item =  new GridItem(mContext);
				
			}  else {
				item = (GridItem) convertView;
			}
			
			item.setTag(tag);
			
			Drawable cacheImage = asyncImageLoader.loadDrawable(tag);
			if (cacheImage == null) {
				cacheImage =	asyncImageLoader.loadDrawable(tag, new ImageCallback() {
				
				@Override
				public void imageLoader(Drawable imageDrawable, String imageUrl) {
					// TODO Auto-generated method stub
					GridItem view = (GridItem) mGridView.findViewWithTag(imageUrl);
					if (view != null) {
						view.setImageDrawable(imageDrawable);
					}
				}
			});
			}
				if (cacheImage == null) {
					item.setImgResId(R.drawable.ic_launcher);
					
				} else {
					item.setImageDrawable(cacheImage);
					
//					((ImageView) view).setImageResource(R.drawable.ic_launcher);
				}
				
				
			
			return item;
		}
	
		
		
    }
    
    
	

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		CursorLoader cursorLoader = new CursorLoader(
    			this, 
    			MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
    			STORE_IMAGES, 
    			null,
    			null, 
    			MediaStore.Images.ImageColumns.BUCKET_ID);
    	return cursorLoader;
	}
	
	

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO Auto-generated method stub
		mGridCursorAdapter.swapCursor(cursor);
		count = cursor.getCount();
		int position = cursor.getPosition();
		cursor.moveToFirst();
		int i = 0;
		while(!cursor.isLast()) {
			mUrlMap.put(i, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
			cursor.moveToNext();
			i ++;
		} 
		mUrlMap.put(i, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
			
//		Log.e("zx", "positon:"+ position + "=i="+i);
		cursor.moveToPosition(position);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		mGridCursorAdapter.swapCursor(null);
	}

	



}