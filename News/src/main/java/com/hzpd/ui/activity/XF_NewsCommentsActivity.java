package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.MyCommentListAdapter;
import com.hzpd.adapter.MycommentsAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.MyCommentListBean;
import com.hzpd.modle.ReplayBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
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

    private ReplayBean bean;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newscomments_layout);
        ViewUtils.inject(this);

        Bundle bundle = getIntent().getExtras();
        bean = (ReplayBean) bundle.getSerializable("reply");
//        Bundle args = new Bundle();
//        args.putSerializable("reply", bean);
        LogUtils.e("bean" + bean.getId() + bean.getType());

        stitle_tv_content.setText(R.string.comment_item_new);
        pushmsg_lv.setEmptyView(pushmsg_tv_empty);
        pushmsg_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        pushmsg_lv.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushmsg_lv.setRefreshing(true);
            }
        }, 500);

        myCommentListAdapter = new MyCommentListAdapter();
        pushmsg_lv.setAdapter(myCommentListAdapter);
        pushmsg_lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                LogUtils.i("下拉刷新");
                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_pull_label));
                Page = 1;
                mFlagRefresh = true;
                getInfoFromServer();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载
                LogUtils.i("上拉加载");
                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_from_bottom_pull_label));
                Page++;
                mFlagRefresh = false;
                getInfoFromServer();
            }
        });

        pushmsg_lv.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushmsg_lv.setRefreshing(false);
            }
        }, 600);
        pushmsg_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.getTag() instanceof MyCommentListAdapter.ViewHolder) {
                    MyCommentListAdapter.ViewHolder viewHolder = (MyCommentListAdapter.ViewHolder) view.getTag();
                    Intent mIntent = new Intent();
                    mIntent.putExtra("uid", viewHolder.userId); //TODO
                    mIntent.setClass(activity, XF_PInfoActivity.class);
                    startActivity(mIntent);
                }
            }
        });
    }


    private void getInfoFromServer() {

        String station = SharePreferecesUtils.getParam(this, StationConfig.STATION, "def").toString();
        String siteid = null;
        String myComm_url = null;
        if (station.equals(StationConfig.DEF)) {
            siteid = InterfaceJsonfile.SITEID;
            myComm_url = InterfaceJsonfile.CHECKCOMMENT;
        } else if (station.equals(StationConfig.YN)) {
            siteid = InterfaceJsonfile_YN.SITEID;
            myComm_url = InterfaceJsonfile_YN.CHECKCOMMENT;
        } else if (station.equals(StationConfig.TW)) {
            siteid = InterfaceJsonfile_TW.SITEID;
            myComm_url = InterfaceJsonfile_TW.CHECKCOMMENT;
        }
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("PageSize", PageSize + "");
        params.addBodyParameter("Page", Page + "");

        params.addBodyParameter("nid", bean.getId());
        params.addBodyParameter("type", bean.getType());

        params.addBodyParameter("siteid", siteid);
        httpUtils.send(HttpRequest.HttpMethod.POST
                , myComm_url
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
