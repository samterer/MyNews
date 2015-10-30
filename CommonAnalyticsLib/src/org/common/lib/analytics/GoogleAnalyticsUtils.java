package org.common.lib.analytics;

import android.content.Context;

import com.boutique.lib.analytics.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class GoogleAnalyticsUtils {

	public enum TrackerName {
		APP_TRACKER
	}

	private HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	private static GoogleAnalyticsUtils sAnalyticUtils;

	public static GoogleAnalyticsUtils getInstance() {
		if (sAnalyticUtils == null) {
			sAnalyticUtils = new GoogleAnalyticsUtils();
		}
		return sAnalyticUtils;
	}

	private GoogleAnalyticsUtils() {

	}

	public synchronized Tracker getTracker(Context context,
			TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
			analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
			if (trackerId == TrackerName.APP_TRACKER) {
				Tracker t = analytics.newTracker(R.xml.ga_app_tracker);
				t.enableAdvertisingIdCollection(true);
				mTrackers.put(trackerId, t);
			}
		}
		return mTrackers.get(trackerId);
	}

	public synchronized Tracker getAppTracker(Context context) {
		return getTracker(context, TrackerName.APP_TRACKER);
	}
}