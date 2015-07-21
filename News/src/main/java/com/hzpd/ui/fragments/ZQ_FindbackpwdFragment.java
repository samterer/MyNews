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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.CipherUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
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

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class ZQ_FindbackpwdFragment extends BaseFragment {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;

	@ViewInject(R.id.findpwdback_root_ll)
	private LinearLayout findpwdback_root_ll;

	@ViewInject(R.id.fpb_ll_veify)
	private LinearLayout fpb_ll_vertify;
	@ViewInject(R.id.fpb_et_phone_id)
	private EditText fpb_et_phone_id;
	@ViewInject(R.id.fpb_et_sms_id)
	private EditText fpb_et_sms_id;
	@ViewInject(R.id.fpb_bt_get)
	private TextView fpb_bt_get;
	@ViewInject(R.id.fpb_bt_verify)
	private Button fpb_bt_verify;

	@ViewInject(R.id.fpb_ll_reset)
	private LinearLayout fpb_ll_reset;
	@ViewInject(R.id.fpb_et_pwd_id)
	private EditText fpb_et_pwd_id;
	@ViewInject(R.id.fpb_et_pwd_id2)
	private EditText fpb_et_pwd_id2;
	@ViewInject(R.id.fpb_bt_submmit)
	private Button fpb_bt_submmit;

	private String phoneNumber;

	private boolean isverify;// 是否验证

	private int type;// 1忘记密码 2修改密码

	private Timer timer;
	private int t = 120;
	private String verify;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 333: {
					fpb_bt_get.setText(t + "秒");
					t--;
					if (t < 0) {
						resetTimer();
					}
				}
				break;
				case 444: {
					verifyCorrect();
				}
				break;
				case 555: {
					TUtils.toast("验证码错误");
					fpb_et_sms_id.setText("");
					resetTimer();
				}
				break;
				case 445: {
					TUtils.toast("验证码获取成功");
					startTime();// 开启定时器
				}
				break;
				case 446: {
					TUtils.toast("验证码获取失败");
					resetTimer();
				}
				break;
			}
		}
	};

	@ResInject(id = R.string.smsappkey, type = ResType.String)
	private String smsappkey;
	@ResInject(id = R.string.smsappsecret, type = ResType.String)
	private String smsappsecret;

	private void resetTimer() {
		if (null != timer) {
			timer.cancel();
		}
		fpb_bt_get.setText("重新获取");
		fpb_bt_get.setBackgroundResource(R.drawable.zq_special_greyborder_selector);
		fpb_bt_get.setClickable(true);
		fpb_et_phone_id.setEnabled(true);
		t = 120;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.findpwdback_layout, container,
				false);
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		stitle_tv_content.setText("密码找回");
		Bundle bundle = getArguments();
		type = bundle.getInt(InterfaceJsonfile.PWDTYPE, 1);
		if (2 == type) {
			stitle_tv_content.setText("修改密码");
			if (!TextUtils.isEmpty(spu.getUser().getMobile())) {
				fpb_et_phone_id.setText(spu.getUser().getMobile());
				fpb_et_phone_id.setEnabled(false);
			}
		}

		SMSSDK.initSDK(activity, smsappkey, smsappsecret);
		SMSSDK.registerEventHandler(new EventHandler() {
			@Override
			public void afterEvent(int event, int result, Object data) {
				if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
					if (result == SMSSDK.RESULT_COMPLETE) {
						//验证码获取成功
						handler.sendEmptyMessage(445);
					} else if (result == SMSSDK.RESULT_ERROR) {
						handler.sendEmptyMessage(446);
					}
				} else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
					if (result == SMSSDK.RESULT_COMPLETE) {
						handler.sendEmptyMessage(444);
					} else if (result == SMSSDK.RESULT_ERROR) {
						handler.sendEmptyMessage(555);
					}
				}
			}
		});

	}

	@OnClick(R.id.fpb_bt_get)
	private void getVerifyCode(View v) {
		phoneNumber = fpb_et_phone_id.getText().toString();

		if (phoneNumber == null || "".equals(phoneNumber)) {
			TUtils.toast("请输入手机号！");
			return;
		}
		if (phoneNumber.length() != 11) {
			TUtils.toast("手机号位数不对!");
			return;
		}
		fpb_bt_get.setClickable(false);
		SMSSDK.getVerificationCode("86", phoneNumber);


		/**
		 RequestParams params = RequestParamsUtis.getParams();
		 params.addBodyParameter("mobile", phoneNumber);
		 params.addBodyParameter("flag", "2");

		 httpUtils.send(HttpMethod.POST, InterfaceJsonfile.smsCode, params,
		 new RequestCallBack<String>() {
		@Override public void onFailure(HttpException arg0, String arg1) {
		TUtils.toast("服务器未响应！");
		}

		@Override public void onSuccess(ResponseInfo<String> arg0) {
		LogUtils.i("findpwdback-getcode->" + arg0.result);

		JSONObject obj = null;
		try {
		obj = JSONObject.parseObject(arg0.result);
		} catch (Exception e) {
		e.printStackTrace();
		handler.sendEmptyMessage(446);
		return;
		}

		if (200 == obj.getIntValue("code")) {
		// 计时
		verify = obj.getJSONObject("data").getString(
		"mobile_code");
		handler.sendEmptyMessage(445);
		} else {
		TUtils.toast(obj.getString("msg"));
		}
		}
		});
		 */

	}

	@OnClick(R.id.fpb_bt_verify)
	private void Verify(View v) {

		verify = fpb_et_sms_id.getText().toString();

		LogUtils.i("sms-->" + verify);
		if (verify == null || "".equals(verify)) {
			TUtils.toast("验证码不能为空!");
			return;
		}

//		SMSSDK.submitVerificationCode("86", phoneNumber, sms);

		RequestParams params = new RequestParams();
		params.addBodyParameter("mobile", phoneNumber);
		params.addBodyParameter("verify", verify);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.SMS_VERIFY
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String json = responseInfo.result;

				LogUtils.i("loginSubmit-->" + json);


				JSONObject obj = FjsonUtil
						.parseObject(responseInfo.result);
				if (null != obj) {
					if (200 == obj.getIntValue("code")) {
						handler.sendEmptyMessage(444);
					} else {
						handler.sendEmptyMessage(555);
					}
				} else {
					TUtils.toast("服务器错误");
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});

	}

	private void verifyCorrect() {
		fpb_ll_vertify.setVisibility(View.GONE);
		fpb_ll_reset.setVisibility(View.VISIBLE);
		TUtils.toast("验证成功");
	}

	@OnClick(R.id.fpb_bt_submmit)
	private void changePassword(View view) {
		String s = fpb_et_pwd_id.getText().toString();
		String s2 = fpb_et_pwd_id2.getText().toString();

		if (s == null || "".equals(s)) {
			TUtils.toast("请输入密码");
			return;
		}
		if (s.length() < 6) {
			TUtils.toast("密码不能太短");
			return;
		}
		if (s.length() > 12) {
			TUtils.toast("密码不能太长");
			return;
		}

		if (s2 == null || "".equals(s2)) {
			TUtils.toast("请输入确认密码");
			return;
		}
		if (!s.equals(s2)) {
			TUtils.toast("俩次密码不一致！");
			fpb_et_pwd_id.setText("");
			fpb_et_pwd_id2.setText("");
			return;
		}

		RequestParams params = RequestParamsUtils.getParamsWithU();
		if (null != spu.getUser()) {
			params.addBodyParameter("token", spu.getUser().getToken());
		}

		String code = CipherUtils.base64Encode(CipherUtils.base64Encode(phoneNumber)
				+ "_" + CipherUtils.md5(phoneNumber + verify + "99cms")
				+ "_" + System.currentTimeMillis() / 1000);

		params.addBodyParameter("code", code);
		params.addBodyParameter("new_pwd", s);
		params.addBodyParameter("is_ucenter", "1");

		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.CHANGEPWD// InterfaceApi.mSmsReset
				, params, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				TUtils.toast("网络连接失败");
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.i("findpwdback-getcode->" + arg0.result);
				JSONObject obj = FjsonUtil.parseObject(arg0.result);
				if (null == obj) {
					TUtils.toast("服务器错误");
					return;
				}
				if (200 == obj.getIntValue("code")) {
					spu.setUser(null);

					TUtils.toast("修改密码成功，请重新登录", Toast.LENGTH_LONG);
					Intent intent = new Intent();
					intent.setAction(ZY_RightFragment.ACTION_QUIT);
					activity.sendBroadcast(intent);
					activity.finish();
				} else {
					TUtils.toast("修改密码失败");
				}
			}
		});
	}

	private void startTime() {
		fpb_bt_get.setBackgroundResource(R.drawable.register_getsms_shape);
		fpb_et_phone_id.setEnabled(false);
		fpb_bt_get.setClickable(false);
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

	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		activity.onBackPressed();
	}

	@Override
	public void onDestroy() {
		if (timer != null) {
			timer.cancel();
		}
		super.onDestroy();
	}


}
