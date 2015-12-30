package com.hzpd.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.hzpd.ui.App;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.HttpHandler;

import org.common.lib.analytics.ActivityLifecycleAction;
import org.common.lib.analytics.AnalyticCallback;

import java.util.ArrayList;
import java.util.List;

public class MWBaseActivity extends FragmentActivity implements AnalyticCallback {
    public MWBaseActivity() {
        Log.e("test", "MWBaseActivity new ");
    }

    private ActivityLifecycleAction action = new ActivityLifecycleAction(this);
    protected HttpUtils httpUtils;
    protected SPUtil spu;//
    protected DBHelper dbHelper;
    protected Activity activity;
    boolean isResume = false;
    List<HttpHandler> handlerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        action.onCreate(this);
        activity = this;
        httpUtils = SPUtil.getHttpUtils();
        spu = SPUtil.getInstance();
        dbHelper = DBHelper.getInstance(getApplicationContext());
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
        for (HttpHandler httpHandler : handlerList) {
            if (httpHandler.getState() == HttpHandler.State.LOADING || httpHandler.getState() == HttpHandler.State.STARTED) {
                httpHandler.setRequestCallBack(null);
                httpHandler.cancel();
            }
        }
        handlerList.clear();
        httpUtils = null;
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