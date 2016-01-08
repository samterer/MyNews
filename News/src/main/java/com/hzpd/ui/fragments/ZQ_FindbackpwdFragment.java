package com.hzpd.ui.fragments;

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

import com.hzpd.hflt.R;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.TUtils;

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
    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.findpwdback_layout, container,
                false);
        initViews(view);
        tag = OkHttpClientManager.getTag();
        return view;
    }

    private void initViews(View view) {
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
            case R.id.fpb_bt_verify:
            break;
            case R.id.fpb_bt_submmit:

            break;
            case R.id.stitle_ll_back: {
                activity.onBackPressed();
            }
            break;
        }
    }

    @Override
    public void onDestroyView() {
        OkHttpClientManager.cancel(tag);
        super.onDestroyView();
    }
}