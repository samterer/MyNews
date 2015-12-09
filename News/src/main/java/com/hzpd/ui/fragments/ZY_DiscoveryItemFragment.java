package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.DiscoveryItemAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.CollectionJsonBean;
import com.hzpd.modle.DiscoveryItemBean;
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

    private String url = "http://www.nutnote.com/ltcms/api.php?s=/Tag/discovery";
    private PullToRefreshListView pushmsg_lv;
    private DiscoveryItemAdapter adapter;

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.leftMenu;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.zy_discovery_item_fragment, container, false);
            adapter = new DiscoveryItemAdapter(getActivity());
            pushmsg_lv = (PullToRefreshListView) view.findViewById(R.id.pushmsg_lv);
            pushmsg_lv.setAdapter(adapter);
            pushmsg_lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                    LogUtils.i("下拉刷新");
                    refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_pull_label));
                    getDiscoveryServer();
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                    //上拉加载
                    LogUtils.i("上拉加载");
                    refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_from_bottom_pull_label));
                    getDiscoveryServer();
                }
            });
            pushmsg_lv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pushmsg_lv.setRefreshing(true);
                }
            }, 600);


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
        httpUtils.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                pushmsg_lv.onRefreshComplete();
                pushmsg_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
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
                    adapter.appendData(mlist, true);
                    adapter.notifyDataSetChanged();
                    LogUtils.i("listsize-->" + mlist.size());
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                pushmsg_lv.onRefreshComplete();
                pushmsg_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
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