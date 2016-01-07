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
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;

import com.squareup.okhttp.Request;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZQ_RegisterFragment_New extends BaseFragment implements View.OnClickListener {

    private TextView stitle_tv_content;
    private EditText lgr_et_name_id;
    private EditText lgr_et_email_id;
    private EditText lgr_et_pwd_id;
    private EditText lgr_et_pwd_id1;
    private Button lgr_bt_register;
    private View stitle_ll_back;


    private String name;
    private String email;
    private String pwd;

    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_rgister_new, container, false);
        initViews(view);
        tag = OkHttpClientManager.getTag();
        return view;
    }

    private void initViews(View view) {
        stitle_ll_back = view.findViewById(R.id.stitle_ll_back);
        stitle_ll_back.setOnClickListener(this);
        stitle_tv_content = (TextView) view.findViewById(R.id.stitle_tv_content);
        lgr_et_name_id = (EditText) view.findViewById(R.id.lgr_et_name_id);
        lgr_et_email_id = (EditText) view.findViewById(R.id.lgr_et_email_id);
        lgr_et_pwd_id = (EditText) view.findViewById(R.id.lgr_et_pwd_id);
        lgr_et_pwd_id1 = (EditText) view.findViewById(R.id.lgr_et_pwd_id1);
        lgr_bt_register = (Button) view.findViewById(R.id.lgr_bt_register);
        lgr_bt_register.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stitle_tv_content.setText(R.string.title_user_register);

    }


    private void showDialog() {
    }

    //判断email格式是否正确
    public boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);

        return m.matches();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lgr_bt_register: {
                if (!MyCommonUtil.isNetworkConnected(activity)) {
                    TUtils.toast(getString(R.string.toast_network_error));
                    return;
                }


                this.name = lgr_et_name_id.getText().toString();
                if (name == null && name == "") {
                    TUtils.toast("用户名不能为空！");
                    return;
                }

                this.email = lgr_et_email_id.getText().toString();
                if (!isEmail(email)) {
                    TUtils.toast("邮箱格式不对！");
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

                showDialog();


                // 注册
                Map<String, String> params = RequestParamsUtils.getMaps();
                params.put("username", name);
                params.put("password", pwd);
                params.put("email", email);
                SPUtil.addParams(params);
                OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.REGISTER// InterfaceApi.mRegister
                        , new OkHttpClientManager.ResultCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        JSONObject object = FjsonUtil.parseObject(response.toString());
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
                        } else if (45001 == object.getIntValue("code")) {
                            TUtils.toast(object.getString("msg"));
                        } else {

                            TUtils.toast(getString(R.string.toast_register_failed));
                        }
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        //服务器未响应
                        TUtils.toast(getString(R.string.toast_server_no_response));
                    }


                }, params);

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

    @Override
    public void onDestroyView() {
        OkHttpClientManager.cancel(tag);
        super.onDestroyView();
    }
}
