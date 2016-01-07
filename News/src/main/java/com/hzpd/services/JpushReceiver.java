package com.hzpd.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.color.tools.mytools.LogUtils;
import com.hzpd.modle.NewsBean;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.utils.FjsonUtil;

import cn.jpush.android.api.JPushInterface;

/**
 * jpush接收器
 */
public class JpushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		String action = intent.getAction();

		if (bundle == null) {
			return;
		}

		LogUtils.i("JpushReceiver：onReceive - " + intent.getAction() + ", extras: "
				+ printBundle(bundle));

		if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(action)) {

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
			String id = null;

			LogUtils.i("JpushReceiver：extra-->" + extra);
			object = FjsonUtil.parseObject(extra);

			if (null == object) {
				return;
			}

			type = object.getString("atype");
			if (TextUtils.isEmpty(type)) {
				return;
			}
			Intent myintent = null;

			if (type.equals("1")) {
				myintent = new Intent(context, NewsDetailActivity.class);
				NewsBean nb = new NewsBean();

				JSONObject nbobj = object.getJSONObject("data");
				nb.setNid(nbobj.getString("nid"));
				nb.setTitle(title);

				nb.setJson_url(nbobj.getString("json_url"));
				nb.setType(nbobj.getString("type"));
				nb.setTid(nbobj.getString("tid"));
				myintent.putExtra("newbean", nb);
				myintent.putExtra("from", "push");
			}

			if (myintent != null) {
				myintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				LogUtils.e("通知跳转");
				context.startActivity(myintent);
			}
		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(action)) {
//			printBundle(bundle);
		}

	}

	// 打印所有的 intent extra 数据
	private String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		String datd = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		for (String key : bundle.keySet()) {
			String data = bundle.getString(key);
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));

			} else {
				if ("cn.jpush.android.NOTIFICATION_ID".equals(key)) {
					sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
				} else {
					sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
				}
			}
		}
		return sb.toString();
	}


}
