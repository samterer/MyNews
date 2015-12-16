package com.hzpd.utils;

import android.content.Context;

import com.hzpd.modle.event.ScoreEvent;
import com.hzpd.modle.event.ScoreEvents;
import com.hzpd.url.InterfaceJsonfile;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;

public class EventUtils {

    public static void sendStart(final Context context) {
        if (null == SPUtil.getInstance().getUser()) {
            return;
        }
        ScoreEvent event = new ScoreEvent("3");
        ScoreEvents events = new ScoreEvents(SPUtil.getInstance().getUser().getUid());
        events.addEvent(event);
        HttpUtils httpUtils = SPUtil.getHttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("jsonstr", FjsonUtil.toJsonString(events));

        httpUtils.send(HttpMethod.POST
                ,  InterfaceJsonfile.XF_UPLOADEVENT
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i("sendStart-->" + responseInfo.result);
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

    public static void sendReadAtical(final Context context) {
        //TODO 关闭
        return;
    }

    public static void sendShareAtival(final Context context) {
        if (null == SPUtil.getInstance().getUser()) {
            return;
        }

        ScoreEvent event = new ScoreEvent("5");
        ScoreEvents events = new ScoreEvents(SPUtil.getInstance().getUser().getUid());
        events.addEvent(event);
        HttpUtils httpUtils = SPUtil.getHttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("jsonstr", FjsonUtil.toJsonString(events));

        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.XF_UPLOADEVENT
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i("sendShareAtival-->" + responseInfo.result);
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

    public static void sendComment(final Context context) {
        if (null == SPUtil.getInstance().getUser()) {
            return;
        }
        if (null == SPUtil.getInstance().getUser()) {
            return;
        }
        ScoreEvent event = new ScoreEvent("6");
        ScoreEvents events = new ScoreEvents(SPUtil.getInstance().getUser().getUid());
        events.addEvent(event);
        HttpUtils httpUtils = SPUtil.getHttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("jsonstr", FjsonUtil.toJsonString(events));

        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.XF_UPLOADEVENT
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i("sendComment-->" + responseInfo.result);
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

    public static void sendPraise(final Context context) {
        if (null == SPUtil.getInstance().getUser()) {
            return;
        }

        ScoreEvent event = new ScoreEvent("7");
        ScoreEvents events = new ScoreEvents(SPUtil.getInstance().getUser().getUid());
        events.addEvent(event);
        HttpUtils httpUtils = SPUtil.getHttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("jsonstr", FjsonUtil.toJsonString(events));

        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.XF_UPLOADEVENT
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i("sendPraise-->" + responseInfo.result);
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

}
