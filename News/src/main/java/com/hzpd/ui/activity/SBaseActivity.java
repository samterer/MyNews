package com.hzpd.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.hzpd.utils.AAnim;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.HttpUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SBaseActivity extends SwipeBackActivity {

	protected ImageLoader mImageLoader;

	protected HttpUtils httpUtils;
	protected SPUtil spu;//

	protected long startMills;
	protected Map<String, String> analyMap;
	protected DBHelper dbHelper;
	protected Activity activity;

	protected FragmentManager fm;
	protected Fragment currentFm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = this;
		fm = getSupportFragmentManager();

		httpUtils = SPUtil.getHttpUtils();
		spu = SPUtil.getInstance();
		mImageLoader = ImageLoader.getInstance();
		startMills = System.currentTimeMillis();
		analyMap = new HashMap<String, String>();
		dbHelper = DBHelper.getInstance(getApplicationContext());


	}

	@Override
	protected void onResume() {
		super.onResume();
		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		JPushInterface.onPause(this);
	}

	@Override
	public void finish() {
		ImageLoader.getInstance().stop();
		super.finish();
		AAnim.ActivityFinish(this);
	}

	public String getLogTag() {
		return getClass().getSimpleName();
	}
}
