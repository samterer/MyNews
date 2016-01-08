package com.hzpd.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.url.OkHttpClientManager;

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
            case R.id.lgr_bt_register:

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
