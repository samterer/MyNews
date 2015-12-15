package com.hzpd.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.color.tools.mytools.TUtil;
import com.facebook.CallbackManager;
import com.facebook.Profile;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.hzpd.adapter.CommentListAdapter;
import com.hzpd.adapter.NewsDetailAdapter;
import com.hzpd.custorm.ShuoMClickableSpan;
import com.hzpd.hflt.BuildConfig;
import com.hzpd.hflt.R;
import com.hzpd.hflt.wxapi.FacebookSharedUtil;
import com.hzpd.modle.CommentsCountBean;
import com.hzpd.modle.CommentzqzxBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsDetailBean;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.ReplayBean;
import com.hzpd.modle.ThirdLoginBean;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.UserLog;
import com.hzpd.ui.App;
import com.hzpd.ui.dialog.FontsizePop;
import com.hzpd.ui.widget.FontTextView;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.CODE;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.GetFileSizeUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.showwebview.MyJavascriptInterface;
import com.joy.update.Utils;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NewsDetailActivity extends MBaseActivity implements OnClickListener, AdListener {

    private static CallbackManager callbackManager = CallbackManager.Factory.create();
    NewsDetailAdapter adapter;
    RecyclerView recyclerView;

    public final static String PREFIX = "P:";
    ViewGroup ad_layout;
    ViewGroup ad_view;
    private NativeAd nativeAd;
    public static final String AD_KEY = "1902056863352757_1922349784656798";

    @Override
    public String getAnalyticPageName() {
        if (nb != null) {
            return PREFIX + nb.getTid() + "#" + nb.getNid();
        }
        return null;
    }

    //    private  String BASEURL = InterfaceJsonfile.ROOT + "index.php?s=/Public/newsview/nid/";
    private String BASEURL = "index.php?s=/Public/newsview/nid/";

    public final static String IMG_PREFIX = "com.hzpd.provider.imageprovider";
    public final static String HOT_NEWS = "hotnews://";

    private PopupWindow mPopupWindow;
    /**
     * popo的布局
     */
    private RelativeLayout mRelativeLayoutPopuBig;
    private RelativeLayout mRelativeLayoutPopuCenter;
    private RelativeLayout mRelativeLayoutPopuSmaill;
    private boolean mFlagPopuShow;
    private WebSettings webSettings;
    private View mBack;
    private ProgressBar load_progress_bar;
    private ViewGroup mRoot;
    private LinearLayout mButtomLayout1;// 底部1
    private NewsDetailBean mBean;
    public ListView mCommentListView;
    private CommentListAdapter mCommentListAdapter;
    private View news_detail_nonetwork;

    // ---------------------------

    private RelativeLayout newdetail_rl_comm;
    private View newdetail_ll_comm;
    private TextView newdetail_tv_comm;// 评论
    private ImageView newdetail_fontsize;// 字体
    private ImageView newdetail_share;// 分享
    private ImageView newdetail_collection;// 收藏
    private ImageView newdetail_more;//更多
    private RelativeLayout mRelativeLayoutTitleRoot;
    // -------------------------
    private FontsizePop fontpop;


    App.Callback callback = new App.Callback() {
        @Override
        public void onSuccess(Profile currentProfile) {
            if (nb != null && currentProfile != null) {
                Log.e("test", "currentProfile " + currentProfile.getId());
                ThirdLoginBean tlb = new ThirdLoginBean();
                tlb.setUserid(currentProfile.getId());
                tlb.setNickname(currentProfile.getName());
                tlb.setPhoto(currentProfile.getProfilePictureUri(200, 200).toString());
                tlb.setGender("3");
                tlb.setThird("FaceBook");
                try {
                    thirdlogin(tlb);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private boolean isTheme;
    private View transparent_layout_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        Log.e("test", "1 " + System.currentTimeMillis());
        nativeAd = new NativeAd(this, AD_KEY);
        nativeAd.setAdListener(this);
        if (App.getInstance().getThemeName().equals("3")) {
            isTheme = true;
        } else {
            isTheme = false;
        }
        App.getInstance().setProfileTracker(callback);
        setContentView(R.layout.news_details_layout);
        initViews();
        getThisIntent();
        try {
            load_progress_bar = (ProgressBar) findViewById(R.id.load_progress_bar);
            if (loading) {
                progress = 0;
                wProgress = 0;
                load_progress_bar.postDelayed(runnable, 50);
            }

            loadingView = findViewById(R.id.app_progress_bar);
            // ----------------------
            initNew();
            String station = SharePreferecesUtils.getParam(this, StationConfig.STATION, "def").toString();
            if (station.equals(StationConfig.DEF)) {
                BASEURL = InterfaceJsonfile.ROOT + "index.php?s=/Public/newsview/nid/";
            } else if (station.equals(StationConfig.YN)) {
                BASEURL = InterfaceJsonfile_YN.ROOT + "index.php?s=/Public/newsview/nid/";
            } else if (station.equals(StationConfig.TW)) {
                BASEURL = InterfaceJsonfile_TW.ROOT + "index.php?s=/Public/newsview/nid/";
            }
            // 适配器设置
//            mCommentListAdapter = new CommentListAdapter();
            mCommentListAdapter = new CommentListAdapter(nb.getNid());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            if (nativeAd != null) {
                nativeAd.loadAd();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setWebview(WebView webView) {
        this.mWebView = webView;
        webViewChangeProgress(webView);
    }

    public void setListView(ListView listView) {
        this.mCommentListView = listView;
        View headView = mLayoutInflater.inflate(R.layout.details_related_news, null);
        ad_layout = (ViewGroup) headView.findViewById(R.id.ad_layout);
        ad_view = (ViewGroup) headView.findViewById(R.id.ad_view);
        mCommentListView.addHeaderView(headView);
        mCommentListView.setAdapter(mCommentListAdapter);
        mCommentListView.setVisibility(View.GONE);
        details_explain = (FontTextView) headView.findViewById(R.id.details_explain);
        String link = getResources().getString(R.string.details_lv_link);
        try {
            SpannableString spanttt = new SpannableString(link);
            ClickableSpan clickttt = new ShuoMClickableSpan(link, this);
            spanttt.setSpan(clickttt, 0, link.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            details_explain.setText(getResources().getString(R.string.details_lv_1));
            details_explain.append(spanttt);
            details_explain.append(" )");
            details_explain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            details_explain.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }

        details_more_check = (FontTextView) headView.findViewById(R.id.details_more_check);
        rl_related = (LinearLayout) headView.findViewById(R.id.rl_related);
        ll_tag = (LinearLayout) headView.findViewById(R.id.ll_tag);
        llayout = (LinearLayout) headView.findViewById(R.id.llayout);
        ll_rob = (LinearLayout) headView.findViewById(R.id.ll_rob);

        headView.findViewById(R.id.details_tag).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ArticlePraise(headView);

        isDalNewsPraise();

        ll_rob.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null == spu.getUser()) {
                    return;
                }
                if (null == nb) {
                    return;
                }
                if (!"0".equals(nb.getComflag())) {
                    String smallimg = "";
                    if (null != nb.getImgs() && nb.getImgs().length > 0) {
                        smallimg = nb.getImgs()[0];
                    }
                    ReplayBean bean = new ReplayBean(nb.getNid(), nb.getTitle(), "News", nb.getJson_url(), smallimg, nb.getComcount());
                    Intent intent = new Intent(NewsDetailActivity.this, ZQ_ReplyActivity.class);
                    intent.putExtra("replay", bean);
                    startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                    AAnim.ActivityStartAnimation(activity);

                }
            }
        });

        TextView details_more_check = (TextView) headView.findViewById(R.id.details_more_check);
        details_more_check.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(NewsDetailActivity.this, "lihat sumber", Toast.LENGTH_SHORT).show();
            }
        });

        mRelativeLayoutTitleRoot = (RelativeLayout) findViewById(R.id.news_detail_layout);
        mBack = findViewById(R.id.news_detail_bak);
        mRoot = (LinearLayout) findViewById(R.id.news_detail_main_root_id);
        newdetail_rl_comm = (RelativeLayout) findViewById(R.id.comment_box);
        newdetail_tv_comm = (TextView) findViewById(R.id.newdetail_tv_comm);
        newdetail_fontsize = (ImageView) findViewById(R.id.newdetail_fontsize);
        newdetail_share = (ImageView) findViewById(R.id.newdetail_share);
        newdetail_collection = (ImageView) findViewById(R.id.newdetail_collection);
        newdetail_more = (ImageView) findViewById(R.id.newdetail_more);
        news_detail_nonetwork = findViewById(R.id.news_detail_nonetwork);
        mButtomLayout1 = (LinearLayout) findViewById(R.id.news_detail_ll_bottom1);

        mBack.setOnClickListener(this);
        newdetail_rl_comm.setOnClickListener(this);
        newdetail_tv_comm.setOnClickListener(this);
        newdetail_fontsize.setOnClickListener(this);
        newdetail_share.setOnClickListener(this);
        newdetail_collection.setOnClickListener(this);
        newdetail_more.setOnClickListener(this);

        if ("yes".equals(isVideo)) {
            newdetail_collection.setVisibility(View.GONE);
            newdetail_share.setVisibility(View.GONE);
        }

        findViewById(R.id.news_detail_nonetwork).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                news_detail_nonetwork.setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);
                loading = true;
                progress = 0;
                wProgress = 0;
                load_progress_bar.postDelayed(runnable, 50);
                getNewsDetails();
            }
        });
        getNewsDetails();
        initCommentListView();
        Log.e("test", "10 " + System.currentTimeMillis());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (mBean == null) {
                        return;
                    }

                    Bundle bundle = data.getExtras();
                    boolean isShare = bundle.getBoolean("isShare");
                    completeCom = bundle.getString("result");
                    if (isShare) {
                        String imgurl = null;
                        if (null != nb.getImgs() && nb.getImgs().length > 0) {
                            imgurl = nb.getImgs()[0];
                        }
                        FacebookSharedUtil.showShares(mBean.getTitle(), mBean.getLink(), imgurl, this);
                    }

                    //显示
                    latestList = new ArrayList<CommentzqzxBean>();
                    CommentzqzxBean bean = new CommentzqzxBean();
                    bean.setAvatar_path(spu.getUser().getAvatar_path());
                    bean.setNickname(spu.getUser().getNickname());
                    bean.setContent(bundle.getString("result"));
                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                    String date = sDateFormat.format(curDate);
                    bean.setDateline(date);
                    bean.setUid(spu.getUser().getUid());
                    bean.setStatus("1");
                    bean.setPraise("0");
                    latestList.add(0, bean);
                    mCommentListAdapter.insertData(latestList, 0);
                }
                break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String from;
    private String detailPathRoot;
    private NewsBean nb;
    private DbUtils dbUtils;
    private boolean isDetail = false;
    private MyJavascriptInterface jsInterface;// 跳转到图集接口

    private void getThisIntent() {
        try {
            Intent intent = getIntent();
            String action = intent.getAction();
            Log.d(getLogTag(), "action:" + action);
            nb = (NewsBean) intent.getSerializableExtra("newbean");
            try {
                from = intent.getStringExtra("from");
            } catch (Exception e) {
                from = null;
            }

            try {
                isVideo = intent.getStringExtra("isVideo");
            } catch (Exception e) {
                isVideo = null;
            }
            if ("0".equals(nb.getTid())) {
                detailPathRoot = App.getInstance().getJsonFileCacheRootDir() + File.separator + "subject" + File.separator
                        + "notid" + File.separator;
            } else {
                detailPathRoot = App.getInstance().getJsonFileCacheRootDir() + File.separator + "newsdetail"
                        + File.separator;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initNew() {
        dbUtils = DbUtils.create(this, App.getInstance().getJsonFileCacheRootDir(), App.collectiondbname);
        initPopupWindows();
        isCollection();
    }

    @Override
    protected void onDestroy() {
        if (nativeAd != null) {
            nativeAd.setAdListener(null);
            nativeAd.unregisterView();
            nativeAd.destroy();
            nativeAd = null;
        }
        callback = null;
        loading = false;
        load_progress_bar.setVisibility(View.GONE);
        runnable = null;
        App.getInstance().setProfileTracker(null);
        super.onDestroy();
        nb = null;
        try {
            mWebView.destroy();
            mWebView = null;
            mBean = null;
            mCommentListAdapter = null;
            mLayoutInflater = null;
            mRoot.removeAllViews();
            latestList = null;
            mCommentListView = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LinearLayout ll_tag;
    private LinearLayout llayout;
    private LinearLayout ll_rob;
    private LinearLayout rl_related;
    private FontTextView details_more_check;
    private FontTextView details_explain;
    private View background_emptyl;
    private android.view.animation.Animation animation;
    private TextView tv_one;
    private TextView tv_undal_one;
    private TextView text_dal_praise;
    private TextView text_undal_praise;
    private View rl_dal_praise;
    private View rl_undal_praise;
    private boolean isPraise = false;
    private boolean isUnPraise = false;

    private void initViews() {

        mLayoutInflater = LayoutInflater.from(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRoot = recyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NewsDetailAdapter(this);
        recyclerView.setAdapter(adapter);

    }

    int like_counts = 0;
    int unlike_counts = 0;

    //文章点赞
    private void ArticlePraise(View headView) {

        rl_dal_praise = headView.findViewById(R.id.rl_dal_praise);
        animation = AnimationUtils.loadAnimation(this, R.anim.nn);
        tv_one = (TextView) headView.findViewById(R.id.tv_one);
        text_dal_praise = (TextView) headView.findViewById(R.id.text_dal_praise);

        if (!TextUtils.isEmpty(nb.getLike())) {
            Log.e("like_counts", "like_counts--->" + like_counts + "::::" + nb.getNid());
            like_counts = Integer.parseInt(nb.getLike());
            text_dal_praise.setText("" + like_counts);
        } else {
            text_dal_praise.setText("0");
        }

        rl_dal_praise.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPraise) {
                    DalNewsPraise("1", "1");
                    SharePreferecesUtils.setParam(NewsDetailActivity.this, nb.getNid(), "1");
                    Drawable img = getResources().getDrawable(R.drawable.news_details_praise_select);
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    text_dal_praise.setCompoundDrawables(img, null, null, null);
                    text_dal_praise.setTextColor(text_dal_praise.getResources().getColor(R.color.praise_select));
                    like_counts = like_counts + 1;
                    text_dal_praise.setText("" + like_counts);
                    tv_one.setVisibility(View.VISIBLE);
                    tv_one.startAnimation(animation);
                    mWebView.postDelayed(new Runnable() {
                        public void run() {
                            tv_one.setVisibility(View.GONE);
                        }
                    }, 1000);
                    rl_undal_praise.setEnabled(false);
                    isPraise = true;
                } else {
                    DalNewsPraise("1", "0");
                    SharePreferecesUtils.setParam(NewsDetailActivity.this, nb.getNid(), "0");
                    Drawable img = getResources().getDrawable(R.drawable.news_details_praise_unselect);
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    text_dal_praise.setCompoundDrawables(img, null, null, null);
                    text_dal_praise.setTextColor(text_dal_praise.getResources().getColor(R.color.praise_unselect));
                    if (like_counts != 0) {
                        like_counts = like_counts - 1;
                        text_dal_praise.setText("" + like_counts);

                    } else {
                        text_dal_praise.setText("0");
                    }
                    isPraise = false;
                    rl_undal_praise.setEnabled(true);
                }
            }
        });

        tv_undal_one = (TextView) headView.findViewById(R.id.tv_undal_one);
        rl_undal_praise = headView.findViewById(R.id.rl_undal_praise);
        text_undal_praise = (TextView) headView.findViewById(R.id.text_undal_praise);


        if (!TextUtils.isEmpty(nb.getUnlike())) {
            unlike_counts = Integer.parseInt(nb.getUnlike());
            text_undal_praise.setText("" + unlike_counts);
        } else {
            text_undal_praise.setText("0");
        }
        rl_undal_praise.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isUnPraise) {
                    DalNewsPraise("2", "1");
                    SharePreferecesUtils.setParam(NewsDetailActivity.this, nb.getNid(), "2");
                    Drawable img = getResources().getDrawable(R.drawable.news_details_unpraise_select);
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    text_undal_praise.setCompoundDrawables(img, null, null, null);
                    unlike_counts = unlike_counts + 1;
                    text_undal_praise.setText("" + unlike_counts);
                    tv_undal_one.setVisibility(View.VISIBLE);
                    tv_undal_one.startAnimation(animation);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            tv_undal_one.setVisibility(View.GONE);
                        }
                    }, 1000);
                    rl_dal_praise.setEnabled(false);
                    isUnPraise = true;
                } else {
                    DalNewsPraise("2", "0");
                    SharePreferecesUtils.setParam(NewsDetailActivity.this, nb.getNid(), "0");
                    Drawable img = getResources().getDrawable(R.drawable.news_details_unpraise_unselect);
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    text_undal_praise.setCompoundDrawables(img, null, null, null);
                    text_undal_praise.setTextColor(text_undal_praise.getResources().getColor(R.color.praise_unselect));
                    if (unlike_counts == 0) {
                        text_undal_praise.setText("" + unlike_counts);
                    } else {
                        unlike_counts = unlike_counts - 1;
                        text_undal_praise.setText("" + unlike_counts);
                    }
                    isUnPraise = false;
                    rl_dal_praise.setEnabled(true);
                }
            }
        });
    }

    private void isDalNewsPraise() {

        String praise = SharePreferecesUtils.getParam(NewsDetailActivity.this, nb.getNid(), "0").toString();
        if (praise.equals("1")) {
            Drawable img = getResources().getDrawable(R.drawable.news_details_praise_select);
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            text_dal_praise.setCompoundDrawables(img, null, null, null);
            if (!TextUtils.isEmpty(nb.getLike()) && Integer.parseInt(nb.getLike()) != 0) {
                text_dal_praise.setText("" + (Integer.parseInt(nb.getLike())));
            } else {
                text_dal_praise.setText("1");
            }
            rl_undal_praise.setEnabled(false);
            isPraise = true;
        } else if (praise.equals("2")) {
            Drawable img = getResources().getDrawable(R.drawable.news_details_unpraise_select);
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            text_undal_praise.setCompoundDrawables(img, null, null, null);
            if (!TextUtils.isEmpty(nb.getUnlike()) && Integer.parseInt(nb.getUnlike()) != 0) {
                Log.e("getUnlike", "getUnlike---1");
                text_undal_praise.setText("" + (Integer.parseInt(nb.getUnlike())));
            } else {
                Log.e("getUnlike", "getUnlike---2");
                text_undal_praise.setText("1");
            }
            rl_dal_praise.setEnabled(false);
            isUnPraise = true;
        } else {

        }
    }

    private void DalNewsPraise(String type, String num) {//type：1：赞   2：踩 , 默认:1 nid: 新闻id ;num: 1 赞或踩；  0 :取消赞  或 取消踩
        String station = SharePreferecesUtils.getParam(NewsDetailActivity.this, StationConfig.STATION, "def").toString();
        String ADDCOLLECTION_url = null;
        if (station.equals(StationConfig.DEF)) {
            ADDCOLLECTION_url = InterfaceJsonfile.News_Price;
        } else if (station.equals(StationConfig.YN)) {
            ADDCOLLECTION_url = InterfaceJsonfile_YN.News_Price;
        } else if (station.equals(StationConfig.TW)) {
            ADDCOLLECTION_url = InterfaceJsonfile_TW.News_Price;
        }
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("type", "" + type);
        params.addBodyParameter("nid", nb.getNid());
        params.addBodyParameter("num", "" + num);
        //like unlike

        HttpHandler httpHandler = httpUtils.send(HttpMethod.POST, ADDCOLLECTION_url// InterfaceApi.addcollection
                , params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
            }

            @Override
            public void onFailure(HttpException error, String msg) {
            }
        });
        handlerList.add(httpHandler);
    }

    private void initCommentListView() {
        // 添加一个占位的HeaderView，避免ListView无任何子View
        TextView headerView = new TextView(this);
        ListView.LayoutParams lp = new AbsListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, 1);
        headerView.setLayoutParams(lp);
        mCommentListView.addHeaderView(headerView);
    }

    private void initPopupWindows() {
        View mPopupMenu = LayoutInflater.from(this).inflate(R.layout.text_size_popu_layout, null);
        mPopupWindow = new PopupWindow(mPopupMenu, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
        mPopupWindow.setBackgroundDrawable(dw);
        mPopupWindow.setOutsideTouchable(true);
        mRelativeLayoutPopuBig = (RelativeLayout) mPopupMenu.findViewById(R.id.news_textsize_big_id);
        mRelativeLayoutPopuCenter = (RelativeLayout) mPopupMenu.findViewById(R.id.text_size_popu_center_root_id);
        mRelativeLayoutPopuSmaill = (RelativeLayout) mPopupMenu.findViewById(R.id.text_size_popu_smail_root_id);
        mRelativeLayoutPopuBig.setOnClickListener(this);
        mRelativeLayoutPopuCenter.setOnClickListener(this);
        mRelativeLayoutPopuSmaill.setOnClickListener(this);
    }


    private void dismissPopupWindows() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            mFlagPopuShow = false;
        }
    }

    private final static int SCANNIN_GREQUEST_CODE = 0;
    private List<String> permissions = Arrays.asList("public_profile", "user_friends");

    @Override
    public void onClick(View v) {
        if (AvoidOnClickFastUtils.isFastDoubleClick())
            return;
        try {
            switch (v.getId()) {
                case R.id.news_textsize_big_id:
                    spu.setTextSizeNews(CODE.textSize_big);
                    setupWebView(CODE.textSize_big);
                    dismissPopupWindows();
                    break;
                case R.id.text_size_popu_center_root_id:
                    spu.setTextSizeNews(CODE.textSize_normal);
                    setupWebView(CODE.textSize_normal);
                    dismissPopupWindows();
                    break;
                case R.id.text_size_popu_smail_root_id:
                    spu.setTextSizeNews(CODE.textSize_small);
                    setupWebView(CODE.textSize_small);
                    dismissPopupWindows();
                    break;
                case R.id.news_detail_bak:
                    this.finish();
                    break;
                case R.id.comment_box: {
                    if (skipComment()) return;
                }
                break;
                //添加评论
                case R.id.newdetail_tv_comm: {
                    if (skipComment()) return;
                }
                break;
                case R.id.newdetail_share: {
                    try {
                        if (mBean == null) {
                            return;
                        }
                        LogUtils.i("click share");
                        String imgurl = null;
                        if (null != nb.getImgs() && nb.getImgs().length > 0) {
                            imgurl = nb.getImgs()[0];
                        }
                        FacebookSharedUtil.showShares(mBean.getTitle(), mBean.getLink(), imgurl, this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                case R.id.newdetail_fontsize: {
                }
                break;
                case R.id.newdetail_more: {
                }
                break;
                case R.id.newdetail_collection: {
                    addCollection();
                }
                break;
            }
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
        }
    }

    private boolean skipComment() {
        if (null == spu.getUser()) {
            LoginManager loginManager = LoginManager.getInstance();
            loginManager.setDefaultAudience(DefaultAudience.FRIENDS);
            loginManager.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
            loginManager.logInWithReadPermissions(this, permissions);
            return true;
        }
        if (null == nb) {
            return true;
        }
        if (!"0".equals(nb.getComflag())) {
            String smallimg = "";
            if (null != nb.getImgs() && nb.getImgs().length > 0) {
                smallimg = nb.getImgs()[0];
            }
            ReplayBean bean = new ReplayBean(nb.getNid(), nb.getTitle(), "News", nb.getJson_url(), smallimg, nb.getComcount());
            Intent intent = new Intent(this, ZQ_ReplyActivity.class);
            intent.putExtra("replay", bean);
            startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            AAnim.bottom2top(this);
        }
        return false;
    }


    public void thirdlogin(ThirdLoginBean tlb) {
        String station = SharePreferecesUtils.getParam(this, StationConfig.STATION, "def").toString();
        String thirdLogin_url = null;
        if (station.equals(StationConfig.DEF)) {
            thirdLogin_url = InterfaceJsonfile.thirdLogin;
        } else if (station.equals(StationConfig.YN)) {
            thirdLogin_url = InterfaceJsonfile_YN.thirdLogin;
        } else if (station.equals(StationConfig.TW)) {
            thirdLogin_url = InterfaceJsonfile_TW.thirdLogin;
        }
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("userid", tlb.getUserid());
        params.addBodyParameter("gender", tlb.getGender());
        params.addBodyParameter("nickname", tlb.getNickname());
        params.addBodyParameter("photo", tlb.getPhoto());
        params.addBodyParameter("third", tlb.getThird());
        params.addBodyParameter("is_ucenter", "0");
        HttpHandler httpHandler = httpUtils.send(HttpRequest.HttpMethod.POST, thirdLogin_url, params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        JSONObject obj = FjsonUtil
                                .parseObject(responseInfo.result);
                        if (null == obj) {
                            return;
                        }
                        if (200 == obj.getIntValue("code")) {
                            UserBean user = FjsonUtil.parseObject(
                                    obj.getString("data"), UserBean.class);
                            spu.setUser(user);
                            if (!"0".equals(nb.getComflag())) {
                                String smallimg = "";
                                if (null != nb.getImgs() && nb.getImgs().length > 0) {
                                    smallimg = nb.getImgs()[0];
                                }
                                ReplayBean bean = new ReplayBean(nb.getNid(), nb.getTitle(), "News", nb.getJson_url(), smallimg, nb.getComcount());
                                Intent intent = new Intent(NewsDetailActivity.this, ZQ_ReplyActivity.class);
                                intent.putExtra("replay", bean);
                                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                                AAnim.bottom2top(NewsDetailActivity.this);
                            }
                        } else {
                            TUtils.toast(getString(R.string.toast_cannot_connect_network));
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        LogUtils.e("test login failed");
                    }
                });
        httpHandler.setRequestCallBack(null);
        handlerList.add(httpHandler);
    }

    private ArrayList<CommentzqzxBean> latestList;
    private String completeCom;


    private View loadingView;

    public void webViewChangeProgress(final WebView webview) {
        //mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // 关闭硬件加速
        webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setDomStorageEnabled(true);

        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(App.getInstance().getAllDiskCacheDir());
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowFileAccess(true);

        jsInterface = new MyJavascriptInterface(this);
        // 添加js交互接口类，并起别名 imagelistner
        webview.addJavascriptInterface(jsInterface, "imagelistner");
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (TextUtils.isEmpty(url)) {
                    return true;
                }
                if (!url.startsWith(HOT_NEWS) && !SPUtil.isImageUri(url)) {
                    try {
                        Intent intent = new Intent(NewsDetailActivity.this, WebActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(WebActivity.KEY_URL, url);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                } else {
                    return true;
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                if (SPUtil.isImageUri(url)) {
                    wProgress = 100;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("test", "wProgress " + wProgress);
                if (!isResume) {
                    load_progress_bar.setVisibility(View.GONE);
                    return;
                }
                try {
                    wProgress = 100;
                    mCommentListView.setVisibility(View.VISIBLE);
                    if (isDetail) {
                        return;
                    }
                    if (nb == null) {
                        return;
                    }
                    Object obj = SharePreferecesUtils.getParam(getApplicationContext(), StationConfig.DETAILS_LOCATION + nb.getNid(), 0);
                    final int y = Integer.parseInt(obj.toString());
                    if (y < 50) {
                        return;
                    }
                    mWebView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!isResume) {
                                    return;
                                }
                                int realY = mWebView.getHeight();
                                mWebView.scrollTo(0, realY < y ? realY : y);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress > wProgress) {
                    wProgress = newProgress;
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            }
        });

        webview.setHorizontalScrollBarEnabled(false);
    }


    boolean loading = true;
    int wProgress = 0;
    int MIDDLE_PROGRESS = 95;
    int progress = 0;
    int delay = 30;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isResume) {
                loading = false;
                load_progress_bar.setVisibility(View.GONE);
                return;
            }
            if (loading) {
                if (wProgress < MIDDLE_PROGRESS && progress < MIDDLE_PROGRESS) {
                    progress += 1;
                } else if (wProgress > MIDDLE_PROGRESS) {
                    progress += 3;
                    delay = 10;
                    if (progress >= 100) {
                        progress = 100;
                        wProgress = 101;
                    }
                }
                if (wProgress > 100) {
                    loading = false;
                    load_progress_bar.setVisibility(View.GONE);
                    mCommentListView.setVisibility(View.VISIBLE);
                    return;
                }
                load_progress_bar.setProgress(progress);
                load_progress_bar.postDelayed(runnable, delay);
            } else {
                load_progress_bar.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 容纳标题栏、视频、WebView
     */
    private TextView title;
    private TextView time;
    private WebView mWebView;

    /**
     * 设置webview的字体大小
     *
     * @param textSize
     */
    private void setupWebView(int textSize) {
        if (mBean != null) {
            setContentData(textSize);
        }
    }

    public static final String CONTENT_START = "<!-- content_start -->";
    public static final String BASE_URL = "file:///android_asset/news/?random=20151615";

    private void setContentData(int textSize) {

        String content = mBean.getContent();
        content = processContent(content);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html><head>");
        stringBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n");
        stringBuilder.append("<style>@font-face {font-family: 'kievit';src: url('file:///android_asset/fonts/KievitPro-Regular.otf');}</style>");
        stringBuilder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        stringBuilder.append("android.css");
        stringBuilder.append("\" />\n");
        stringBuilder.append("</head><body>");
        stringBuilder.append(content);
        stringBuilder.append("<script type=\"text/javascript\" src=\"");
        stringBuilder.append("android.js");
        stringBuilder.append("\" ></script>");
        stringBuilder.append("<script type=\"text/javascript\" >");
        stringBuilder.append("</body></html>");
        mWebView.loadDataWithBaseURL(BASE_URL, formatStringToHtml(stringBuilder.toString(), textSize), "text/html", "utf-8", BASE_URL);
        Log.e("test", "loadDataWithBaseURL");
        //mWebView.loadUrl("http://www.nutnote.com/ltcms/api.php?s=/View/details/nid/378216");
        jsInterface.setNewsDetailBean(mBean);

    }

    public static final String DIV = "</div>";

    // 对内容做特殊处理
    private String processContent(String content) {
        String localTime = CalendarUtil.loaclTime(nb.getUpdate_time());
        Log.e("localTime", "head  localTime" + localTime);
        int start = content.indexOf(CONTENT_START) + CONTENT_START.length();
        Log.e("head", "head  length--->" + start);
        String head = "";
        String str = "";
        if (start > 10) {
            head = content.substring(0, start);
            head = head.substring(0, start - 45);
            localTime = localTime + DIV + " " + CONTENT_START;
            Log.e("test", "head " + head);
            head = head + localTime;
            content = content.substring(start);
        }
        Log.e("head", "head  --->" + head);
        int headStart = head.indexOf(DIV) + DIV.length();
        content = content.replaceAll("<style>[^/]*?</style>", " ");
        content = content.replaceAll("style=\"[^\"]*?\"", " ");
        content = content.replaceAll("style='[^']*?'", " ");
        content = content.replaceAll("<p><br/></p>", "");
        content = content.replaceAll("<p><br></p>", "");
        content = content.replaceAll("<p></p>", "");
        content = content.replaceAll("&nbsp;", "");
        content = content.replaceAll("width=\"[^\"]*?\"", "");
        content = content.replaceAll("width='[^']*?'", "");
        content = content.replaceAll("height=\"[^\"]*?\"", "");
        content = content.replaceAll("height='[^']*?'", "");

        return head + content;
    }

    private LayoutInflater mLayoutInflater;
    private String isVideo;

    private String formatStringToHtml(String content, int textSize) {
        String data = "";
        if (mBean != null) {
            if (null != content && !"".equals(content)) {
                switch (textSize) {
                    case CODE.textSize_small:
                        webSettings.setTextSize(WebSettings.TextSize.NORMAL);
                        break;
                    case CODE.textSize_normal:
                        webSettings.setTextSize(WebSettings.TextSize.LARGER);
                        break;
                    case CODE.textSize_big:
                        webSettings.setTextSize(WebSettings.TextSize.LARGEST);
                        break;
                }
                data = content;
            }
        }
        return data;
    }


    private volatile int mCurPage = 1;
    private int mPageSize = 20;


    //查看评论
    private void getLatestComm() {
        Log.e("test", "getLatestComm ");
        if (mBean == null) {
            return;
        }
        String siteid = InterfaceJsonfile.SITEID;
        String mLatestComm_url = InterfaceJsonfile.CHECKCOMMENT;
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("Page", "" + mCurPage);
        params.addBodyParameter("PageSize", "" + mPageSize);
        params.addBodyParameter("nid", mBean.getNid());
        params.addBodyParameter("type", "News");
        params.addBodyParameter("siteid", siteid);
        String url = mLatestComm_url;
        HttpHandler httpHandler = httpUtils.send(HttpMethod.POST
                , url
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {

                if (responseInfo.result == null) {
                    ll_rob.setVisibility(View.VISIBLE);
                } else {
                    parseCommentJson(responseInfo.result);
                }

                mCurPage++;
            }


            @Override
            public void onFailure(HttpException error, String msg) {
            }
        });
        handlerList.add(httpHandler);
    }

    private void parseCommentJson(String json) {
        try {
            JSONObject obj = null;
            try {
                obj = JSONObject.parseObject(json);
            } catch (Exception e) {
                return;
            }
            boolean hasMore = false;
            if (obj != null && 200 == obj.getIntValue("code")) {
                Log.e("test", "getLatestComm   200");

                ll_rob.setVisibility(View.GONE);
                latestList = (ArrayList<CommentzqzxBean>) JSONArray.parseArray(
                        obj.getString("data"), CommentzqzxBean.class);

                Log.e("test", "test--->" + latestList.toString());
                // 添加数据到Adpater
                mCommentListAdapter.appendData(latestList);
                hasMore = true;
            } else {
                hasMore = false;
                if (mCurPage <= 1) {
                    ll_rob.setVisibility(View.VISIBLE);
                }
            }
            boolean emptyResult = (mCommentListAdapter == null || mCommentListAdapter.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //获取详细信息
    private void getNewsDetails() {
        try {
            File pageFile = App.getFile(detailPathRoot + "detail_" + nb.getNid());
            //从缓存中获取
            if (GetFileSizeUtil.getInstance().getFileSizes(pageFile) > 30) {
                try {
                    String data = App.getFileContext(pageFile);
                    JSONObject obj = JSONObject.parseObject(data);
                    mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), NewsDetailBean.class);
                    int textSize = spu.getTextSize();
                    setupWebView(textSize);
                    mRoot.setVisibility(View.VISIBLE);
                    mButtomLayout1.setVisibility(View.VISIBLE);
                    news_detail_nonetwork.setVisibility(View.GONE);

                    loadingView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingView.setVisibility(View.GONE);
                        }
                    }, 500);
                    if ((mBean.getRef() != null && mBean.getRef().size() > 0) || (mBean.getTag() != null) && mBean.getTag().length > 0) {

                        rl_related.setVisibility(View.VISIBLE);
                        ll_tag.setVisibility(View.VISIBLE);
                        addRelatedNewsView();
                        addRelatedTagView();
                    }
                    if (mBean.getTag() != null) {
                        rl_related.setVisibility(View.VISIBLE);
                        ll_tag.setVisibility(View.VISIBLE);
                        Log.e("tag", "从缓存中获取tag--->" + mBean.getTag().toString());
                    } else {
                        Log.e("tag", "从缓存中获取tag--->null");
                    }

                    getLatestComm();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            if (!Utils.isNetworkConnected(this)) {
                showEmpty();
                if (isResume) {
                    TUtils.toast(getString(R.string.toast_check_network));
                }
            }
            final long start = System.currentTimeMillis();
            HttpHandler httpHandler = httpUtils.download(nb.getJson_url(), detailPathRoot + "detail_" + nb.getNid(), new RequestCallBack<File>() {
                @Override
                public void onStart() {
                    super.onStart();
                    loadingView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    try {
                        Log.e("test", "news " + (System.currentTimeMillis() - start));
                        loadingView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingView.setVisibility(View.GONE);
                            }
                        }, 500);
                        String data = App.getFileContext(responseInfo.result);
                        if (TextUtils.isEmpty(data)) {
                            //TODO 考虑 没有获取内容 或者 内容为空的情况 服务器满
                            responseInfo.result.delete();
                            showEmpty();
                            if (isResume) {
                                TUtil.toast(NewsDetailActivity.this, getString(R.string.error_delete));
                            }
                            return;
                        }
                        JSONObject obj = FjsonUtil.parseObject(data);
                        if (null == obj) {
                            responseInfo.result.delete();
                            if (isResume) {
                                TUtil.toast(NewsDetailActivity.this, getString(R.string.error_delete));
                            }
                            return;
                        }
                        try {
                            mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), NewsDetailBean.class);
                            int textSize = spu.getTextSize();
                            setupWebView(textSize);
                            if ((mBean.getRef() != null && mBean.getRef().size() > 0) || (mBean.getTag() != null) && mBean.getTag().length > 0) {
                                rl_related.setVisibility(View.VISIBLE);
                                addRelatedNewsView();
                                addRelatedTagView();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        getLatestComm();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    showEmpty();
                    if (isResume) {
                        TUtils.toast(getString(R.string.toast_cannot_connect_network));
                        AnalyticUtils.sendGaEvent(getApplicationContext(), AnalyticUtils.ACTION.networkErrorOnDetail, null, null, 0L);
                        AnalyticUtils.sendUmengEvent(getApplicationContext(), AnalyticUtils.ACTION.networkErrorOnDetail);
                    }
                }
            });
            handlerList.add(httpHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEmpty() {
        loading = false;
        wProgress = 100;
        progress = 0;
        loadingView.setVisibility(View.GONE);
        load_progress_bar.setVisibility(View.GONE);
        news_detail_nonetwork.setVisibility(View.VISIBLE);
    }

    //获取相关新闻
    private void addRelatedNewsView() {
        if (mBean.getRef() != null && mBean.getRef().size() > 0) {
            for (int i = 0; i < mBean.getRef().size(); i++) {
                final NewsBean bean = mBean.getRef().get(i);
                View view = null;
                if ("4".equals(bean.getType())) {
                    view = LayoutInflater.from(this).inflate(R.layout.news_3_item_layout, null);
                    setThreePic(bean, view);
                } else if ("99".equals(bean.getType())) {
                    view = LayoutInflater.from(this).inflate(R.layout.news_large_item_layout, null);
                    setLargePic(bean, view);
                } else {
                    view = LayoutInflater.from(this).inflate(R.layout.news_list_item_layout, null);
                    setLeftPic(bean, view);
                }
//                View view = LayoutInflater.from(this).inflate(R.layout.news_detail_other_layout, null);
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (AvoidOnClickFastUtils.isFastDoubleClick())
                            return;
                        Intent mIntent = new Intent();
                        mIntent.putExtra("newbean", bean);
                        mIntent.putExtra("from", "newsitem");
                        mIntent.setClass(NewsDetailActivity.this, NewsDetailActivity.class);
                        startActivity(mIntent);
                    }
                });

                llayout.addView(view);
            }
        }
    }

    //左边图片，右title，评论，时间，脚标
    private class VHLeftPic {
        TextView newsitem_title;
        private ImageView nli_foot;
        //来源
        private TextView newsitem_source;
        //收藏数
        private TextView newsitem_collectcount;
        //评论数
        private TextView newsitem_commentcount;
        private TextView newsitem_time;
        private ImageView newsitem_img;
        private ImageView newsitem_unlike;
        private ImageView item_type_iv;
        private LinearLayout ll_tag;
    }

    private void setLeftPic(NewsBean bean, View view) {
        VHLeftPic vhLeftPic = new VHLeftPic();
        vhLeftPic.newsitem_title = (TextView) view.findViewById(R.id.newsitem_title);
        vhLeftPic.newsitem_title.setText(bean.getTitle());
        vhLeftPic.item_type_iv = (ImageView) view.findViewById(R.id.item_type_iv);
        if (!TextUtils.isEmpty(bean.getAttname())) {
            String attname = bean.getAttname();
            if (attname.equals("a")) {
                vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_hot);
            } else if (attname.equals("b")) {
                vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_rekom);
            } else if (attname.equals("c")) {
                vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_kolom);
            } else if (attname.equals("f")) {
                vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_fokus);
            } else if (attname.equals("h")) {
                vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_xtend);
            } else if (attname.equals("j")) {
                vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_issue);
            } else if (attname.equals("p")) {
                vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_album);
            } else if (attname.equals("s")) {
                vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_video);
            } else {
                vhLeftPic.item_type_iv.setVisibility(View.GONE);
            }
        } else {
            vhLeftPic.item_type_iv.setVisibility(View.GONE);
        }


        vhLeftPic.newsitem_collectcount = (TextView) view.findViewById(R.id.newsitem_collectcount);
        String fav = bean.getFav();
        if (!TextUtils.isEmpty(fav)) {

            int fav_counts = Integer.parseInt(fav);
            if (fav_counts > 0) {
                vhLeftPic.newsitem_collectcount.setVisibility(View.VISIBLE);
                vhLeftPic.newsitem_collectcount.setText(fav_counts + "");
            } else {
                vhLeftPic.newsitem_collectcount.setVisibility(View.GONE);
            }
        } else {
            vhLeftPic.newsitem_collectcount.setVisibility(View.GONE);
        }
        vhLeftPic.newsitem_source = (TextView) view.findViewById(R.id.newsitem_source);
        String from = bean.getCopyfrom();
        if (!TextUtils.isEmpty(from)) {
            vhLeftPic.newsitem_source.setVisibility(View.VISIBLE);
            vhLeftPic.newsitem_source.setText(from);
        } else {
            vhLeftPic.newsitem_source.setVisibility(View.GONE);
        }
        vhLeftPic.newsitem_commentcount = (TextView) view.findViewById(R.id.newsitem_commentcount);
        String comcount = bean.getComcount();
        if (!TextUtils.isEmpty(comcount)) {
            int counts = Integer.parseInt(comcount);
            if (counts > 0) {
                vhLeftPic.newsitem_commentcount.setVisibility(View.VISIBLE);
                bean.setComcount(counts + "");
                vhLeftPic.newsitem_commentcount.setText(counts + "");
            } else {
                vhLeftPic.newsitem_commentcount.setVisibility(View.GONE);
            }
        } else {
            vhLeftPic.newsitem_commentcount.setVisibility(View.GONE);
        }
        vhLeftPic.newsitem_time = (TextView) view.findViewById(R.id.newsitem_time);
        if (CalendarUtil.friendlyTime(bean.getUpdate_time(), this) == null) {
            vhLeftPic.newsitem_time.setText("");
        } else {
            vhLeftPic.newsitem_time.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), this));
        }

        vhLeftPic.ll_tag = (LinearLayout) view.findViewById(R.id.ll_tag);
        vhLeftPic.newsitem_img = (ImageView) view.findViewById(R.id.newsitem_img);
        vhLeftPic.newsitem_img.setVisibility(View.VISIBLE);
        if ("1".equals(bean.getType())) {
            vhLeftPic.newsitem_img.setVisibility(View.GONE);
        }
        vhLeftPic.newsitem_img.setImageResource(R.drawable.default_bg);
        if (vhLeftPic.newsitem_img.getVisibility() == View.VISIBLE
                && null != bean.getImgs()
                && bean.getImgs().length > 0) {
            Log.i("test", "addRelatedNewsView---> vhLeftPic.newsitem_img.setVisibility(View.VISIBLE);");
            vhLeftPic.newsitem_title.setPadding(App.px_15dp, 0, 0, 0);
            vhLeftPic.ll_tag.setPadding(App.px_15dp, 0, 0, 0);
            SPUtil.displayImage(bean.getImgs()[0], vhLeftPic.newsitem_img,
                    DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
        } else {
            Log.i("test", "addRelatedNewsView---> vhLeftPic.newsitem_img.setVisibility(View.GONE);");
            vhLeftPic.newsitem_img.setVisibility(View.GONE);
            vhLeftPic.newsitem_title.setPadding(0, 0, 0, App.px_15dp);
            vhLeftPic.ll_tag.setPadding(0, 0, 0, 0);
        }
    }

    //三联图
    private class VHThree {
        private TextView newsitem_title;
        private TextView news_3_tv_time;
        private ImageView img0;
        private ImageView img1;
        private ImageView img2;
        private TextView newsitem_comments;
        private TextView newsitem_source;
        private TextView newsitem_collectcount;
        private ImageView item_type_iv;
    }

    private void setThreePic(NewsBean bean, View view) {
        VHThree vhThree = new VHThree();
        vhThree.newsitem_title = (TextView) view.findViewById(R.id.newsitem_title);
        vhThree.newsitem_title.setText(bean.getTitle());

        vhThree.newsitem_source = (TextView) view.findViewById(R.id.newsitem_source);
        String from = bean.getCopyfrom();
        if (!TextUtils.isEmpty(from)) {
            vhThree.newsitem_source.setVisibility(View.VISIBLE);
            vhThree.newsitem_source.setText(from);
        } else {
            vhThree.newsitem_source.setVisibility(View.GONE);
        }

        vhThree.newsitem_collectcount = (TextView) view.findViewById(R.id.newsitem_collectcount);
        String fav = bean.getFav();
        if (!TextUtils.isEmpty(fav)) {
            int fav_counts = Integer.parseInt(fav);
            if (fav_counts > 0) {
                vhThree.newsitem_collectcount.setVisibility(View.VISIBLE);
                vhThree.newsitem_collectcount.setText(fav_counts + "");
            } else {
                vhThree.newsitem_collectcount.setVisibility(View.GONE);
            }
        } else {
            vhThree.newsitem_collectcount.setVisibility(View.GONE);
        }

        vhThree.newsitem_comments = (TextView) view.findViewById(R.id.newsitem_commentcount);
        String comcount = bean.getComcount();
        if (!TextUtils.isEmpty(comcount)) {
            int counts = Integer.parseInt(comcount);
            if (counts > 0) {
                vhThree.newsitem_comments.setVisibility(View.VISIBLE);
                bean.setComcount(counts + "");
                vhThree.newsitem_comments.setText(counts + "");
            } else {
                vhThree.newsitem_comments.setVisibility(View.GONE);
            }
        } else {
            vhThree.newsitem_comments.setVisibility(View.GONE);
        }

        vhThree.item_type_iv = (ImageView) view.findViewById(R.id.item_type_iv);
        if (!TextUtils.isEmpty(bean.getAttname())) {
            String attname = bean.getAttname();
            if (attname.equals("a")) {
                vhThree.item_type_iv.setVisibility(View.VISIBLE);
                vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_hot);
            } else if (attname.equals("b")) {
                vhThree.item_type_iv.setVisibility(View.VISIBLE);
                vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_rekom);
            } else if (attname.equals("c")) {
                vhThree.item_type_iv.setVisibility(View.VISIBLE);
                vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_kolom);
            } else if (attname.equals("f")) {
                vhThree.item_type_iv.setVisibility(View.VISIBLE);
                vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_fokus);
            } else if (attname.equals("h")) {
                vhThree.item_type_iv.setVisibility(View.VISIBLE);
                vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_xtend);
            } else if (attname.equals("j")) {
                vhThree.item_type_iv.setVisibility(View.VISIBLE);
                vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_issue);
            } else if (attname.equals("p")) {
                vhThree.item_type_iv.setVisibility(View.VISIBLE);
                vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_album);
            } else if (attname.equals("s")) {
                vhThree.item_type_iv.setVisibility(View.VISIBLE);
                vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_video);
            } else {
                vhThree.item_type_iv.setVisibility(View.GONE);
            }
        } else {
            vhThree.item_type_iv.setVisibility(View.GONE);
        }

        vhThree.news_3_tv_time = (TextView) view.findViewById(R.id.news_3_tv_time);
        vhThree.news_3_tv_time.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), this));

        vhThree.img0 = (ImageView) view.findViewById(R.id.news_3_item1);
        vhThree.img1 = (ImageView) view.findViewById(R.id.news_3_item2);
        vhThree.img2 = (ImageView) view.findViewById(R.id.news_3_item3);
        vhThree.img0.setImageResource(R.drawable.default_bg);
        vhThree.img1.setImageResource(R.drawable.default_bg);
        vhThree.img2.setImageResource(R.drawable.default_bg);
        String s[] = bean.getImgs();
        if (s.length == 1) {
            SPUtil.displayImage(s[0], vhThree.img0, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
            SPUtil.displayImage("", vhThree.img1, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
            SPUtil.displayImage("", vhThree.img2, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
        } else if (s.length == 2) {
            SPUtil.displayImage(s[0], vhThree.img0, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
            SPUtil.displayImage(s[1], vhThree.img1, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
            SPUtil.displayImage("", vhThree.img2, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
        } else if (s.length > 2) {
            SPUtil.displayImage(s[0], vhThree.img0, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
            SPUtil.displayImage(s[1], vhThree.img1, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
            SPUtil.displayImage(s[2], vhThree.img2, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
        }
    }

    //大图
    private class VHLargePic {
        private TextView newsitem_title;
        //来源
        private TextView newsitem_source;
        //收藏数
        private TextView newsitem_collectcount;
        //评论数
        private TextView newsitem_commentcount;
        private TextView newsitem_time;
        private ImageView newsitem_img;
        private ImageView item_type_iv;
    }

    private void setLargePic(NewsBean bean, View view) {
        VHLargePic vhLargePic = new VHLargePic();

        vhLargePic.newsitem_title = (TextView) view.findViewById(R.id.newsitem_title);
        vhLargePic.newsitem_title.setText(bean.getTitle());

        vhLargePic.item_type_iv = (ImageView) view.findViewById(R.id.item_type_iv);
        if (bean.getAttname() != null) {
            String attname = bean.getAttname();
            if (attname.equals("a")) {
                vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_hot);
            } else if (attname.equals("b")) {
                vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_rekom);
            } else if (attname.equals("c")) {
                vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_kolom);
            } else if (attname.equals("f")) {
                vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_fokus);
            } else if (attname.equals("h")) {
                vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_xtend);
            } else if (attname.equals("j")) {
                vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_issue);
            } else if (attname.equals("p")) {
                vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_album);
            } else if (attname.equals("s")) {
                vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_video);
            } else {
                vhLargePic.item_type_iv.setVisibility(View.GONE);
            }
        } else {
            vhLargePic.item_type_iv.setVisibility(View.GONE);
        }

        vhLargePic.newsitem_collectcount = (TextView) view.findViewById(R.id.newsitem_collectcount);
        String fav = bean.getFav();
        if (!TextUtils.isEmpty(fav)) {
            int fav_counts = Integer.parseInt(fav);
            if (fav_counts > 0) {
                vhLargePic.newsitem_collectcount.setVisibility(View.VISIBLE);
                vhLargePic.newsitem_collectcount.setText(fav_counts + "");
            } else {
                vhLargePic.newsitem_collectcount.setVisibility(View.GONE);
            }
        } else {
            vhLargePic.newsitem_collectcount.setVisibility(View.GONE);
        }

        vhLargePic.newsitem_source = (TextView) view.findViewById(R.id.newsitem_source);
        String from = bean.getCopyfrom();
        if (!TextUtils.isEmpty(from)) {
            vhLargePic.newsitem_source.setVisibility(View.GONE);
        } else {
            vhLargePic.newsitem_source.setText(from);
            vhLargePic.newsitem_source.setVisibility(View.VISIBLE);
        }

        vhLargePic.newsitem_commentcount = (TextView) view.findViewById(R.id.newsitem_commentcount);
        String comcount = bean.getComcount();
        if (!TextUtils.isEmpty(comcount)) {
            int counts = Integer.parseInt(comcount);
            if (counts > 0) {
                vhLargePic.newsitem_commentcount.setVisibility(View.VISIBLE);
                bean.setComcount(counts + "");
                vhLargePic.newsitem_commentcount.setText(counts + "");
            } else {
                vhLargePic.newsitem_commentcount.setVisibility(View.GONE);
            }
        } else {
            vhLargePic.newsitem_commentcount.setVisibility(View.GONE);
        }

        vhLargePic.newsitem_time = (TextView) view.findViewById(R.id.newsitem_time);
        if (CalendarUtil.friendlyTime(bean.getUpdate_time(), this) == null) {
            vhLargePic.newsitem_time.setText("");
        } else {
            vhLargePic.newsitem_time.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), this));
        }
        vhLargePic.newsitem_img = (ImageView) view.findViewById(R.id.newsitem_img);
        vhLargePic.newsitem_img.setVisibility(View.VISIBLE);
        if (vhLargePic.newsitem_img.getVisibility() == View.VISIBLE
                && null != bean.getImgs()
                && bean.getImgs().length > 0) {
            SPUtil.displayImage(bean.getImgs()[0], vhLargePic.newsitem_img,
                    DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
        } else {
            SPUtil.displayImage("", vhLargePic.newsitem_img,
                    DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
        }
    }

    //获取相关tag
    private void addRelatedTagView() {
        if (mBean != null && mBean.getTag() != null) {
            Log.e("tag", "addRelatedTagView tag--->" + mBean.getTag());
            if (mBean.getTag().length > 3) {
                for (int i = 0; i < 3; i++) {
                    View view = LayoutInflater.from(this).inflate(R.layout.details_related_tag, null);
                    TextView text = (TextView) view.findViewById(R.id.details_tag1);
                    text.setText(mBean.getTag()[i]);
                    String tagcontent = mBean.getTag()[i];
                    view.setOnClickListener(new MyOnClickListener(tagcontent));
                    ll_tag.addView(view);
                }
            } else {
                for (int i = 0; i < mBean.getTag().length; i++) {
                    View view = LayoutInflater.from(this).inflate(R.layout.details_related_tag, null);
                    TextView text = (TextView) view.findViewById(R.id.details_tag1);
                    text.setText(mBean.getTag()[i]);
                    String tagcontent = mBean.getTag()[i];
                    view.setOnClickListener(new MyOnClickListener(tagcontent));
                    ll_tag.addView(view);
                }
            }

        }
    }

    @Override
    public void onError(Ad ad, AdError adError) {
        Log.e("test", "Ad onError");
    }

    @Override
    public void onAdLoaded(Ad ad) {
        addAdView();
    }

    private void addAdView() {
        try {
            View view = NativeAdView.render(this, nativeAd, NativeAdView.Type.HEIGHT_120);
            ad_view.addView(view);
            ad_layout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAdClicked(Ad ad) {
        return;
    }


    private class MyOnClickListener implements OnClickListener {
        private String tag;

        public MyOnClickListener(String tag) {
            this.tag = tag;
        }

        @Override
        public void onClick(View v) {
            if (AvoidOnClickFastUtils.isFastDoubleClick())
                return;
            Intent intent = new Intent();
            intent.putExtra("TAGCONNENT", tag);
            intent.setClass(NewsDetailActivity.this, SearchActivity.class);
            startActivity(intent);
            AAnim.ActivityStartAnimation(NewsDetailActivity.this);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (!App.isStartApp) {
            Intent in = new Intent();
            in.setClass(this, MainActivity.class);
            startActivity(in);
        }

    }

    class MyOtherClickListener implements OnClickListener {
        private int i;

        public MyOtherClickListener(int i) {
            this.i = i;
        }

        @Override
        public void onClick(View v) {
            Intent in = new Intent();
            Bundle bu = new Bundle();
            bu.putString("nid", mBean.getRef().get(i).getNid());
            bu.putString("type", "1");
            in.putExtras(bu);
            in.setClass(NewsDetailActivity.this, NewsDetailActivity.class);
            startActivity(in);
            AAnim.ActivityStartAnimation(NewsDetailActivity.this);
        }

    }

    private void popUpwindow() {
        final PopupWindow pinlunpop = new PopupWindow(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View popRoot = inflater.inflate(R.layout.newsdetail_popupwindow_layout, null);
        pinlunpop.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        pinlunpop.setContentView(popRoot);
        ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
        pinlunpop.setBackgroundDrawable(dw);
        pinlunpop.setOutsideTouchable(true);
        ImageView pop_fenxiang_img = (ImageView) popRoot.findViewById(R.id.pop_fenxiang_img);
        ImageView pop_shoucang_img = (ImageView) popRoot.findViewById(R.id.pop_shoucang_img);
        ImageView pop_ziti_img = (ImageView) popRoot.findViewById(R.id.pop_ziti_img);
        ImageView pop_xiazai_iv = (ImageView) popRoot.findViewById(R.id.pop_xiazai_iv);

        pop_xiazai_iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pinlunpop.dismiss();
            }
        });
        pop_fenxiang_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pinlunpop.dismiss();
                // SharedUtil.showShares(true, null, mBean.getTitle(),
                // mBean.getLink(),nb.getSmallimgurl(),NewsDetailActivity.this);
                String imgurl = null;
                if (null != nb.getImgs() && nb.getImgs().length > 0) {
                    imgurl = nb.getImgs()[0];
                }
                FacebookSharedUtil.showShares(mBean.getTitle(), mBean.getLink(), imgurl,
                        NewsDetailActivity.this);
            }
        });
        pop_shoucang_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pinlunpop.dismiss();
                addCollection();
            }
        });
        pop_ziti_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pinlunpop.dismiss();
                if (mFlagPopuShow) {
                    dismissPopupWindows();
                } else {
                    // mPopupWindow.showAsDropDown(mPinLun,-30,-15);
                }
                mFlagPopuShow = !mFlagPopuShow;
            }
        });
        // pinlunpop.showAsDropDown(mPinLun, -60,-15);

    }

    // ----------------------
    // 添加收藏
    private void addCollection() {
        NewsItemBeanForCollection nibfc = new NewsItemBeanForCollection(nb);
        try {
            NewsItemBeanForCollection mnbean = dbUtils.findFirst(
                    Selector.from(NewsItemBeanForCollection.class).where("nid", "=", nb.getNid()));

            if (mnbean == null) {
                dbUtils.save(nibfc);
                TUtils.toast(getString(R.string.toast_collect_success));
                long co = dbUtils.count(NewsItemBeanForCollection.class);
                LogUtils.i("num:" + co);
                LogUtils.i("type-->" + nibfc.getType());
                if (App.getInstance().getThemeName().equals("0"))
                    newdetail_collection.setImageResource(R.drawable.details_collect_already_select);
                else
                    newdetail_collection.setImageResource(R.drawable.details_collect_already_select);

            } else {
                dbUtils.delete(NewsItemBeanForCollection.class, WhereBuilder.b("nid", "=", nb.getNid()));
                TUtils.toast(getString(R.string.toast_collect_cancelled));
                newdetail_collection.setImageResource(R.drawable.details_collect_select);
            }
        } catch (Exception e) {
            e.printStackTrace();
            TUtils.toast(getString(R.string.toast_collect_failed));
        }

        if (spu.getUser() != null) {
            String station = SharePreferecesUtils.getParam(NewsDetailActivity.this, StationConfig.STATION, "def").toString();
            String siteid = null;
            String ADDCOLLECTION_url = null;
            if (station.equals(StationConfig.DEF)) {
                siteid = InterfaceJsonfile.SITEID;
                ADDCOLLECTION_url = InterfaceJsonfile.ADDCOLLECTION;
            } else if (station.equals(StationConfig.YN)) {
                siteid = InterfaceJsonfile_YN.SITEID;
                ADDCOLLECTION_url = InterfaceJsonfile_YN.ADDCOLLECTION;
            } else if (station.equals(StationConfig.TW)) {
                siteid = InterfaceJsonfile_TW.SITEID;
                ADDCOLLECTION_url = InterfaceJsonfile_TW.ADDCOLLECTION;
            }
            LogUtils.i("Type-->" + nb.getType() + "  Fid-->" + nb.getNid());
            RequestParams params = RequestParamsUtils.getParamsWithU();
            params.addBodyParameter("type", "1");
            params.addBodyParameter("typeid", nb.getNid());
            params.addBodyParameter("siteid", siteid);
            params.addBodyParameter("data", nb.getJson_url());

            LogUtils.i("params-->" + params.toString());

            HttpHandler httpHandler = httpUtils.send(HttpMethod.POST, ADDCOLLECTION_url// InterfaceApi.addcollection
                    , params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                }
            });
            handlerList.add(httpHandler);
        }

    }

    // 是否收藏
    private void isCollection() {
        if (null != spu.getUser()) {
            String station = SharePreferecesUtils.getParam(NewsDetailActivity.this, StationConfig.STATION, "def").toString();
            String ISCELLECTION_url = null;
            if (station.equals(StationConfig.DEF)) {
                ISCELLECTION_url = InterfaceJsonfile.ISCELLECTION;
            } else if (station.equals(StationConfig.YN)) {
                ISCELLECTION_url = InterfaceJsonfile_YN.ISCELLECTION;
            } else if (station.equals(StationConfig.TW)) {
                ISCELLECTION_url = InterfaceJsonfile_TW.ISCELLECTION;
            }
            RequestParams params = RequestParamsUtils.getParamsWithU();
            params.addBodyParameter("typeid", nb.getNid());
            params.addBodyParameter("type", "1");

            HttpHandler httpHandler = httpUtils.send(HttpMethod.POST, ISCELLECTION_url, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    JSONObject obj = null;
                    try {
                        obj = JSONObject.parseObject(responseInfo.result);
                    } catch (Exception e) {
                        return;
                    }

                    if (200 == obj.getIntValue("code")) {
                        JSONObject object = obj.getJSONObject("data");
                        if ("1".equals(object.getString("status"))) {
                            if (App.getInstance().getThemeName().equals("0"))
                                newdetail_collection.setImageResource(R.drawable.details_collect_already_select);
                            else
                                newdetail_collection.setImageResource(R.drawable.details_collect_already_select_red);

                        }
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    LogUtils.i("isCollection failed");
                }
            });
            handlerList.add(httpHandler);
        } else {
            try {
                NewsItemBeanForCollection nbfc = dbHelper.getCollectionDBUitls()
                        .findFirst(Selector.from(NewsItemBeanForCollection.class).where("nid", "=", nb.getNid())
                                .and("type", "=", "1"));
                if (null != nbfc) {
                    if (App.getInstance().getThemeName().equals("0"))
                        newdetail_collection.setImageResource(R.drawable.details_collect_already_select);
                    else
                        newdetail_collection.setImageResource(R.drawable.details_collect_already_select_red);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    private CommentsCountBean ccBean;

    @Override
    protected void onPause() {
        super.onPause();
        try {
            SharePreferecesUtils.setParam(this, StationConfig.DETAILS_LOCATION + nb.getNid(), mWebView.getScrollY());
            SharePreferecesUtils.setParam(this, StationConfig.DETAILS_LOCATION + nb.getNid(), mWebView.getScrollY());
            super.onPause();
            long totalTile = System.currentTimeMillis() - enterTime;
            totalTile = totalTile / 1000;
            AnalyticUtils.sendGaEvent(this, AnalyticUtils.CATEGORY.newsDetail, AnalyticUtils.ACTION.viewPage, PREFIX + nb.getTid() + "#" + nb.getNid(), totalTile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            enterTime = System.currentTimeMillis();
            AnalyticUtils.sendUmengEvent(this, PREFIX + nb.getTid() + "#" + nb.getNid(), nb.getAuthorname());
            AnalyticUtils.sendGaScreenViewHit(this, PREFIX + nb.getTid() + "#" + nb.getNid(), nb.getCnname(), nb.getAuthorname());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (BuildConfig.DEBUG) {
                return;
            }
            if (null != nb && !TextUtils.isEmpty(nb.getNid())) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(enterTime);
//                DBHelper.getInstance(getApplicationContext()).getLogDbUtils()
//                        .save(new UserLog(nb.getNid(), SPUtil.format(calendar), (int) ((System.currentTimeMillis() - enterTime) / 1000)));
            }
        } catch (Exception e) {
        }
    }

    private long enterTime = 0;
    private final int MAX_SIZE = 30;
}