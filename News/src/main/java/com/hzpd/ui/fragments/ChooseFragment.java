package com.hzpd.ui.fragments;


import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.CustomSwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.color.tools.mytools.LogUtils;
import com.facebook.ads.NativeAd;
import com.hzpd.adapter.ChooseAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.NewsPageListBean;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.modle.event.RefreshEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.ui.widget.RecyclerViewPauseOnScrollListener;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.NewsListDbTask;
import com.melnykov.fab.FloatingActionButton;
import com.news.update.Utils;
import com.squareup.okhttp.Request;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * 推荐频道
 */
public class ChooseFragment extends BaseFragment implements View.OnClickListener {
    public final static String PREFIX = "C:";

    private NewsChannelBean channelbean;//本频道
    private NewsListDbTask newsListDbTask; //新闻列表数据库
    private boolean isRefresh = true;//是否首次加载
    private CustomSwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ChooseAdapter adapter;

    private int tagIndex = 1; //TAG索引
    private int pageIndex = 1; //TAG页数
    private int page = 1;
    private static final int pageSize = 15;//
    private boolean loading = false;
    private ImageView background_empty;
    private FloatingActionButton mFloatBtn;
    private RecyclerView.OnScrollListener onScrollListener;

    @Override
    public String getAnalyticPageName() {
        return PREFIX + "REKOMENDASI";
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    break;
                default:
                    break;
            }

        }

    };
    private TextView update_counts;
    public static int scroll_status = RecyclerView.SCROLL_STATE_IDLE;
    boolean addLoading = false;
    ChooseAdapter.CallBack callBack;
    private boolean isRefreshCounts;

    public ChooseFragment(NewsChannelBean channelbean) {
        this.channelbean = channelbean;
    }

    public ChooseFragment() {
        this.channelbean = new NewsChannelBean();
        channelbean.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
        channelbean.setType(NewsChannelBean.TYPE_RECOMMEND);
        channelbean.setCnname(getString(R.string.recommend));
    }

    private boolean isAgainLoading;
    private boolean pullRefresh;

    private int color;
    private Object tag;
    private HashMap<String, NativeAd> ads = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //解决RecyclerView和SwipeRefreshLayout共用存在的bug
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshWidget.setEnabled(topRowVerticalPosition >= 0);
                if (addLoading && !adapter.showLoading) {
                    addLoading = false;
                    mRecyclerView.scrollToPosition(0);
                    mRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int count = adapter.getItemCount();
                            adapter.showLoading = true;
                            adapter.notifyDataSetChanged();
                        }
                    }, 300);
                }
            }

        };
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.choose_fragment, container, false);
        tag = OkHttpClientManager.getTag();
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.choose_fragment_line, typedValue, true);
        color = typedValue.data;
        background_empty = (ImageView) view.findViewById(R.id.background_empty);

        background_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAgainLoading = true;
                mSwipeRefreshWidget.setRefreshing(true);
            }
        });
        mSwipeRefreshWidget = (CustomSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recylerlist);
        mSwipeRefreshWidget.setColorSchemeResources(R.color.google_blue);
        mSwipeRefreshWidget.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefresh = true;
                page = 1;
                ++tagIndex;
                pageIndex = 1;
                getFlash();
                getServerList("");
                isRefreshCounts = false;
            }
        });


        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setOnScrollListener(onScrollListener);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ChooseAdapter(getActivity(), this);
        mRecyclerView.setAdapter(adapter);
        callBack = new ChooseAdapter.CallBack() {
            @Override
            public void loadMore() {
                if (loading) {
                    return;
                }
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        page = 2;
                        ++pageIndex;
                        getFlash();
                        getServerList("");
                    }
                }, 10);
            }
        };
        adapter.callBack = callBack;
        adapter.setAds(ads);
        mFloatBtn = (FloatingActionButton) view.findViewById(R.id.float_feedback_btn);

        return view;
    }

    //获取幻灯
    private void getFlash() {
        if (TextUtils.isEmpty(channelbean.getTid())) {
            return;
        }
        String path = InterfaceJsonfile.FLASH + channelbean.getTid();
        String country = SPUtil.getCountry();
        path = path.replace("#country#", country.toLowerCase());
        OkHttpClientManager.getAsyn(tag, path
                , new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                try {
                    if (!isAdded()) {
                        return;
                    }
                    String data = response.toString();
                    JSONObject obj = FjsonUtil.parseObject(data);
                    if (null == obj) {
                        return;
                    }
                    mSwipeRefreshWidget.setRefreshing(false);
                    List<NewsPageListBean> mViewPagelist = null;
                    if (200 == obj.getIntValue("code")) {
                        JSONObject object = obj.getJSONObject("data");
                        mViewPagelist = FjsonUtil.parseArray(object.getString("flash"), NewsPageListBean.class);
                        if (mRecyclerView.computeVerticalScrollOffset() < 10) {
                            mRecyclerView.scrollToPosition(0);
                        }
                    }
                    if (mViewPagelist != null && mViewPagelist.size() > 0) {
                        adapter.setFlashlist(mViewPagelist);
                        background_empty.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                LogUtils.i("getFlash-failed");
            }

        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsListDbTask = new NewsListDbTask(activity);
    }

    @Override
    public String getTitle() {
        return channelbean.getCnname();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        mFloatBtn.attachToRecyclerView(mRecyclerView, null, onScrollListener);
        mFloatBtn.setOnClickListener(mFloatBtnClickListener);
        mFloatBtn.setVisibility(View.VISIBLE);
        loading = false;
        page = 1;
        firstLoading = false;
        getDbList();
    }

    boolean firstLoading = false;

    public void loadData() {
        if (firstLoading) {
            return;
        }
        if (page == 1 && mRecyclerView != null) {
            firstLoading = true;
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getFlash();
                    getDbList();
                    isRefresh = true;
                }
            }, 200);
        }
    }


    public void showFeedback() {
        if (FeedbackTagFragment.shown) {
            return;
        }
        FeedbackTagFragment fragment = new FeedbackTagFragment();
        fragment.show(getActivity().getSupportFragmentManager(), FeedbackTagFragment.TAG);
    }

    private View.OnClickListener mFloatBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (AvoidOnClickFastUtils.isFastDoubleClick(v))
                return;
            // 显示反馈对话框
            showFeedback();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        OkHttpClientManager.cancel(tag);
        SPUtil.clearAds(ads);
    }

    //新闻列表
    public void getDbList() {
        newsListDbTask.findList(channelbean, page, pageSize, new I_SetList<NewsBeanDB>() {
            @Override
            public void setList(List<NewsBeanDB> list) {
                if (!isAdded()) {
                    return;
                }
                if (null != list && list.size() > 0) {
                    isRefresh = false;
                    List<NewsBean> nbList = new ArrayList<NewsBean>();
                    for (NewsBeanDB nbdb : list) {
                        NewsBean newsBean = nbdb.getNewsBean();
                        newsBean.setCnname(channelbean.getCnname());
                        nbList.add(newsBean);
                    }
                    for (NewsBean newsBean : nbList) {
                        if (newsBean.getType().equals("99")) {
                            nbList.remove(newsBean);
                            nbList.add(0, newsBean);
                            break;
                        }
                    }

                    adapter.showLoading = true;
                    adapter.setData(nbList);
                    background_empty.setVisibility(View.GONE);
                } else {
                    Log.e("list", "list--->null");
                    getChooseNewsJson();
                }
            }

        });
    }

    //TODO 提前获取推荐频道第一页
    public void getChooseNewsJson() {
        Map<String, String> params = RequestParamsUtils.getMaps();
        params.put("siteid", InterfaceJsonfile.SITEID);
        params.put("tid", "" + NewsChannelBean.TYPE_RECOMMEND);
        params.put("newTime", App.getInstance().newTime);
        params.put("oldTime", App.getInstance().oldTime);
        params.put("Page", "1");
        params.put("PageSize", "" + pageSize);
        params.put("tagIndex", "" + tagIndex);
        params.put("pageIndex", "" + pageIndex);
        UserBean user = SPUtil.getInstance().getUser();
        if (user != null && !TextUtils.isEmpty(user.getUid())) {
            params.put("uid", "" + user.getUid());
        }
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.CHANNEL_RECOMMEND_NEW
                , new OkHttpClientManager.ResultCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            isRefresh = false;
                            mSwipeRefreshWidget.setRefreshing(false);
                            if (response == null) {
                                return;
                            }
                            final JSONObject obj = FjsonUtil.parseObject(response.toString());
                            if (null != obj) {
                                setData(obj);
                                try {
                                    App.getInstance().newTime = obj.getString("newTime");
                                    App.getInstance().oldTime = obj.getString("oldTime");
                                } catch (Exception e) {
                                }
                                List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);
                                if (list != null) {
                                    for (NewsBean bean : list) {
                                        bean.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
                                        bean.setCnname("REKOMENDASI");
                                    }
                                }
                                if (null != list) {
                                    new NewsListDbTask(getActivity()).saveList(list, null);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        isRefresh = false;
                        pullRefresh = false;
                        mSwipeRefreshWidget.setRefreshing(false);
                    }


                }, params
        );
    }

    //获取新闻list
    public void getServerList(String nids) {
        if (!isAdded() || !Utils.isNetworkConnected(getActivity())) {
            mSwipeRefreshWidget.setRefreshing(false);
            return;
        }
        Map<String, String> params = RequestParamsUtils.getMaps();
        params.put("siteid", InterfaceJsonfile.SITEID);
        params.put("newTime", App.getInstance().newTime);
        params.put("oldTime", App.getInstance().oldTime);
        params.put("Page", "" + page);
        params.put("PageSize", "" + pageSize);
        UserBean user = SPUtil.getInstance().getUser();
        if (user != null && !TextUtils.isEmpty(user.getUid())) {
            params.put("uid", "" + user.getUid());
            params.put("tagIndex", "" + tagIndex);
            params.put("pageIndex", "" + pageIndex);
        }
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.CHANNEL_RECOMMEND_NEW
                , new OkHttpClientManager.ResultCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            loading = false;
                            isRefresh = false;
                            if (!isAdded()) {
                                return;
                            }
                            try {
                                mSwipeRefreshWidget.setRefreshing(false);
                                final JSONObject obj = FjsonUtil.parseObject(response.toString());
                                if (null != obj) {
                                    setData(obj);//处理数据
                                    try {
                                        if (!TextUtils.isEmpty(obj.getString("newTime"))) {
                                            App.getInstance().newTime = obj.getString("newTime");
                                        } else if (!TextUtils.isEmpty(obj.getString("oldTime"))) {
                                            App.getInstance().oldTime = obj.getString("oldTime");
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            } catch (Exception e) {
                                onFailure(null, null);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        loading = false;
                        isRefresh = false;
                        pullRefresh = false;
                        if (!isAdded()) {
                            return;
                        }
                        TUtils.toast(getString(R.string.toast_cannot_connect_network));
                        mSwipeRefreshWidget.setRefreshing(false);
                    }

                }, params
        );
    }


    //服务端返回数据处理
    public void setData(JSONObject obj) {
        //数据处理
        switch (obj.getIntValue("code")) {
            case 200: {
                List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);
                if (list != null) {
                    for (NewsBean bean : list) {
                        bean.setTid(channelbean.getTid());
                    }
                }
                if (null != list) {
                    Log.i("setData", "setData Choose NewsBean size==" + list.size());
                    if (isRefreshCounts) {
                        update_counts.setVisibility(View.VISIBLE);
                        update_counts.setText(String.format(getString(R.string.update_counts), list.size()));
                        mRecyclerView.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                Log.e("update_counts", "update_counts");
                                update_counts.setVisibility(View.GONE);
                            }
                        }, 1000);
                    }
                    mRecyclerView.stopScroll();
                    if (adapter.getItemCount() > 0) {
//                        mRecyclerView.scrollToPosition(0);
                    }
                    if (!TextUtils.isEmpty(obj.getString("newTime"))) {
                        adapter.addTop(list);
                    } else if (!TextUtils.isEmpty(obj.getString("oldTime"))) {
                        adapter.addBottom(list);
                    } else if (pageIndex == 1) {
                        adapter.addTop(list);
                    } else {
                        adapter.addBottom(list);
                    }
                    background_empty.setVisibility(View.GONE);
                }
                newsListDbTask.saveList(list, null);
                adapter.showLoading = true;
            }
            break;
            default: {
                if (pullRefresh) {
                    TUtils.toast(getString(R.string.pull_to_refresh_reached_end));
                }
                if (adapter.showLoading) {
                    adapter.showLoading = false;
                    mRecyclerView.scrollToPosition(0);
                    adapter.notifyDataSetChanged();
                    mRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addLoading = true;
                        }
                    }, 2000);
                }
            }
        }
        isRefresh = false;
        pullRefresh = false;
    }


    @Override
    public void onClick(View v) {
        if (AvoidOnClickFastUtils.isFastDoubleClick(v)) {
            return;
        }
        try {
            if (v.getTag() instanceof NewsBean) {
                TextView title = (TextView) v.findViewById(R.id.newsitem_title);
                if (null != title) {
                    title.setTextColor(getResources().getColor(R.color.grey_font));
                }
                NewsBean nb = (NewsBean) v.getTag();
                Intent mIntent = new Intent();
                mIntent.putExtra("newbean", nb);
                mIntent.putExtra("from", "chooseItem");
                adapter.setReadedId(nb.getNid());
                //1新闻
                mIntent.setClass(v.getContext(), NewsDetailActivity.class);
                v.getContext().startActivity(mIntent);
                AAnim.ActivityStartAnimation(getActivity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void onEventMainThread(FontSizeEvent event) {
        adapter.setFontSize(event.getFontSize());
    }

    public void onEventMainThread(RefreshEvent event) {
        if (isVisible || event.force) {
            mRecyclerView.scrollToPosition(0);
            mSwipeRefreshWidget.setRefreshing(true);
            pullRefresh = true;
            page = 1;
            ++tagIndex;
            pageIndex = 1;
            getServerList("");
            isRefreshCounts = false;
        }
    }
}
