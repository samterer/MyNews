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
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;

import java.util.Timer;
import java.util.TimerTask;


public class ZQ_FindbackpwdFragment extends BaseFragment implements View.OnClickListener {

    private TextView stitle_tv_content;
    private LinearLayout findpwdback_root_ll;
    private LinearLayout fpb_ll_vertify;
    private EditText fpb_et_phone_id;
    private EditText fpb_et_sms_id;
    private TextView fpb_bt_get;
    private Button fpb_bt_verify;
    private LinearLayout fpb_ll_reset;
    private EditText fpb_et_pwd_id;
    private EditText fpb_et_pwd_id2;
    private Button fpb_bt_submmit;
    private String phoneNumber;
    private boolean isverify;// 是否验证
    private int type;// 1忘记密码 2修改密码
    private Timer timer;
    private int t = 120;
    private String verify;
    private View stitle_ll_back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.findpwdback_layout, container,
                false);
        stitle_tv_content = (TextView) view.findViewById(R.id.stitle_tv_content);
        findpwdback_root_ll = (LinearLayout) view.findViewById(R.id.findpwdback_root_ll);
        fpb_ll_vertify = (LinearLayout) view.findViewById(R.id.fpb_ll_veify);
        fpb_et_phone_id = (EditText) view.findViewById(R.id.fpb_et_phone_id);
        fpb_et_sms_id = (EditText) view.findViewById(R.id.fpb_et_sms_id);
        fpb_bt_get = (TextView) view.findViewById(R.id.fpb_bt_get);
        fpb_bt_get.setOnClickListener(this);
        fpb_bt_verify = (Button) view.findViewById(R.id.fpb_bt_verify);
        fpb_bt_verify.setOnClickListener(this);
        fpb_ll_reset = (LinearLayout) view.findViewById(R.id.fpb_ll_reset);
        fpb_et_pwd_id = (EditText) view.findViewById(R.id.fpb_et_pwd_id);
        fpb_et_pwd_id2 = (EditText) view.findViewById(R.id.fpb_et_pwd_id2);
        fpb_bt_submmit = (Button) view.findViewById(R.id.fpb_bt_submmit);
        fpb_bt_submmit.setOnClickListener(this);
        stitle_ll_back = view.findViewById(R.id.stitle_ll_back);
        stitle_ll_back.setOnClickListener(this);
        return view;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 333: {
                    fpb_bt_get.setText(getString(R.string.prompt_seconds, t));
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
                    TUtils.toast(getString(R.string.toast_captcha_is_wrong));
                    fpb_et_sms_id.setText("");
                    resetTimer();
                }
                break;
                case 445: {
                    TUtils.toast(getString(R.string.toast_captcha_get_success));
                    startTime();// 开启定时器
                }
                break;
                case 446: {
                    TUtils.toast(getString(R.string.toast_captcha_get_failed));
                    resetTimer();
                }
                break;
            }
        }
    };

    private void resetTimer() {
        if (null != timer) {
            timer.cancel();
        }
        fpb_bt_get.setText(getString(R.string.prompt_get_again));
        fpb_bt_get.setBackgroundResource(R.drawable.zq_special_greyborder_selector);
        fpb_bt_get.setClickable(true);
        fpb_et_phone_id.setEnabled(true);
        t = 120;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stitle_tv_content.setText(R.string.prompt_find_password);
        Bundle bundle = getArguments();
        type = bundle.getInt(InterfaceJsonfile.PWDTYPE, 1);
        if (2 == type) {
            stitle_tv_content.setText(getString(R.string.prompt_change_password));
            if (!TextUtils.isEmpty(spu.getUser().getMobile())) {
                fpb_et_phone_id.setText(spu.getUser().getMobile());
                fpb_et_phone_id.setEnabled(false);
            }
        }


    }

    private void verifyCorrect() {
        fpb_ll_vertify.setVisibility(View.GONE);
        fpb_ll_reset.setVisibility(View.VISIBLE);
        TUtils.toast(getString(R.string.toast_verify_success));
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


    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fpb_bt_get: {
                phoneNumber = fpb_et_phone_id.getText().toString();

                if (phoneNumber == null || "".equals(phoneNumber)) {
                    TUtils.toast(getString(R.string.toast_input_phone));
                    return;
                }
                if (phoneNumber.length() != 11) {
                    TUtils.toast(getString(R.string.toast_phone_number_count_error));
                    return;
                }
                fpb_bt_get.setClickable(false);
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
            break;
            case R.id.fpb_bt_verify: {

                verify = fpb_et_sms_id.getText().toString();

                LogUtils.i("sms-->" + verify);
                if (verify == null || "".equals(verify)) {
                    TUtils.toast(getString(R.string.toast_captcha_cannot_be_empty));
                    return;
                }

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
                            TUtils.toast(getString(R.string.toast_server_error));
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                    }
                });

            }
            break;
            case R.id.fpb_bt_submmit: {
                String s = fpb_et_pwd_id.getText().toString();
                String s2 = fpb_et_pwd_id2.getText().toString();

                if (s == null || "".equals(s)) {
                    TUtils.toast(getString(R.string.toast_input_password));
                    return;
                }
                if (s.length() < 6) {
                    TUtils.toast(getString(R.string.toast_password_too_short));
                    return;
                }
                if (s.length() > 12) {
                    TUtils.toast(getString(R.string.toast_password_too_long));
                    return;
                }

                if (s2 == null || "".equals(s2)) {
                    TUtils.toast(getString(R.string.toast_input_confirm_password));
                    return;
                }
                if (!s.equals(s2)) {
                    TUtils.toast(getString(R.string.toast_two_password_is_diff));
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
                        TUtils.toast(getString(R.string.toast_cannot_connect_network));
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        LogUtils.i("findpwdback-getcode->" + arg0.result);
                        JSONObject obj = FjsonUtil.parseObject(arg0.result);
                        if (null == obj) {
                            TUtils.toast(getString(R.string.toast_server_error));
                            return;
                        }
                        if (200 == obj.getIntValue("code")) {
                            spu.setUser(null);

                            TUtils.toast(getString(R.string.toast_password_change_success), Toast.LENGTH_LONG);
                            Intent intent = new Intent();
                            intent.setAction(ZY_RightFragment.ACTION_QUIT);
                            activity.sendBroadcast(intent);
                            activity.finish();
                        } else {
                            TUtils.toast(getString(R.string.toast_password_change_failed));
                        }
                    }
                });
            }

            break;
            case R.id.stitle_ll_back: {
                activity.onBackPressed();
            }
            break;
        }
    }
}