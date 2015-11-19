package com.hzpd.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.hzpd.ui.App;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.HttpUtils;

import org.common.lib.analytics.ActivityLifecycleAction;
import org.common.lib.analytics.AnalyticCallback;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

public class MWBaseActivity extends FragmentActivity implements AnalyticCallback {

    private ActivityLifecycleAction action = new ActivityLifecycleAction(this);
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
//        setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        activity = this;
        fm = getSupportFragmentManager();

        httpUtils = SPUtil.getHttpUtils();
        spu = SPUtil.getInstance();
        startMills = System.currentTimeMillis();
        analyMap = new HashMap<String, String>();
        dbHelper = DBHelper.getInstance(getApplicationContext());
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
    protected void onDestroy() {
        super.onDestroy();
        App.getInstance().getRefWatcher().watch(this);
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