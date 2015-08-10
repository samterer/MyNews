package com.hzpd.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.ui.App;
import com.hzpd.ui.fragments.MySearchFragment;
import com.hzpd.ui.fragments.NewsAlbumFragment;
import com.hzpd.ui.fragments.NewsFragment;
import com.hzpd.ui.fragments.VideoListFragment;
import com.hzpd.ui.fragments.WebviewFragment;
import com.hzpd.ui.fragments.ZhuantiFragment;
import com.hzpd.ui.fragments.action.ActionListFragment;
import com.hzpd.ui.interfaces.I_ChangeFm;
import com.hzpd.utils.CODE;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.ExitApplication;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;


public class MainActivity extends BaseActivity implements I_ChangeFm {
	public static final int REQUEST_IMAGE = 2;

	private int itemSelectPositon = 0;
	private Fragment currentFrag;

	@ViewInject(R.id.title_content)
	private TextView mTextView;

	public final static int FILECHOOSER_RESULTCODE = 1900;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_main);
		ViewUtils.inject(this);

		FragmentTransaction ft = fm.beginTransaction();
		currentFrag = new NewsFragment();
		ft.add(R.id.root, currentFrag, App.menuList.get(CODE.MENU_NEWS).getName());
		ft.commit();
		setTitleText(App.menuList.get(CODE.MENU_NEWS).getName());

		updateApp();
		App.isStartApp = true;
		EventUtils.sendStart(this);
	}

	@OnClick({R.id.main_title_left, R.id.main_title_right})
	private void onclick(View view) {
		switch (view.getId()) {
			case R.id.main_title_left: {
				slidingMenu.showMenu();
			}
			break;
			case R.id.main_title_right: {
			}
			break;

			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		if (getSlidingMenu().isMenuShowing() || getSlidingMenu().isSecondaryMenuShowing()) {
			getSlidingMenu().toggle(true);
			return;
		}
		if (currentFrag instanceof WebviewFragment) {
			if (((WebviewFragment) currentFrag).canback()) {
				return;
			}
		}


		ExitApplication.exit(this);
	}

	public void setTitleText(String name) {
		mTextView.setText(name);
	}

	@Override
	public void changeFm(int position) {

		if (itemSelectPositon == position) {
			getSlidingMenu().toggle(true);
			return;
		}

		itemSelectPositon = position;
		Fragment fragment = null;
		setTitleText(App.menuList.get(position).getName());

		FragmentTransaction ft = fm.beginTransaction();
		ft.hide(currentFrag);
		fragment = fm.findFragmentByTag(App.menuList.get(position).getName());
		if (null == fragment) {
			switch (position) {
				case CODE.MENU_NEWS:// 新闻
					fragment = new NewsFragment();
					break;
				case CODE.MENU_ALBUM:// 图集
					fragment = new NewsAlbumFragment();
					break;
				case CODE.MENU_VIDEO_RECORDING:// 视频
					fragment = new VideoListFragment();
					break;
				case CODE.MENU_SPECIAL: { // 专题(专题报道)
					fragment = new ZhuantiFragment();
				}
				break;
				case CODE.MENU_ACTION: {// 活动
					fragment = new ActionListFragment();
				}
				break;
				case CODE.MENU_SEARCH: {// 搜索
					fragment = new MySearchFragment();
				}
				break;
				default: {

				}
				break;
			}
			ft.add(R.id.root, fragment, App.menuList.get(position).getName());
		} else {
			ft.show(fragment);
		}
		ft.commit();
		currentFrag = fragment;
		getSlidingMenu().toggle(true);

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