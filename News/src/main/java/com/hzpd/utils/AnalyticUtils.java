package com.hzpd.utils;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hzpd.hflt.BuildConfig;
import com.umeng.analytics.MobclickAgent;

import org.common.lib.analytics.GoogleAnalyticsUtils;

import java.util.Map;

/**
 * 统计分析工具类
 * Created by liuqing on 2014/12/23.
 */
public class AnalyticUtils {

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
     * 发送谷歌分析的屏幕浏览事件,每个频道，每条新闻都是一个屏幕
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
//            tracker.send(new HitBuilders.ScreenViewBuilder().setCustomDimension(1, dimensiion).build());
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
        //MobclickAgent.onEvent(context, eventId);
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
        //MobclickAgent.onEvent(context, eventId, param);
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

    // 事件分类
    public interface CATEGORY {
        String welecome = "欢迎页";
        String slidMenu = "侧边栏";
        String newsType = "新闻频道页";
        String newsDetail = "新闻详情页";
        String search = "搜索页";
        //

    }

    // 事件名称
    public interface ACTION {
        String viewPage = "浏览页面";
        String loginIn = "登录";
        String loginOut = "登出";
        String newsItem = "点击新闻item";
    }

    // 屏幕分类
    public interface SCREEN {
        String newsType = "频道";
        String newsDetail = "详情";
        String search = "搜索";
        String welcome = "欢迎";
        String setting = "设置";
        String leftMenu = "侧边栏";
        String edit = "编辑栏目";
        String feedback = "反馈";
        String comment = "评论";
        String info = "用户详情";
        String myComment = "我的评论";
    }

}
