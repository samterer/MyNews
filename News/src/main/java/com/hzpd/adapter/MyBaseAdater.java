package com.hzpd.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.hzpd.utils.DBHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class MyBaseAdater extends BaseAdapter {

	protected Activity mContext;
	protected LayoutInflater mLayoutInflater;
	protected ImageLoader mImageLoader;
	protected DBHelper dbHelper;


	public MyBaseAdater(Activity c) {
		mContext = c;
		mImageLoader = ImageLoader.getInstance();
		mLayoutInflater = LayoutInflater.from(c);
		dbHelper = DBHelper.getInstance();
	}

}
