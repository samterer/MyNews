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
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;

import com.squareup.okhttp.Request;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


public class XF_NewsCommentsFragment extends BaseFragment implements View.OnClickListener {
    private static final String HTMLURL = InterfaceJsonfile.PATH_ROOT + "/Comment/showcommentv3/nid/";


    private ImageView xf_comments_iv_back;
    private WebView xf_comments_wv_detail;
    private EditText xf_comment_et_comment;
    private TextView xf_comment_tv_publish;

    private ReplayBean bean;

    private String commentItemCid;
    private Object tag;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xf_newscomments_layout2, container, false);
        xf_comments_iv_back = (ImageView) view.findViewById(R.id.xf_comments_iv_back);
        xf_comments_iv_back.setOnClickListener(this);
        xf_comments_wv_detail = (WebView) view.findViewById(R.id.xf_comments_wv_detail);
        xf_comment_et_comment = (EditText) view.findViewById(R.id.xf_comment_et_comment);
        xf_comment_tv_publish = (TextView) view.findViewById(R.id.xf_comment_tv_publish);
        xf_comment_tv_publish.setOnClickListener(this);
        tag = OkHttpClientManager.getTag();

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
        Map<String, String> params = new HashMap<>();
        params.put("uid", spu.getUser().getUid());
        params.put("title", bean.getTitle());
        params.put("type", bean.getType());
        params.put("nid", bean.getId());
        params.put("content", content);
        params.put("json_url", bean.getJsonUrl());
        params.put("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.PUBLISHCOMMENT// InterfaceApi.mSendComment
                , new OkHttpClientManager.ResultCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            JSONObject obj = FjsonUtil.parseObject(response.toString());
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        TUtil.toast(activity, "服务器未响应");
                        xf_comment_tv_publish.setClickable(true);
                    }
                }, params
        );
    }

    private void reply(String content) {
        if (null == spu.getUser()) {
            TUtil.toast(activity, "请登录");
            return;
        }

        xf_comment_tv_publish.setClickable(false);
        Map<String, String> params = new HashMap<>();
        params.put("uid", spu.getUser().getUid());
        params.put("title", bean.getTitle());
        params.put("type", bean.getType());
        params.put("nid", bean.getId());
        params.put("cid", commentItemCid);
        params.put("content", content);
        params.put("json_url", bean.getJsonUrl());
//		params.addBodyParameter("smallimg", bean.getImgUrl());
        params.put("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.PUBLISHCOMMENT// InterfaceApi.mSendComment
                , new OkHttpClientManager.ResultCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            JSONObject obj = FjsonUtil.parseObject(response.toString());
                            commentItemCid = null;
                            if (null == obj) {
                                return;
                            }

                            if (200 == obj.getIntValue("code")) {
                                TUtil.toast(activity, obj.getString("msg"));
                                xf_comment_et_comment.setText("");
                                EventUtils.sendComment(activity);
                            } else if (isAdded()) {
                                TUtil.toast(activity, getString(R.string.toast_collect_failed));
                            }
                            xf_comment_tv_publish.setClickable(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        TUtil.toast(activity, "服务器未响应");
                        xf_comment_tv_publish.setClickable(true);
                    }

                }, params
        );


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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.xf_comments_iv_back:
                activity.onBackPressed();
                break;
            case R.id.xf_comment_tv_publish:

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
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OkHttpClientManager.cancel(tag);
    }
}
