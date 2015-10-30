package com.hzpd.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;

import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.ZY_RightFragment;
import com.hzpd.utils.SystemBarTintManager;
import com.shangc.slidingmenu.lib.SlidingMenu;
import com.shangc.slidingmenu.lib.app.SlidingFragmentActivity;

import org.common.lib.analytics.ActivityLifecycleAction;
import org.common.lib.analytics.AnalyticCallback;

public class BaseActivity extends SlidingFragmentActivity implements AnalyticCallback {

    private ActivityLifecycleAction action = new ActivityLifecycleAction(this);

    //	protected LeftFragment_zqzx mLeftFragment;
//	protected ZY_LeftFragment mLeftFragment;
    protected ZY_RightFragment mRightFragment;

    protected SlidingMenu slidingMenu;
    protected FragmentManager fm;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        action.onCreate(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.toolbar_bg);

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
        slidingMenu.setBehindScrollScale(1.0f);
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

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

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
        super.onPause();
        action.onPause(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        action.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        action.onStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        action.onResume(this);
    }

    @Override
    public String getAnalyticPageName() {
        return getClass().getSimpleName();
    }
}