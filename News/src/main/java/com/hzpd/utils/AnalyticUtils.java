package com.hzpd.utils;

import android.content.Context;

import com.hzpd.hflt.BuildConfig;
import com.umeng.analytics.MobclickAgent;

import org.common.lib.analytics.AnalyticEventUtils;

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
        AnalyticEventUtils.sendGaEvent(context, category, action, label, value);
    }

    /**
     * 发送谷歌分析的屏幕浏览事件
     */
    public static final void sendGaScreenViewHit(Context context, String screenName) {
        if (BuildConfig.DEBUG) {
            return;
        }
        AnalyticEventUtils.sendGaScreenViewHit(context, screenName);
    }


    /**
     * 发送友盟统计事件
     *
     * @param context 上下文
     * @param eventId 事件名称
     */
    public static final void sendUmengEvent(Context context, String eventId) {
        if (android.support.v4.BuildConfig.DEBUG) {
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
        if (android.support.v4.BuildConfig.DEBUG) {
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
        if (android.support.v4.BuildConfig.DEBUG) {
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

}
