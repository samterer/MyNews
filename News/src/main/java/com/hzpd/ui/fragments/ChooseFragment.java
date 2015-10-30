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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.ChooseAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.db.NewsBeanDB;
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
import com.hzpd.utils.db.NewsListDbTask;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
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

    private NewsChannelBean channelbean;//本频道
    private NewsListDbTask newsListDbTask; //新闻列表数据库
    private boolean isRefresh = false;//是否首次加载
    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ChooseAdapter adapter;
    private int page = 1;
    private static final int pageSize = 15;//
    String nids = "";
    private View floatingView;
    private Animation animation;
    private ImageView background_empty;

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

    int padding = 20;
    public static int scroll_status = RecyclerView.SCROLL_STATE_IDLE;

    public ChooseFragment(NewsChannelBean channelbean) {
        this.channelbean = channelbean;
    }

    public ChooseFragment() {
        this.channelbean = new NewsChannelBean();
        channelbean.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
        channelbean.setType(NewsChannelBean.TYPE_RECOMMEND);
        channelbean.setCnname(getString(R.string.recommend));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_fragment, container, false);
        background_empty = (ImageView) view.findViewById(R.id.background_empty);
        floatingView = view.findViewById(R.id.floating_button);
        floatingView.setOnClickListener(this);
        ViewHelper.setAlpha(floatingView, 0.7f);
        mSwipeRefreshWidget = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recylerlist);

        try {
            padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            mRecyclerView.setOnScrollListener(new RecyclerViewPauseOnScrollListener(ImageLoader.getInstance(), true, true));
            mRecyclerView.addItemDecoration(new SpacingItemDecoration(0, padding));
            mRecyclerView.setClipToPadding(false);
        } catch (Exception e) {
            e.printStackTrace();
        }


        mSwipeRefreshWidget.setColorScheme(R.color.google_blue, R.color.google_red, R.color.google_yellow, R.color.google_green);
        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefresh) {
                    floatingView.startAnimation(animation);
                }
                getServerList(nids);
            }
        });


        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getActivity());
        layoutManager.setOrientation(StaggeredGridLayoutManager.Orientation.VERTICAL);
        layoutManager.setNumColumns(ChooseAdapter.COUNT_COLUMS);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //解决RecyclerView和SwipeRefreshLayout共用存在的bug
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshWidget.setEnabled(topRowVerticalPosition >= 0);

            }

        });
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ChooseAdapter(getActivity(), this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addItemDecoration(itemDecoration);
        paddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.floating_button_anim);
        return view;
    }

    RecyclerView.ItemDecoration itemDecoration = new MyItemDecoration();

    int paddingTop = 5;

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
        page = 1;
        getDbList();
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception e) {
        }
    }


    //新闻列表
    public void getDbList() {
        LogUtils.i("page-->" + page + "  pageSize-->" + pageSize);
        newsListDbTask.findList(channelbean.getTid(), page, pageSize, new I_SetList<NewsBeanDB>() {
            @Override
            public void setList(List<NewsBeanDB> list) {
                if (null != list && list.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    List<NewsBean> nbList = new ArrayList<NewsBean>();
                    for (NewsBeanDB nbdb : list) {
                        sb.append(nbdb.getNid() + ",");
                        nbList.add(nbdb.getNewsBean());
                    }
                    adapter.setData(nbList);
                    background_empty.setVisibility(View.GONE);
                    if (sb.length() > 1) {
                        nids = sb.substring(0, sb.length() - 1);
                    }
                }
            }

        });
        getServerList(nids);
    }

    //获取新闻list
    public void getServerList(String nids) {
        LogUtils.i("nids-->" + nids);
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("tid", channelbean.getTid());
        params.addBodyParameter("nids", nids);
        params.addBodyParameter("Page", "" + page);
        params.addBodyParameter("PageSize", "" + pageSize);
        httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.CHANNEL_RECOMMEND
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if(!isAdded()){
                    return;
                }
                mSwipeRefreshWidget.setRefreshing(false);
                page++;
                isRefresh = false;
                final JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj) {
                    setData(obj);//处理数据
                } else {
                    page--;
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                if(!isAdded()){
                    return;
                }
                Log.e("onFailure", msg + error);
                mSwipeRefreshWidget.setRefreshing(false);
                isRefresh = false;
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
                    mRecyclerView.stopScroll();
                    if (adapter.getItemCount() > 0) {
                        mRecyclerView.scrollToPosition(0);
                    }
                    if (page > 2) {
                        adapter.addNews(list);
                    } else {
                        adapter.setData(list);
                    }
                    background_empty.setVisibility(View.GONE);
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
        }

    }


    @Override
    public void onClick(View v) {
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
            return;
        }
        TextView title = (TextView) v.findViewById(R.id.newsitem_title);
        if (null != title) {
            title.setTextColor(getResources().getColor(R.color.grey_font));
        }
//        NewsBean nb=(NewsBean)v.getTag();
//        adapter.setReadedId(nb.getNid());
        try {
            if (!isRefresh && v == floatingView) {
                mRecyclerView.scrollToPosition(0);
                mSwipeRefreshWidget.setRefreshing(true);
                getServerList(nids);
                //TODO
                isRefresh = true;
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
}
