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
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.ResType;
import com.lidroid.xutils.view.annotation.ResInject;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.Timer;
import java.util.TimerTask;

public class ZQ_RegisterFragment extends BaseFragment implements View.OnClickListener {

    private TextView stitle_tv_content;
    private EditText lgr_et_phone_id;
    private EditText lgr_et_sms_id;
    private TextView lgr_bt_get;
    private EditText lgr_et_pwd_id;
    private EditText lgr_et_pwd_id1;
    private Button lgr_bt_register;
    private View stitle_ll_back;
    private Timer timer;
    private int t = 120;

    private String ph;
    private String pwd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_rgister, container, false);
        stitle_tv_content = (TextView) view.findViewById(R.id.stitle_tv_content);
        lgr_et_phone_id = (EditText) view.findViewById(R.id.lgr_et_phone_id);
        lgr_et_sms_id = (EditText) view.findViewById(R.id.lgr_et_sms_id);
        lgr_bt_get = (TextView) view.findViewById(R.id.lgr_bt_get);
        lgr_bt_get.setOnClickListener(this);
        lgr_et_pwd_id = (EditText) view.findViewById(R.id.lgr_et_pwd_id);
        lgr_et_pwd_id1 = (EditText) view.findViewById(R.id.lgr_et_pwd_id1);
        lgr_bt_register = (Button) view.findViewById(R.id.lgr_bt_register);
        lgr_bt_register.setOnClickListener(this);
        stitle_ll_back = view.findViewById(R.id.stitle_ll_back);
        stitle_ll_back.setOnClickListener(this);

        return view;
    }


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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stitle_tv_content.setText(R.string.title_user_register);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lgr_bt_get: {
                String ph = lgr_et_phone_id.getText().toString();
                if (!TextUtils.isEmpty(ph) && 11 == ph.length()) {

                    // 获取sms

                    lgr_bt_get.setClickable(false);

                } else {
                    // 提示
                    TUtils.toast(getString(R.string.toast_please_input_correct_phone));
                }
            }
            break;
            case R.id.lgr_bt_register: {
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

                // 注册
                RequestParams params = RequestParamsUtils.getParams();
                params.addBodyParameter("username", ph);
                params.addBodyParameter("password", pwd);
                params.addBodyParameter("verify", sms);
//		params.addBodyParameter("email", "");
                SPUtil.addParams(params);
                httpUtils.send(HttpMethod.POST, InterfaceJsonfile.REGISTER// InterfaceApi.mRegister
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
            break;
            case R.id.stitle_ll_back: {
                if (null != spu.getUser()) {
                    // setResult(10);
                    activity.finish();
                } else {
                    activity.onBackPressed();
                }
            }
            break;
        }
    }
}