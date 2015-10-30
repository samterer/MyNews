package com.hzpd.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.facebook.ads.NativeAd;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.Adbean;
import com.hzpd.modle.CacheBean;
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
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.DataCleanManager;
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

    private PullToRefreshRecyclerView pullToRefreshRecyclerView;
    private RecyclerView mXListView;
    private View background_empty;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        page = 1;
        isRefresh = true;
        View view = inflater.inflate(R.layout.news_channel_fragment, container, false);
        pullToRefreshRecyclerView = (PullToRefreshRecyclerView) view.findViewById(R.id.pull_to_refresh_recyclerview);
        update_counts = (TextView) view.findViewById(R.id.update_counts);
        mXListView = pullToRefreshRecyclerView.getRefreshableView();
        layoutManager = new LinearLayoutManager(activity);
        mXListView.setLayoutManager(layoutManager);
        mXListView.scrollToPosition(0);
        adapter = new NewsItemListViewAdapter(activity, this);
        mXListView.setAdapter(adapter);
//        mXListView.addItemDecoration(itemDecoration);
        background_empty = view.findViewById(R.id.background_empty);
        background_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pullToRefreshRecyclerView.setRefreshing(true);
            }
        });
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        return view;
    }

    RecyclerView.ItemDecoration itemDecoration = new MyItemDecoration();

    int padding = 20;

    class MyItemDecoration extends RecyclerView.ItemDecoration {
        Paint mPaint;

        MyItemDecoration() {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(0xfff5f5f5);
        }

        @Override
        public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
            final int childSize = parent.getChildCount();
            for (int i = 0; i < childSize; i++) {
                final View child = parent.getChildAt(i);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + layoutParams.bottomMargin;
                final int bottom = top + padding;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, 0, 0, padding);
        }
    }

    public void setIsNeedRefresh() {
        isNeedRefresh = true;
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
        pullToRefreshRecyclerView.setMode(Mode.BOTH);
        pullToRefreshRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {

                //下拉刷新
                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_pull_label));
                page = 1;
                mFlagRefresh = true;
                getFlash();
                getServerList("");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                //上拉加载
                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_from_bottom_pull_label));
                mFlagRefresh = false;
                getServerList("");
            }
        });
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
        Log.e("test", " init ");
        if (page == 1) {
            mXListView.postDelayed(new Runnable() {
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
                getServerList(nids);
            }
        });
    }

    //获取新闻list
    @Override
    public void getServerList(String nids) {
        Log.e("test", "getServerList ");

        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("tid", channelbean.getTid());
        params.addBodyParameter("nids", nids);
        params.addBodyParameter("Page", "" + page);
        params.addBodyParameter("PageSize", "" + pageSize);
        params.addBodyParameter("update_time", spu.getCacheUpdatetime());

        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.NEWSLIST
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!isAdded()) {
                    return;
                }
//                update_counts.setVisibility(View.VISIBLE);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        update_counts.setVisibility(View.GONE);
//                    }
//                }, 1000);
                page++;
                pullToRefreshRecyclerView.onRefreshComplete();
//                Log.e("obj1", "obj--->" + responseInfo.result.toString());
                final JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
//                Log.e("obj","obj--->"+obj.toString());
                if (null != obj) {
                    //缓存更新
                    JSONObject cache = obj.getJSONObject("cachetime");
                    if (null != cache) {
                        spu.setCacheUpdatetime(cache.getString("update_time"));
                        List<CacheBean> cacheList = FjsonUtil.parseArray(cache.getString("data"), CacheBean.class);
                        DataCleanManager dcm = new DataCleanManager();
                        dcm.deleteDb(cacheList, activity, new I_Result() {
                            @Override
                            public void setResult(Boolean flag) {
                                setData(obj);
                            }
                        });
                    } else {
                        setData(obj);
                    }
                } else if (isAdded()) {
                    TUtils.toast(getString(R.string.toast_server_error));
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                if (!isAdded()) {
                    return;
                }
                pullToRefreshRecyclerView.onRefreshComplete();
                pullToRefreshRecyclerView.setMode(Mode.BOTH);
                TUtils.toast(getString(R.string.toast_server_error));
            }
        });
    }

    boolean loadad = true;

    //服务端返回数据处理
    @Override
    public void setData(JSONObject obj) {
        //数据处理
        switch (obj.getIntValue("code")) {
            case 200: {
                List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);
                if (null != list) {
                    adapter.removeOld();
                    StringBuilder builder = new StringBuilder();
                    for (NewsBean bean : list) {
                        builder.append(bean.getNid() + ",");
                    }
                    adapter.appendData(list, mFlagRefresh, false);
                    background_empty.setVisibility(View.GONE);
                    pullToRefreshRecyclerView.setMode(Mode.BOTH);
                    if (loadad) {
                        try {
                            nativeAd.loadAd();
                            loadad = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                newsListDbTask.saveList(list, new I_Result() {
                    @Override
                    public void setResult(Boolean flag) {
                        if (!flag) {
                            return;
                        }
                    }
                });
            }
            break;
            case 201: {
                pullToRefreshRecyclerView.setMode(Mode.BOTH);
            }
            break;
            case 202: {
                pullToRefreshRecyclerView.setMode(Mode.BOTH);
                TUtils.toast(getString(R.string.pull_to_refresh_reached_end));
            }
            break;
            case 209: {
                pullToRefreshRecyclerView.setMode(Mode.BOTH);
            }
            break;

            default: {
//                TUtils.toast(obj.getString("msg"));
            }
            break;
        }
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
