package com.hzpd.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.facebook.ads.NativeAd;
import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.Adbean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.NewsPageListBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.modle.event.UpdateNewsBeanDbEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.HtmlActivity;
import com.hzpd.ui.activity.NewsAlbumActivity;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.XF_NewsHtmlDetailActivity;
import com.hzpd.ui.activity.ZhuanTiActivity;
import com.hzpd.ui.interfaces.I_Control;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.NewsListDbTask;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class NewsItemFragment extends BaseFragment implements I_Control, View.OnClickListener {
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisible != isVisibleToUser) {
            isVisible = isVisibleToUser;
            try {
                if (isVisibleToUser) {
                    AnalyticUtils.sendGaEvent(getActivity(), AnalyticUtils.CATEGORY.newsType, AnalyticUtils.ACTION.viewPage, channelbean.getCnname(),
                            0L);
                    AnalyticUtils.sendUmengEvent(getActivity(), AnalyticUtils.CATEGORY.newsType, channelbean.getCnname());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    private NativeAd nativeAd;
    private TextView update_counts;

    private SwipeRefreshLayout mSwipeRefreshWidget;
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
        EventBus.getDefault().register(this);
    }


    public String getTitle() {
        return channelbean.getCnname();
    }

    RecyclerView.LayoutManager layoutManager;
    private ImageView background_empty;

    boolean addLoading = false;

    private boolean isRefreshCounts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        page = 1;
        isRefresh = true;
        View view = inflater.inflate(R.layout.news_channel_fragment, container, false);
        background_empty = (ImageView) view.findViewById(R.id.background_empty);
        update_counts = (TextView) view.findViewById(R.id.update_counts);
        background_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeRefreshWidget.setRefreshing(true);
            }
        });
        mSwipeRefreshWidget = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recylerlist);

        mSwipeRefreshWidget.setColorScheme(R.color.google_blue,R.color.google_tool);
        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                mFlagRefresh = true;
                getFlash();
                getServerList("");
                isRefreshCounts = false; //TODO hide
            }
        });

        mRecyclerView.setHasFixedSize(true);
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
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        layoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new NewsItemListViewAdapter(activity, this);
        mRecyclerView.setAdapter(adapter);
        callBack = new NewsItemListViewAdapter.CallBack() {
            @Override
            public void loadMore() {
                getServerList("");
            }
        };
        adapter.callBack = callBack;
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nativeAd = null;
        Adbean adbean = App.getInstance().channelADMap.get(channelbean.getTid());
        if (adbean != null && !TextUtils.isEmpty(adbean.getFacebookid())) {
            nativeAd = new NativeAd(getActivity().getApplicationContext(), adbean.getFacebookid());
            adapter.setNativeAd(nativeAd, adbean.getPosition());
        }

        page = 1;
        mFlagRefresh = true;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void init() {
        if (page == 1) {
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
        Log.e("test", "getDbList ");
        newsListDbTask.findList(channelbean.getTid(), page, pageSize, new I_SetList<NewsBeanDB>() {

            @Override
            public void setList(List<NewsBeanDB> list) {
                if (!isAdded()) {
                    return;
                }
                String nids = "";
                if (null != list && list.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    List<NewsBean> nbList = new ArrayList<NewsBean>();
                    for (NewsBeanDB nbdb : list) {
                        sb.append(nbdb.getNid() + ",");
                        nbList.add(nbdb.getNewsBean());
                    }
                    adapter.appendData(nbList, mFlagRefresh, true);
                    background_empty.setVisibility(View.GONE);
                    if (sb.length() > 1) {
                        nids = sb.substring(0, sb.length() - 1);
                    }
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
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("tid", channelbean.getTid());
        params.addBodyParameter("Page", "" + page);
        params.addBodyParameter("PageSize", "" + pageSize);
        if (page == 1) {
            String newTimew = App.getInstance().newTimeMap.get(channelbean.getTid());
            newTimew = newTimew == null ? "" : newTimew;
            params.addBodyParameter("newTime", newTimew);
        }

        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.NEWSLIST
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                isLoading = false;
                if (!isAdded()) {
                    return;
                }
                mSwipeRefreshWidget.setRefreshing(false);

                final JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj) {
                    //缓存更新
                    setData(obj);
                    page++;
                } else if (isAdded()) {
                    TUtils.toast(getString(R.string.toast_cannot_connect_network));
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                isLoading = false;
                if (!isAdded()) {
                    return;
                }
                mSwipeRefreshWidget.setRefreshing(false);
                TUtils.toast(getString(R.string.toast_cannot_connect_network));
            }
        });
    }

    boolean loadad = true;

    //服务端返回数据处理
    @Override
    public void setData(JSONObject obj) {
        if (!isAdded()) {
            return;
        }
        //数据处理
        switch (obj.getIntValue("code")) {
            case 200: {
                List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);

                if (null != list) {

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

                    adapter.removeOld();
                    StringBuilder builder = new StringBuilder();
                    for (NewsBean bean : list) {
                        builder.append(bean.getNid() + ",");
                    }
                    adapter.showLoading = true;
                    adapter.appendData(list, mFlagRefresh, false);
                    background_empty.setVisibility(View.GONE);
                    if (page == 1) {
                        App.getInstance().newTimeMap.put(channelbean.getTid(), obj.getString("newTime"));
                    }
                    if (loadad) {
                        try {
                            nativeAd.loadAd();
                            loadad = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    newsListDbTask.saveList(list, null);
                }

            }
            break;
            default: {
                TUtils.toast(getString(R.string.pull_to_refresh_reached_end));
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
        mFlagRefresh = false;
    }


    //获取幻灯
    private void getFlash() {
        final File pageFile = App.getFile(newsItemPath + File.separator
                + "channel_" + channelbean.getTid()
                + File.separator + "flash");
        String path = InterfaceJsonfile.FLASH + channelbean.getTid();

        httpUtils.download(
                path
                , pageFile.getAbsolutePath()
                , new RequestCallBack<File>() {
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        String data = App.getFileContext(responseInfo.result);
                        JSONObject obj = FjsonUtil.parseObject(data);

                        if (null == obj) {
                            responseInfo.result.delete();
                            return;
                        }
                        mSwipeRefreshWidget.setRefreshing(false);
                        List<NewsPageListBean> mViewPagelist = null;
                        if (200 == obj.getIntValue("code")) {
                            JSONObject object = obj.getJSONObject("data");
                            mViewPagelist = FjsonUtil.parseArray(object.getString("flash"), NewsPageListBean.class);
                        }
                        if (mViewPagelist != null && mViewPagelist.size() > 0) {
                            adapter.setFlashlist(mViewPagelist);
                            background_empty.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        LogUtils.i("getFlash-failed");
                    }
                });
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (nativeAd != null) {
            nativeAd.unregisterView();
            nativeAd.setAdListener(null);
            nativeAd.destroy();
            nativeAd = null;
        }
        super.onDestroy();
    }

    public void onEventMainThread(FontSizeEvent event) {
        adapter.setFontSize(event.getFontSize());
    }

    public void onEventMainThread(UpdateNewsBeanDbEvent event) {
        String msg = event.getmMsg();
        Log.e("NewsItemFragment", "msg--->" + msg);
        if (msg.equals("Update_OK")) {
            Log.e("NewsItemFragment", "msg--->数据更新");
//            getDbList();
        }
    }


    //点击操作
    @Override
    public void onClick(View view) {
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
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
        adapter.setReadedId(nb.getNid());
        ////////////////////////////
        //1新闻  2图集  3直播 4专题  5关联新闻 6视频
        if ("1".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsDetailActivity.class);
        } else if ("2".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsAlbumActivity.class);
        } else if ("3".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), HtmlActivity.class);//直播界面
        } else if ("4".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), ZhuanTiActivity.class);
        } else if ("5".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsDetailActivity.class);
        } else if ("6".equals(nb.getRtype())) {
//					mIntent.setClass(getActivity(),VideoPlayerActivity.class);
            mIntent.setClass(getActivity(), NewsDetailActivity.class);
        } else if ("7".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), HtmlActivity.class);
        } else if ("9".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), XF_NewsHtmlDetailActivity.class);
        } else {
            return;
        }

        activity.startActivityForResult(mIntent, 0);
        AAnim.ActivityStartAnimation(getActivity());
    }

}
