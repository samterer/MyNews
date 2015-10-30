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
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.FjsonUtil;
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
		String station= SharePreferecesUtils.getParam(getActivity(), StationConfig.STATION, "def").toString();
		String PWDTYPE =null;
		int NICKNAME=1;
		int GENDER=2;
		if (station.equals(StationConfig.DEF)){
			NICKNAME=InterfaceJsonfile.NICKNAME;
			GENDER=InterfaceJsonfile.GENDER;
			PWDTYPE =InterfaceJsonfile.PWDTYPE;
		}else if (station.equals(StationConfig.YN)){
			NICKNAME=InterfaceJsonfile_YN.NICKNAME;
			GENDER=InterfaceJsonfile_YN.GENDER;
			PWDTYPE = InterfaceJsonfile_YN.PWDTYPE;
		}else if (station.equals(StationConfig.TW)){
			NICKNAME=InterfaceJsonfile_TW.NICKNAME;
			GENDER=InterfaceJsonfile_TW.GENDER;
			PWDTYPE = InterfaceJsonfile_TW.PWDTYPE;
		}
		Bundle bundle = getArguments();
		type = bundle.getInt(PWDTYPE, NICKNAME);
		if (NICKNAME == type) {
			mi_ll_nick.setVisibility(View.VISIBLE);
			mi_ll_gender.setVisibility(View.GONE);
			stitle_tv_content.setText(R.string.prompt_change_nickname);
			mi_et_context.setText(spu.getUser().getNickname());
		} else if (GENDER == type) {

			mi_ll_nick.setVisibility(View.GONE);
			mi_ll_gender.setVisibility(View.VISIBLE);
			stitle_tv_content.setText(R.string.prompt_change_sex);
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
		String station= SharePreferecesUtils.getParam(getActivity(), StationConfig.STATION, "def").toString();
		String CHANGEPINFO_url =null;
		if (station.equals(StationConfig.DEF)){
			CHANGEPINFO_url =InterfaceJsonfile.CHANGEPINFO;
		}else if (station.equals(StationConfig.YN)){
			CHANGEPINFO_url = InterfaceJsonfile_YN.CHANGEPINFO;
		}else if (station.equals(StationConfig.TW)){
			CHANGEPINFO_url = InterfaceJsonfile_TW.CHANGEPINFO;
		}
		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("token", spu.getUser().getToken());
		if (1 == type) {
			nickname = mi_et_context.getText().toString();
			if (TextUtils.isEmpty(nickname)) {
				TUtils.toast(getString(R.string.toast_nickname_cannot_be_empty));
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
				, CHANGEPINFO_url//InterfaceApi.modify_gender
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				TUtils.toast(getString(R.string.toast_server_no_response));
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.i("-->" + arg0.result);
				JSONObject obj = FjsonUtil.parseObject(arg0.result);
				if (null == obj) {
					return;
				}

				if (200 == obj.getIntValue("code")) {
					TUtils.toast(getString(R.string.toast_modify_success));
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