package com.hzpd.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.Window;
import android.view.animation.Interpolator;

import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.ZY_LeftFragment;
import com.hzpd.ui.fragments.ZY_RightFragment;
import com.shangc.slidingmenu.lib.SlidingMenu;
import com.shangc.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;

import cn.jpush.android.api.JPushInterface;

public class BaseActivity extends SlidingFragmentActivity {

	//	protected LeftFragment_zqzx mLeftFragment;
	protected ZY_LeftFragment mLeftFragment;
	protected ZY_RightFragment mRightFragment;

	protected SlidingMenu slidingMenu;
	protected FragmentManager fm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		slidingMenu = getSlidingMenu();

		fm = getSupportFragmentManager();

		setBehindContentView(R.layout.menu_left_frame);// 左边布局
		mRightFragment = new ZY_RightFragment();
		fm.beginTransaction()
				.replace(R.id.menu_left_frame, mRightFragment)
				.commit();

		// customize the SlidingMenu
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setFadeEnabled(false);//是否有渐变  
		slidingMenu.setBehindScrollScale(0.0f);
		slidingMenu.setTouchmodeMarginThreshold(30);

		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setBehindWidthRes(R.dimen.left_menu_width);
	}

	private Interpolator interp = new Interpolator() {
		@Override
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t + 1.0f;
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				toggle();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		JPushInterface.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		JPushInterface.onResume(this);
	}

}
