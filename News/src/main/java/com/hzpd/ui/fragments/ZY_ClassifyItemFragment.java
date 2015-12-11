package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.ClassifyItemAdapter;
import com.hzpd.adapter.ClassifyItemListAdapter;
import com.hzpd.adapter.SampleAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.TagBean;
import com.hzpd.modle.event.ClassifItemEvent;
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

import de.greenrobot.event.EventBus;

public class ZY_ClassifyItemFragment extends BaseFragment {

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.leftMenu;
    }

    String classify_top_url = "http://www.nutnote.com/ltcms/api.php?s=/Tag/category";
    String classify_url = "http://www.nutnote.com/ltcms/api.php?s=/Tag/tagList";


    private RecyclerView hRecyclerView;
    private ClassifyItemAdapter adapter;

    private RecyclerView vRecyclerView;
    private ClassifyItemListAdapter sampleAdapter;

    private int Page = 1;
    private int pageSize = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
        }
        View view = null;
        try {
            view = inflater.inflate(R.layout.zy_classify_item_fragment, container, false);
            hRecyclerView = (RecyclerView) view.findViewById(R.id.id_recyclerview_horizontal);
            //设置布局管理器
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            hRecyclerView.setLayoutManager(linearLayoutManager);
            adapter = new ClassifyItemAdapter(getActivity());
            //设置适配器
            hRecyclerView.setAdapter(adapter);
            getClassifyHorServer();

            vRecyclerView = (RecyclerView) view.findViewById(R.id.id_recyclerview_vertical);
            //设置布局管理器
            LinearLayoutManager vlinearLayoutManager = new LinearLayoutManager(getActivity());
            vlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            vRecyclerView.setLayoutManager(vlinearLayoutManager);
            sampleAdapter = new ClassifyItemListAdapter(getActivity());
            //设置适配器
            vRecyclerView.setAdapter(sampleAdapter);


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

                    getClassifyVerServer(mlist.get(0).getId());
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
    private int tPage=1;
    private int tpageSize=10;
    private boolean isClearOld;

    private void getClassifyVerServer(String id) {
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("categoryId", id + "");
        params.addBodyParameter("Page", tPage + "");
        params.addBodyParameter("PageSize", tpageSize + "");

        httpUtils.send(HttpRequest.HttpMethod.POST, classify_url, params, new RequestCallBack<String>() {
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
                    Log.i("getClassifyVerServer", "getClassifyVerServer" + mlist.toString());

                    sampleAdapter.appendData(mlist,isClearOld);
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

    public void onEventMainThread(ClassifItemEvent event) {
        String id = event.getId();
        Log.i("onEventMainThread", "onEventMainThread  id-->" + id);
        isClearOld=true;
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

}