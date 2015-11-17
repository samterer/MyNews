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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.avatarqing.loadmore.lib.LoadMoreContainer;
import com.avatarqing.loadmore.lib.LoadMoreHandler;
import com.avatarqing.loadmore.lib.LoadMoreListViewContainer;
import com.facebook.CallbackManager;
import com.facebook.Profile;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.hzpd.adapter.CommentListAdapter;
import com.hzpd.custorm.CustomScrollView;
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
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.db.UserLog;
import com.hzpd.ui.App;
import com.hzpd.ui.dialog.FontsizePop;
import com.hzpd.ui.widget.FontTextView;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.CODE;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.Constant;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.EventUtils;
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
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
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
import java.util.List;

public class NewsDetailActivity extends MBaseActivity implements OnClickListener {
    private CallbackManager callbackManager;

    @Override
    public String getAnalyticPageName() {
        return "新闻详情页-";
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
    private RelativeLayout mRelativeLayoutTitleRoot;
    private boolean mFlagPopuShow;
    private CustomScrollView mLayoutRoot;
    private WebSettings webSettings;
    private View mBack;
    private ProgressBar load_progress_bar;
    private LinearLayout mRoot;
    private LinearLayout mButtomLayout1;// 底部1
    private NewsDetailBean mBean;
    private ListView mCommentListView;
    private LoadMoreListViewContainer mLoadMoreContainer;
    private CommentListAdapter mCommentListAdapter;
    private View news_detail_nonetwork;

    // ---------------------------

    private RelativeLayout newdetail_rl_comm;
    private TextView newdetail_tv_comm;// 评论
    private ImageView newdetail_fontsize;// 字体
    private ImageView newdetail_share;// 分享
    private ImageView newdetail_collection;// 收藏
    private ImageView newdetail_more;//更多

    private LinearLayout newsdetails_title_comment;// 跳转到评论
    // -------------------------
    private FontsizePop fontpop;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE.font_big: {
//					SPUtil.getInstance().setTextSize(CODE.textSize_big);
                    setupWebView(CODE.textSize_big);
//					FontSizeEvent event = new FontSizeEvent(CODE.textSize_big);
//					EventBus.getDefault().post(event);
                }
                break;
                case CODE.font_mid: {
//					SPUtil.getInstance().setTextSize(CODE.textSize_normal);
                    setupWebView(CODE.textSize_normal);
//					FontSizeEvent event = new FontSizeEvent(CODE.textSize_normal);
//					EventBus.getDefault().post(event);
                }
                break;
                case CODE.font_small: {
//					SPUtil.getInstance().setTextSize(CODE.textSize_small);
                    setupWebView(CODE.textSize_small);
//					FontSizeEvent event = new FontSizeEvent(CODE.textSize_small);
//					EventBus.getDefault().post(event);
                }
                break;
            }

        }
    };

    App.Callback callback = new App.Callback() {
        @Override
        public void onSuccess(Profile currentProfile) {
//            Log.e("test", "onSuccess " + nb + "::" + currentProfile);
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

    private ImageView details_iv_comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getThisIntent();
        super.onCreate(savedInstanceState);
        App.getInstance().setProfileTracker(callback);
        setContentView(R.layout.news_details_layout);

        try {
            load_progress_bar = (ProgressBar) findViewById(R.id.load_progress_bar);
            details_iv_comment = (ImageView) findViewById(R.id.details_iv_comment);
            if (loading) {
                progress = 0;
                wProgress = 0;
                load_progress_bar.postDelayed(runnable, 50);
            }
            details_iv_comment.setVisibility(View.VISIBLE);
            details_iv_comment.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (skipComment()) return;
                }
            });

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
            try {
                if (null != nb && !TextUtils.isEmpty(nb.getNid())) {
                    DBHelper.getInstance(getApplicationContext()).getLogDbUtils()
                            .save(new UserLog(nb.getNid(), SPUtil.format(Calendar.getInstance())));
                }
            } catch (Exception e) {
            }

            // 适配器设置
//            mCommentListAdapter = new CommentListAdapter();
            mCommentListAdapter = new CommentListAdapter(nb.getNid());
            mCommentListView.setAdapter(mCommentListAdapter);
            callbackManager = CallbackManager.Factory.create();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
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

                    try {
                        NewsBeanDB nbfc = dbHelper.getNewsListDbUtils().findFirst(
                                Selector.from(NewsBeanDB.class).where("nid", "=", nb.getNid()));
                        if (null != nbfc) {
                            if (Integer.parseInt(nbfc.getComcount()) > 0) {
//                                details_iv_comment.setVisibility(View.GONE);
                            }
                        } else {
                        }

                    } catch (DbException e) {
                        e.printStackTrace();
                    }

                    Bundle bundle = data.getExtras();
                    boolean isShare = bundle.getBoolean("isShare");
                    //评论内容
//                    Log.e("test", "bundle" + bundle.getString("result"));
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
                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String date = sDateFormat.format(new java.util.Date());
                    LogUtils.e("date" + date);
                    bean.setDateline(date);
                    bean.setUid(spu.getUser().getUid());
                    bean.setStatus("1");
                    bean.setPraise("0");
//                    nb.setFav(Integer);
                    latestList.add(0, bean);
                    LogUtils.e("latestList" + latestList.toString());
                    mCommentListAdapter.insertData(latestList, 0);
//                    ll_rob.setVisibility(View.GONE);
                    mLayoutRoot.updateUI();
//                    if (nb.getComcount()!=null||latestList.size()>=0){
//                        details_iv_comment.setVisibility(View.GONE);
//                    }
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
        initViews();
        initPopupWindows();

        getNewsDetails();

        isCollection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nb = null;
    }

    private LinearLayout ll_tag;
    private LinearLayout llayout;
    private LinearLayout ll_rob;
    private LinearLayout rl_related;
    private FontTextView details_more_check;
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
        mLoadMoreContainer = (LoadMoreListViewContainer) findViewById(R.id.load_more_list_view_container);
        mCommentListView = (ListView) findViewById(R.id.comment_listview);

        LayoutInflater infla = LayoutInflater.from(this);
        View headView = infla.inflate(R.layout.details_related_news, null);
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

        mCommentListView.addHeaderView(headView);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // 关闭硬件加速
        mRelativeLayoutTitleRoot = (RelativeLayout) findViewById(R.id.news_detail_layout);
        mBack = findViewById(R.id.news_detail_bak);
        mRoot = (LinearLayout) findViewById(R.id.news_detail_main_root_id);
        mLayoutRoot = (CustomScrollView) findViewById(R.id.news_detail_root_id);
        newdetail_rl_comm = (RelativeLayout) findViewById(R.id.comment_box);
        newdetail_tv_comm = (TextView) findViewById(R.id.newdetail_tv_comm);
        newdetail_fontsize = (ImageView) findViewById(R.id.newdetail_fontsize);
        newdetail_share = (ImageView) findViewById(R.id.newdetail_share);
        newdetail_collection = (ImageView) findViewById(R.id.newdetail_collection);
        newdetail_more = (ImageView) findViewById(R.id.newdetail_more);
        news_detail_nonetwork = findViewById(R.id.news_detail_nonetwork);
        newsdetails_title_comment = (LinearLayout) findViewById(R.id.newsdetails_title_comment);
        mButtomLayout1 = (LinearLayout) findViewById(R.id.news_detail_ll_bottom1);

        mBack.setOnClickListener(this);
        newdetail_rl_comm.setOnClickListener(this);
        newdetail_tv_comm.setOnClickListener(this);
        newdetail_fontsize.setOnClickListener(this);
        newdetail_share.setOnClickListener(this);
        newdetail_collection.setOnClickListener(this);
        newdetail_more.setOnClickListener(this);
        newsdetails_title_comment.setOnClickListener(this);

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

        initCommentListView();
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
                    new Handler().postDelayed(new Runnable() {
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
//            text_undal_praise.setText("" + unlike_counts);
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

        httpUtils.send(HttpMethod.POST, ADDCOLLECTION_url// InterfaceApi.addcollection
                , params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
            }

            @Override
            public void onFailure(HttpException error, String msg) {
            }
        });
    }

    private void initCommentListView() {
        // 添加一个占位的HeaderView，避免ListView无任何子View
        TextView headerView = new TextView(this);
        ListView.LayoutParams lp = new AbsListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, 1);
        headerView.setLayoutParams(lp);
        mCommentListView.addHeaderView(headerView);
        mLoadMoreContainer.setLoadMoreHandler(new LoadMoreHandler() {
            @Override
            public void onLoadMore(LoadMoreContainer loadMoreContainer) {
                getLatestComm();
            }
        });

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
                case R.id.newsdetails_title_comment: {
                    // 跳转到评论页
                    if (null == nb) {
                        return;
                    }
                    String img = "";
                    if (null != nb.getImgs() && nb.getImgs().length > 0) {
                        img = nb.getImgs()[0];
                    }
                    ReplayBean bean = new ReplayBean(nb.getNid(), nb.getTitle(),
                            Constant.TYPE.News.toString(), nb.getJson_url(), img, nb.getComcount());

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("reply", bean);
                    Intent intent = new Intent(NewsDetailActivity.this, XF_NewsCommentsActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    AAnim.ActivityStartAnimation(NewsDetailActivity.this);
                }
                break;
                case R.id.comment_box: {
                    if (skipComment()) return;
                }
                break;
                case R.id.details_iv_comment: {

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

                    if (null == fontpop) {
                        View view = this.getLayoutInflater().inflate(R.layout.nd_fontsize_pop, null);
                        fontpop = new FontsizePop(view, handler);
                        fontpop.showAsDropDown(mButtomLayout1, 0, -mButtomLayout1.getHeight() * 3 - 60);

                    } else {

                        if (fontpop.isShowing()) {
                            fontpop.dismiss();
                        } else {
                            fontpop.showAsDropDown(mButtomLayout1, 0, -mButtomLayout1.getHeight() * 3 - 60);

                        }
                    }
                }
                break;
                case R.id.newdetail_more: {
                    if (null == fontpop) {
                        View view = this.getLayoutInflater().inflate(R.layout.nd_more_pop, null);
                        fontpop = new FontsizePop(view, handler);
                        fontpop.showAsDropDown(mButtomLayout1, 0, -mButtomLayout1.getHeight() * 2 - 20);
                    } else {
                        if (fontpop.isShowing()) {
                            fontpop.dismiss();
                        } else {
                            fontpop.showAsDropDown(mButtomLayout1, 0, -mButtomLayout1.getHeight() * 2 - 20);
                        }
                    }
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
        httpUtils.send(HttpRequest.HttpMethod.POST, thirdLogin_url, params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        LogUtils.i("result-->" + responseInfo.result);
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
    }

    private ArrayList<CommentzqzxBean> latestList;
    private String completeCom;


    private View loadingView;

    private void webViewChangeProgress(final WebView webview) {
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
//                super.onReceivedSslError(view, handler, error);
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (TextUtils.isEmpty(url)) {
                    return true;
                }
                if (!url.startsWith(HOT_NEWS) && !SPUtil.isImageUri(url)) {
                    try {
                        Log.e("webview", "startActivity shouldOverrideUrlLoading " + url);
                        Intent intent = new Intent(NewsDetailActivity.this, WebActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(WebActivity.KEY_URL, url);
                        startActivity(intent);
                        Log.e("webview", "startActivity shouldOverrideUrlLoading " + url);
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
                Log.e("test", "url " + url);
                super.onLoadResource(view, url);
                if (SPUtil.isImageUri(url)) {
                    wProgress = 100;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e("webview", "onPageStarted");
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("webview", "onPageFinished ");
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
                                mLayoutRoot.updateUI();
                                int realY = mWebView.getContentHeight();
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
                Log.e("webview", "onCreateWindow");
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            }
        });

        webview.setHorizontalScrollBarEnabled(false);
    }


    boolean loading = true;
    int wProgress = 0;
    int MIDDLE_PROGRESS = 95;
    int progress = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (loading) {
                if (wProgress < MIDDLE_PROGRESS && progress < MIDDLE_PROGRESS) {
                    progress += 1;
                } else if (wProgress > MIDDLE_PROGRESS) {
                    progress += 5;
                    if (progress > 100) {
                        progress = 100;
                        wProgress = 101;
                    }
                }
                if (wProgress > 100) {
                    loading = false;
                    load_progress_bar.setVisibility(View.GONE);
                    return;
                }
                load_progress_bar.setProgress(progress);
                load_progress_bar.postDelayed(runnable, 20);
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
        webViewChangeProgress(mWebView);
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
//        stringBuilder.append("<a class='image loading 'style=\"width:500px;height:300px;\" ></a>");
//        stringBuilder.append("<img src='content://com.hzpd.provider.imageprovider/test/1'/>");
        stringBuilder.append(content);
        stringBuilder.append("<script type=\"text/javascript\" src=\"");
        stringBuilder.append("android.js");
        stringBuilder.append("\" ></script>");
        stringBuilder.append("<script type=\"text/javascript\" >");
        stringBuilder.append("</body></html>");
        mWebView.loadDataWithBaseURL(BASE_URL, formatStringToHtml(stringBuilder.toString(), textSize), "text/html", "utf-8", BASE_URL);
        jsInterface.setNewsDetailBean(mBean);

        EventUtils.sendReadAtical(activity);
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
            str = head.substring(start - 45, start-29);
            head=head.substring(0,start-45);
            localTime=localTime+DIV+" "+CONTENT_START;
            head=head+localTime;
            content = content.substring(start);
        }
        Log.e("head", "head  --->" + head);
        Log.e("head", "head  str--->" + str);
        Log.e("head", "head  localTime--->" + localTime);
        int headStart = head.indexOf(DIV) + DIV.length();
        Log.e("head", "head  headStart--->" + headStart);


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
        String station = SharePreferecesUtils.getParam(NewsDetailActivity.this, StationConfig.STATION, "def").toString();
        String siteid = null;
        String mLatestComm_url = null;
        if (station.equals(StationConfig.DEF)) {
            siteid = InterfaceJsonfile.SITEID;
            mLatestComm_url = InterfaceJsonfile.CHECKCOMMENT;
        } else if (station.equals(StationConfig.YN)) {
            siteid = InterfaceJsonfile_YN.SITEID;
            mLatestComm_url = InterfaceJsonfile_YN.CHECKCOMMENT;
        } else if (station.equals(StationConfig.TW)) {
            siteid = InterfaceJsonfile_TW.SITEID;
            mLatestComm_url = InterfaceJsonfile_TW.CHECKCOMMENT;
        }
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("Page", "" + mCurPage);
        params.addBodyParameter("PageSize", "" + mPageSize);
        params.addBodyParameter("nid", mBean.getNid());
        params.addBodyParameter("type", "News");
        params.addBodyParameter("siteid", siteid);
        String url = mLatestComm_url;
        httpUtils.send(HttpMethod.POST
                , url
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {

                if (responseInfo.result == null) {
//                    ll_rob.setVisibility(View.VISIBLE);
                } else {
                    parseCommentJson(responseInfo.result);
                }

                mCurPage++;
            }


            @Override
            public void onFailure(HttpException error, String msg) {
                mLoadMoreContainer.loadMoreFinish(true, false);
            }
        });
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
                ll_rob.setVisibility(View.GONE);
                latestList = (ArrayList<CommentzqzxBean>) JSONArray.parseArray(
                        obj.getString("data"), CommentzqzxBean.class);

                Log.e("test", "test--->" + latestList.toString());
                // 添加数据到Adpater
                mCommentListAdapter.appendData(latestList);
                hasMore = true;
                // 加载更多容器的设置
                if (latestList.size() > 15) {
                    mLoadMoreContainer.useDefaultHeader();
                }

                Log.e("mLoadMoreContainer", "parseCommentJson");
            } else {
                hasMore = false;
                if (mCurPage <= 1) {
                    //没有评论，显示抢沙发
//                    ll_rob.setVisibility(View.VISIBLE);
                }
                //TUtils.toast(obj.getString("msg"));
                mLoadMoreContainer.removeFooterView(mLoadMoreContainer.getmFooterView());
                mLayoutRoot.updateUI();
            }
            boolean emptyResult = (mCommentListAdapter == null || mCommentListAdapter.isEmpty());
            mLoadMoreContainer.loadMoreFinish(emptyResult, hasMore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<NewsBean> refList;

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
                    Log.e("mBean", "mBean--->" + mBean.toString());
                    refList = mBean.getRef();
                    int textSize = spu.getTextSize();
                    setupWebView(textSize);
                    mRoot.setVisibility(View.VISIBLE);
                    mLayoutRoot.setVisibility(View.VISIBLE);
                    mButtomLayout1.setVisibility(View.VISIBLE);
                    news_detail_nonetwork.setVisibility(View.GONE);
                    if (mBean.getRef() != null && refList.size() > 0) {
                        rl_related.setVisibility(View.VISIBLE);
                        addRelatedNewsView();
                    }
                    loadingView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingView.setVisibility(View.GONE);
                        }
                    }, 500);
                    if (mBean.getTag() != null) {
                        rl_related.setVisibility(View.VISIBLE);
                        ll_tag.setVisibility(View.VISIBLE);
                        Log.e("tag", "从缓存中获取tag--->" + mBean.getTag().toString());
                    } else {
                        Log.e("tag", "从缓存中获取tag--->null");
                    }
                    addRelatedTagView();
                    getLatestComm();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            httpUtils.download(nb.getJson_url(), detailPathRoot + "detail_" + nb.getNid(), new RequestCallBack<File>() {
                @Override
                public void onStart() {
                    super.onStart();
                    loadingView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    try {
                        loadingView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingView.setVisibility(View.GONE);
                            }
                        }, 500);
                        String data = App.getFileContext(responseInfo.result);
                        if (TextUtils.isEmpty(data)) {
                            //                    TUtils.toast(getString(R.string.toast_request_failed));
                            return;
                        }
                        JSONObject obj = FjsonUtil.parseObject(data);
                        if (null == obj) {
                            responseInfo.result.delete();
                            //TODO 请求服务器重新生成json文件
                            TUtils.toast(getString(R.string.toast_cache_invalidate));
                            return;
                        }
                        try {
                            mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), NewsDetailBean.class);
                            Log.e("mBean", "mBean--->" + mBean.toString());
                            int textSize = spu.getTextSize();
                            setupWebView(textSize);
                            if (mBean.getRef() != null && mBean.getRef().size() < 0) {
                                rl_related.setVisibility(View.VISIBLE);
                            }
                            addRelatedNewsView();
                            if (mBean.getTag() != null) {
                                rl_related.setVisibility(View.VISIBLE);
                                ll_tag.setVisibility(View.VISIBLE);
                                Log.e("tag", "httpUtils tag--->" + mBean.getTag().toString());
                            } else {
                                Log.e("tag", "httpUtils tag--->null");
                            }
                            addRelatedTagView();
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
                    loading = false;
                    wProgress = 100;
                    progress = 0;
                    loadingView.setVisibility(View.GONE);
                    news_detail_nonetwork.setVisibility(View.VISIBLE);
                    if (isResume) {
                        TUtils.toast(getString(R.string.toast_cannot_connect_network));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取相关新闻
    private void addRelatedNewsView() {
        if (refList != null) {
            for (int i = 0; i < refList.size(); i++) {
                final NewsBean bean = refList.get(i);
                View view = LayoutInflater.from(this).inflate(R.layout.news_detail_other_layout, null);

                TextView text = (TextView) view.findViewById(R.id.news_detail_other_title_id);
                View line = view.findViewById(R.id.view_line);
                text.setText(bean.getTitle());

                Log.e("addRelatedNewsView", "addRelatedNewsView-->" + bean.getTitle());
                text.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
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

    //获取相关tag
    private void addRelatedTagView() {
        if (mBean != null && mBean.getTag() != null) {
            Log.e("tag", "tag--->" + mBean.getTag());
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

    private class MyOnClickListener implements OnClickListener {
        private String tag;

        public MyOnClickListener(String tag) {
            this.tag = tag;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra("TAGCONNENT", tag);
            intent.setClass(NewsDetailActivity.this, SearchActivity.class);
            startActivity(intent);
            AAnim.ActivityStartAnimation(NewsDetailActivity.this);
        }
    }

    @Override
    public void finish() {
        if (isDetail) {
            isDetail = false;
            if (null != mWebView) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                    int textSize = spu.getTextSizeNews();
                    setupWebView(textSize);
                }
            }
            return;
        }

        if (!App.isStartApp) {
            Intent in = new Intent();
            in.setClass(this, MainActivity.class);
            startActivity(in);
        }

        super.finish();
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
            bu.putString("nid", refList.get(i).getNid());
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
            newdetail_collection.setImageResource(R.drawable.details_collect_select);
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

            httpUtils.send(HttpMethod.POST, ADDCOLLECTION_url// InterfaceApi.addcollection
                    , params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    JSONObject obj = null;

                    try {
                        obj = JSONObject.parseObject(responseInfo.result);
                        if (200 == obj.getIntValue("code")) {
                            JSONObject object = obj.getJSONObject("data");
                            // 1:收藏操作成功 2:取消收藏操作成功
                            if ("1".equals(object.getString("status"))) {
                                newdetail_collection.setImageResource(R.drawable.details_collect_already_select);
                                TUtils.toast(getString(R.string.toast_collect_success));
                            } else {
                                newdetail_collection.setImageResource(R.drawable.details_collect_select);
                                TUtils.toast(getString(R.string.toast_collect_cancelled));
                            }
                        } else {
                            TUtils.toast(getString(R.string.toast_collect_failed));
                        }
                    } catch (Exception e) {
                        TUtils.toast(getString(R.string.toast_collect_failed));
                        return;
                    }

                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    TUtils.toast(getString(R.string.toast_cannot_connect_to_server));
                }
            });
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

            httpUtils.send(HttpMethod.POST, ISCELLECTION_url, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    LogUtils.i("isCollection result-->" + responseInfo.result);
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
        } else {
            try {
                NewsItemBeanForCollection nbfc = dbHelper.getCollectionDBUitls()
                        .findFirst(Selector.from(NewsItemBeanForCollection.class).where("nid", "=", nb.getNid())
                                .and("type", "=", "1"));
                if (null != nbfc) {
                    newdetail_collection.setImageResource(R.drawable.details_collect_already_select);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    private CommentsCountBean ccBean;

    @Override
    protected void onPause() {
        SharePreferecesUtils.setParam(this, StationConfig.DETAILS_LOCATION + nb.getNid(), mWebView.getScrollY());
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticUtils.sendGaEvent(this, AnalyticUtils.CATEGORY.newsDetail, AnalyticUtils.ACTION.viewPage, nb.getNid() + "@" + nb.getTitle(), 0L);
        AnalyticUtils.sendUmengEvent(this, AnalyticUtils.CATEGORY.newsDetail, nb.getNid() + "@" + nb.getTitle());
    }

}