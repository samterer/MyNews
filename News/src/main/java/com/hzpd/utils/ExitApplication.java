/**
 *
 */

package com.hzpd.utils;

import android.app.Activity;
import android.content.Intent;

import com.hzpd.services.InitService;
import com.umeng.analytics.MobclickAgent;


public class ExitApplication {

	public static long beforeTime = 0;


	public static void exit(Activity activity) {
		VibUtil.Vibrate(activity, 60);

		long currentTime = System.currentTimeMillis();
		if (currentTime - beforeTime < 2500) {
			MobclickAgent.onKillProcess(activity);
			Intent service = new Intent(activity, InitService.class);
			activity.stopService(service);
			activity.finish();
		} else {
			beforeTime = currentTime;
			TUtils.toast("再按一次退出程序");
		}
	}


}