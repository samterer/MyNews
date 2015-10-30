package org.common.lib.analytics;

import android.app.Activity;
import android.text.TextUtils;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.InitializationException;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.MobileAnalyticsManager;
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

	private static MobileAnalyticsManager analytics;

	public void onCreate(Activity activity) {
		try {
			analytics = MobileAnalyticsManager.getOrCreateInstance(
					activity.getApplicationContext(),
					"e4adec8e5d8f472f96b03fa459ee69e9", //Amazon Mobile Analytics App ID
					"us-east-1:f8ad36dd-e3c8-4c48-a9b2-10e67ba092c7" //Amazon Cognito Identity Pool ID
			);
		} catch(InitializationException ex) {
		}

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
		if(analytics != null) {
			analytics.getSessionClient().resumeSession();
		}
		MobclickAgent.onResume(activity);
		if (analyticCallback != null && !TextUtils.isEmpty(analyticCallback.getAnalyticPageName())) {
			MobclickAgent.onPageStart(analyticCallback.getAnalyticPageName());
		}
	}

	public void onPause(Activity activity) {
		if(analytics != null) {
			analytics.getSessionClient().pauseSession();
			analytics.getEventClient().submitEvents();
		}
		MobclickAgent.onPause(activity);
		if (analyticCallback != null && !TextUtils.isEmpty(analyticCallback.getAnalyticPageName())) {
			MobclickAgent.onPageEnd(analyticCallback.getAnalyticPageName());
		}
	}

	public void onStop(Activity activity) {
		GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
	}
}