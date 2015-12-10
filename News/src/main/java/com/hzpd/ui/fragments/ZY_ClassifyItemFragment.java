package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.hzpd.adapter.ClassifyItemAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.DiscoveryItemBean;
import com.hzpd.modle.TagBean;
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

public class ZY_ClassifyItemFragment extends BaseFragment {

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.leftMenu;
    }

    String classify_top_url = "http://www.nutnote.com/ltcms/api.php?s=/Tag/category";

    private RecyclerView mRecyclerView;

    private ClassifyItemAdapter adapter;

    private int Page = 1;
    private int pageSize = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.zy_classify_item_fragment, container, false);
            mRecyclerView = (RecyclerView) view.findViewById(R.id.id_recyclerview_horizontal);
            //设置布局管理器
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            adapter = new ClassifyItemAdapter(getActivity());
            //设置适配器
            mRecyclerView.setAdapter(adapter);
            getClassifyHorServer();

        } catch (Exception e) {

        }
        return view;
    }

    /**
     * 获取
     */
    private void getClassifyHorServer() {

        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("Page", Page + "");
        params.addBodyParameter("PageSize", pageSize + "");
        httpUtils.send(HttpRequest.HttpMethod.POST, classify_top_url, params, new RequestCallBack<String>() {
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