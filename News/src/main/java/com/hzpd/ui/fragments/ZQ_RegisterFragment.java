package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.UserBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.ResType;
import com.lidroid.xutils.view.annotation.ResInject;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.Timer;
import java.util.TimerTask;

public class ZQ_RegisterFragment extends BaseFragment {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;
	@ViewInject(R.id.lgr_et_phone_id)
	private EditText lgr_et_phone_id;
	@ViewInject(R.id.lgr_et_sms_id)
	private EditText lgr_et_sms_id;
	@ViewInject(R.id.lgr_bt_get)
	private TextView lgr_bt_get;
	@ViewInject(R.id.lgr_et_pwd_id)
	private EditText lgr_et_pwd_id;
	@ViewInject(R.id.lgr_et_pwd_id1)
	private EditText lgr_et_pwd_id1;

	@ViewInject(R.id.lgr_bt_register)
	private Button lgr_bt_register;


	private Timer timer;
	private int t = 120;

	private String ph;
	private String pwd;

//	private String verify;

	@ResInject(id = R.string.smsappkey, type = ResType.String)
	private String smsappkey;
	@ResInject(id = R.string.smsappsecret, type = ResType.String)
	private String smsappsecret;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (333 == msg.what) {
				lgr_bt_get.setText(t + "秒");
				t--;
				if (t < 0) {
					resetTimer();
				}
			} else if (444 == msg.what) {
				LogUtils.i("验证成功");
			} else if (555 == msg.what) {
				lgr_et_sms_id.setText("");
			} else if (445 == msg.what) {
				TUtils.toast(getString(R.string.toast_captcha_get_success));
				startTime();// 开启定时器
			} else if (446 == msg.what) {
				TUtils.toast(getString(R.string.toast_captcha_is_wrong));
				resetTimer();
			} else if (447 == msg.what) {
				lgr_bt_get.setClickable(true);
				TUtils.toast(getString(R.string.toast_captcha_get_failed));
				resetTimer();
			}
		}
	};

	private void resetTimer() {
		if (null == timer) {
			return;
		}
		timer.cancel();
		lgr_bt_get.setText(R.string.prompt_get_again);
		lgr_bt_get.setBackgroundResource(R.drawable.zq_special_greyborder_selector);
		lgr_et_phone_id.setEnabled(true);
		lgr_bt_get.setClickable(true);
		t = 120;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_rgister, container, false);
		ViewUtils.inject(this, view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		stitle_tv_content.setText(R.string.title_user_register);

	}

	@OnClick(R.id.lgr_bt_get)
	private void getSMS(View v) {
		String ph = lgr_et_phone_id.getText().toString();
		if (!TextUtils.isEmpty(ph) && 11 == ph.length()) {

			// 获取sms

			lgr_bt_get.setClickable(false);


			/**
			 RequestParams params = RequestParamsUtis.getParams();
			 params.addBodyParameter("mobile", ph);
			 params.addBodyParameter("flag", "1");// 1用户注册 2修改密码

			 httpUtils.send(HttpMethod.POST, InterfaceJsonfile.smsCode, params,
			 new RequestCallBack<String>() {
			@Override public void onFailure(HttpException arg0, String arg1) {
			handler.sendEmptyMessage(446);
			}

			@Override public void onSuccess(ResponseInfo<String> arg0) {
			LogUtils.i("verify-->" + arg0.result);
			JSONObject obj = null;
			try {
			obj = JSONObject.parseObject(arg0.result);
			} catch (Exception e) {
			e.printStackTrace();
			handler.sendEmptyMessage(446);
			return;
			}

			switch (obj.getIntValue("code")) {
			case 200: {
			verify = obj.getJSONObject("data").getString(
			"mobile_code");
			LogUtils.i("verify-->" + verify);
			// 获取验证码成功
			handler.sendEmptyMessage(445);
			}
			break;
			case 201: {
			TUtils.toast("已注册,请登录");
			lgr_bt_get.setClickable(false);
			}
			break;
			default: {
			handler.sendEmptyMessage(446);
			}
			break;
			}

			}
			});
			 */
		} else {
			// 提示
			TUtils.toast(getString(R.string.toast_please_input_correct_phone));
		}
	}


	@OnClick(R.id.lgr_bt_register)
	private void register(View v) {
		if (!MyCommonUtil.isNetworkConnected(activity)) {
			TUtils.toast(getString(R.string.toast_network_error));
			return;
		}

		String sms = lgr_et_sms_id.getText().toString();
		if (sms == null || "".equals(sms)) {
			TUtils.toast(getString(R.string.toast_captcha_cannot_be_empty));
			return;
		}

		final String pwd1 = lgr_et_pwd_id.getText().toString();
		if (pwd1 == null || "".equals(pwd1)) {
			TUtils.toast(getString(R.string.toast_password_cannot_be_empty));
			return;
		}
		if (pwd1.length() < 6) {
			TUtils.toast(getString(R.string.toast_password_too_short));
			return;
		}
		if (pwd1.length() > 12) {
			TUtils.toast(getString(R.string.toast_password_too_long));
			return;
		}
		final String pwd2 = lgr_et_pwd_id1.getText().toString();
		if (pwd2 == null || "".equals(pwd2)) {
			TUtils.toast(getString(R.string.toast_input_password_again));
			return;
		}

		if (!pwd1.equals(pwd2)) {
			TUtils.toast(getString(R.string.toast_two_password_is_diff));
			return;
		}
		pwd = pwd1;

		this.ph = lgr_et_phone_id.getText().toString();

		String station = SharePreferecesUtils.getParam(getActivity(), StationConfig.STATION, "def").toString();
		String REGISTER_url = null;
		if (station.equals(StationConfig.DEF)) {
			REGISTER_url = InterfaceJsonfile.REGISTER;
		} else if (station.equals(StationConfig.YN)) {
			REGISTER_url = InterfaceJsonfile_YN.REGISTER;
		} else if (station.equals(StationConfig.TW)) {
			REGISTER_url = InterfaceJsonfile_TW.REGISTER;
		}
		// 注册
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("username", ph);
		params.addBodyParameter("password", pwd);
		params.addBodyParameter("verify", sms);
//		params.addBodyParameter("email", "");

		httpUtils.send(HttpMethod.POST, REGISTER_url// InterfaceApi.mRegister
				, params, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				TUtils.toast(getString(R.string.toast_server_no_response));
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.i("login-success-->" + arg0.result);

				JSONObject object = FjsonUtil.parseObject(arg0.result);
				if (null == object) {
					return;
				}

				if (200 == object.getIntValue("code")) {
					UserBean user = FjsonUtil.parseObject(object.getString("data"), UserBean.class);
					spu.setUser(user);

					Intent intent = new Intent();
					intent.setAction(ZY_RightFragment.ACTION_USER);
					activity.sendBroadcast(intent);

					TUtils.toast(getString(R.string.toast_register_success));
					// setResult(10);
					activity.finish();
				} else {
					TUtils.toast(getString(R.string.toast_register_failed));
				}
			}
		});

	}

	@OnClick(R.id.stitle_ll_back)
	private void goBack(View v) {
		if (null != spu.getUser()) {
			// setResult(10);
			activity.finish();
		} else {
			activity.onBackPressed();
		}
	}

	@Override
	public void onDestroy() {
		if (timer != null) {
			timer.cancel();
		}
		super.onDestroy();
	}

	private void startTime() {
		lgr_et_phone_id.setEnabled(false);
		lgr_bt_get.setBackgroundResource(R.drawable.register_getsms_shape);
		lgr_bt_get.setClickable(false);
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();

		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(333);
			}
		};

		timer.schedule(tt, 0, 1000);
	}

}