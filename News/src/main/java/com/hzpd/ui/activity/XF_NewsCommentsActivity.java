package com.hzpd.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.MyCommentListAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.MyCommentListBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.squareup.okhttp.Request;

import java.util.List;
import java.util.Map;

public class XF_NewsCommentsActivity extends MBaseActivity implements View.OnClickListener {

    private View stitle_ll_back;
    private TextView stitle_tv_content;
    private View app_progress_bar;
    private ImageView data_empty;

    private int Page = 1;
    private static final int PageSize = 10;
    private MyCommentListAdapter myCommentListAdapter;
    private String nid;
    private RecyclerView recyclerview;

    private int lastVisibleItem;
    private LinearLayoutManager vlinearLayoutManager;
    private Object tag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newscomments_layout);
        super.changeStatusBar();
        nid = getIntent().getStringExtra("News_nid");
        Log.i("test", "nid--->" + nid);
        tag = OkHttpClientManager.getTag();
        initViews();
        getInfoFromServer();
    }

    private void initViews() {
        stitle_ll_back = findViewById(R.id.stitle_ll_back);
        stitle_ll_back.setOnClickListener(this);
        stitle_tv_content = (TextView) findViewById(R.id.stitle_tv_content);
        stitle_tv_content.setText(getString(R.string.comment));
        app_progress_bar = findViewById(R.id.app_progress_bar);
        data_empty = (ImageView) findViewById(R.id.data_empty);
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        vlinearLayoutManager = new LinearLayoutManager(this);
        vlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerview.setLayoutManager(vlinearLayoutManager);
        myCommentListAdapter = new MyCommentListAdapter(this);
        recyclerview.setAdapter(myCommentListAdapter);
        recyclerview.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == myCommentListAdapter.getItemCount()) {
                    myCommentListAdapter.setShowLoading(true);
                    getInfoFromServer();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = vlinearLayoutManager.findLastVisibleItemPosition();
            }
        });

    }


    private void getInfoFromServer() {
        Map<String, String> params = RequestParamsUtils.getMaps();
        params.put("Page", Page + "");
        params.put("PageSize", PageSize + "");
        params.put("nid", nid);
        params.put("type", "News");
        params.put("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.CHECKCOMMENT, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                Log.i("test", "onSuccess");
                app_progress_bar.setVisibility(View.GONE);
                JSONObject obj = FjsonUtil.parseObject(response.toString());
                myCommentListAdapter.setShowLoading(false);
                if (null == obj) {
                    data_empty.setVisibility(View.VISIBLE);
                    return;
                }

                if (200 == obj.getIntValue("code")) {
                    List<MyCommentListBean> mlist = FjsonUtil.parseArray(obj.getString("data"), MyCommentListBean.class);
                    Log.e("test", "mlist" + mlist.toString());
                    if (null == mlist) {
                        return;
                    }
                    Page++;
                    Log.i("MyCommentListBean", "MyCommentListBean" + mlist.toString());
                    myCommentListAdapter.appendData(mlist);
//                    myCommentListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                Log.i("test", "onFailure");
                data_empty.setVisibility(View.VISIBLE);
                app_progress_bar.setVisibility(View.GONE);
                myCommentListAdapter.setShowLoading(false);
            }

        }, params);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stitle_ll_back: {
                finish();
            }
            break;
        }
    }

    @Override
    protected void onDestroy() {
        OkHttpClientManager.cancel(tag);
        super.onDestroy();
    }
}
