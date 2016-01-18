package com.hzpd.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.hzpd.ui.App;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;

import org.common.lib.analytics.ActivityLifecycleAction;
import org.common.lib.analytics.AnalyticCallback;

public class MWBaseActivity extends FragmentActivity implements AnalyticCallback {
    public MWBaseActivity() {
        Log.e("test", "MWBaseActivity new ");
    }

    private ActivityLifecycleAction action = new ActivityLifecycleAction(this);
    protected SPUtil spu;//
    protected DBHelper dbHelper;
    protected Activity activity;
    boolean isResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        action.onCreate(this);
        activity = this;
        spu = SPUtil.getInstance();
        dbHelper = DBHelper.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
        action.onResume(this);
    }

    @Override
    protected void onStart() {
        isResume = true;
        super.onStart();
        action.onStart(this);
    }


    @Override
    protected void onStop() {
        Log.e("exit", "onStop");
        super.onStop();
        isResume = false;
        action.onStop(this);
    }

    @Override
    protected void onPause() {
        isResume = false;
        super.onPause();
        action.onPause(this);
    }

    @Override
    public void finish() {
        super.finish();
        AAnim.ActivityFinish(this);
    }

    @Override
    protected void onDestroy() {
        activity = null;
        spu = null;
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