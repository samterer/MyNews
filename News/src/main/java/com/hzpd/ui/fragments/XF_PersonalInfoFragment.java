package com.hzpd.ui.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.XF_UserCommentsAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.XF_UserCommentsBean;
import com.hzpd.modle.XF_UserInfoBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;

import com.squareup.okhttp.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class XF_PersonalInfoFragment extends BaseFragment {

    private XF_UserCommentsAdapter adapter;
    private String uid;
    private XF_UserInfoBean userInfoBean;

    private int page = 1;
    private static final int pagesize = 10;
    private RecyclerView recylerlist;
    private int lastVisibleItem;
    private LinearLayoutManager vlinearLayoutManager;
    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xf_personalinfofm_layout, container, false);
        tag = OkHttpClientManager.getTag();
        recylerlist = (RecyclerView) view.findViewById(R.id.recylerlist);
        vlinearLayoutManager = new LinearLayoutManager(getActivity());
        recylerlist.setLayoutManager(vlinearLayoutManager);
        recylerlist.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    adapter.setShowLoading(true);

                    getCommentsFromServer();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = vlinearLayoutManager.findLastVisibleItemPosition();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    protected void initData() {
        Bundle bundle = getArguments();
        uid = bundle.getString("uid");
        if (TextUtils.isEmpty(uid)) {
            return;
        }
        getUserInfoFromServer();
    }

    private void getUserInfoFromServer() {
        Map<String, String> params = new HashMap<>();
        params.put("uid", uid);
        params.put("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.XF_USERINFO
                , new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                if (!isAdded()) {
                    return;
                }
                String json = response.toString();
                Log.i("getUserInfoFromServer", json.toString());
                JSONObject obj = FjsonUtil.parseObject(json);
                if (null == obj) {
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    userInfoBean = FjsonUtil.parseObject(obj.getString("data")
                            , XF_UserInfoBean.class);
                    Log.i("userInfoBean", "onSuccess  userInfoBean:" + userInfoBean);
                    adapter = new XF_UserCommentsAdapter(activity, userInfoBean);
                    recylerlist.setAdapter(adapter);
                    getCommentsFromServer();
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {

            }

        }, params);
    }

    private void getCommentsFromServer() {
        Map<String, String> params = new HashMap<>();
        params.put("uid", uid);
        params.put("siteid", InterfaceJsonfile.SITEID);
        params.put("page", page + "");
        params.put("pagesize", pagesize + "");
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.XF_MYCOMMENTS
                , new OkHttpClientManager.ResultCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        String json = response.toString();
                        JSONObject obj = FjsonUtil.parseObject(json);
                        if (null != obj) {
                            page++;
                            if (200 == obj.getIntValue("code")) {
                                List<XF_UserCommentsBean> list = FjsonUtil.parseArray(obj.getString("data")
                                        , XF_UserCommentsBean.class);
                                if (userInfoBean != null) {
                                    Log.i("userInfoBean", "initData  userInfoBean:" + userInfoBean.toString());
                                }
                                Log.i("XF_UserCommentsBean", "onSuccess   XF_UserCommentsBean" + list.toString());
                                adapter.appendData(list, false);
                            }
                        } else {
                            page--;
                        }
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        page--;
                    }
                }, params
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OkHttpClientManager.cancel(tag);
    }

}