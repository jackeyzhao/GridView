package com.xyz.gridview;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
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
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
    private GridAdapter mGridAdapter;
    private TextView mActionText;
    private static final int MENU_SELECT_ALL = 0;
    private static final int MENU_UNSELECT_ALL = MENU_SELECT_ALL + 1;
    private Map<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
    private int count = 0;

    private static final String[] STORE_IMAGES = {
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.LATITUDE,
        MediaStore.Images.Media.LONGITUDE,
        MediaStore.Images.Media._ID,
        
	};
    private int[] mImgIds = new int[] { R.drawable.img_1, R.drawable.img_2,
            R.drawable.img_3, R.drawable.img_4, R.drawable.img_5,
            R.drawable.img_6, R.drawable.img_7, R.drawable.img_8,
            R.drawable.img_9, R.drawable.img_1, R.drawable.img_2,
            R.drawable.img_3, R.drawable.img_4, R.drawable.img_5,
            R.drawable.img_6, R.drawable.img_7 };
	private GridViewCursorAdapter mGridCursorAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGridAdapter = new GridAdapter(this);
        mGridCursorAdapter = new GridViewCursorAdapter(this,
        		R.layout.grid_item,
        		null,
        		STORE_IMAGES,
        		new int[] {R.id.img_view, R.id.select},
        		0);
        mGridCursorAdapter.setViewBinder(new ImageLocationBinder());
        
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
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // TODO Auto-generated method stub
        mGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
            long id, boolean checked) {
        // TODO Auto-generated method stub
        mActionText.setText(formatString(mGridView.getCheckedItemCount()));
        mSelectMap.put(position, checked);
        mode.invalidate();
    }

    /** Override MultiChoiceModeListener end **/

    private String formatString(int count) {
        return String.format(getString(R.string.selection), count);
    }

    private class GridAdapter extends BaseAdapter {

        private Context mContext;

        public GridAdapter(Context ctx) {
            mContext = ctx;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mImgIds.length;
        }

        @Override
        public Integer getItem(int position) {
            // TODO Auto-generated method stub
            return Integer.valueOf(mImgIds[position]);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            GridItem item;
            if (convertView == null) {
                item = new GridItem(mContext);
                item.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                        LayoutParams.FILL_PARENT));
            } else {
                item = (GridItem) convertView;
            }
            item.setImgResId(getItem(position));
            item.setChecked(mSelectMap.get(position) == null ? false
                    : mSelectMap.get(position));
            return item;
        }
    }

    private class GridViewCursorAdapter extends SimpleCursorAdapter {

		public GridViewCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			// TODO Auto-generated constructor stub
		}
    	
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
//			return mImgIds.length;
			return count;
		}
		
		@Override
		public Integer getItem(int position) {
			// TODO Auto-generated method stub
			return Integer.valueOf(mImgIds[position]);
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		@Override
		public Cursor getCursor() {
			// TODO Auto-generated method stub
			return super.getCursor();
		}
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			// TODO Auto-generated method stub
//			GridItem item;
//            if (convertView == null) {
//                item = new GridItem(mContext);
//                item.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
//                        LayoutParams.FILL_PARENT));
//            } else {
//                item = (GridItem) convertView;
//            }
//            item.setImgResId(getItem(position));
//            item.setChecked(mSelectMap.get(position) == null ? false
//                    : mSelectMap.get(position));
//            return item;
//		}
		@Override
		public ViewBinder getViewBinder() {
			// TODO Auto-generated method stub
			return super.getViewBinder();
		}@Override
		public void bindView(View arg0, Context arg1, Cursor arg2) {
			// TODO Auto-generated method stub
			super.bindView(arg0, arg1, arg2);
		}
		
		
    }
    
    
	
	 // 将图片的位置绑定到视图
    private class ImageLocationBinder implements ViewBinder{ 
    	@Override
    	public boolean setViewValue(View view, Cursor cursor, int arg2) {
    		// TODO Auto-generated method stub
    			String zx = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
    			Log.d("zx", zx);
    		if (arg2 == 0) {	
    			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().
  					  appendPath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID))).build();
  			FileUtil file = new FileUtil();
  			ContentResolver resolver = getContentResolver();
  			// 从Uri中读取图片资源
  			try {
  				byte [] mContent = null;
  				mContent = file.readInputStream(resolver.openInputStream(Uri.parse(uri.toString())));
  				Bitmap bitmap = null;
  				Options opt = new BitmapFactory.Options();
  				opt.inJustDecodeBounds = true;
  				BitmapFactory.decodeByteArray(mContent, 0, mContent.length, opt);
  				
  				opt.inSampleSize = computeInitialSampleSize(opt, -1, 128 * 128);
  				opt.inJustDecodeBounds = false;
  				bitmap = file.getBitmapFromBytes(mContent, opt );
  				((ImageView) view).setImageBitmap(bitmap);
  			} catch (Exception e) {
  				// TODO: handle exception
  				e.printStackTrace();
  			}
    			
    			
    			
    			
                return true;
    			}
            
    		return true;
    	}
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

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		CursorLoader cursorLoader = new CursorLoader(
    			this, 
    			MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
    			STORE_IMAGES, 
    			null, 
    			null, 
    			null);
    	return cursorLoader;
	}
	
	

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO Auto-generated method stub
		mGridCursorAdapter.swapCursor(cursor);
		count = cursor.getCount();
		
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		mGridCursorAdapter.swapCursor(null);
	}

	



}