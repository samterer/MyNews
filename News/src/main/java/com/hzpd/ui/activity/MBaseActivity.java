package com.hzpd.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import com.hzpd.custorm.SwipeBackLayout;
import com.hzpd.hflt.R;
import com.hzpd.ui.App;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.SystemBarTintManager;
import com.lidroid.xutils.HttpUtils;

import org.common.lib.analytics.ActivityLifecycleAction;
import org.common.lib.analytics.AnalyticCallback;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

public class MBaseActivity extends FragmentActivity implements AnalyticCallback {

    private ActivityLifecycleAction action = new ActivityLifecycleAction(this);

    protected SwipeBackLayout layout;

    protected HttpUtils httpUtils;
    protected SPUtil spu;//

    protected long startMills;
    protected Map<String, String> analyMap;
    protected DBHelper dbHelper;
    protected Activity activity;

    protected FragmentManager fm;
    protected Fragment currentFm;


    boolean isResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
//        initSystemBar();
        action.onCreate(this);
        if (App.getInstance().getThemeName().equals("1")) {
            setTheme(R.style.ThemeNight);
        } else {
            setTheme(R.style.ThemeDefault);
        }
        setTheme(android.R.style.Theme_Translucent_NoTitleBar);//不能删


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setTranslucentStatus(true);
//            //透明状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //透明导航栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }
//
//
//        SystemBarTintManager tintManager = new SystemBarTintManager(this);
//        tintManager.setStatusBarTintEnabled(true);
//        TypedValue typedValue = new TypedValue();
//        getTheme().resolveAttribute(R.attr.title_bar_color, typedValue, true);
//        int color = typedValue.data;
//        tintManager.setStatusBarTintColor(color);
////        tintManager.setStatusBarTintColor(R.color.toolbar_bg);

        layout = (SwipeBackLayout) LayoutInflater.from(this).inflate(
                R.layout.base, null);
        layout.attachToActivity(this);
        activity = this;
        fm = getSupportFragmentManager();

        httpUtils = SPUtil.getHttpUtils();
        spu = SPUtil.getInstance();
        startMills = System.currentTimeMillis();
        analyMap = new HashMap<String, String>();
        dbHelper = DBHelper.getInstance(getApplicationContext());
    }

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
    protected void onDestroy() {
        super.onDestroy();
        App.getInstance().getRefWatcher().watch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
        action.onResume(this);
        JPushInterface.onResume(this);
    }

    @Override
    protected void onStart() {
        isResume = true;
        super.onStart();
        action.onStart(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        isResume = false;
        action.onStop(this);
    }

    @Override
    protected void onPause() {
        isResume = false;
        super.onPause();
        action.onPause(this);
        JPushInterface.onPause(this);
    }

    @Override
    public void finish() {
        super.finish();
        AAnim.ActivityFinish(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        return;
    }

    public String getLogTag() {
        return getClass().getSimpleName();
    }

    @Override
    public String getAnalyticPageName() {
        return getClass().getSimpleName();
    }
}