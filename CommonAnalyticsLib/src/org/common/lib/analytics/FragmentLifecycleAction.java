package org.common.lib.analytics;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.umeng.analytics.MobclickAgent;

public class FragmentLifecycleAction {

    AnalyticCallback analyticCallback;

    public FragmentLifecycleAction(AnalyticCallback callback) {
        analyticCallback = callback;
    }

    public void onCreate(Fragment fragment) {
    }

    public void onStart(Fragment fragment) {
    }

    public void onResume(Fragment fragment) {
        if (analyticCallback != null && !TextUtils.isEmpty(analyticCallback.getAnalyticPageName())) {
            MobclickAgent.onPageStart(analyticCallback.getAnalyticPageName());
        }
    }

    public void onPause(Fragment fragment) {
        if (analyticCallback != null && !TextUtils.isEmpty(analyticCallback.getAnalyticPageName())) {
            MobclickAgent.onPageEnd(analyticCallback.getAnalyticPageName());
        }
    }

    public void onStop(Fragment fragment) {
    }

    public void onDestroy(Fragment fragment) {
    }
}