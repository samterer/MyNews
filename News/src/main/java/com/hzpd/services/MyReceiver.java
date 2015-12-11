package com.hzpd.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.db.PushListDbTask;
import com.lidroid.xutils.util.LogUtils;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";

	@Override
	public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
		Log.e(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", 信息extras: " + printBundle(bundle));
		
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.e(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...
                        
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.e(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
//        	processCustomMessage(context, bundle);
        
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.e(TAG, "[MyReceiver] 接收到推送下来的通知");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
        	
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.e(TAG, "[MyReceiver] 用户点击打开了通知");
			Log.e(TAG, "[MyReceiver] 用户点击打开了通知 " + bundle.getString(JPushInterface.EXTRA_EXTRA));



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

			LogUtils.e("JpushReceiver：extra-->" + extra);
			object = FjsonUtil.parseObject(extra);

			if (null == object) {
				return;
			}

			type = object.getString("atype");

			Log.e(TAG, "[MyReceiver] 用户点击打开了通知 " + type);
			if (TextUtils.isEmpty(type)) {
				return;
			}
			Intent myintent = null;

			if (type.equals("1")) {
				myintent = new Intent(context, NewsDetailActivity.class);
				NewsBean nb = new NewsBean();
				JSONObject nbobj = object.getJSONObject("data");
				Log.i("MyReceiver","MyReceiver--->"+nbobj.toString());

				nb.setNid(nbobj.getString("nid"));
				nb.setTitle(title);
				nb.setJson_url(nbobj.getString("json_url"));
				nb.setType(nbobj.getString("type"));
				nb.setTid(nbobj.getString("tid"));


				new PushListDbTask(context).saveList(nb, new I_Result() {
					@Override
					public void setResult(Boolean flag) {
						Log.i("","PushDB"+flag);
					}
				});

//				NewsBeanDB

				myintent.putExtra("newbean", nb);
				myintent.putExtra("from", "push");
			}
			if (myintent != null) {
				myintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				LogUtils.e("通知跳转到详情页面成功");
				context.startActivity(myintent);
			}

//        	//打开自定义的Activity
//        	Intent i = new Intent(context, SettingActivity.class);
//        	i.putExtras(bundle);
//        	//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
//        	context.startActivity(i);
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.e(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        	
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	Log.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
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
			}else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} 
			else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
	
	//send msg to MainActivity
//	private void processCustomMessage(Context context, Bundle bundle) {
//		if (MainActivity.isForeground) {
//			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
//			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
//			Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
//			msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
//			if (!ExampleUtil.isEmpty(extras)) {
//				try {
//					JSONObject extraJson = new JSONObject(extras);
//					if (null != extraJson && extraJson.length() > 0) {
//						msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
//					}
//				} catch (JSONException e) {
//
//				}
//
//			}
//			context.sendBroadcast(msgIntent);
//		}
//	}
}
