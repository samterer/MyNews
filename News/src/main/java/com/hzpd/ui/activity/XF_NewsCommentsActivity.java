package com.hzpd.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.MyCommentListAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.MyCommentListBean;
import com.hzpd.modle.ReplayBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SystemBarTintManager;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.List;

public class XF_NewsCommentsActivity extends MBaseActivity {

    @ViewInject(R.id.stitle_tv_content)
    private TextView stitle_tv_content;
    @ViewInject(R.id.pushmsg_lv)
    private PullToRefreshListView pushmsg_lv;
    @ViewInject(R.id.pushmsg_tv_empty)
    private ImageView pushmsg_tv_empty;
    private int Page = 1;
    private static final int PageSize = 15;
    private boolean mFlagRefresh;
    private MyCommentListAdapter myCommentListAdapter;

    private String nid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newscomments_layout);
        ViewUtils.inject(this);
//        nid = getIntent().getStringExtra("News_nid");
//        LogUtils.e("nid--->" + nid);
//
//        stitle_tv_content.setText(R.string.comment_item_new);
//        pushmsg_lv.setEmptyView(pushmsg_tv_empty);
//        pushmsg_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
//
//        pushmsg_lv.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                pushmsg_lv.setRefreshing(true);
//            }
//        }, 500);
//
//        myCommentListAdapter = new MyCommentListAdapter();
//        pushmsg_lv.setAdapter(myCommentListAdapter);
//        pushmsg_lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
//            @Override
//            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
//                LogUtils.i("下拉刷新");
//                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_pull_label));
//                Page = 1;
//                mFlagRefresh = true;
//                getInfoFromServer();
//            }
//
//            @Override
//            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
//                //上拉加载
//                LogUtils.i("上拉加载");
//                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_from_bottom_pull_label));
//                Page++;
//                mFlagRefresh = false;
//                getInfoFromServer();
//            }
//        });
//
//        pushmsg_lv.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                pushmsg_lv.setRefreshing(false);
//            }
//        }, 600);
//        pushmsg_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (view.getTag() instanceof MyCommentListAdapter.ViewHolder) {
//                    MyCommentListAdapter.ViewHolder viewHolder = (MyCommentListAdapter.ViewHolder) view.getTag();
//                    Intent mIntent = new Intent();
//                    mIntent.putExtra("uid", viewHolder.userId); //TODO
//                    mIntent.setClass(activity, XF_PInfoActivity.class);
//                    startActivity(mIntent);
//                }
//            }
//        });
    }


    private void getInfoFromServer() {
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("PageSize", PageSize + "");
        params.addBodyParameter("Page", Page + "");
        params.addBodyParameter("nid", nid);
        params.addBodyParameter("type", "News");
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.CHECKCOMMENT
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                pushmsg_lv.onRefreshComplete();
                pushmsg_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                LogUtils.i("data-->" + responseInfo.result);
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);

                if (null == obj) {
                    return;
                }

                if (200 == obj.getIntValue("code")) {
                    pushmsg_lv.setVisibility(View.VISIBLE);
                    List<MyCommentListBean> mlist = FjsonUtil.parseArray(obj.getString("data"), MyCommentListBean.class);
                    Log.e("test", "mlist" + mlist.toString());
                    if (null == mlist) {
                        return;
                    }
                    if (mlist.size() >= PageSize) {
                        pushmsg_lv.setMode(PullToRefreshBase.Mode.BOTH);
                    }
                    myCommentListAdapter.appendData(mlist, mFlagRefresh);
                    myCommentListAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                pushmsg_lv.onRefreshComplete();
                pushmsg_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                if (!mFlagRefresh) {
                    Page--;
                }
            }
        });
    }

    @OnClick(R.id.stitle_ll_back)
    private void goback(View v) {
        finish();
    }

}
