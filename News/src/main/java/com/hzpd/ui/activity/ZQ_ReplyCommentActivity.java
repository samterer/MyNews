package com.hzpd.ui.activity;

/**
 * Created by taoshuang on 2015/10/8.
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.Log;
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

public class ZQ_ReplyCommentActivity extends MBaseActivity {

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.comment;
    }

    @ViewInject(R.id.zq_reply_et_content)
    private EditText zq_reply_et_content;

    private String bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zq_reply_comment_layout);
        ViewUtils.inject(this);

        Intent intent = getIntent();
        if (null != intent) {
            bean = intent.getStringExtra("USER_UID");
            Log.e("test", "bean-->" + bean);
        }

    }

    @OnClick({R.id.zq_reply_tv_cancle, R.id.zq_reply_tv_send})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.zq_reply_tv_cancle: {
                finish();
            }
            break;
            case R.id.zq_reply_tv_send: {

                String comment = zq_reply_et_content.getText().toString();
                if (null == comment || "".equals(comment)) {
                    TUtils.toast(getString(R.string.toast_input_cannot_be_empty));
                    return;
                }
                sendComment(comment);
            }
            break;

            default:
                break;
        }
    }

    //7784
    // 发表评论
    private void sendComment(String content) {
        if (spu.getUser() == null) {
            return;
        }
        String uid = spu.getUser().getUid();

        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("uid", uid);
        params.addBodyParameter("type", "2");
        params.addBodyParameter("nid", bean);
        params.addBodyParameter("content", content);
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.PUBLISHCOMMENTCOMENT// InterfaceApi.mSendComment
                , params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                LogUtils.i("arg1-->" + arg1);
                Log.i("msg", arg1);
                TUtils.toast(getString(R.string.toast_server_no_response));
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                LogUtils.i("news-comment-->" + arg0.result);
                JSONObject obj = null;
                try {
                    obj = JSONObject.parseObject(arg0.result);
                } catch (Exception e) {
                    return;
                }

                if (200 == obj.getIntValue("code")) {
//                    TUtils.toast(obj.getString("msg"));
                    EventUtils.sendComment(activity);
                    finish();
                } else {
                    TUtils.toast(getString(R.string.toast_fail_to_comment));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}