package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.color.tools.mytools.FjsonUtil;
import com.color.tools.mytools.LogUtils;
import com.color.tools.mytools.SystemUtils;
import com.color.tools.mytools.TUtil;
import com.hzpd.hflt.R;
import com.hzpd.modle.ReplayBean;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.XF_PInfoActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.lang.reflect.InvocationTargetException;


public class XF_NewsCommentsFragment extends BaseFragment {
    private static final String HTMLURL = InterfaceJsonfile.PATH_ROOT + "/Comment/showcommentv3/nid/";


    @ViewInject(R.id.xf_comments_iv_back)
    private ImageView xf_comments_iv_back;
    @ViewInject(R.id.xf_comments_wv_detail)
    private WebView xf_comments_wv_detail;
    @ViewInject(R.id.xf_comment_et_comment)
    private EditText xf_comment_et_comment;
    @ViewInject(R.id.xf_comment_tv_publish)
    private TextView xf_comment_tv_publish;

    private ReplayBean bean;

    private String commentItemCid;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xf_newscomments_layout2, container, false);
        ViewUtils.inject(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    protected void initData() {
        Bundle bundle = getArguments();
        bean = (ReplayBean) bundle.getSerializable("reply");
        LogUtils.i("nid---" + bean.getId() + " mNewtype-->" + bean.getType());

        if (null == bean) {
            return;
        }

        WebSettings webSettings = xf_comments_wv_detail.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(App.getInstance().getAllDiskCacheDir());
        if (SystemUtils.isNetworkAvailable(activity)) {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webSettings.setAllowFileAccess(true);


        xf_comments_wv_detail.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                TUtil.toast(activity, message);
                result.cancel();
                return true;
            }
        });

        xf_comments_wv_detail.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				xfzj://userinfo/uid/113
//				xfzj://comment?nid=66&uid=1&cid=5&type=News&siteid=1
                if (TextUtils.isEmpty(url)) {
                    return true;
                }
                Log.i("comment_url-->", url);
                if (url.startsWith("xfzj://")) {
                    url = url.replace("xfzj://", "");

                    if (url.startsWith("comment?")) {
                        url = url.replace("comment?", "");
                        LogUtils.i("query-->" + url);
                        String[] querys = url.split("&");
                        for (String s : querys) {
                            String[] kv = s.split("=");
                            if ("cid".equals(kv[0])) {
                                commentItemCid = kv[1];
                                break;
                            }
                        }

                        if (!TextUtils.isEmpty(commentItemCid)) {
                            LogUtils.i("commentItemCid--" + commentItemCid);

                            App.inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                            xf_comment_et_comment.requestFocus();

                        }

                    } else if (url.startsWith("userinfo/uid/")) {
                        url = url.replace("userinfo/uid/", "");

                        Intent intent = new Intent(activity, XF_PInfoActivity.class);
                        intent.putExtra("uid", url);
                        activity.startActivity(intent);
                        AAnim.ActivityStartAnimation(activity);
                    }

                }

//				commentItemCid=
                return true;
            }
        });

        String url = HTMLURL + bean.getId() + "/type/" + bean.getType() + "/siteid/" + InterfaceJsonfile.SITEID + "/page/1/pagesize/10";
        if (null != spu.getUser()) {
            url += "/uid/" + spu.getUser().getUid();
        }

        Log.i("url-->", url);
        xf_comments_wv_detail.loadUrl(url);

    }

    @OnClick(R.id.xf_comment_tv_publish)
    private void publish(View view) {

        String content = xf_comment_et_comment.getText().toString();
        if (TextUtils.isEmpty(content)) {
            TUtil.toast(activity, "输入内容不能为空");
            return;
        }

        if (TextUtils.isEmpty(commentItemCid)) {
            commentNews(content);
        } else {
            reply(content);
        }

    }

    private void commentNews(String content) {

        if (null == spu.getUser()) {
            TUtil.toast(activity, "请登录");
            return;
        }

        LogUtils.i("type:" + bean.getType());
        LogUtils.i("json_url:" + bean.getJsonUrl());
        LogUtils.i("uid:" + spu.getUser().getUid());
        LogUtils.i("nid:" + bean.getId());


        xf_comment_tv_publish.setClickable(false);
        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", spu.getUser().getUid());
        params.addBodyParameter("title", bean.getTitle());
        params.addBodyParameter("type", bean.getType());
        params.addBodyParameter("nid", bean.getId());
        params.addBodyParameter("content", content);
        params.addBodyParameter("json_url", bean.getJsonUrl());
//		params.addBodyParameter("smallimg", bean.getImgUrl());
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.PUBLISHCOMMENT// InterfaceApi.mSendComment
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                LogUtils.i("arg1-->" + arg1);
                TUtil.toast(activity, "服务器未响应");
                xf_comment_tv_publish.setClickable(true);
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                LogUtils.i("news-comment-->" + arg0.result);
                JSONObject obj = FjsonUtil.parseObject(arg0.result);
                if (null == obj) {
                    return;
                }

                if (200 == obj.getIntValue("code")) {
                    TUtil.toast(activity, obj.getString("msg"));
                    xf_comment_et_comment.setText("");
                    EventUtils.sendComment(activity);
                } else {
                    TUtil.toast(activity, "评论失败");
                }
                xf_comment_tv_publish.setClickable(true);
            }
        });
    }


    private void reply(String content) {
        if (null == spu.getUser()) {
            TUtil.toast(activity, "请登录");
            return;
        }

        xf_comment_tv_publish.setClickable(false);
        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", spu.getUser().getUid());
        params.addBodyParameter("title", bean.getTitle());
        params.addBodyParameter("type", bean.getType());
        params.addBodyParameter("nid", bean.getId());
        params.addBodyParameter("cid", commentItemCid);
        params.addBodyParameter("content", content);
        params.addBodyParameter("json_url", bean.getJsonUrl());
//		params.addBodyParameter("smallimg", bean.getImgUrl());
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.PUBLISHCOMMENT// InterfaceApi.mSendComment
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                LogUtils.i("arg1-->" + arg1);
                TUtil.toast(activity, "服务器未响应");
                xf_comment_tv_publish.setClickable(true);
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                LogUtils.i("news-comment-->" + arg0.result);
                JSONObject obj = FjsonUtil.parseObject(arg0.result);
                commentItemCid = null;
                if (null == obj) {
                    return;
                }

                if (200 == obj.getIntValue("code")) {
                    TUtil.toast(activity, obj.getString("msg"));
                    xf_comment_et_comment.setText("");
                    EventUtils.sendComment(activity);
                } else {
                    TUtil.toast(activity, "评论失败");
                }
                xf_comment_tv_publish.setClickable(true);
            }
        });


    }

    @OnClick(R.id.xf_comments_iv_back)
    private void goback(View v) {
        activity.onBackPressed();
    }


    @Override
    public void onPause() {
        try {
            xf_comments_wv_detail.getClass().getMethod("onPause")
                    .invoke(xf_comments_wv_detail, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            xf_comments_wv_detail.getClass().getMethod("onResume")
                    .invoke(xf_comments_wv_detail, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
