package com.hzpd.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.hzpd.utils.ACache;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.HttpUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;

public class BaseFragment extends Fragment {

	protected ImageLoader mImageLoader;
	protected HttpUtils httpUtils;
	protected SPUtil spu;//
	protected ACache aCache;
	protected FragmentManager fm;
	protected DBHelper dbHelper;
	protected Activity activity;
	protected String title;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = getActivity();
		fm = getChildFragmentManager();
		httpUtils = new HttpUtils();
		spu = SPUtil.getInstance();
		mImageLoader = ImageLoader.getInstance();
		dbHelper = DBHelper.getInstance(activity);
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getName()); //统计页面
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getName());
	}

	@Override
	public void onDetach() {
		super.onDetach();
		try {
			Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getLogTag() {
		return getClass().getSimpleName();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
