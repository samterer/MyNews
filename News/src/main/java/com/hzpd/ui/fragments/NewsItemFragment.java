package com.hzpd.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.CustomSwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.color.tools.mytools.LogUtils;
import com.facebook.ads.NativeAd;
import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.NewsPageListBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.modle.event.JoKeBadEvent;
import com.hzpd.modle.event.JokeGoodEvent;
import com.hzpd.modle.event.RefreshEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.ConfigBean;
import com.hzpd.ui.activity.NewsAlbumActivity;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.SearchActivity;
import com.hzpd.ui.activity.VideoPlayerActivity;
import com.hzpd.ui.activity.ZhuanTiActivity;
import com.hzpd.ui.interfaces.I_Control;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.NewsListDbTask;
import com.news.update.Utils;
import com.squareup.okhttp.Request;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class NewsItemFragment extends BaseFragment implements I_Control, View.OnClickListener {
    public final static String PREFIX = "C:";

    @Override
    public String getAnalyticPageName() {
        if (channelbean != null) {
            return PREFIX + channelbean.getCnname();
        } else {
            return AnalyticUtils.SCREEN.newsType;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisible != isVisibleToUser) {
            if (isVisibleToUser) {
                if (isVisibleToUser) {
                    AnalyticUtils.sendGaEvent(getActivity(), AnalyticUtils.CATEGORY.newsType, AnalyticUtils.ACTION.viewPage, channelbean.getCnname(),
                            0L);
                    AnalyticUtils.sendUmengEvent(getActivity(), AnalyticUtils.CATEGORY.newsType, channelbean.getCnname());
                }
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    private NewsItemListViewAdapter adapter;
    private NewsChannelBean channelbean;//本频道
    private String newsItemPath;//本频道根目录flash
    private int page = 1;
    private static final int pageSize = 15;//
    private NewsListDbTask newsListDbTask; //新闻列表数据库
    // 是否刷新最新数据
    private boolean mFlagRefresh = true;
    private boolean isRefresh = true;//是否首次加载
    private boolean isNeedRefresh = true;
    private int position = -1;
    private HashMap<String, NativeAd> ads = new HashMap<>();
    private TextView update_counts;

    private CustomSwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView mRecyclerView;

    NewsItemListViewAdapter.CallBack callBack;

    public NewsItemFragment() {

    }

    public NewsItemFragment(NewsChannelBean channelbean) {
        this.channelbean = channelbean;
        setTitle(channelbean.getCnname());
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsItemPath = App.getInstance().getJsonFileCacheRootDir();

        newsListDbTask = new NewsListDbTask(activity);
    }


    public String getTitle() {
        return channelbean.getCnname();
    }

    RecyclerView.LayoutManager layoutManager;
    private ImageView background_empty;

    boolean addLoading = false;

    private boolean isRefreshCounts;
    boolean pullRefresh = false;
    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        page = 1;
        isRefresh = true;
        View view = inflater.inflate(R.layout.news_channel_fragment, container, false);
        view.findViewById(R.id.main_top_layout).setVisibility(View.GONE);
        if (!ConfigBean.getInstance().open_channel.contains(SPUtil.getCountry())) {
            view.findViewById(R.id.main_top_layout).setVisibility(View.VISIBLE);
            View main_top_search = view.findViewById(R.id.main_top_search);
            main_top_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AvoidOnClickFastUtils.isFastDoubleClick(v))
                        return;
                    Intent mIntent = new Intent();
                    mIntent.setClass(getActivity(), SearchActivity.class);
                    startActivity(mIntent);
                    AAnim.ActivityStartAnimation(getActivity());
                }
            });
        }
        background_empty = (ImageView) view.findViewById(R.id.background_empty);
        update_counts = (TextView) view.findViewById(R.id.update_counts);
        background_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeRefreshWidget.setRefreshing(true);
                refresh();
            }
        });
        mSwipeRefreshWidget = (CustomSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recylerlist);

        tag = OkHttpClientManager.getTag();

        mSwipeRefreshWidget.setColorSchemeResources(R.color.google_blue);

        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshWidget.setRefreshing(true);
                refresh();
            }
        });
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //解决RecyclerView和SwipeRefreshLayout共用存在的bug
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshWidget.setEnabled(topRowVerticalPosition >= 0);

                if (addLoading && !adapter.showLoading) {
                    addLoading = false;
                    int count = adapter.getItemCount();
                    adapter.showLoading = true;
                    adapter.notifyItemInserted(count);
                }
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        layoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new NewsItemListViewAdapter(activity, this);
        adapter.setAds(ads);
        mRecyclerView.setAdapter(adapter);
        callBack = new NewsItemListViewAdapter.CallBack() {
            @Override
            public void loadMore() {
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getServerList("");
                    }
                }, 10);
            }
        };
        adapter.callBack = callBack;
        return view;
    }

    private void refresh() {
        mRecyclerView.scrollToPosition(0);
        pullRefresh = true;
        page = 1;
        mFlagRefresh = true;
        getFlash();
        getServerList("");
        isRefreshCounts = false; //TODO hide
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        page = 1;
        mFlagRefresh = true;
        firstLoading = false;
        if (!ConfigBean.getInstance().open_channel.contains(SPUtil.getCountry())) {
            loadData();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        Log.e("onDestroyView", "onDestroyView " + getAnalyticPageName());
        OkHttpClientManager.cancel(tag);
        EventBus.getDefault().unregister(this);
        SPUtil.clearAds(ads);
        adapter.appendData(null, true, false);
        mRecyclerView.setAdapter(null);
        mRecyclerView.removeAllViews();
        mRecyclerView = null;
        adapter = null;
        SPUtil.clearAds(ads);
        super.onDestroyView();
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

    //新闻列表
    @Override
    public void getDbList() {
        newsListDbTask.findList(channelbean, page, pageSize, new I_SetList<NewsBeanDB>() {

            @Override
            public void setList(List<NewsBeanDB> list) {
                String nids = "";
                if (null != list && list.size() > 5) {
                    StringBuilder sb = new StringBuilder();
                    List<NewsBean> nbList = new ArrayList<NewsBean>();
                    for (NewsBeanDB nbdb : list) {
                        sb.append(nbdb.getNid() + ",");
                        NewsBean newsBean = nbdb.getNewsBean();
                        newsBean.setCnname(channelbean.getCnname());
                        nbList.add(newsBean);
                    }
                    addLoading = true;
                    adapter.appendData(nbList, mFlagRefresh, true);
                    background_empty.setVisibility(View.GONE);
                }
                getServerList("");
            }
        });
    }

    boolean isLoading = false;

    //获取新闻list
    @Override
    public void getServerList(String nids) {
        if (isLoading) {
            return;
        }
        if (!Utils.isNetworkConnected(App.getInstance())) {
            showEmpty();
            TUtils.toast(getString(R.string.toast_check_network));
            return;
        }
        Map<String, String> params = RequestParamsUtils.getMaps();
        params.put("siteid", InterfaceJsonfile.SITEID);
        params.put("tid", channelbean.getTid());
        params.put("tagId", channelbean.getId());
        params.put("Page", "" + page);
        params.put("PageSize", "" + pageSize);
        if (page == 1) {
            String newTimew = App.getInstance().newTimeMap.get(channelbean.toString());
            newTimew = newTimew == null ? "" : newTimew;
            params.put("newTime", newTimew);
        }
        SPUtil.addParams(params);
        isLoading = true;
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.NEWSLIST

                , new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                try {
                    isLoading = false;
                    mSwipeRefreshWidget.setRefreshing(false);
                    final JSONObject obj = FjsonUtil.parseObject(response.toString());
                    if (null != obj) {
                        //缓存更新
                        setData(obj);
                        page++;
                    } else if (isAdded()) {
                        TUtils.toast(getString(R.string.toast_cannot_connect_network));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                showEmpty();
                TUtils.toast(getString(R.string.toast_cannot_connect_network));
                AnalyticUtils.sendGaEvent(getActivity(), AnalyticUtils.ACTION.networkErrorOnList, null, null, 0L);
                AnalyticUtils.sendUmengEvent(getActivity(), AnalyticUtils.ACTION.networkErrorOnList);
            }
        }, params);
    }

    private void showEmpty() {
        isLoading = false;
        pullRefresh = false;
        mSwipeRefreshWidget.setRefreshing(false);
        if (!isAdded()) {
            return;
        }
    }

    boolean loadad = true;

    //服务端返回数据处理
    @Override
    public void setData(JSONObject obj) {
        //数据处理
        switch (obj.getIntValue("code")) {
            case 200: {
                List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);
                if (null != list && list.size() > 0) {
//                    if (BuildConfig.DEBUG) {
//                        Log.i("BuildConfig.DEBUG", "BuildConfig.DEBUG");
//                        for (int i = 0; i < list.size(); i++) {
//                            NewsBean bean = list.get(i);
//                            if (bean.getType().equals("2")) {
//                                bean.setType("11");
//                                list.add(bean);
//                                Log.i("BuildConfig.DEBUG", "BuildConfig.DEBUG   111");
//                            }
//                            Log.i("BuildConfig.DEBUG","BuildConfig.DEBUG"+list.get(i).getType());
//                        }
//                    }
                    newsListDbTask.saveList(list, null);
                    for (NewsBean bean : list) {
                        bean.setCnname(channelbean.getCnname());
                    }
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

                    if (page == 1) {
                        if (list.size() > 7) {
                            adapter.removeOld();
                        }
                    }
                    if (list.size() >= 5) {
                        adapter.showLoading = true;
                    } else {
                        addLoading = true;
                    }

                    adapter.appendData(list, mFlagRefresh, false);
                    background_empty.setVisibility(View.GONE);
                    if (page == 1) {
                        App.getInstance().newTimeMap.put(channelbean.toString(), obj.getString("newTime"));
                    }
                }

            }
            break;
            default: {
                if (pullRefresh) {
                    TUtils.toast(getString(R.string.pull_to_refresh_reached_end));
                }
                if (page > 1 && adapter.showLoading) {
                    int count = adapter.getItemCount();
                    adapter.showLoading = false;
                    adapter.notifyItemRemoved(count);
                    mRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addLoading = true;
                        }
                    }, 2000);
                }
            }
            break;
        }
        pullRefresh = false;
        mFlagRefresh = false;
    }

    //获取幻灯
    private void getFlash() {
        if (TextUtils.isEmpty(channelbean.getTid())) {
            return;
        }
        final File pageFile = App.getFile(newsItemPath + File.separator
                + "channel_" + channelbean.getTid()
                + File.separator + "flash");
        String path = InterfaceJsonfile.FLASH + channelbean.getTid();
        String country = SPUtil.getCountry();
        path = path.replace("#country#", country.toLowerCase());
        Log.i("", "pageFile.getAbsolutePath()" + pageFile.getAbsolutePath());
//        HttpHandler httpHandler =
        OkHttpClientManager.getAsyn(tag,
                path
//                        , pageFile.getAbsolutePath()
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
//        handlerList.add(httpHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onEventMainThread(FontSizeEvent event) {
        adapter.setFontSize(event.getFontSize());
    }


    public void onEventMainThread(RefreshEvent event) {
        if (event.force || isVisible) {
            mSwipeRefreshWidget.setRefreshing(true);
            refresh();
        }
    }

    public void onEventMainThread(JokeGoodEvent event) {
        String nid = event.nid;
        Log.i("JokeGoodEvent", "JokeGoodEvent---nid:" + nid + "isJokeGood:" + event.isJokeGood);
        if (event.isJokeGood) {//点赞

        } else {

        }
    }


    public void onEventMainThread(JoKeBadEvent event) {
        String nid = event.nid;
        Log.i("JokeGoodEvent", "JoKeBadEvent---nid:" + nid + "JoKeBadEvent:" + event.isJokeBad);
        if (event.isJokeBad) {//点赞

        } else {

        }
    }


    //点击操作
    @Override
    public void onClick(View view) {
        if (AvoidOnClickFastUtils.isFastDoubleClick(view)) {
            return;
        }
        TextView title = (TextView) view.findViewById(R.id.newsitem_title);
        if (null != title) {
            title.setTextColor(getResources().getColor(R.color.grey_font));
        }

        NewsBean nb = (NewsBean) view.getTag();
        Intent mIntent = new Intent();
        mIntent.putExtra("newbean", nb);
        mIntent.putExtra("from", "newsitem");
        adapter.setReadedId(nb);
        //1新闻  2图集  3直播 4专题  5关联新闻 6视频
//TODO 视频新闻
        if ("1".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsDetailActivity.class);
        } else if ("2".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsAlbumActivity.class);
        } else if ("3".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsDetailActivity.class);//直播界面
        } else if ("4".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), ZhuanTiActivity.class);
        } else if ("5".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsDetailActivity.class);
        } else if ("6".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), VideoPlayerActivity.class);
        } else if ("7".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsDetailActivity.class);
        } else {
            return;
        }

        activity.startActivityForResult(mIntent, 0);
        AAnim.ActivityStartAnimation(getActivity());
    }

}
