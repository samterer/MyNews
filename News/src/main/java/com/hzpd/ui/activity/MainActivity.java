package com.hzpd.ui.activity;


import android.content.Intent;
import android.os.Bundle;

import com.hzpd.hflt.R;
import com.hzpd.ui.App;
import com.hzpd.ui.fragments.NewsFragment;
import com.hzpd.ui.interfaces.I_ChangeFm;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.ExitApplication;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.util.LogUtils;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;


public class MainActivity extends BaseActivity implements I_ChangeFm {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_main);

		NewsFragment currentFrag = new NewsFragment();
		fm.beginTransaction()
				.add(R.id.root, currentFrag, currentFrag.getLogTag())
				.commit();

		updateApp();

		App.isStartApp = true;
		EventUtils.sendStart(this);
	}

	@Override
	public void onBackPressed() {
		if (getSlidingMenu().isMenuShowing() || getSlidingMenu().isSecondaryMenuShowing()) {
			getSlidingMenu().toggle(true);
			return;
		}

		ExitApplication.exit(this);
	}

	@Override
	public void changeFm(int position) {

	}

	public void showMenu() {
		slidingMenu.showMenu();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (2 == requestCode) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				String result = bundle.getString("result");
				LogUtils.i("result--->" + result);
				TUtils.toast(getString(R.string.toast_scan_content, result));
			}
		}
	}

	// umeng更新
	private void updateApp() {
		UmengUpdateAgent.setChannel(null);
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.setDeltaUpdate(false);
		UmengUpdateAgent.setUpdateAutoPopup(true);
		UmengUpdateAgent.setRichNotification(true);
		UmengUpdateAgent.setUpdateUIStyle(UpdateStatus.STYLE_DIALOG);
		UmengUpdateAgent.setUpdateListener(null);
		UmengUpdateAgent.setDialogListener(null);
		UmengUpdateAgent.setDownloadListener(null);
		UmengUpdateAgent.setUpdateCheckConfig(false);
		try {
			UmengUpdateAgent.update(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}