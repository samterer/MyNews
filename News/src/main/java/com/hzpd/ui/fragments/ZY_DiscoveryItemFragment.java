package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.DiscoveryItemAdapter;
import com.hzpd.adapter.DiscoveryItemNewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.CollectionJsonBean;
import com.hzpd.modle.DiscoveryItemBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;

import java.util.List;

public class ZY_DiscoveryItemFragment extends BaseFragment {

    private PullToRefreshListView pushmsg_lv;
    private DiscoveryItemAdapter adapter;

    private RecyclerView discovery_recyclerview;
    private DiscoveryItemNewAdapter newAdapter;

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.leftMenu;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.zy_discovery_item_fragment, container, false);
            discovery_recyclerview = (RecyclerView) view.findViewById(R.id.discovery_recyclerview);
            //设置布局管理器
            LinearLayoutManager vlinearLayoutManager = new LinearLayoutManager(getActivity());
            vlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            discovery_recyclerview.setLayoutManager(vlinearLayoutManager);
            newAdapter = new DiscoveryItemNewAdapter(getContext());
            discovery_recyclerview.setAdapter(newAdapter);
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
        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.discovery_url, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
                if (null == obj) {
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    List<DiscoveryItemBean> mlist = FjsonUtil.parseArray(obj.getString("data")
                            , DiscoveryItemBean.class);
                    Log.i("", "ZY_DiscoveryItemFragment" + mlist.toString());
                    if (null == mlist) {
                        return;
                    }
                    newAdapter.appendData(mlist, false);
                    LogUtils.i("listsize-->" + mlist.size());
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