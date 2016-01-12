package com.hzpd.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.db.PushBeanDB;
import com.hzpd.modle.db.PushBeanDBDao;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * <p/>
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = "JPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }
        Log.e(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", 信息extras: " + printBundle(bundle));
        String title = "";
        String extra = "";
        for (String key : bundle.keySet()) {
            if (key.equals("cn.jpush.android.ALERT")) {
                title = bundle.getString(key);
                continue;
            }
            if (key.equals("cn.jpush.android.EXTRA")) {
                extra = bundle.getString(key);
            }
        }
        JSONObject object = null;
        String type = null;
        Log.e(TAG, "JpushReceiver：extra-->" + extra);
        object = FjsonUtil.parseObject(extra);
        if (null == object) {
            return;
        }
        type = object.getString("atype");
        Log.e(TAG, "[MyReceiver] 用户点击打开了通知 " + type);
        if (TextUtils.isEmpty(type)) {
            return;
        }
        NewsBean newsBean = FjsonUtil.parseObject(object.getString("data"), NewsBean.class);
        Log.i(TAG, "MyReceiver" + newsBean.getNid());
        PushBeanDB unique = DBHelper.getInstance(context).getPushList().queryBuilder().where(PushBeanDBDao.Properties.Nid.eq(newsBean.getNid())).build().unique();
        if (unique==null){
            DBHelper.getInstance(context).getPushList().insert(new PushBeanDB(newsBean));
        }
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.e(TAG, "[MyReceiver] 接收Registration Id : " + regId);
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.e(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.e(TAG, "[MyReceiver] 接收到推送下来的通知");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.e(TAG, "[MyReceiver] 用户点击打开了通知");
            Log.e(TAG, "[MyReceiver] 用户点击打开了通知 " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            Intent myintent = null;
            if (type.equals("1")) {
                myintent = new Intent(context, NewsDetailActivity.class);
                myintent.putExtra("newbean", newsBean);
                myintent.putExtra("from", "push");
            }
            if (myintent != null) {
                myintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.e(TAG, "通知跳转到详情页面成功");
                context.startActivity(myintent);
            }
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.e(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Log.w(TAG, "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
        } else {
            Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }
}