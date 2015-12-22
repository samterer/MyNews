package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.DiscoveryItemNewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.DiscoveryItemBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.List;

public class ZY_DiscoveryItemFragment extends BaseFragment {


    private RecyclerView discovery_recyclerview;
    private DiscoveryItemNewAdapter newAdapter;

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.leftMenu;
    }

    private int lastVisibleItem;
    private LinearLayoutManager vlinearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.zy_discovery_item_fragment, container, false);
            discovery_recyclerview = (RecyclerView) view.findViewById(R.id.discovery_recyclerview);
            //设置布局管理器
            vlinearLayoutManager = new LinearLayoutManager(getActivity());
            vlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            discovery_recyclerview.setLayoutManager(vlinearLayoutManager);
            newAdapter = new DiscoveryItemNewAdapter(getContext());
            discovery_recyclerview.setAdapter(newAdapter);

            discovery_recyclerview.setOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == newAdapter.getItemCount()) {
                        newAdapter.setShowLoading(true);

                        getDiscoveryServer();
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    lastVisibleItem = vlinearLayoutManager.findLastVisibleItemPosition();
                }
            });

            getDiscoveryServer();
        } catch (Exception e) {

        }
        return view;
    }

    private int Page = 1;
    private int pageSize = 10;

    private void getDiscoveryServer() {

        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("Page", Page + "");
        params.addBodyParameter("PageSize", pageSize + "");
        SPUtil.addParams(params);
        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.discovery_url, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
                if (null == obj) {
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    Page++;
                    List<DiscoveryItemBean> mlist = FjsonUtil.parseArray(obj.getString("data")
                            , DiscoveryItemBean.class);
                    if (null == mlist) {
                        return;
                    }

                    for (int i = 0; i < mlist.size(); i++) {

                        if (mlist.get(i).getNews() == null || mlist.get(i).getNews().size() < 1) {
                            DiscoveryItemBean bean = mlist.get(i);
                            mlist.remove(bean);
                            i--;
                        }
                    }
                    newAdapter.appendData(mlist, false);
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


}