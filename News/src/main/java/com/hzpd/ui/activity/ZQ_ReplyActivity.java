package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.ReplayBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.UpdateNewsBeanDbEvent;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;

import de.greenrobot.event.EventBus;

public class ZQ_ReplyActivity extends MBaseActivity implements View.OnClickListener {
    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.comment;
    }

    private EditText zq_reply_et_content;
    private ImageView zq_reply_share_iv;
    private TextView stitle_tv_content;
    private View stitle_ll_back;
    private ImageView iv_reply_share;
    private boolean isShare = false;
    private ReplayBean bean;
    private View loadingView;
    private RelativeLayout rl_share1;
    private View zq_reply_tv_send;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zq_reply_layout);
        Intent intent = getIntent();
        if (null != intent) {
            bean = (ReplayBean) intent.getSerializableExtra("replay");
        }
        super.changeStatusBar();
        
        initViews();

        rl_share1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShare) {
                    iv_reply_share.setImageResource(R.drawable.iv_reply_share_select);
                    isShare = true;
                } else {
                    iv_reply_share.setImageResource(R.drawable.iv_reply_share_unselect);
                    isShare = false;
                }
            }
        });

    }

    private void initViews() {
        stitle_tv_content = (TextView) findViewById(R.id.stitle_tv_content);
        stitle_tv_content.setText(R.string.comment);
        zq_reply_et_content = (EditText) findViewById(R.id.zq_reply_et_content);
        zq_reply_share_iv = (ImageView) findViewById(R.id.zq_reply_share_iv);
        zq_reply_share_iv.setOnClickListener(this);
        loadingView = findViewById(R.id.app_progress_bar);
        rl_share1 = (RelativeLayout) findViewById(R.id.rl_share1);
        stitle_ll_back = findViewById(R.id.stitle_ll_back);
        stitle_ll_back.setOnClickListener(this);
        zq_reply_tv_send = findViewById(R.id.zq_reply_tv_send);
        zq_reply_tv_send.setOnClickListener(this);
        iv_reply_share = (ImageView) findViewById(R.id.iv_reply_share);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("replay", bean);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (null == bean) {
            bean = (ReplayBean) savedInstanceState.getSerializable("replay");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    // 发表评论
    private void sendComment(final String content, final String comcount) {

        spu.getUser();
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("uid", spu.getUser().getUid());
//        params.addBodyParameter("uid","53");
        params.addBodyParameter("title", bean.getTitle());
        params.addBodyParameter("type", bean.getType());//"News"
        params.addBodyParameter("nid", bean.getId());
        params.addBodyParameter("content", content);
        params.addBodyParameter("json_url", bean.getJsonUrl());
        params.addBodyParameter("smallimg", bean.getImgUrl());
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.PUBLISHCOMMENT// InterfaceApi.mSendComment
                , params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                Log.i("msg", arg1);
                loadingView.setVisibility(View.GONE);
                TUtils.toast(getString(R.string.toast_server_no_response));
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                LogUtils.i("news-comment-->" + arg0.result);
                loadingView.setVisibility(View.GONE);
                JSONObject obj = null;
                try {
                    obj = JSONObject.parseObject(arg0.result);
                } catch (Exception e) {
                    return;
                }

                if (200 == obj.getIntValue("code")) {
                    setComcountsId(bean.getId(), comcount);
                    TUtils.toast(getString(R.string.comment_ok));
                    EventBus.getDefault().post(new UpdateNewsBeanDbEvent("Update_OK"));
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", content);
                    bundle.putBoolean("isShare", isShare);
                    resultIntent.putExtras(bundle);
                    setResult(RESULT_OK, resultIntent);
                    EventUtils.sendComment(activity);
                    finish();
                } else {
                    TUtils.toast(getString(R.string.toast_fail_to_comment));
                }
            }
        });
    }

    public void setComcountsId(String nid, String comcount) {
        try {
            NewsBeanDB nbdb = new NewsBeanDB();
            nbdb.setNid(Integer.parseInt(nid));
            int counts = Integer.parseInt(comcount) + 1;
            nbdb.setComcount("" + counts);
            dbHelper.getNewsListDbUtils().update(nbdb
                    , WhereBuilder.b("nid", "=", nid)
                    , "comcount");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        if (AvoidOnClickFastUtils.isFastDoubleClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.stitle_ll_back:
                finish();
                break;
            case R.id.zq_reply_tv_cancle: {
                finish();
            }
            break;
            case R.id.zq_reply_tv_send: {
                String comcount = bean.getComcount();
                String comment = zq_reply_et_content.getText().toString();
                if (null == comment || "".equals(comment)) {
                    TUtils.toast(getString(R.string.toast_input_cannot_be_empty));
                    return;
                }
                loadingView.setVisibility(View.VISIBLE);
                sendComment(comment, comcount);
            }
            break;

            default:
                break;
        }
    }
}