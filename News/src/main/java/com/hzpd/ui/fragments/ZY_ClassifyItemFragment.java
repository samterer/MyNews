package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;

import java.util.List;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
        }
        View view = null;
        try {
            view = inflater.inflate(R.layout.zy_classify_item_fragment, container, false);
            progress_container = (FrameLayout) view.findViewById(R.id.progress_container);
            hRecyclerView = (RecyclerView) view.findViewById(R.id.id_recyclerview_horizontal);
            //设置布局管理器
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            hRecyclerView.setLayoutManager(linearLayoutManager);
            adapter = new ClassifyItemAdapter(getActivity(), this);
            //设置适配器
            hRecyclerView.setAdapter(adapter);
            getClassifyHorServer();

            vRecyclerView = (RecyclerView) view.findViewById(R.id.id_recyclerview_vertical);
            //设置布局管理器
            vlinearLayoutManager = new LinearLayoutManager(getActivity());
            vlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            vRecyclerView.setLayoutManager(vlinearLayoutManager);
            sampleAdapter = new ClassifyItemListAdapter(getActivity(), this);
            //设置适配器
            vRecyclerView.setAdapter(sampleAdapter);

            vRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == sampleAdapter.getItemCount()) {
                        sampleAdapter.setShowLoading(true);
                        vPage++;
                        isClearOld = false;
                        if (!TextUtils.isEmpty(id)) {
                            Log.i("vRecyclerView", "vRecyclerView" + id);
                        }
                        getClassifyVerServer(id);
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    lastVisibleItem = vlinearLayoutManager.findLastVisibleItemPosition();
                }
            });


        } catch (Exception e) {

        }
        return view;
    }

    /**
     * 获取水平数据
     */
    private void getClassifyHorServer() {

        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("Page", Page + "");
        params.addBodyParameter("PageSize", pageSize + "");
        SPUtil.addParams(params);
        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.classify_top_url, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
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
                    LogUtils.i("listsize-->" + mlist.size());
//                    isClearOld = true;
                    id = mlist.get(0).getId();
                    getClassifyVerServer(id);
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
            }
        });
    }

    //    categoryId , int , 必选 ，  tag大类ID
//    page , int ,可选， 页码，默认值：1
//    pageSize , int ，可选， 每页数量，默认值：10
    private int vPage = 1;
    private int tpageSize = 10;
    private boolean isClearOld;

    private void getClassifyVerServer(String id) {
        Log.i("getClassifyVerServer", "getClassifyVerServer id:" + id + "\nvPage:" + vPage + "\ntpageSize:" + tpageSize);
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("categoryId", id + "");
        params.addBodyParameter("Page", vPage + "");
        params.addBodyParameter("PageSize", tpageSize + "");
        SPUtil.addParams(params);
        handler = httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.classify_url, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                progress_container.setVisibility(View.GONE);
                sampleAdapter.setShowLoading(false);
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
                if (null == obj) {
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    List<TagBean> mlist = FjsonUtil.parseArray(obj.getString("data")
                            , TagBean.class);
                    if (null == mlist) {
                        return;
                    }
                    Log.i("getClassifyVerServer", "getClassifyVerServer vPage:::" + vPage);
                    Log.i("getClassifyVerServer", "getClassifyVerServer" + mlist.toString());
                    sampleAdapter.appendData(mlist, isClearOld);
                    Log.i("getClassifyVerServer", "getClassifyVerServer listsize:::" + mlist.size());
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                sampleAdapter.setShowLoading(false);
            }
        });
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
        Log.i("onEventMainThread", "onEventMainThread  id-->" + id);
        isClearOld = true;
        vPage = 1;
        progress_container.setVisibility(View.VISIBLE);
        if (handler != null) {
            if (handler.getState() == HttpHandler.State.LOADING || handler.getState() == HttpHandler.State.STARTED) {
                handler.setRequestCallBack(null);
                handler.cancel();
            }
            handlerList.remove(handler);
            handler = null;
        }
        getClassifyVerServer(id);
    }

    @Override
    public void onDestroy() {
        try {
            EventBus.getDefault().unregister(this);
            super.onDestroy();
        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getTag() != null & v.getTag() instanceof ClassifyItemAdapter.ItemViewHolder) {
                if (v.isSelected()) {
                    return;
                }
                ClassifyItemAdapter.ItemViewHolder holder = (ClassifyItemAdapter.ItemViewHolder) v.getTag();
                if (adapter.last != null) {
                    adapter.last.classify_item.setSelected(false);
                    adapter.last.mImg.setLayoutParams(SPUtil.NORMAL);
                }
                v.setSelected(true);
                adapter.last = holder;
                holder.mImg.setLayoutParams(SPUtil.LARGE);
                EventBus.getDefault().post(new ClassifItemEvent("" + holder.tagBean.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}