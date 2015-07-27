package com.xyz.gridview;

import android.view.View;
import android.widget.ImageView;

public class ViewCache {
	private View baseView;
	private ImageView picView;
	private ImageView selectedView;
	
	private ImageView getPicView() {
		if(picView == null) {
			picView = (ImageView) baseView.findViewById(R.id.img_view);
		}
		return picView;
	}
}
