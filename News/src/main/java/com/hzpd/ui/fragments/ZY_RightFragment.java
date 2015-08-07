package com.hzpd.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hzpd.custorm.CircleImageView;
import com.hzpd.hflt.R;
import com.hzpd.modle.event.DayNightEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.LoginActivity;
import com.hzpd.ui.activity.MipcaActivityCapture;
import com.hzpd.ui.activity.MyCommentsActivity;
import com.hzpd.ui.activity.MyPMColAvtivity;
import com.hzpd.ui.activity.PersonalInfoActivity;
import com.hzpd.ui.activity.SettingActivity;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import de.greenrobot.event.EventBus;

public class ZY_RightFragment extends BaseFragment {

	public static final String ACTION_USER = "com.hzpd.cms.user";
	public static final String ACTION_QUIT = "com.hzpd.cms.quit";
	public static final String ACTION_QUIT_LOGIN = "com.hzpd.cms.quit.login";
	@ViewInject(R.id.zy_rfrag_ll_login)
	private LinearLayout zy_rfrag_ll_login;
	@ViewInject(R.id.zy_rfrag_tv_login)
	private TextView zy_rfrag_tv_login;
	@ViewInject(R.id.zy_rfrag_iv_login)
	private CircleImageView zy_rfrag_iv_login;

	@ViewInject(R.id.zy_rfrag_ll_comm)
	private LinearLayout zy_rfrag_ll_comm;
	@ViewInject(R.id.zy_rfrag_ll_collect)
	private LinearLayout zy_rfrag_ll_collect;
	@ViewInject(R.id.zy_rfrag_ll_push)
	private LinearLayout zy_rfrag_ll_push;
	@ViewInject(R.id.zy_rfrag_ll_sun)
	private LinearLayout zy_rfrag_ll_sun;
	@ViewInject(R.id.zy_rfrag_ll_setting)
	private LinearLayout zy_rfrag_ll_setting;
	@ViewInject(R.id.zy_rfrag_tv_des)
	private TextView zy_rfrag_tv_des;

	private LoginQuitBR br;
	private boolean isDay = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.zy_rightfragment, container, false);
		ViewUtils.inject(this, view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		zy_rfrag_tv_des.setText(
				getString(R.string.prompt_developer_info, getString(R.string.app_name), App.getInstance().getVersionName()));

		br = new LoginQuitBR();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ZY_RightFragment.ACTION_QUIT);
		filter.addAction(ZY_RightFragment.ACTION_USER);
		filter.addAction(ZY_RightFragment.ACTION_QUIT_LOGIN);
		activity.registerReceiver(br, filter);

		if (null != spu.getUser()) {
			LogUtils.i("userimg-->" + spu.getUser().getAvatar_path());
			mImageLoader.displayImage(spu.getUser().getAvatar_path(), zy_rfrag_iv_login,
					DisplayOptionFactory.getOption(OptionTp.Logo));
			zy_rfrag_tv_login.setText("" + spu.getUser().getNickname());
		}

	}

	@OnClick({R.id.zy_rfrag_ll_login, R.id.zy_rfrag_ll_comm, R.id.zy_rfrag_ll_collect, R.id.zy_rfrag_ll_push,
			R.id.zy_rfrag_ll_sun, R.id.zy_rfrag_ll_setting, R.id.zy_rfrag_ll_ewm})
	private void rightClick(View v) {
		boolean flag = false;
		Intent mIntent = new Intent();
		switch (v.getId()) {
			case R.id.zy_rfrag_ll_login: {
				if (null == spu.getUser()) {
					mIntent.setClass(activity, LoginActivity.class);
				} else {
					mIntent.setClass(activity, PersonalInfoActivity.class);
				}
				flag = true;
			}
			break;
			case R.id.zy_rfrag_ll_comm: {
				mIntent.setClass(activity, MyCommentsActivity.class);
				flag = true;
			}
			break;
			case R.id.zy_rfrag_ll_collect: {
				Log.i("", "shoucang--");
				mIntent.setClass(activity, MyPMColAvtivity.class);
				mIntent.putExtra("type", "collection");
				flag = true;
			}
			break;
			case R.id.zy_rfrag_ll_push: {
				mIntent.setClass(activity, MyPMColAvtivity.class);
				mIntent.putExtra("type", "pushmsg");
				flag = true;
			}
			break;
			case R.id.zy_rfrag_ll_sun:
				DayNightEvent event = new DayNightEvent();
				if (isDay) {
					isDay = false;
					event.setDaynightColor(activity.getResources().getColor(R.color.grey21));

				} else {
					isDay = true;
					event.setDaynightColor(activity.getResources().getColor(R.color.white));

				}
				EventBus.getDefault().post(event);
				Toast.makeText(activity, R.string.prompt_daytime, Toast.LENGTH_SHORT).show();
				break;
			case R.id.zy_rfrag_ll_setting: {
				mIntent.setClass(activity, SettingActivity.class);
				flag = true;
			}
			break;
			case R.id.zy_rfrag_ll_ewm: {
				mIntent.setClass(activity, MipcaActivityCapture.class);
				flag = true;
			}
			break;
			default:
				break;
		}
		if (flag) {
			startActivity(mIntent);
			AAnim.ActivityStartAnimation(activity);
		}
	}

	public class LoginQuitBR extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ZY_RightFragment.ACTION_USER)) {
				setLogin();
			} else if (action.equals(ZY_RightFragment.ACTION_QUIT)) {
				setQuit();
				LogUtils.i("setquit");
			} else if (action.equals(ZY_RightFragment.ACTION_QUIT_LOGIN)) {
				setQuit();
				LogUtils.i("setquitlogin");

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						zy_rfrag_ll_login.callOnClick();
						LogUtils.i("r_login_layout.callOnClick()");
					}
				}, 500);
			}
		}
	}

	private void setLogin() {
		LogUtils.i("imgUrl-->" + spu.getUser().getAvatar_path() + "  name-->" + spu.getUser().getNickname());
		mImageLoader.displayImage(spu.getUser().getAvatar_path(), zy_rfrag_iv_login,
				DisplayOptionFactory.getOption(OptionTp.Logo));
		zy_rfrag_tv_login.setText(spu.getUser().getNickname());

		// -----

		JPushInterface.setAlias(activity, spu.getUser().getUid(), new TagAliasCallback() {
			@Override
			public void gotResult(int arg0, String arg1, Set<String> arg2) {
				LogUtils.i("arg0-->" + arg0 + " arg1-->" + arg1);
				if (arg2 != null) {
					for (String s : arg2) {
						LogUtils.i("arg2->" + s);
					}
				}
			}
		});

	}

	private void setQuit() {
		zy_rfrag_iv_login.setImageResource(R.drawable.zy_pic_touxiang);
		zy_rfrag_tv_login.setText(R.string.login);

		JPushInterface.setAlias(activity, "", new TagAliasCallback() {
			@Override
			public void gotResult(int arg0, String arg1, Set<String> arg2) {
				LogUtils.i("arg0-->" + arg0 + " arg1-->" + arg1);
				if (arg2 != null) {
					for (String s : arg2) {
						LogUtils.i("arg2->" + s);
					}
				}
			}
		});

	}

	@Override
	public void onDestroy() {
		try {
			activity.unregisterReceiver(br);
		} catch (Exception e) {

		}

		super.onDestroy();
	}

}
