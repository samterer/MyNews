package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.CheckcommenthotAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.CommentzqzxBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;


public class CheckCommenthotActivity extends MBaseActivity {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;
	@ViewInject(R.id.check_commtenthot_lv)
	private PullToRefreshListView checkcomment_lv;

	private CheckcommenthotAdapter adapter;

	private int currentPage = 1;
	private final int pageSize = 15;
	private boolean isRefresh;
	private String nid;
	private String mNewtype;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commentlist_layout);
		ViewUtils.inject(this);

		stitle_tv_content.setText("查看评论");

		Intent intent = getIntent();
		nid = intent.getStringExtra("id");
		mNewtype = intent.getStringExtra("mNewtype");
		LogUtils.i("nid---" + nid);


		adapter = new CheckcommenthotAdapter(this, mNewtype);
		checkcomment_lv.setAdapter(adapter);

		checkcomment_lv.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				LogUtils.i("下拉刷新");
				//下拉刷新
				isRefresh = true;
				currentPage = 1;
				getLatestComm();
				getHotComm();

			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				isRefresh = false;
				currentPage++;
				getLatestComm();
			}
		});

		checkcomment_lv.postDelayed(new Runnable() {
			@Override
			public void run() {
				checkcomment_lv.setRefreshing(true);
			}
		}, 500);

	}

	private void getLatestComm() {
		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("Page", "" + currentPage);
		params.addBodyParameter("PageSize", "" + pageSize);
		params.addBodyParameter("nid", nid);
		params.addBodyParameter("type", mNewtype);
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.mLatestComm
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				checkcomment_lv.onRefreshComplete();
				LogUtils.i("getLatestComm-result-->" + responseInfo.result);
				JSONObject obj = null;
				try {
					obj = JSONObject.parseObject(responseInfo.result);
				} catch (Exception e) {
					return;
				}
				if (200 == obj.getIntValue("code")) {
					ArrayList<CommentzqzxBean> latestList = (ArrayList<CommentzqzxBean>) JSONArray.parseArray(
							obj.getString("data"), CommentzqzxBean.class);
					if (latestList.size() < pageSize) {
						checkcomment_lv.setMode(Mode.PULL_FROM_START);
					} else {
						checkcomment_lv.setMode(Mode.BOTH);
					}

					if (isRefresh) {
						adapter.setLatestData(latestList);
					} else {
						adapter.addLatestData(latestList);
					}
				} else {
					TUtils.toast(obj.getString("msg"));
				}
				isRefresh = false;
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				checkcomment_lv.setMode(Mode.PULL_FROM_START);
				TUtils.toast("服务器未响应");
				currentPage--;
				checkcomment_lv.onRefreshComplete();
				isRefresh = false;

			}
		});
	}

	private void getHotComm() {
		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("Page", "" + currentPage);
		params.addBodyParameter("PageSize", "" + pageSize);
		params.addBodyParameter("nid", nid);
		params.addBodyParameter("type", mNewtype);
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.mHotComm
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				checkcomment_lv.onRefreshComplete();
				LogUtils.i("getHotComm-result-->" + responseInfo.result);
				JSONObject obj = null;
				try {
					obj = JSONObject.parseObject(responseInfo.result);
				} catch (Exception e) {
					return;
				}
				if (200 == obj.getIntValue("code")) {
					ArrayList<CommentzqzxBean> hotlist = (ArrayList<CommentzqzxBean>) JSONArray.parseArray(
							obj.getString("data"), CommentzqzxBean.class);
					adapter.setHotCommData(hotlist);
				} else {
					TUtils.toast(obj.getString("msg"));
				}

			}

			@Override
			public void onFailure(HttpException error, String msg) {
				checkcomment_lv.onRefreshComplete();
				TUtils.toast("服务器未响应");
			}
		});
	}

	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		finish();
	}

}
