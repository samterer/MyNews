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
		mLeftFragment = new ZY_LeftFragment();
		fm.beginTransaction()
				.replace(R.id.menu_left_frame, mLeftFragment)
				.commit();

		// customize the SlidingMenu
		slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setFadeEnabled(false);//是否有渐变  
		slidingMenu.setBehindScrollScale(0.0f);
		slidingMenu.setTouchmodeMarginThreshold(30);

		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setBehindWidthRes(R.dimen.left_menu_width);

		slidingMenu.setSecondaryMenu(R.layout.menu_frame_two);// 右边布局
		slidingMenu.setSecondaryShadowDrawable(R.drawable.shadowright);
		slidingMenu.setRightBehindWidthRes(R.dimen.right_menu_width);


		mRightFragment = new ZY_RightFragment();
		fm.beginTransaction()
				.replace(R.id.menu_frame_two, mRightFragment).commit();

//		GlobalUtils.mSlidingMenu
//				.setBehindCanvasTransformer(new CanvasTransformer(){
//					@Override
//					public void transformCanvas(Canvas canvas, float percentOpen){
////						canvas.scale(percentOpen, 1, 0, 0);
//						 float scale = (float) (percentOpen * 0.25 + 0.75);
//						 canvas.scale(scale, scale, canvas.getWidth() / 2,
//						 canvas.getHeight() / 2);
//						 canvas.translate(
//						 0,
//						 canvas.getHeight()
//						 * (1 - interp.getInterpolation(percentOpen)));
//					}
//				});
		// getSupportActionBar().setDisplayHomeAsUpEnabled(false);

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
