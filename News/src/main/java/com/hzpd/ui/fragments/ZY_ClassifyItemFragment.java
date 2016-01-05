package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.ClassifyItemAdapter;
import com.hzpd.adapter.ClassifyItemListAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.TagBean;
import com.hzpd.modle.event.ClassifItemEvent;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.squareup.okhttp.Request;

import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class ZY_ClassifyItemFragment extends BaseFragment implements View.OnClickListener {

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.leftMenu;
    }

    private RecyclerView hRecyclerView;
    private ClassifyItemAdapter adapter;

    private RecyclerView vRecyclerView;
    private ClassifyItemListAdapter sampleAdapter;

    private int Page = 1;
    private int pageSize = 10;
    private int lastVisibleItem;
    private FrameLayout progress_container;
    private LinearLayoutManager vlinearLayoutManager;
    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
        }
        View view = null;
        try {
            view = inflater.inflate(R.layout.zy_classify_item_fragment, container, false);
            tag= OkHttpClientManager.getTag();
            progress_container = (FrameLayout) view.findViewById(R.id.progress_container);
            hRecyclerView = (RecyclerView) view.findViewById(R.id.id_recyclerview_horizontal);
            //设置布局管理器
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            hRecyclerView.setLayoutManager(linearLayoutManager);
            adapter = new ClassifyItemAdapter(getActivity(), this);
            //设置适配器
            hRecyclerView.setAdapter(adapter);
            hRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (changing) {
                            changing = false;
                            first = 0;
                            return;
                        }
                        try {
                            int position = 0;
                            do {
                                View view = recyclerView.getChildAt(position);
                                if (view == null) {
                                    return;
                                }
                                Object tag = view.getTag();
                                if (tag != null) {
                                    ClassifyItemAdapter.ItemViewHolder holder = (ClassifyItemAdapter.ItemViewHolder) tag;
                                    if (first == 0) {
                                        first = holder.postion;
                                    }
                                    DisplayMetrics displayMetrics = App.getInstance().getResources().getDisplayMetrics();
                                    int target = displayMetrics.widthPixels / 2;
                                    if (view.getLeft() < target && view.getRight() > target) {
                                        holder.classify_item.performClick();
                                    }
                                }
                                ++position;
                            } while (position < adapter.getItemCount());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });

            vRecyclerView = (RecyclerView) view.findViewById(R.id.id_recyclerview_vertical);
            //设置布局管理器
            vlinearLayoutManager = new LinearLayoutManager(getActivity());
            vlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            vRecyclerView.setLayoutManager(vlinearLayoutManager);
            sampleAdapter = new ClassifyItemListAdapter(getActivity(), this);
            //设置适配器
            vRecyclerView.setAdapter(sampleAdapter);

            vRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == sampleAdapter.getItemCount()) {
                        sampleAdapter.setShowLoading(true);
                        vPage++;
                        isClearOld = false;
                        getClassifyVerServer(id);
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    lastVisibleItem = vlinearLayoutManager.findLastVisibleItemPosition();
                }
            });

            String json = (String) SharePreferecesUtils.getParam(getActivity(), KEY_CATEGERY, "{}");
            parseJson(json);
            getClassifyHorServer();

        } catch (Exception e) {

        }
        return view;
    }

    final static String KEY_CATEGERY = "tag_category";

    /**
     * 获取水平数据
     */
    private void getClassifyHorServer() {

        final Map<String,String> params = RequestParamsUtils.getMaps();
        params.put("Page", String.valueOf(Page));
        params.put("PageSize", String.valueOf(pageSize));
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.classify_top_url, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                parseJson(response.toString());
                if (isAdded()) {
                    SharePreferecesUtils.setParam(getActivity(), KEY_CATEGERY, response.toString());
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {

            }

        }, params);
    }

    public void parseJson(String json) {
        try {
            JSONObject obj = FjsonUtil.parseObject(json);
            if (null == obj) {
                return;
            }
            if (200 == obj.getIntValue("code")) {
                List<TagBean> mlist = FjsonUtil.parseArray(obj.getString("data")
                        , TagBean.class);
                if (null == mlist) {
                    return;
                }
                adapter.appendData(mlist);
                id = mlist.get(0).getId();
                getClassifyVerServer(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseJsonTag(String json) {
        try {
            if (!TextUtils.isEmpty(json)) {
                JSONObject obj = FjsonUtil.parseObject(json);
                if (null == obj) {
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    List<TagBean> mlist = FjsonUtil.parseArray(obj.getString("data")
                            , TagBean.class);
                    if (null == mlist) {
                        return;
                    }
                    progress_container.setVisibility(View.GONE);
                    sampleAdapter.setShowLoading(false);
                    sampleAdapter.appendData(mlist, isClearOld);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    categoryId , int , 必选 ，  tag大类ID
//    page , int ,可选， 页码，默认值：1
//    pageSize , int ，可选， 每页数量，默认值：10
    private int vPage = 1;
    private int tpageSize = 10;
    private boolean isClearOld;

    private void getClassifyVerServer(final String id) {
        if (vPage == 1) {
            String json = (String) SharePreferecesUtils.getParam(getActivity(), KEY_CATEGERY + id, "");
            parseJsonTag(json);
            releaseHandler();
        }
        Map<String,String> params = RequestParamsUtils.getMaps();
        params.put("categoryId", id);
        params.put("Page", String.valueOf(vPage));
        params.put("PageSize", String.valueOf(tpageSize));
        SPUtil.addParams(params);
       OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.classify_url, new OkHttpClientManager.ResultCallback() {
           @Override
           public void onSuccess(Object response) {
               progress_container.setVisibility(View.GONE);
               sampleAdapter.setShowLoading(false);
               parseJsonTag(response.toString());
               if (vPage == 1) {
                   if (isAdded()) {
                       SharePreferecesUtils.setParam(getActivity(), KEY_CATEGERY + id, response.toString());
                   }
               }
           }

           @Override
           public void onFailure(Request request, Exception e) {
               sampleAdapter.setShowLoading(false);
           }

       }, params);
        handlerList.add(handler);
    }

    HttpHandler handler;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private String id;

    public void onEventMainThread(ClassifItemEvent event) {
        id = event.getId();
        isClearOld = true;
        vPage = 1;
        progress_container.setVisibility(View.VISIBLE);
        releaseHandler();
        getClassifyVerServer(id);
    }

    public void releaseHandler() {
        if (handler != null) {
            if (handler.getState() == HttpHandler.State.LOADING || handler.getState() == HttpHandler.State.STARTED) {
                handler.setRequestCallBack(null);
                handler.cancel();
            }
            handlerList.remove(handler);
            handler = null;
        }
    }

    @Override
    public void onDestroy() {
        try {
            EventBus.getDefault().unregister(this);
            super.onDestroy();
        } catch (Exception e) {
        }
    }

    boolean changing = false;
    int first = 0;

    @Override
    public void onClick(View v) {
        try {
            if (v.getTag() != null & v.getTag() instanceof ClassifyItemAdapter.ItemViewHolder) {
                ClassifyItemAdapter.ItemViewHolder holder = (ClassifyItemAdapter.ItemViewHolder) v.getTag();
                if (v.isSelected()) {
                    moveHead(holder);
                    return;
                }
                if (adapter.last != null) {
                    adapter.last.classify_item.setSelected(false);
                    adapter.last.mImg.setLayoutParams(SPUtil.NORMAL);
                }
                v.setSelected(true);
                holder.mImg.setLayoutParams(SPUtil.LARGE);
                moveHead(holder);
                EventBus.getDefault().post(new ClassifItemEvent("" + holder.tagBean.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getTarget(ClassifyItemAdapter.ItemViewHolder holder) {
        changing = true;
        DisplayMetrics displayMetrics = App.getInstance().getResources().getDisplayMetrics();
        int itemWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 95, displayMetrics);
        int target = (displayMetrics.widthPixels - itemWidth) / 2;

        if (adapter.last.postion >= first && holder.postion > adapter.last.postion) {
            target += (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, displayMetrics);
        }
        return target;
    }

    private void moveHead(ClassifyItemAdapter.ItemViewHolder holder) {
        int target = getTarget(holder);
        int left = holder.itemView.getLeft();
        adapter.last = holder;
        hRecyclerView.smoothScrollBy(left - target, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OkHttpClientManager.cancel(tag);
    }
}