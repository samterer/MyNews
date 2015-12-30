package com.hzpd.ui.fragments;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.color.tools.mytools.FileUtil;
import com.color.tools.mytools.FjsonUtil;
import com.color.tools.mytools.LogUtils;
import com.color.tools.mytools.NetworkUtils;
import com.color.tools.mytools.SystemUtils;
import com.color.tools.mytools.TUtil;
import com.hzpd.custorm.VideoEnabledWebChromeClient;
import com.hzpd.custorm.VideoEnabledWebView;
import com.hzpd.hflt.R;
import com.hzpd.hflt.wxapi.FacebookSharedUtil;
import com.hzpd.modle.CommentsCountBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsDetailBean;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.ReplayBean;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.XF_NewsHtmlDetailActivity;
import com.hzpd.url.Constant;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.showwebview.MyJavascriptInterface;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;


public class XF_NewsDetailFragment extends BaseFragment implements View.OnClickListener {
    private static final String BASEURL = InterfaceJsonfile.PATH_ROOT + "/Public/newsview/nid/";

    private static final String HTMLURL = InterfaceJsonfile.PATH_ROOT + "/News/newsInfo2Html/nid/";

    private ImageView xf_newshtmldetail_iv_back;// 返回
    private TextView xf_newshtmldetail_tv_comments;// 跟贴数
    private TextView xf_newshtmldetail_tv_comment;// 编辑评论
    private ImageView xf_newshtmldetail_iv_share;// 分享
    private ImageView xf_newshtmldetail_iv_collection;// 收藏
    private ImageView xf_newshtmldetail_iv_praise;// 赞文章
    private VideoEnabledWebView xf_newshtmldetail_wv_detail;
    private VideoEnabledWebChromeClient webChromeClient;
    private RelativeLayout xf_newshtmldetail_nonVideoLayout;
    private FrameLayout xf_newshtmldetail_videoLayout;
    private View xf_newshtmldetail_videoLoading;

    private NewsBean nb;
    private NewsDetailBean newsdetailBean;
    private String detailPathRoot;

    private DbUtils dbUtils;

    private String from;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xf_newshtmldetails_layout, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        xf_newshtmldetail_iv_back = (ImageView) view.findViewById(R.id.xf_newshtmldetail_iv_back);
        xf_newshtmldetail_iv_back.setOnClickListener(this);
        xf_newshtmldetail_tv_comments = (TextView) view.findViewById(R.id.xf_newshtmldetail_tv_comments);
        xf_newshtmldetail_tv_comments.setOnClickListener(this);
        xf_newshtmldetail_tv_comment = (TextView) view.findViewById(R.id.xf_newshtmldetail_tv_comment);
        xf_newshtmldetail_tv_comment.setOnClickListener(this);
        xf_newshtmldetail_iv_share = (ImageView) view.findViewById(R.id.xf_newshtmldetail_iv_share);
        xf_newshtmldetail_iv_share.setOnClickListener(this);
        xf_newshtmldetail_iv_collection = (ImageView) view.findViewById(R.id.xf_newshtmldetail_iv_collection);
        xf_newshtmldetail_iv_collection.setOnClickListener(this);
        xf_newshtmldetail_iv_praise = (ImageView) view.findViewById(R.id.xf_newshtmldetail_iv_praise);
        xf_newshtmldetail_iv_praise.setOnClickListener(this);
        xf_newshtmldetail_wv_detail = (VideoEnabledWebView) view.findViewById(R.id.xf_newshtmldetail_wv_detail);
        xf_newshtmldetail_nonVideoLayout = (RelativeLayout) view.findViewById(R.id.xf_newshtmldetail_nonVideoLayout);
        xf_newshtmldetail_videoLayout = (FrameLayout) view.findViewById(R.id.xf_newshtmldetail_videoLayout);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    protected void initData() {

        Bundle bundle = getArguments();
        nb = (NewsBean) bundle.getSerializable("nb");
        from = (String) bundle.getSerializable("from");
        if (null == nb) {
            return;
        }
        Log.i("nb nid-->", nb.getNid() + ":::" + nb.getComcount());
        dbUtils = DbUtils.create(activity, App.getInstance()
                .getJsonFileCacheRootDir(), App.collectiondbname);
        if ("0".equals(nb.getTid())) {
            detailPathRoot = App.getInstance().getJsonFileCacheRootDir()
                    + File.separator + "subject" + File.separator + "notid"
                    + File.separator;
        } else {
            detailPathRoot = App.getInstance().getJsonFileCacheRootDir()
                    + File.separator + "channel_" + nb.getTid()
                    + File.separator + "newsdetail" + File.separator;
        }


        if (!"0".equals(nb.getComflag())) {
            String counts = nb.getComcount();
            if (!TextUtils.isEmpty(counts)) {
                try {
                    int cou = Integer.parseInt(counts);
                    if (cou > 0) {
                        xf_newshtmldetail_tv_comments.setText(counts + "跟贴");
                    } else if (cou < 0) {
                        getCommentsCounts();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        WebSettings webSettings = xf_newshtmldetail_wv_detail.getSettings();
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


        xf_newshtmldetail_videoLoading = activity.getLayoutInflater().inflate(
                R.layout.xf_view_loading_video, null);

        xf_newshtmldetail_wv_detail.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url)) {
                    Log.i("hot url-->", url);
                    if (url.startsWith("xfzj://")) {
                        String imgs[] = nb.getImgs();
                        String img = "";
                        if (null == imgs || imgs.length > 0) {
                            img = nb.getImgs()[0];
                        }
                        ReplayBean bean = new ReplayBean(nb.getNid(), nb.getTitle(),
                                Constant.TYPE.News.toString(), nb.getJson_url(), img, nb.getComcount());

                        ((XF_NewsHtmlDetailActivity) activity).toNewsComments(bean);
                        return true;
                    }
                }
                LogUtils.i("newsdetail url--->" + url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                xf_newshtmldetail_wv_detail.loadUrl("javascript:getimg()");
            }

        });

        webChromeClient = new VideoEnabledWebChromeClient(xf_newshtmldetail_nonVideoLayout
                , xf_newshtmldetail_videoLayout, xf_newshtmldetail_videoLoading
                , xf_newshtmldetail_wv_detail) {
            @Override
            public void onProgressChanged(WebView view, int progress) {
            }
        };
        webChromeClient.setOnToggledFullscreen(
                new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
                    @Override
                    public void toggledFullscreen(boolean fullscreen) {
                        if (fullscreen) {
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            WindowManager.LayoutParams attrs = activity.getWindow()
                                    .getAttributes();
                            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                            activity.getWindow().setAttributes(attrs);
                            if (android.os.Build.VERSION.SDK_INT >= 14) {
                                activity.getWindow()
                                        .getDecorView()
                                        .setSystemUiVisibility(
                                                View.SYSTEM_UI_FLAG_LOW_PROFILE);
                            }

                        } else {
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            WindowManager.LayoutParams attrs = activity.getWindow()
                                    .getAttributes();
                            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                            activity.getWindow().setAttributes(attrs);
                            if (android.os.Build.VERSION.SDK_INT >= 14) {
                                activity.getWindow().getDecorView()
                                        .setSystemUiVisibility(
                                                View.SYSTEM_UI_FLAG_VISIBLE);
                            }
                        }

                    }

                });
        xf_newshtmldetail_wv_detail.setWebChromeClient(webChromeClient);


        if ("browser".equals(from)) {
            getNewsDetails(nb.getTid());
        } else {
            getNewsDetails();
        }

        isCollection();
        addBrowse();
        EventUtils.sendReadAtical(activity);
    }

    // 监听事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.xf_newshtmldetail_tv_comments:// 跳转到评论列表页
                break;
            case R.id.xf_newshtmldetail_tv_comment: {// 打开评论
                if (null == nb || null == newsdetailBean) {
                    return;
                }
                if (!"0".equals(nb.getComflag())) {
                    // 跳转到评论页
                    if (!NetworkUtils.isNetworkAvailable(activity)) {
                        TUtil.toast(activity, "请检查网络");
                        return;
                    }
                    if (null == nb) {
                        return;
                    }

//				Intent intent = new Intent(activity, XF_CommentActivity.class);
                    String imgs[] = nb.getImgs();
                    String img = "";
                    if (null != imgs && imgs.length > 0) {
                        img = nb.getImgs()[0];
                    }
                    ReplayBean bean = new ReplayBean(nb.getNid(), nb.getTitle(),
                            Constant.TYPE.News.toString(), nb.getJson_url(), img, nb.getComcount());

                    ((XF_NewsHtmlDetailActivity) activity).toNewsComments(bean);

                }

            }
            break;
            case R.id.xf_newshtmldetail_iv_share: {
                LogUtils.i("click share");
                String imgurl = null;
                if (null != nb.getImgs() && nb.getImgs().length > 0) {
                    imgurl = nb.getImgs()[0];
                }
                FacebookSharedUtil.showShares(newsdetailBean.getTitle(),
                        BASEURL + newsdetailBean.getNid(), imgurl, activity);

            }
            break;
            case R.id.xf_newshtmldetail_iv_collection: {// 收藏
                addCollection();
            }
            break;
            case R.id.xf_newshtmldetail_iv_praise: {// 赞文章
                praiseArtical();
            }
            break;
            case R.id.xf_newshtmldetail_iv_back: {
                activity.onBackPressed();
            }
            break;
        }
    }

    // 自浏览器打开
    private void getNewsDetails(String tid) {

        // 获取列表项
        RequestParams params = new RequestParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("nid", tid);
        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.bnewsItem, params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        LogUtils.i("result-->" + responseInfo.result);
                        JSONObject obj = null;
                        try {
                            obj = JSONObject.parseObject(responseInfo.result);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        if (200 == obj.getIntValue("code")) {
                            JSONObject object = obj.getJSONObject("data");
                            nb = JSONObject.parseObject(object.toJSONString(),
                                    NewsBean.class);

                            getNewsDetails();

                        } else {
                            TUtil.toast(activity, obj.getString("msg"));
                        }
                    }

                    @Override
                    public void onFailure(com.lidroid.xutils.exception.HttpException e, String s) {
                        TUtil.toast(activity, R.string.toast_server_no_response);
                    }

                });
    }

    // 添加收藏
    private void addCollection() {
        if (null == spu.getUser()) {
            NewsItemBeanForCollection nibfc = new NewsItemBeanForCollection(nb);
            try {
                NewsItemBeanForCollection mnbean = dbUtils.findFirst(Selector
                        .from(NewsItemBeanForCollection.class).where(
                                "colldataid", "=", nb.getNid()));

                if (mnbean == null) {
                    dbUtils.save(nibfc);
                    TUtil.toast(activity, "收藏成功");
                    long co = dbUtils.count(NewsItemBeanForCollection.class);
                    LogUtils.i("num:" + co);
                    LogUtils.i("type-->" + nibfc.getType());
                    xf_newshtmldetail_iv_collection
                            .setImageResource(R.drawable.xf_bt_yishoucang);
                } else {
                    dbUtils.delete(NewsItemBeanForCollection.class,
                            WhereBuilder.b("colldataid", "=", nb.getNid()));
                    TUtil.toast(activity, "收藏取消");
                    xf_newshtmldetail_iv_collection
                            .setImageResource(R.drawable.xf_bt_shoucang);
                }
            } catch (Exception e) {
                e.printStackTrace();
                TUtil.toast(activity, "收藏失败");
            }
            return;
        }

        xf_newshtmldetail_iv_collection.setImageResource(R.drawable.xf_bt_shoucang);

        LogUtils.i("Type-->" + nb.getType() + "  Fid-->" + nb.getNid());
        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", spu.getUser().getUid());
        params.addBodyParameter("type", "1");
        params.addBodyParameter("typeid", nb.getNid());
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("data", nb.getJson_url());

        LogUtils.i("uid-->" + spu.getUser().getUid());
        LogUtils.i("type-->" + 1);
        LogUtils.i("typeid-->" + nb.getNid());
        LogUtils.i("siteid-->" + InterfaceJsonfile.SITEID);

        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.ADDCOLLECTION// InterfaceApi.addcollection
                , params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i("result-->" + responseInfo.result);
                JSONObject obj = null;

                try {
                    obj = JSONObject.parseObject(responseInfo.result);
                    if (200 == obj.getIntValue("code")) {
                        JSONObject object = obj.getJSONObject("data");
                        // 1:收藏操作成功 2:取消收藏操作成功
                        if ("1".equals(object.getString("status"))) {
                            xf_newshtmldetail_iv_collection
                                    .setImageResource(R.drawable.xf_bt_yishoucang);
                            TUtil.toast(activity, "收藏成功");
                        } else {
                            xf_newshtmldetail_iv_collection
                                    .setImageResource(R.drawable.xf_bt_shoucang);
                            TUtil.toast(activity, "收藏取消");
                        }
                    } else {
                        TUtil.toast(activity, "收藏失败");
                    }
                } catch (Exception e) {
                    TUtil.toast(activity, "收藏失败");
                    return;
                }

            }

            @Override
            public void onFailure(com.lidroid.xutils.exception.HttpException e, String s) {

            }


        });

    }

    /**
     * 获取新闻
     */
    private void getNewsDetails() {
        File pageFile = App.getFile(detailPathRoot + "detail_" + nb.getNid());

        if (FileUtil.getFileSizes(pageFile) > 30) {

            String data = App.getFileContext(pageFile);
            LogUtils.i("data newsdetail cache-->" + data);
            JSONObject obj = FjsonUtil.parseObject(data);
            if (null == obj) {
                return;
            }
            newsdetailBean = FjsonUtil.parseObject(obj.getJSONObject("data")
                    .toJSONString(), NewsDetailBean.class);

            setData();
            return;
        }

        httpUtils.download(nb.getJson_url(),
                detailPathRoot + "detail_" + nb.getNid(),
                new RequestCallBack<File>() {
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {

                        String data = App.getFileContext(responseInfo.result);
                        if (null == data || "".equals(data)) {
                            TUtil.toast(activity, "请求失败");
                            return;
                        }
                        LogUtils.i("http data-->" + data);
                        JSONObject obj = null;
                        try {
                            obj = JSONObject.parseObject(data);
                        } catch (Exception e) {
                            responseInfo.result.delete();
                            e.printStackTrace();
                            TUtil.toast(activity, "缓存失效，请重新打开");
                            return;
                        }
                        newsdetailBean = JSONObject.parseObject(obj
                                        .getJSONObject("data").toJSONString(),
                                NewsDetailBean.class);
                        setData();
                    }

                    @Override
                    public void onFailure(com.lidroid.xutils.exception.HttpException e, String s) {

                    }


                });
    }


    // 是否收藏
    private void isCollection() {
        if (null != spu.getUser()) {
            RequestParams params = new RequestParams();
            params.addBodyParameter("uid", spu.getUser().getUid());
            params.addBodyParameter("typeid", nb.getNid());
            params.addBodyParameter("type", "1");
            httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.ISCELLECTION,
                    params, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            LogUtils.i("isCollection result-->"
                                    + responseInfo.result);
                            JSONObject obj = null;
                            try {
                                obj = JSONObject
                                        .parseObject(responseInfo.result);
                            } catch (Exception e) {
                                return;
                            }

                            if (200 == obj.getIntValue("code")) {
                                JSONObject object = obj.getJSONObject("data");
                                if ("1".equals(object.getString("status"))) {
                                    xf_newshtmldetail_iv_collection
                                            .setImageResource(R.drawable.xf_bt_yishoucang);
                                } else {
                                    xf_newshtmldetail_iv_collection
                                            .setImageResource(R.drawable.xf_bt_shoucang);
                                }
                            }
                        }

                        @Override
                        public void onFailure(com.lidroid.xutils.exception.HttpException e, String s) {

                        }

                    });
        } else {
            try {
                NewsItemBeanForCollection nbfc = dbHelper
                        .getCollectionDBUitls().findFirst(
                                Selector.from(NewsItemBeanForCollection.class)
                                        .where("colldataid", "=", nb.getNid())
                                        .and("type", "=", "1"));
                if (null != nbfc) {
                    xf_newshtmldetail_iv_collection
                            .setImageResource(R.drawable.xf_bt_yishoucang);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }

        if (null != spu.getUser()) {
            RequestParams params = new RequestParams();
            params.addBodyParameter("uid", spu.getUser().getUid());
            params.addBodyParameter("typeid", nb.getNid());
            params.addBodyParameter("type", "1");
            httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.ISCELLECTION,
                    params, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            LogUtils.i("isCollection result-->"
                                    + responseInfo.result);
                            JSONObject obj = null;
                            try {
                                obj = JSONObject
                                        .parseObject(responseInfo.result);
                            } catch (Exception e) {
                                return;
                            }

                            if (200 == obj.getIntValue("code")) {
                                JSONObject object = obj.getJSONObject("data");
                                if ("1".equals(object.getString("status"))) {
                                    xf_newshtmldetail_iv_collection
                                            .setImageResource(R.drawable.xf_bt_yishoucang);
                                } else {
                                    xf_newshtmldetail_iv_collection
                                            .setImageResource(R.drawable.xf_bt_shoucang);
                                }
                            }
                        }

                        @Override
                        public void onFailure(com.lidroid.xutils.exception.HttpException e, String s) {

                        }

                    });
        } else {
            try {
                NewsItemBeanForCollection nbfc = dbHelper
                        .getCollectionDBUitls().findFirst(
                                Selector.from(NewsItemBeanForCollection.class)
                                        .where("colldataid", "=", nb.getNid())
                                        .and("type", "=", "1"));
                if (null != nbfc) {
                    xf_newshtmldetail_iv_collection
                            .setImageResource(R.drawable.xf_bt_yishoucang);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    // 设置新闻详情
    private void setData() {
        if (null == newsdetailBean) {
            return;
        }
        Log.i("ndb nid-->", newsdetailBean.getNid() + "");
        String url = "";
        if ("9".equals(nb.getRtype())) {
            url = InterfaceJsonfile.HTMLURL + nb.getNid();
        } else {
            url = HTMLURL + nb.getNid() + "/device/" + SystemUtils.getDeviceId(activity);
        }
        if (null != spu.getUser()) {
            url = url + "/uid/" + spu.getUser().getUid();
        }
        xf_newshtmldetail_wv_detail.addJavascriptInterface(new MyJavascriptInterface(activity,
                newsdetailBean), "imagelistner");
        LogUtils.i("url-->" + url);
        xf_newshtmldetail_wv_detail.loadUrl(url);

        getCommentsCounts();//

    }

    // 评论数量
    private void getCommentsCounts() {
        RequestParams params = new RequestParams();
        params.addBodyParameter("type", Constant.TYPE.NewsA.toString());
        params.addBodyParameter("nids", nb.getNid());
        HttpUtils httpUtils = SPUtil.getHttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.Stat, params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String json = responseInfo.result;
                        LogUtils.i("getCommentsCounts-->" + json);

                        JSONObject obj = FjsonUtil
                                .parseObject(responseInfo.result);
                        if (null != obj) {
                            if (200 == obj.getIntValue("code")) {

                                List<CommentsCountBean> li = JSONObject
                                        .parseArray(obj.getString("data"),
                                                CommentsCountBean.class);
                                if (null != li) {
                                    for (CommentsCountBean cc : li) {
                                        if (nb.getNid().equals(cc.getNid())) {
                                            xf_newshtmldetail_tv_comments
                                                    .setVisibility(View.VISIBLE);
                                            xf_newshtmldetail_tv_comments
                                                    .setText(cc.getC_num()
                                                            + "跟贴");
                                        }
                                    }
                                }
                            } else {
                                TUtil.toast(activity, obj.getString("msg"));
                            }
                        } else {
                            TUtil.toast(activity, "服务器错误");
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                    }
                });
    }

    // 添加浏览量
    private void addBrowse() {
        RequestParams params = new RequestParams();
        params.addBodyParameter("type", Constant.TYPE.NewsA.toString());
        params.addBodyParameter("nid", nb.getNid());
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("num", "1");

        HttpUtils httpUtils = SPUtil.getHttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.XF_BROWSE, params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String json = responseInfo.result;
                        LogUtils.i("addBrowse-->" + json);

                        JSONObject obj = FjsonUtil
                                .parseObject(responseInfo.result);
                        if (null != obj) {
                            if (200 == obj.getIntValue("code")) {
                                JSONObject object = obj.getJSONObject("data");
                                if (null != object) {
                                    LogUtils.i("浏览量："
                                            + object.getString("v_num"));
                                }

                            } else {
                                LogUtils.i(obj.getString("msg"));
                            }
                        } else {
                            LogUtils.i("服务器错误");
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                    }
                });
    }

    // 赞文章
    private void praiseArtical() {

        if (null == spu.getUser()) {
            TUtil.toast(activity, "请登录");
            return;
        }
        TUtil.toast(activity, "赞");

        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", spu.getUser().getUid());
        params.addBodyParameter("type", Constant.TYPE.NewsA.toString());
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("cid", nb.getNid());

        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.XF_PRAISECOM, params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        LogUtils.i("praiseArtical-result-->"
                                + responseInfo.result);
                        JSONObject obj = FjsonUtil
                                .parseObject(responseInfo.result);
                        if (null == obj) {
                            return;
                        }
                        if (200 == obj.getIntValue("code")) {
                            JSONObject object = obj.getJSONObject("data");
                            Log.i("praise-->", object.getString("exp"));
                            EventUtils.sendPraise(activity);
                        } else {
                            LogUtils.i(obj.getString("msg"));
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        LogUtils.e("praiseArtical-failed->" + msg);
                    }
                });
    }

    @Override
    public void onPause() {
        try {
            xf_newshtmldetail_wv_detail.getClass().getMethod("onPause")
                    .invoke(xf_newshtmldetail_wv_detail, (Object[]) null);
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
            xf_newshtmldetail_wv_detail.getClass().getMethod("onResume")
                    .invoke(xf_newshtmldetail_wv_detail, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
