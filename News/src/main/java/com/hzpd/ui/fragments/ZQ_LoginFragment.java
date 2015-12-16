package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ZQ_LoginFragment extends BaseFragment {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;
	@ViewInject(R.id.login_uname_id)
	private EditText login_uname_id;        //用户名
	@ViewInject(R.id.login_passwd_id)
	private EditText login_passwd_id;        //密码
	@ViewInject(R.id.login_not_passwd)
	private TextView login_not_passwd;        //忘记密码
	@ViewInject(R.id.login_login_comfirm_id)
	private Button login_login_comfirm_id;    //登录

	@ViewInject(R.id.login_register_tv_id)
	private TextView login_register_tv_id;//注册


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_layout, container, false);
		ViewUtils.inject(this, view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		stitle_tv_content.setText(R.string.title_login);
	}

	private void showDialog() {
	}

	@OnClick(R.id.login_login_comfirm_id)
	private void login(View v) {
		if (!MyCommonUtil.isNetworkConnected(activity)) {
			TUtils.toast(getString(R.string.toast_network_error));
			return;
		}

		final String uname = login_uname_id.getText().toString();
		final String pwd = login_passwd_id.getText().toString();
		if (uname == null || "".equals(uname)) {
			TUtils.toast(getString(R.string.toast_input_username));
			return;
		}
		if (pwd == null || "".equals(pwd)) {
			TUtils.toast(getString(R.string.toast_input_password));
			return;
		}

		showDialog();

		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("username", uname);
		params.addBodyParameter("password", pwd);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.LOGIN//InterfaceApi.mUserLogin
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				LogUtils.i("login-failed");
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
					activity.finish();
					TUtils.toast(getString(R.string.toast_login_success));
				} else {
					TUtils.toast(object.getString("msg"));
				}
			}
		});

	}

	@OnClick(R.id.stitle_ll_back)
	private void back(View v) {
		activity.onBackPressed();
	}

}