package com.hzpd.ui.fragments;


import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.ChooseAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.ui.widget.RecyclerViewPauseOnScrollListener;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.NewsListDbTask;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.lucasr.twowayview.widget.SpacingItemDecoration;
import org.lucasr.twowayview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 推荐频道
 */
public class ChooseFragment extends BaseFragment implements View.OnClickListener {
    public final static String PREFIX = "C:";

    private NewsChannelBean channelbean;//本频道
    private NewsListDbTask newsListDbTask; //新闻列表数据库
    private boolean isRefresh = true;//是否首次加载
    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ChooseAdapter adapter;

    private int tagIndex = 1; //TAG索引
    private int pageIndex = 1; //TAG页数
    private int page = 1;
    private static final int pageSize = 15;//
    private boolean loading = false;
    private View floatingView;
    private Animation animation;
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
    int padding = 20;
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
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.title_bar_color, typedValue, true);
        color = typedValue.data;
        background_empty = (ImageView) view.findViewById(R.id.background_empty);

        background_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAgainLoading = true;
                mSwipeRefreshWidget.setRefreshing(true);
//                getChooseNewsJson();
            }
        });
        floatingView = view.findViewById(R.id.floating_button);
        update_counts = (TextView) view.findViewById(R.id.update_counts);
        floatingView.setOnClickListener(this);
        ViewHelper.setAlpha(floatingView, 0.7f);
        mSwipeRefreshWidget = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
//        mSwipeRefreshWidget.setLayoutMode();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recylerlist);

        try {
            padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            mRecyclerView.setOnScrollListener(new RecyclerViewPauseOnScrollListener(ImageLoader.getInstance(), true, true));
            mRecyclerView.addItemDecoration(new SpacingItemDecoration(0, padding));
            mRecyclerView.setClipToPadding(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TypedValue typedValue1 = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.title_bar_color, typedValue1, true);
        int color1 = typedValue1.data;
        mSwipeRefreshWidget.setColorSchemeColors(color1);
//        mSwipeRefreshWidget.setColorScheme(R.color.google_blue);
//        mSwipeRefreshWidget.setColorSchemeResources(R.color.google_blue);
        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isAgainLoading) {
                    page = 1;
                    tagIndex = 1;
                    pageIndex = 1;
                    getServerList("");
                    floatingView.startAnimation(animation);
                    return;
                }
                Log.e("isAgainLoading", "isAgainLoading--->false");
                if (!isRefresh) {
                    floatingView.startAnimation(animation);
                }
                pullRefresh = true;
                page = 1;
                ++tagIndex;
                pageIndex = 1;
                getServerList("");
                isRefreshCounts = false;
            }
        });


//        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getActivity());
        layoutManager.setOrientation(StaggeredGridLayoutManager.Orientation.VERTICAL);
        layoutManager.setNumColumns(ChooseAdapter.COUNT_COLUMS);
        mRecyclerView.setOnScrollListener(onScrollListener);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ChooseAdapter(getActivity(), this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addItemDecoration(itemDecoration);
        paddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.floating_button_anim);
        callBack = new ChooseAdapter.CallBack() {
            @Override
            public void loadMore() {
                if (loading) {
                    return;
                }
                page = 2;
                ++pageIndex;
                getServerList("");
            }
        };
        adapter.callBack = callBack;

        mFloatBtn = (FloatingActionButton) view.findViewById(R.id.float_feedback_btn);

        return view;
    }

    RecyclerView.ItemDecoration itemDecoration = new MyItemDecoration();

    int paddingTop = 5;

    class MyItemDecoration extends RecyclerView.ItemDecoration {
        Paint mPaint;

        MyItemDecoration() {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

//            mPaint.setColor(0xfff5f5f5);
            mPaint.setColor(color);
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
                final int bottom = top + paddingTop;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, paddingTop, 0, 0);
        }
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
//        mFloatBtn.setMaxWidth(100);
//        mFloatBtn.setMaxHeight(100);
        mFloatBtn.attachToRecyclerView(mRecyclerView, null, onScrollListener);
        mFloatBtn.setOnClickListener(mFloatBtnClickListener);
        mFloatBtn.setVisibility(View.VISIBLE);
        loading = false;
        page = 1;
        getDbList();
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
            if (AvoidOnClickFastUtils.isFastDoubleClick())
                return;
            Log.i("","mFloatBtn.getWidth()--->"+mFloatBtn.getWidth()+":::mFloatBtn.getHeight()--->"+mFloatBtn.getHeight());
            // 显示反馈对话框
            showFeedback();
        }
    };

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception e) {
        }
    }


    //新闻列表
    public void getDbList() {
        newsListDbTask.findList(channelbean.getTid(), page, pageSize, new I_SetList<NewsBeanDB>() {
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
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("tid", "" + NewsChannelBean.TYPE_RECOMMEND);
        params.addBodyParameter("newTime", App.getInstance().newTime);
        params.addBodyParameter("oldTime", App.getInstance().oldTime);
        params.addBodyParameter("Page", "1");
        params.addBodyParameter("PageSize", "" + pageSize);
        UserBean user = SPUtil.getInstance().getUser();
        if (user != null && !TextUtils.isEmpty(user.getUid())) {
            params.addBodyParameter("uid", "" + user.getUid());
            params.addBodyParameter("tagIndex", "" + tagIndex);
            params.addBodyParameter("pageIndex", "" + pageIndex);
        }

        httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.CHANNEL_RECOMMEND
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                isRefresh = false;
                mSwipeRefreshWidget.setRefreshing(false);
                final JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
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
                        LogUtils.i(" getChooseNewsJson --> " + list.size());
                        new NewsListDbTask(getActivity()).saveList(list, new I_Result() {
                            @Override
                            public void setResult(Boolean flag) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                isRefresh = false;
                pullRefresh = false;
                mSwipeRefreshWidget.setRefreshing(false);
//                loadMainUI();
            }
        });
    }

    //获取新闻list
    public void getServerList(String nids) {
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("newTime", App.getInstance().newTime);
        params.addBodyParameter("oldTime", App.getInstance().oldTime);
        params.addBodyParameter("Page", "" + page);
        params.addBodyParameter("PageSize", "" + pageSize);
        UserBean user = SPUtil.getInstance().getUser();
        if (user != null && !TextUtils.isEmpty(user.getUid())) {
            params.addBodyParameter("uid", "" + user.getUid());
            params.addBodyParameter("tagIndex", "" + tagIndex);
            params.addBodyParameter("pageIndex", "" + pageIndex);
        }
        httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.CHANNEL_RECOMMEND_NEW
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                loading = false;
                isRefresh = false;
                if (!isAdded()) {
                    return;
                }
                try {
                    mSwipeRefreshWidget.setRefreshing(false);
                    final JSONObject obj = FjsonUtil
                            .parseObject(responseInfo.result);
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
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                loading = false;
                isRefresh = false;
                pullRefresh = false;
                if (!isAdded()) {
                    return;
                }
                TUtils.toast(getString(R.string.toast_cannot_connect_network));
                mSwipeRefreshWidget.setRefreshing(false);
            }
        });
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
                        mRecyclerView.scrollToPosition(0);
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

        TextView title = (TextView) v.findViewById(R.id.newsitem_title);
        if (null != title) {
            title.setTextColor(getResources().getColor(R.color.grey_font));
        }
//        NewsBean nb=(NewsBean)v.getTag();
//        adapter.setReadedId(nb.getNid());
        Log.e("isRefresh", "isRefresh--->" + isRefresh);
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
            return;
        }
        try {
            if (!isRefresh && v == floatingView) {
                mRecyclerView.scrollToPosition(0);
                mSwipeRefreshWidget.setRefreshing(true);
                pageIndex = 1;
                ++tagIndex;
                page = 1;
                getServerList("");
                v.setAnimation(animation);
                v.startAnimation(animation);
                return;
            }
            if (v.getTag() instanceof NewsBean) {
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

//    @Override
//    public void onClick(View v) {
//        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
//            return;
//        }
//        switch (v.getId()) {
//            case R.id.floating_button:
//                if (!isRefresh) {
//                    mRecyclerView.scrollToPosition(0);
//                    mSwipeRefreshWidget.setRefreshing(true);
//                    pageIndex = 1;
//                    ++tagIndex;
//                    page = 1;
//                    getServerList("");
//                    v.setAnimation(animation);
//                    v.startAnimation(animation);
//                    return;
//                }
//        }
//
//        TextView title = (TextView) v.findViewById(R.id.newsitem_title);
//        if (null != title) {
//            title.setTextColor(getResources().getColor(R.color.grey_font));
//        }
//        Log.e("isRefresh", "isRefresh--->" + isRefresh);
//        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
//            return;
//        }
//        try {
//            if (!isRefresh && v == floatingView) {
//                mRecyclerView.scrollToPosition(0);
//                mSwipeRefreshWidget.setRefreshing(true);
//                pageIndex = 1;
//                ++tagIndex;
//                page = 1;
//                getServerList("");
//                v.setAnimation(animation);
//                v.startAnimation(animation);
//                return;
//            }
//            if (v.getTag() instanceof NewsBean) {
//                NewsBean nb = (NewsBean) v.getTag();
//                Intent mIntent = new Intent();
//                mIntent.putExtra("newbean", nb);
//                mIntent.putExtra("from", "chooseItem");
//                adapter.setReadedId(nb.getNid());
//                //1新闻
//                mIntent.setClass(v.getContext(), NewsDetailActivity.class);
//                v.getContext().startActivity(mIntent);
//                AAnim.ActivityStartAnimation(getActivity());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    public void onEventMainThread(FontSizeEvent event) {
        adapter.setFontSize(event.getFontSize());
    }

}
