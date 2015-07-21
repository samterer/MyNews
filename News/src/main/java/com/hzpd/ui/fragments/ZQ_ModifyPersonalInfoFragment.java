package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.UserBean;
import com.hzpd.url.InterfaceJsonfile;
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
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ZQ_ModifyPersonalInfoFragment extends BaseFragment {
	@ViewInject(R.id.mi_ll_nick)
	private LinearLayout mi_ll_nick;//昵称布局
	@ViewInject(R.id.mi_ll_gender)
	private LinearLayout mi_ll_gender;//性别布局

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;

	@ViewInject(R.id.mi_et_context)
	private EditText mi_et_context;//昵称edit
	@ViewInject(R.id.mi_im_clean)
	private ImageView mi_im_clean;//清除按钮
	@ViewInject(R.id.mi_bt_comfirm)
	private Button mi_bt_comfirm;//提交按钮

	@ViewInject(R.id.mi_rb1)
	private RadioButton mi_rb1;
	@ViewInject(R.id.mi_rb2)
	private RadioButton mi_rb2;
	@ViewInject(R.id.mi_rb3)
	private RadioButton mi_rb3;
	@ViewInject(R.id.mi_rg)
	private RadioGroup mi_rg;


	private int type;//1昵称  2性别
	private String gender = "3";
	private String nickname;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.modifypersonalinfo_layout, container, false);
		ViewUtils.inject(this, view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = getArguments();
		type = bundle.getInt(InterfaceJsonfile.PWDTYPE, InterfaceJsonfile.NICKNAME);
		if (InterfaceJsonfile.NICKNAME == type) {
			mi_ll_nick.setVisibility(View.VISIBLE);
			mi_ll_gender.setVisibility(View.GONE);
			stitle_tv_content.setText("修改昵称");
			mi_et_context.setText(spu.getUser().getNickname());
		} else if (InterfaceJsonfile.GENDER == type) {

			mi_ll_nick.setVisibility(View.GONE);
			mi_ll_gender.setVisibility(View.VISIBLE);
			stitle_tv_content.setText("修改性别");
			String gender = spu.getUser().getSex();

			if ("1".equals(gender)) {
				mi_rb1.setChecked(true);
			} else if ("2".equals(gender)) {
				mi_rb2.setChecked(true);
			} else {
				mi_rb3.setChecked(true);
			}
		}
	}


	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		if (1 == type) {
			Intent intent = new Intent();
			intent.setAction(ZY_RightFragment.ACTION_USER);
			activity.sendBroadcast(intent);
		}
		activity.onBackPressed();
	}

	@OnClick(R.id.mi_bt_comfirm)
	private void confirm(View v) {

		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("token", spu.getUser().getToken());
		if (1 == type) {
			nickname = mi_et_context.getText().toString();
			if (TextUtils.isEmpty(nickname)) {
				TUtils.toast("昵称不能为空");
			} else {
				params.addBodyParameter("nickname", nickname);
			}
		} else {
			int id = mi_rg.getCheckedRadioButtonId();

			LogUtils.i("id-->" + id);
			switch (mi_rg.getCheckedRadioButtonId()) {
				case R.id.mi_rb1:
					gender = "1";
					break;
				case R.id.mi_rb2:
					gender = "2";
					break;
				default:
					gender = "3";
					break;
			}
			params.addBodyParameter("sex", gender);//1男 2女 3保密
		}

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.CHANGEPINFO//InterfaceApi.modify_gender
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				TUtils.toast("服务器未响应");
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.i("-->" + arg0.result);
				JSONObject obj = FjsonUtil.parseObject(arg0.result);
				if (null == obj) {
					return;
				}

				if (200 == obj.getIntValue("code")) {
					TUtils.toast("修改成功");
					UserBean user = FjsonUtil.parseObject(obj.getString("data"), UserBean.class);
					spu.setUser(user);

					Intent intent = new Intent();
					intent.setAction(ZY_RightFragment.ACTION_USER);
					activity.sendBroadcast(intent);
				} else {
					TUtils.toast(obj.getString("msg"));
				}
			}
		});
	}

	@OnClick(R.id.mi_im_clean)
	private void clean(View v) {
		mi_et_context.setText("");
	}

}
