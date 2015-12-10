package org.common.lib.analytics;

import android.app.Activity;
import android.text.TextUtils;

import com.boutique.lib.analytics.BuildConfig;
import com.boutique.lib.analytics.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.umeng.analytics.MobclickAgent;

public class ActivityLifecycleAction {

    AnalyticCallback analyticCallback;

    public ActivityLifecycleAction(AnalyticCallback callback) {
        analyticCallback = callback;
    }

    public void onCreate(Activity activity) {

        MobclickAgent.updateOnlineConfig(activity);
        MobclickAgent.openActivityDurationTrack(activity.getResources().getBoolean(R.bool.open_activity_duration_track));

        Tracker tracker = GoogleAnalyticsUtils.getInstance().getAppTracker(
                activity);
        tracker.enableAutoActivityTracking(true);

        MobclickAgent.setDebugMode(BuildConfig.DEBUG);

        // When dry run is set, hits will not be dispatched, but will still be logged as though they were dispatched.
        GoogleAnalytics.getInstance(activity).setDryRun(BuildConfig.DEBUG);

        // Stop to use google analytics
        // GoogleAnalytics.getInstance(activity).setAppOptOut(!BuildConfig.DEBUG);
    }

    public void onStart(Activity activity) {
        GoogleAnalytics.getInstance(activity).reportActivityStart(activity);
    }

    public void onResume(Activity activity) {
        MobclickAgent.onResume(activity);
        if (analyticCallback != null && !TextUtils.isEmpty(analyticCallback.getAnalyticPageName())) {
            AnalyticEventUtils.sendGaScreenViewHit(activity, analyticCallback.getAnalyticPageName());
            MobclickAgent.onPageStart(analyticCallback.getAnalyticPageName());
        }
    }

    public void onPause(Activity activity) {
        MobclickAgent.onPause(activity);
        if (analyticCallback != null && !TextUtils.isEmpty(analyticCallback.getAnalyticPageName())) {
            MobclickAgent.onPageEnd(analyticCallback.getAnalyticPageName());
        }
    }

    public void onStop(Activity activity) {
        GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
    }
}