package com.hzpd.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.color.tools.mytools.LogUtils;
import com.hzpd.hflt.R;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.squareup.okhttp.Request;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZQ_FeedBackActivity extends MBaseActivity implements View.OnClickListener {
    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.feedback;
    }

    private TextView stitle_tv_content;
    private EditText zq_feedback_et_content;
    private EditText zq_feedback_et_email;
    private Button zq_feedback_btn_submit;
    private long start;
    private View cover_top;
    private View stitle_ll_back;

    private Object tag;
    private View app_progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zq_feedback_layout);
        super.changeStatusBar();
        initViews();
        tag = OkHttpClientManager.getTag();
        SPUtil.setFont(zq_feedback_et_content);
        SPUtil.setFont(zq_feedback_et_email);
        SPUtil.setFont(zq_feedback_btn_submit);
        stitle_tv_content.setText(getString(R.string.prompt_feedback));
    }

    private void initViews() {
        stitle_tv_content = (TextView) findViewById(R.id.stitle_tv_content);
        zq_feedback_et_content = (EditText) findViewById(R.id.zq_feedback_et_content);
        zq_feedback_et_email = (EditText) findViewById(R.id.zq_feedback_et_email);
        zq_feedback_btn_submit = (Button) findViewById(R.id.zq_feedback_btn_submit);
        zq_feedback_btn_submit.setOnClickListener(this);
        stitle_ll_back = findViewById(R.id.stitle_ll_back);
        stitle_ll_back.setOnClickListener(this);
        cover_top = findViewById(R.id.cover_top);
        if (App.getInstance().getThemeName().equals("0")) {
            cover_top.setVisibility(View.GONE);
        } else {
            cover_top.setVisibility(View.VISIBLE);
        }
        app_progress_bar = findViewById(R.id.app_progress_bar);
    }

    @Override
    protected void onDestroy() {
        OkHttpClientManager.cancel(tag);
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zq_feedback_btn_submit:
                if (start > 0) {
                    if (System.currentTimeMillis() - start < 2000) {
                        return;
                    }
                }
                String content = zq_feedback_et_content.getText().toString();
                String email = zq_feedback_et_email.getText().toString();
                submit(content, email);
                start = System.currentTimeMillis();
                break;
            case R.id.stitle_ll_back:
                finish();
                break;
        }

    }


    private void submit(String content, String email) {
        if (TextUtils.isEmpty(content) || content.length() < 10) {
            TUtils.toast(getString(R.string.toast_feedback_cannot_be_short));//太短
            return;
        }
        if (!TextUtils.isEmpty(email)) {
            if (!isEmail(email)) {
                TUtils.toast(getString(R.string.toast_email_cannot_be_error));//格式不正确
                return;
            }
        }

        app_progress_bar.setVisibility(View.VISIBLE);
        Map<String, String> params = RequestParamsUtils.getMaps();
        params.put("siteid", InterfaceJsonfile.SITEID);
        params.put("Email", email);
        params.put("content", content);
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.feedback, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onFailure(Request request, Exception e) {
                app_progress_bar.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(Object response) {
                app_progress_bar.setVisibility(View.GONE);
                String json = response.toString();
                JSONObject obj = FjsonUtil.parseObject(json);
                if (null != obj) {
                    if (200 == obj.getIntValue("code")) {
                        TUtils.toast(getString(R.string.feed_ok));
                        zq_feedback_et_content.setText("");
                        zq_feedback_et_email.setText("");
                        finish();
                    } else {
                        TUtils.toast(getString(R.string.feed_fail));
                    }
                } else {
                    TUtils.toast(getString(R.string.toast_server_error));
                }
            }

        }, params);

    }

    //判断email格式是否正确
    public boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

}