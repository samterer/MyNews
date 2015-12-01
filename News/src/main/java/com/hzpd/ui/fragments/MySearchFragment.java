package com.hzpd.ui.fragments;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.ui.activity.HtmlActivity;
import com.hzpd.ui.activity.NewsAlbumActivity;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.ZhuanTiActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.List;

public class MySearchFragment extends BaseFragment implements View.OnClickListener {

    @ViewInject(R.id.search_listview_id)
    private PullToRefreshRecyclerView search_listview_id;
    private RecyclerView recyclerView;

    private NewsItemListViewAdapter adapter;
    @ViewInject(R.id.app_progress_bar)
    private View loadingView;

    private boolean isSearch = false;//是否已有搜索结果
    private boolean isRefresh = false;
    private int page = 1;
    private static final int pageSize = 15;

    public static final String SEARCH_KEY = "search_key";
    public static final String is_Refresh = "is_Refresh";
    String con;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.search_main_layout, container, false);
        ViewUtils.inject(this, mView);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
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

    private void init() {
        con = getArguments().getString(SEARCH_KEY);
        isRefresh = getArguments().getBoolean(is_Refresh);
        search_listview_id.setMode(Mode.DISABLED);
        recyclerView = search_listview_id.getRefreshableView();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NewsItemListViewAdapter(activity, this);
        recyclerView = search_listview_id.getRefreshableView();
        recyclerView.setAdapter(adapter);
//        recyclerView.addItemDecoration(itemDecoration);
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        search_listview_id.setOnRefreshListener(new OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<RecyclerView> refreshView) {
                if (null == con || "".equals(con)) {
                    TUtils.toast(getString(R.string.toast_input_content));
                    search_listview_id.setMode(Mode.DISABLED);
                    return;
                }
                isRefresh = true;
                page = 1;
                getSearchData(con);
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<RecyclerView> refreshView) {

                if (null == con || "".equals(con)) {
                    TUtils.toast(getString(R.string.toast_input_content));
                    return;
                }
                isRefresh = false;
                page++;
                getSearchData(con);
            }
        });
        getSearchData(con);
    }


    public void getSearchData(String content) {

        String station = SharePreferecesUtils.getParam(getActivity(), StationConfig.STATION, "def").toString();
        String siteid = null;
        String SEARCH_url = null;
        if (station.equals(StationConfig.DEF)) {
            siteid = InterfaceJsonfile.SITEID;
            SEARCH_url = InterfaceJsonfile.SEARCH;
        } else if (station.equals(StationConfig.YN)) {
            siteid = InterfaceJsonfile_YN.SITEID;
            SEARCH_url = InterfaceJsonfile_YN.SEARCH;
        } else if (station.equals(StationConfig.TW)) {
            siteid = InterfaceJsonfile_TW.SITEID;
            SEARCH_url = InterfaceJsonfile_TW.SEARCH;
        }
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", siteid);
        params.addBodyParameter("content", content);
        params.addBodyParameter("Page", "" + page);
        params.addBodyParameter("PageSize", "" + pageSize);

        HttpHandler httpHandler = httpUtils.send(HttpMethod.POST
                , SEARCH_url
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!isAdded()) {
                    return;
                }
                search_listview_id.onRefreshComplete();
                loadingView.setVisibility(View.GONE);
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
                if (null == obj) {
                    TUtils.toast(getString(R.string.toast_no_data_now));
                    return;
                }

                if (200 == obj.getIntValue("code")) {

                    List<NewsBean> l = FjsonUtil.parseArray(
                            obj.getString("data"), NewsBean.class);
                    if (null == l) {
                        TUtils.toast(getString(R.string.toast_no_data_now));
                        return;
                    }
                    LogUtils.i("l size-->" + l.size() + ":::" + l.get(0).toString());
//                    recyclerView.setAdapter(null);
//                    adapter.clear();
                    adapter.appendData(l, isRefresh, false);

                    if (l.size() < pageSize) {
                        LogUtils.i("PULL_FROM_START");
                        search_listview_id.setMode(Mode.PULL_FROM_START);
                    } else {
                        LogUtils.i("both");
                        search_listview_id.setMode(Mode.BOTH);
                    }
                    adapter.notifyDataSetChanged();
                } else {
//                    TUtils.toast(obj.getString("msg"));
                    if (!isRefresh) {
                        page--;
                    }
                    search_listview_id.setMode(Mode.PULL_FROM_START);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                if (!isAdded()) {
                    return;
                }
                TUtils.toast(getString(R.string.toast_server_no_response));
                search_listview_id.onRefreshComplete();
                loadingView.setVisibility(View.GONE);
                if (!isRefresh) {
                    page--;
                }
                search_listview_id.setMode(Mode.PULL_FROM_START);
            }
        });
        handlerList.add(httpHandler);
    }


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
        } else {
            return;
        }

        activity.startActivityForResult(mIntent, 0);
        AAnim.ActivityStartAnimation(getActivity());
    }

}