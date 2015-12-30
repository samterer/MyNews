package com.hzpd.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.color.tools.mytools.TUtil;
import com.facebook.CallbackManager;
import com.facebook.Profile;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
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
import com.hzpd.modle.TagBean;
import com.hzpd.modle.ThirdLoginBean;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.UserLog;
import com.hzpd.modle.event.TagEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.dialog.FontsizePop;
import com.hzpd.ui.widget.CustomRecyclerView;
import com.hzpd.ui.widget.FontTextView;
import com.hzpd.ui.widget.SwipeCloseLayout;
import com.hzpd.url.InterfaceJsonfile;
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
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.news.update.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

public class NewsDetailActivity extends MBaseActivity implements OnClickListener, AdListener {

    private static CallbackManager callbackManager = CallbackManager.Factory.create();
    NewsDetailAdapter adapter;
    CustomRecyclerView recyclerView;

    private SwipeCloseLayout swipeCloseLayout;
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

    private String BASEURL = "index.php?s=/Public/newsview/nid/";

    public final static String IMG_PREFIX = "com.hzpd.provider.imageprovider";
    public final static String HOT_NEWS = "hotnews://";

    private boolean mFlagPopuShow;
    private WebSettings webSettings;
    private View mBack;
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
    private ImageView newdetail_comment;//评论页
    private ImageView newdetail_collection;// 收藏
    private ImageView newdetail_more;//更多
    private RelativeLayout mRelativeLayoutTitleRoot;
    // -------------------------
    private FontsizePop fontpop;
    private View details_tag_layout;
    private ImageView details_head_tag_img;
    private TextView details_head_tag_name;
    private TextView details_head_tag_num;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        Log.e("test", "1 " + System.currentTimeMillis());
        nativeAd = new NativeAd(this, AD_KEY);
        nativeAd.setAdListener(this);
        App.getInstance().setProfileTracker(callback);
        setContentView(R.layout.news_details_layout);
        initViews();
        //显示tag订阅相关
        details_tag_layout = findViewById(R.id.details_tag_layout);
        details_head_tag_img = (ImageView) findViewById(R.id.details_head_tag_img);
        details_head_tag_name = (TextView) findViewById(R.id.details_head_tag_name);
        details_head_tag_num = (TextView) findViewById(R.id.details_head_tag_num);
        details_tv_subscribe = (TextView) findViewById(R.id.details_tv_subscribe);
        newdetail_share = (ImageView) findViewById(R.id.newdetail_share);
        newdetail_share.setOnClickListener(this);
        mBack = findViewById(R.id.news_detail_bak);
        mBack.setOnClickListener(this);
        mRoot = (LinearLayout) findViewById(R.id.news_detail_main_root_id);
        newdetail_collection = (ImageView) findViewById(R.id.newdetail_collection);
        newdetail_collection.setOnClickListener(this);
        newdetail_comment = (ImageView) findViewById(R.id.newdetail_comment);
        newdetail_comment.setOnClickListener(this);
        getThisIntent();
        if (nb != null && BuildConfig.DEBUG) {
            TUtils.toast("nid=" + nb.getNid());
        }
        try {
            if (loading) {
                progress = 0;
                wProgress = 0;
            }

            loadingView = findViewById(R.id.app_progress_bar);
            initNew();
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


        super.changeStatusBar();
    }

    private View view;
    private TextView details_title_name;
    private TextView details_source_time;
    private TextView details_time;
    private LinearLayout details_head_title_layout;

    public void setDetailsHead(View view) {
        this.view = view;
        details_head_title_layout = (LinearLayout) view.findViewById(R.id.details_head_title_layout);
        details_title_name = (TextView) view.findViewById(R.id.details_title_name);
        details_source_time = (TextView) view.findViewById(R.id.details_source);
        details_time = (TextView) view.findViewById(R.id.details_time);

    }

    public void setWebview(WebView webView) {
        this.mWebView = webView;
        recyclerView.setWebView(mWebView);
        webViewChangeProgress(webView);
        getNewsDetails();
    }

    public void setListView(ListView listView) {
        this.mCommentListView = listView;
        View headView = mLayoutInflater.inflate(R.layout.details_related_news, null);
        ad_layout = (ViewGroup) headView.findViewById(R.id.ad_layout);
        ad_view = (ViewGroup) headView.findViewById(R.id.ad_view);
        mCommentListView.addHeaderView(headView);

        mCommentListView.setAdapter(mCommentListAdapter);
        mCommentListView.setVisibility(View.GONE);
        //请联系我们
        details_explain = (FontTextView) headView.findViewById(R.id.details_explain);
        try {
            String link = getResources().getString(R.string.details_lv_link);
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
        //新闻点赞
        ArticlePraise(headView);
        isDalNewsPraise();
        //查看原文
        details_more_check = (TextView) headView.findViewById(R.id.details_more_check);
        //相关
        rl_related = (LinearLayout) headView.findViewById(R.id.rl_related);
        //相关tag
        ll_tag = (LinearLayout) headView.findViewById(R.id.ll_tag);
        //相关新闻
        llayout = (LinearLayout) headView.findViewById(R.id.llayout);
        //没有评论
        ll_rob = (LinearLayout) headView.findViewById(R.id.ll_rob);

        mRelativeLayoutTitleRoot = (RelativeLayout) findViewById(R.id.news_detail_layout);


        newdetail_rl_comm = (RelativeLayout) findViewById(R.id.comment_box);
        newdetail_tv_comm = (TextView) findViewById(R.id.newdetail_tv_comm);
        newdetail_fontsize = (ImageView) findViewById(R.id.newdetail_fontsize);


        newdetail_more = (ImageView) findViewById(R.id.newdetail_more);
        news_detail_nonetwork = findViewById(R.id.news_detail_nonetwork);
        mButtomLayout1 = (LinearLayout) findViewById(R.id.news_detail_ll_bottom1);


        newdetail_rl_comm.setOnClickListener(this);
        newdetail_tv_comm.setOnClickListener(this);
        newdetail_fontsize.setOnClickListener(this);


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
                getNewsDetails();
            }
        });

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

                    ll_rob.setVisibility(View.GONE);
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
            detailPathRoot = App.getInstance().getJsonFileCacheRootDir() + File.separator + "newsdetail"
                    + File.separator;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initNew() {
        dbUtils = DBHelper.getInstance(this).getCollectionDBUitls();
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
        try {
            callback = null;
            loading = false;
            App.getInstance().setProfileTracker(null);
            nb = null;
            mWebView = null;
            swipeCloseLayout.removeAllViews();
            mRoot = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    private LinearLayout ll_tag;
    private LinearLayout llayout;
    private LinearLayout ll_rob;
    private LinearLayout rl_related;
    private TextView details_more_check;
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
    private TextView details_tv_subscribe;

    private void initViews() {
        mLayoutInflater = LayoutInflater.from(this);
        swipeCloseLayout = (SwipeCloseLayout) findViewById(R.id.swipe_container);
        swipeCloseLayout.setActivity(this);
        recyclerView = (CustomRecyclerView) findViewById(R.id.recycler_view);
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
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("type", "" + type);
        params.addBodyParameter("nid", nb.getNid());
        params.addBodyParameter("num", "" + num);
        //like unlike
        SPUtil.addParams(params);
        HttpHandler httpHandler = httpUtils.send(HttpMethod.POST, InterfaceJsonfile.News_Price// InterfaceApi.addcollection
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

    private final static int SCANNIN_GREQUEST_CODE = 0;
    private List<String> permissions = Arrays.asList("public_profile", "user_friends");

    @Override
    public void onClick(View v) {
        if (AvoidOnClickFastUtils.isFastDoubleClick())
            return;
        try {
            switch (v.getId()) {
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
                case R.id.newdetail_comment: {
                    // 跳转到评论页
                    if (nb == null) {
                        return;
                    }
                    Intent commentsIntent = new Intent(this, XF_NewsCommentsActivity.class);
                    commentsIntent.putExtra("News_nid", nb.getNid());
                    startActivity(commentsIntent);
                    AAnim.ActivityStartAnimation(NewsDetailActivity.this);
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
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("userid", tlb.getUserid());
        params.addBodyParameter("gender", tlb.getGender());
        params.addBodyParameter("nickname", tlb.getNickname());
        params.addBodyParameter("photo", tlb.getPhoto());
        params.addBodyParameter("third", tlb.getThird());
        params.addBodyParameter("is_ucenter", "0");
        SPUtil.addParams(params);
        HttpHandler httpHandler = httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.thirdLogin, params,
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
                        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
                            return true;
                        }
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
                    hideLoading();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!isResume) {
                    return;
                }
                try {
                    wProgress = 100;
                    hideLoading();
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

    /**
     * 容纳标题栏、视频、WebView
     */
    private TextView title;
    private TextView time;
    private WebView mWebView;

    private void hideLoading() {
        if (loadingView.getVisibility() == View.VISIBLE) {
            loadingView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingView.setVisibility(View.GONE);
                }
            }, 500);
        }
    }

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
    public static final String HEAD = "<html><head>" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"
            + " <meta name=\"viewport\"\n" +
            "          content=\"width=device-width, initial-scale=1.0, minimum-scale=0.5, maximum-scale=2.0, user-scalable=yes\"/>"
            + "<style>@font-face {font-family: 'kievit';src: url('file:///android_asset/fonts/KievitPro-Regular.otf');}</style>"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"android.css\" />\n"
            + "<script type=\"text/javascript\" src=\"android.js\" ></script>"
            + "</head><body>";

    public static final String HEAD_night = "<html><head>" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"
            + " <meta name=\"viewport\"\n" +
            "          content=\"width=device-width, initial-scale=1.0, minimum-scale=0.5, maximum-scale=2.0, user-scalable=yes\"/>"
            + "<style>@font-face {font-family: 'kievit';src: url('file:///android_asset/fonts/KievitPro-Regular.otf');}</style>"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"androidnight.css\" />\n"
            + "<script type=\"text/javascript\" src=\"android.js\" ></script>"
            + "</head><body>";
    public static final String FOOTER = "</body></html>";

    private void setContentData(int textSize) {

        String content = mBean.getContent();
        content = processContent(content);
        StringBuilder stringBuilder = new StringBuilder();
        if (App.getInstance().getThemeName().equals("2")) {
            stringBuilder.append(HEAD_night);
        } else {
            stringBuilder.append(HEAD);
        }
        stringBuilder.append(content);
        stringBuilder.append(FOOTER);
        mWebView.loadDataWithBaseURL(BASE_URL, formatStringToHtml(stringBuilder.toString(), textSize), "text/html", "utf-8", BASE_URL);
        jsInterface.setNewsDetailBean(mBean);
    }

    public static final String DIV = "</div>";

    // 对内容做特殊处理
    private String processContent(String content) {
        content = content.replaceAll("data-src=", "src=");
        content = content.replaceAll("data-origin=", "src=");
        content = content.replaceAll("data-lazy-src=", "src=");
        content = content.replaceAll("src=\"//", "src=\"http://");
        content = content.replaceAll("src='//", "src='http://");

        content = content.replaceAll("<script[^/]*?</script>", " ");
        content = content.replaceAll("<script[^/]*?</script>", " ");

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

        return content;
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
    private int mPageSize = 10;


    //查看评论
    private void getLatestComm() {
        //TODO 评论
        if (mBean == null) {
            return;
        }
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("Page", "" + mCurPage);
        params.addBodyParameter("PageSize", "" + mPageSize);
        params.addBodyParameter("nid", mBean.getNid());
        params.addBodyParameter("type", "News");
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        HttpHandler httpHandler = httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.CHECKCOMMENT
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {

                if (responseInfo.result == null) {
                    ll_rob.setVisibility(View.VISIBLE);
                } else {
                    parseCommentJson(responseInfo.result);
                }

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
            if (obj != null && 200 == obj.getIntValue("code")) {
                ll_rob.setVisibility(View.GONE);
                latestList = (ArrayList<CommentzqzxBean>) JSONArray.parseArray(
                        obj.getString("data"), CommentzqzxBean.class);

                // 添加数据到Adpater
                mCommentListAdapter.appendData(latestList);
            } else {
                if (mCurPage <= 1) {
                    ll_rob.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //获取详情
    private void getNewsDetails() {
        //TODO  获取详细信息
        try {
            File pageFile = App.getFile(detailPathRoot + "detail_" + nb.getNid());
            //从缓存中获取
            if (GetFileSizeUtil.getInstance().getFileSizes(pageFile) > 30) {
                try {
                    String data = App.getFileContext(pageFile);
                    JSONObject obj = JSONObject.parseObject(data);
                    mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), NewsDetailBean.class);
                    if (mBean == null) {
                        return;
                    }
                    setContents();
                    getLatestComm();
                    return;
                } catch (Exception e) {
                    pageFile.delete();
                    e.printStackTrace();
                }

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
                            if (mBean == null) {
                                return;
                            }
                            setContents();
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
                        if (error.getExceptionCode() == 404) {
                            TUtil.toast(NewsDetailActivity.this, getString(R.string.error_delete));
                        } else {
                            TUtils.toast(getString(R.string.toast_cannot_connect_network));
                        }
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

    private boolean isTagSelect;

    private void setContents() {
        int textSize = spu.getTextSize();
        setupWebView(textSize);
        mRoot.setVisibility(View.VISIBLE);
        mButtomLayout1.setVisibility(View.VISIBLE);
        news_detail_nonetwork.setVisibility(View.GONE);

        //新闻标题时间
        if (mBean != null) {
            details_head_title_layout.setVisibility(View.VISIBLE);
            details_title_name.setText("" + nb.getTitle());
            if (!TextUtils.isEmpty(nb.getCopyfrom())) {
                details_source_time.setText("" + nb.getCopyfrom());
            } else {
                details_source_time.setText("");
            }
            if (!TextUtils.isEmpty(nb.getUpdate_time())) {
                String localTime = CalendarUtil.loaclTime(nb.getUpdate_time());
                details_time.setText("" + localTime);
            } else {
                details_time.setText("");
            }
        }

        //查看原文
        if (mBean.getSource() != null) {
            details_more_check.setVisibility(View.VISIBLE);
            details_more_check.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AvoidOnClickFastUtils.isFastDoubleClick()) {
                        return;
                    }
                    Intent intent = new Intent(NewsDetailActivity.this, WebActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(WebActivity.KEY_URL, mBean.getSource());
                    startActivity(intent);
                }
            });
        }

//        tag相关
        if ((mBean.getTag() != null && mBean.getTag().size() > 0)) {
            final TagBean tagBean = mBean.getTag().get(0);
            rl_related.setVisibility(View.VISIBLE);
            ll_tag.setVisibility(View.VISIBLE);
            addRelatedTagView();
            details_tag_layout.setVisibility(View.VISIBLE);
            if (tagBean.getIcon() != null) {
                details_head_tag_img.setVisibility(View.VISIBLE);
                SPUtil.displayImage(mBean.getTag().get(0).getIcon(), details_head_tag_img
                        , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Personal_center_News));
            } else {
                details_head_tag_img.setVisibility(View.GONE);
            }
            details_head_tag_name.setText(tagBean.getName());
            if (tagBean.getNum() != null) {
                int num = Integer.parseInt(tagBean.getNum());
                if (num > 1) {
                    details_head_tag_num.setVisibility(View.VISIBLE);
                    details_head_tag_num.setText("" + num + "" + getString(R.string.follow_num));
                }
            } else {
                details_head_tag_num.setVisibility(View.GONE);
            }

            if (SPUtil.checkTag(tagBean)) {
                details_tv_subscribe.setTextColor(getResources().getColor(R.color.white));
                details_tv_subscribe.setCompoundDrawables(null, null, null, null);
                details_tv_subscribe.setText(getString(R.string.discovery_followed));
                details_tv_subscribe.setText(getString(R.string.look_over));
                isTagSelect = true;
            } else {
                details_tv_subscribe.setTextColor(getResources().getColor(R.color.white));
                Drawable nav_up = getResources().getDrawable(R.drawable.editcolum_image);
                nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                details_tv_subscribe.setCompoundDrawables(nav_up, null, null, null);
                isTagSelect = false;
            }


            details_tv_subscribe.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isTagSelect) {
                        details_tv_subscribe.setTextColor(getResources().getColor(R.color.white));
                        Intent intent = new Intent(NewsDetailActivity.this, TagActivity.class);
                        intent.putExtra("tagbean", tagBean);
                        startActivity(intent);
                        isTagSelect = false;
                    } else {
                        details_tv_subscribe.setTextColor(getResources().getColor(R.color.white));
                        details_tv_subscribe.setCompoundDrawables(null, null, null, null);
                        EventBus.getDefault().post(new TagEvent(mBean.getTag().get(0)));
                        details_tv_subscribe.setText(getString(R.string.look_over));
                        Toast.makeText(NewsDetailActivity.this, getString(R.string.tag_followed), Toast.LENGTH_SHORT).show();
                        if (Utils.isNetworkConnected(NewsDetailActivity.this)) {
                            RequestParams params = RequestParamsUtils.getParamsWithU();
                            if (spu.getUser() != null) {
                                params.addBodyParameter("uid", spu.getUser().getUid());
                            }
                            params.addBodyParameter("tagId", mBean.getTag().get(0).getId());
                            SPUtil.addParams(params);
                            HttpHandler httpHandler = httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.tag_click_url, params, new RequestCallBack<String>() {
                                @Override
                                public void onSuccess(ResponseInfo<String> responseInfo) {
                                    JSONObject obj = null;
                                    try {
                                        obj = JSONObject.parseObject(responseInfo.result);
                                    } catch (Exception e) {
                                        return;
                                    }
                                    if (200 == obj.getIntValue("code")) {

                                    }
                                }

                                @Override
                                public void onFailure(HttpException error, String msg) {
                                    LogUtils.i("isCollection failed");
                                }
                            });
                            handlerList.add(httpHandler);
                        }
                        isTagSelect = true;
                    }
                }
            });
        }
        if (mBean.getRef() != null && mBean.getRef().size() > 0) {
            for (int i = 0; i < mBean.getRef().size(); i++) {
                if (mBean.getRef().get(i).getTitle() == null) {
                    NewsBean bean = mBean.getRef().get(i);
                    mBean.getRef().remove(bean);
                    i--;
                }
            }
            rl_related.setVisibility(View.VISIBLE);
            ll_tag.setVisibility(View.VISIBLE);
            addRelatedNewsView();
        }

    }

    private void showEmpty() {
        loading = false;
        wProgress = 100;
        progress = 0;
        loadingView.setVisibility(View.GONE);
        news_detail_nonetwork.setVisibility(View.VISIBLE);
    }

    //获取相关新闻
    private void addRelatedNewsView() {
        if (mBean.getRef() != null && mBean.getRef().size() > 0) {
            for (int i = 0; i < mBean.getRef().size(); i++) {
                final NewsBean bean = mBean.getRef().get(i);
                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(R.attr.item_title, typedValue, true);
                int color = typedValue.data;
                View view = null;
                if ("4".equals(bean.getType())) {
                    view = LayoutInflater.from(this).inflate(R.layout.news_3_item_layout, null);
                    setThreePic(bean, view, color);
                } else if ("99".equals(bean.getType())) {
                    view = LayoutInflater.from(this).inflate(R.layout.news_large_item_layout, null);
                    setLargePic(bean, view, color);
                } else {
                    if (bean.getImgs() == null || bean.getImgs().length == 0) {
                        view = LayoutInflater.from(this).inflate(
                                R.layout.news_list_text_layout, null);
                        setTextPic(bean, view, color);

                    } else {
                        view = LayoutInflater.from(this).inflate(R.layout.news_list_item_layout, null);
                        setLeftPic(bean, view, color);
                    }
                }
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

    private void setTextPic(NewsBean bean, View view, int color) {
        TextViewHolder textViewHolder = new TextViewHolder();
        textViewHolder.newsitem_title = (TextView) view.findViewById(R.id.newsitem_title);
        textViewHolder.newsitem_title.setTextColor(color);
        textViewHolder.newsitem_title.setText(bean.getTitle());
        textViewHolder.item_type_iv = (ImageView) view.findViewById(R.id.item_type_iv);
        SPUtil.setAtt(textViewHolder.item_type_iv, bean.getAttname());

        textViewHolder.newsitem_collectcount = (TextView) view.findViewById(R.id.newsitem_collectcount);
        String fav = bean.getFav();
        if (!TextUtils.isEmpty(fav)) {

            int fav_counts = Integer.parseInt(fav);
            if (fav_counts > 0) {
                textViewHolder.newsitem_collectcount.setVisibility(View.VISIBLE);
                textViewHolder.newsitem_collectcount.setText(fav_counts + "");
            } else {
                textViewHolder.newsitem_collectcount.setVisibility(View.GONE);
            }
        } else {
            textViewHolder.newsitem_collectcount.setVisibility(View.GONE);
        }
        textViewHolder.newsitem_source = (TextView) view.findViewById(R.id.newsitem_source);
        String from = bean.getCopyfrom();
        if (!TextUtils.isEmpty(from)) {
            textViewHolder.newsitem_source.setVisibility(View.VISIBLE);
            textViewHolder.newsitem_source.setText(from);
        } else {
            textViewHolder.newsitem_source.setVisibility(View.GONE);
        }
        textViewHolder.newsitem_commentcount = (TextView) view.findViewById(R.id.newsitem_commentcount);
        String comcount = bean.getComcount();
        if (!TextUtils.isEmpty(comcount)) {
            int counts = Integer.parseInt(comcount);
            if (counts > 0) {
                textViewHolder.newsitem_commentcount.setVisibility(View.VISIBLE);
                bean.setComcount(counts + "");
                textViewHolder.newsitem_commentcount.setText(counts + "");
            } else {
                textViewHolder.newsitem_commentcount.setVisibility(View.GONE);
            }
        } else {
            textViewHolder.newsitem_commentcount.setVisibility(View.GONE);
        }
        textViewHolder.newsitem_time = (TextView) view.findViewById(R.id.newsitem_time);
        if (CalendarUtil.friendlyTime(bean.getUpdate_time(), this) == null) {
            textViewHolder.newsitem_time.setText("");
        } else {
            textViewHolder.newsitem_time.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), this));
        }
    }

    //纯文本，评论，时间，脚标
    private class TextViewHolder {
        private TextView newsitem_title;
        private ImageView nli_foot;
        //		来源
        private TextView newsitem_source;
        //		收藏数
        private TextView newsitem_collectcount;
        //		评论数
        private TextView newsitem_commentcount;
        private TextView newsitem_time;
        private ImageView newsitem_unlike;
        private ImageView item_type_iv;
        private LinearLayout ll_tag;

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

    private void setLeftPic(NewsBean bean, View view, int color) {
        VHLeftPic vhLeftPic = new VHLeftPic();
        vhLeftPic.newsitem_title = (TextView) view.findViewById(R.id.newsitem_title);
        vhLeftPic.newsitem_title.setTextColor(color);
        vhLeftPic.newsitem_title.setText(bean.getTitle());
        vhLeftPic.item_type_iv = (ImageView) view.findViewById(R.id.item_type_iv);
        SPUtil.setAtt(vhLeftPic.item_type_iv, bean.getAttname());

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

    private void setThreePic(NewsBean bean, View view, int color) {
        VHThree vhThree = new VHThree();
        vhThree.newsitem_title = (TextView) view.findViewById(R.id.newsitem_title);
        vhThree.newsitem_title.setTextColor(color);
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
        SPUtil.setAtt(vhThree.item_type_iv, bean.getAttname());

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

    private void setLargePic(NewsBean bean, View view, int color) {
        VHLargePic vhLargePic = new VHLargePic();

        vhLargePic.newsitem_title = (TextView) view.findViewById(R.id.newsitem_title);
        vhLargePic.newsitem_title.setTextColor(color);
        vhLargePic.newsitem_title.setText(bean.getTitle());
        vhLargePic.item_type_iv = (ImageView) view.findViewById(R.id.item_type_iv);
        SPUtil.setAtt(vhLargePic.item_type_iv, bean.getAttname());
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
        if (mBean.getTag() != null) {
            if (mBean.getTag().size() > 3) {
                for (int i = 0; i < 3; i++) {
                    View view = LayoutInflater.from(this).inflate(R.layout.details_related_tag, null);
                    TextView text = (TextView) view.findViewById(R.id.details_tag1);
                    TagBean tagBean = mBean.getTag().get(i);
                    String tagcontent = tagBean.getName();
                    text.setText("" + tagcontent);
                    view.setOnClickListener(new MyOnClickListener(tagBean));
                    ll_tag.addView(view);
                }
            } else {
                for (int i = 0; i < mBean.getTag().size(); i++) {
                    View view = LayoutInflater.from(this).inflate(R.layout.details_related_tag, null);
                    TextView text = (TextView) view.findViewById(R.id.details_tag1);
                    TagBean tagBean = mBean.getTag().get(i);
                    String tagcontent = tagBean.getName();
                    text.setText(tagcontent);
                    view.setOnClickListener(new MyOnClickListener(tagBean));
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
            View view = SPUtil.getRandomAdView(this, nativeAd);
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
        private TagBean tagBean;

        public MyOnClickListener(TagBean tagBean) {
            this.tagBean = tagBean;
        }

        @Override
        public void onClick(View v) {
            if (AvoidOnClickFastUtils.isFastDoubleClick())
                return;
//            Intent intent = new Intent();
//            intent.putExtra("TAGCONNENT", tagBean);
//            intent.setClass(NewsDetailActivity.this, SearchActivity.class);
//            startActivity(intent);
            Intent intent = new Intent(NewsDetailActivity.this, TagActivity.class);
            intent.putExtra("tagbean", tagBean);
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
            LogUtils.i("Type-->" + nb.getType() + "  Fid-->" + nb.getNid());
            RequestParams params = RequestParamsUtils.getParamsWithU();
            params.addBodyParameter("type", "1");
            params.addBodyParameter("typeid", nb.getNid());
            params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
            params.addBodyParameter("data", nb.getJson_url());
            SPUtil.addParams(params);
            HttpHandler httpHandler = httpUtils.send(HttpMethod.POST, InterfaceJsonfile.ADDCOLLECTION// InterfaceApi.addcollection
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
        if (spu.getUser() == null) {
            try {
                LogUtils.i("isCollection");
                NewsItemBeanForCollection nbfc = dbUtils
                        .findFirst(Selector.from(NewsItemBeanForCollection.class).where("nid", "=", nb.getNid()));
                LogUtils.i("isCollection");
                if (null != nbfc) {
                    LogUtils.i("isCollection   getTitle:" + nbfc.getTitle());
                    newdetail_collection.setImageResource(R.drawable.details_collect_already_select);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            RequestParams params = RequestParamsUtils.getParamsWithU();
            params.addBodyParameter("typeid", nb.getNid());
            params.addBodyParameter("type", "1");
            SPUtil.addParams(params);
            HttpHandler httpHandler = httpUtils.send(HttpMethod.POST, InterfaceJsonfile.ISCELLECTION, params, new RequestCallBack<String>() {
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
                            newdetail_collection.setImageResource(R.drawable.details_collect_already_select);
                        }
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    LogUtils.i("isCollection failed");
                }
            });
            handlerList.add(httpHandler);
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
                DBHelper.getInstance(getApplicationContext()).getLogDbUtils()
                        .save(new UserLog(nb.getNid(), SPUtil.format(calendar), (int) ((System.currentTimeMillis() - enterTime) / 1000)));
            }
        } catch (Exception e) {
        }
    }

    private long enterTime = 0;

    private final int MAX_SIZE = 30;
}