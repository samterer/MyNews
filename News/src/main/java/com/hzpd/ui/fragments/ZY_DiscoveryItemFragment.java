package com.hzpd.ui.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.hzpd.modle.event.TagEvent;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.news.update.Utils;
import com.squareup.okhttp.Request;

import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class ZY_DiscoveryItemFragment extends BaseFragment implements View.OnClickListener {


    private RecyclerView discovery_recyclerview;
    private DiscoveryItemNewAdapter newAdapter;

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.leftMenu;
    }

    private int lastVisibleItem;
    private LinearLayoutManager vlinearLayoutManager;
    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.zy_discovery_item_fragment, container, false);
            tag = OkHttpClientManager.getTag();
            discovery_recyclerview = (RecyclerView) view.findViewById(R.id.discovery_recyclerview);
            //设置布局管理器
            vlinearLayoutManager = new LinearLayoutManager(getActivity());
            vlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            discovery_recyclerview.setLayoutManager(vlinearLayoutManager);
            newAdapter = new DiscoveryItemNewAdapter(getContext(), this);
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
        Log.i("getDiscoveryServer", "getDiscoveryServer  000");
        Map<String, String> params = RequestParamsUtils.getMaps();
        params.put("Page", Page + "");
        params.put("PageSize", pageSize + "");
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.discovery_url, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                try {
                    JSONObject obj = FjsonUtil.parseObject(response.toString());
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
                    } else {
                        Page--;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                Log.i("getDiscoveryServer", "getDiscoveryServer  onFailure");

                Page--;
            }
        }, params);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    @Override
    public void onDestroyView() {
        OkHttpClientManager.cancel(tag);
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getTag() != null && v.getTag() instanceof DiscoveryItemNewAdapter.ItemViewHolder) {
                Log.i("DiscoveryItemNewAdapter.ItemViewHolder", "DiscoveryItemNewAdapter.ItemViewHolder");
                DiscoveryItemNewAdapter.ItemViewHolder viewHolder = (DiscoveryItemNewAdapter.ItemViewHolder) v.getTag();
                {
                    Log.i("DiscoveryItemNewAdapter", "DiscoveryItemNewAdapter  viewHolder.tv_subscribe  onClick");
                    viewHolder.tv_subscribe.setBackgroundResource(R.drawable.corners_bg);
                    viewHolder.tv_subscribe.setTextColor(getActivity().getResources().getColor(R.color.details_tv_check_color));
                    Drawable nav_up = getActivity().getResources().getDrawable(R.drawable.discovery_image_select);
                    nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                    viewHolder.tv_subscribe.setCompoundDrawables(nav_up, null, null, null);
                    viewHolder.tv_subscribe.setText(getActivity().getString(R.string.discovery_followed));
                    EventBus.getDefault().post(new TagEvent(viewHolder.tagBean));
                    if (Utils.isNetworkConnected(getActivity())) {
                        Map<String, String> params = RequestParamsUtils.getMapWithU();
                        if (spu.getUser() != null) {
                            params.put("uid", spu.getUser().getUid() + "");
                        }
                        params.put("tagId", viewHolder.tagBean.getId() + "");
                        SPUtil.addParams(params);

                        OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.tag_click_url, new OkHttpClientManager.ResultCallback() {
                            @Override
                            public void onSuccess(Object response) {
                                Log.i("onSuccess", "onSuccess");
                            }

                            @Override
                            public void onFailure(Request request, Exception e) {
                                Log.i("onFailure","onFailure");
                            }
                        }, params);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}