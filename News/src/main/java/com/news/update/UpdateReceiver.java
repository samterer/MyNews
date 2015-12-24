package com.news.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.hzpd.utils.Log;
import com.joy.lmt.LMTInvoker;

/**
 * listen boot and network
 */
public class UpdateReceiver extends BroadcastReceiver {
	final static String ACTION_SAFE = "com.lmt.alarm.SafeTime";

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (!UpdateUtils.isRomVersion(context)) {
			return;
		}
		String action = intent.getAction();
		Log.e("test", action);
		switch (action) {
			case ConnectivityManager.CONNECTIVITY_ACTION:
				checkNetwork(context);
				break;
			case Intent.ACTION_BOOT_COMPLETED:
				UpdateUtils.setUpdateAlarm(context);
				break;
			case UpdateUtils.ACTION_UPDATE_ALARM:
				UpdateService.resetDate(context);
				new Thread(new Runnable() {
					@Override
					public void run() {
						LMTInvoker invoker = new LMTInvoker(context, "GameLink");
						invoker.BindLMT();
						if (invoker.isNetConnected(false)) {
							Intent service = new Intent(context.getApplicationContext(), UpdateService.class);
							context.getApplicationContext().startService(service);
					}
					}
				}).start();
				break;

			case ACTION_SAFE:
				UpdateService.resetDate(context);
				new Thread(new Runnable() {
					@Override
					public void run() {
						LMTInvoker invoker = new LMTInvoker(context, "GameLink");
						invoker.BindLMT();
						if (invoker.isNetConnected(false)) {
							Intent service = new Intent(context.getApplicationContext(), UpdateService.class);
							context.getApplicationContext().startService(service);
						}
					}
				}).start();
				break;
		}

	}

	private void checkNetwork(Context context) {
		NetworkInfo networkInfo = UpdateUtils.getNetworkInfo(context);
		if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
			Intent intent = new Intent(context, UpdateService.class);
			context.startService(intent);
		}
	}
}