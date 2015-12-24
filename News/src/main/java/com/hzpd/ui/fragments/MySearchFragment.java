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
import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.ui.activity.NewsAlbumActivity;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.ZhuanTiActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
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

    private int lastVisibleItem;
    private LinearLayoutManager layoutManager;
    NewsItemListViewAdapter.CallBack callBack;
    boolean addLoading = false;

    private void init() {
        con = getArguments().getString(SEARCH_KEY);
        isRefresh = getArguments().getBoolean(is_Refresh);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NewsItemListViewAdapter(activity, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (addLoading && !adapter.showLoading) {
                    addLoading = false;
                    int count = adapter.getItemCount();
                    adapter.showLoading = true;
                    adapter.notifyItemInserted(count);
                }
            }
        });

        callBack = new NewsItemListViewAdapter.CallBack() {
            @Override
            public void loadMore() {
                Log.i("loadMore", "loadMore");
                getSearchData(con);
            }
        };
        adapter.callBack = callBack;

//        recyclerView.addItemDecoration(itemDecoration);
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        getSearchData(con);
    }


    public void getSearchData(String content) {

        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("content", content);
        params.addBodyParameter("Page", "" + page);
        params.addBodyParameter("PageSize", "" + pageSize);
        SPUtil.addParams(params);
        HttpHandler httpHandler = httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.SEARCH
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!isAdded()) {
                    return;
                }
                loadingView.setVisibility(View.GONE);
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
                if (null == obj) {
                    TUtils.toast(getString(R.string.toast_no_data_now));
                    return;
                }

                if (200 == obj.getIntValue("code")) {
                    page++;
                    addLoading = true;
                    List<NewsBean> l = FjsonUtil.parseArray(
                            obj.getString("data"), NewsBean.class);
                    if (null == l) {
                        TUtils.toast(getString(R.string.toast_no_data_now));
                        return;
                    }
                    LogUtils.i("l size-->" + l.size() + ":::" + l.get(0).toString());
//                    recyclerView.setAdapter(null);
//                    adapter.clear();
                    adapter.appendData(l, false, false);
                    if (page > 1 && adapter.showLoading) {
                        int count = adapter.getItemCount();
                        adapter.showLoading = false;
                        adapter.notifyItemRemoved(count);
                    }
                    if (l.size() < pageSize) {
                        LogUtils.i("PULL_FROM_START");
                    } else {
                        LogUtils.i("both");
                        adapter.showLoading = true;
                    }
                    adapter.notifyDataSetChanged();
                } else {
//                    TUtils.toast(obj.getString("msg"));
                    if (!isRefresh) {
                        page--;
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                if (!isAdded()) {
                    return;
                }
                TUtils.toast(getString(R.string.toast_server_no_response));
                loadingView.setVisibility(View.GONE);
                if (!isRefresh) {
                    page--;
                }
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
            mIntent.setClass(getActivity(), NewsDetailActivity.class);//直播界面
        } else if ("4".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), ZhuanTiActivity.class);
        } else if ("5".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsDetailActivity.class);
        } else if ("6".equals(nb.getRtype())) {
//					mIntent.setClass(getActivity(),VideoPlayerActivity.class);
            mIntent.setClass(getActivity(), NewsDetailActivity.class);
        } else if ("7".equals(nb.getRtype())) {
            mIntent.setClass(getActivity(), NewsDetailActivity.class);
        } else {
            return;
        }

        activity.startActivityForResult(mIntent, 0);
        AAnim.ActivityStartAnimation(getActivity());
    }

}