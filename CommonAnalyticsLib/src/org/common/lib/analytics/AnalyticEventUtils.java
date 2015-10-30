package org.common.lib.analytics;

import android.content.Context;
import android.support.v4.BuildConfig;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.umeng.analytics.MobclickAgent;

import java.util.Map;

/**
 * 统计分析工具类
 * Created by liuqing on 2015/4/16.
 */
public class AnalyticEventUtils {
	/**
	 * 发送谷歌统计事件
	 *
	 * @param context  上下文
	 * @param category 事件目录
	 * @param action   事件动作
	 * @param label    事件标签
	 * @param value    事件的值
	 */
	public static final void sendGaEvent(Context context, String category, String action, String label, Long value) {
		if (BuildConfig.DEBUG) {
			return;
		}
		HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
		if (!TextUtils.isEmpty(category)) {
			eventBuilder.setCategory(category);
		}
		if (!TextUtils.isEmpty(action)) {
			eventBuilder.setAction(action);
		}
		if (!TextUtils.isEmpty(label)) {
			eventBuilder.setLabel(label);
		}
		if (value != null) {
			eventBuilder.setValue(value);
		}
		Tracker tracker = GoogleAnalyticsUtils.getInstance().getAppTracker(context);
		if (tracker != null) {
			tracker.send(eventBuilder.build());
		}
	}

	/**
	 * 发送谷歌分析的屏幕浏览事件
	 */
	public static final void sendGaScreenViewHit(Context context, String screenName) {
		if (BuildConfig.DEBUG) {
			return;
		}
		if (TextUtils.isEmpty(screenName)) {
			return;
		}
		// 谷歌分析
		Tracker tracker = GoogleAnalyticsUtils.getInstance().getAppTracker(context);
		if (tracker != null) {
			tracker.setScreenName(screenName);
			tracker.send(new HitBuilders.ScreenViewBuilder().build());
			GoogleAnalytics.getInstance(context).dispatchLocalHits();
			tracker.setScreenName(null);
		}
	}


	/**
	 * 发送友盟统计事件
	 *
	 * @param context 上下文
	 * @param eventId 事件名称
	 */
	public static final void sendUmengEvent(Context context, String eventId) {
		if (BuildConfig.DEBUG) {
			return;
		}
		MobclickAgent.onEvent(context, eventId);
	}


	/**
	 * 发送友盟统计事件
	 *
	 * @param context 上下文
	 * @param eventId 事件名称
	 * @param param   事件参数
	 */
	public static final void sendUmengEvent(Context context, String eventId, String param) {
		if (BuildConfig.DEBUG) {
			return;
		}
		MobclickAgent.onEvent(context, eventId, param);
	}

	/**
	 * 发送友盟统计事件
	 *
	 * @param context  上下文
	 * @param eventId  事件名称
	 * @param paramMap 当前事件的属性和取值
	 */
	public static final void sendUmengEvent(Context context, String eventId, Map<String, String> paramMap) {
		if (BuildConfig.DEBUG) {
			return;
		}
		MobclickAgent.onEvent(context, eventId, paramMap);
	}
}