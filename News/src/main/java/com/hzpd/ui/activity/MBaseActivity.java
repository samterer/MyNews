package com.hzpd.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;

import com.hzpd.custorm.SwipeBackLayout;
import com.hzpd.hflt.R;
import com.hzpd.ui.App;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
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
        setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        layout = (SwipeBackLayout) LayoutInflater.from(this).inflate(
                R.layout.base, null);
        layout.attachToActivity(this);
        activity = this;
        fm = getSupportFragmentManager();

        httpUtils = new HttpUtils();
        httpUtils.configSoTimeout(5000);
        httpUtils.configTimeout(1000);
        spu = SPUtil.getInstance();
        startMills = System.currentTimeMillis();
        analyMap = new HashMap<String, String>();
        dbHelper = DBHelper.getInstance(this);
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