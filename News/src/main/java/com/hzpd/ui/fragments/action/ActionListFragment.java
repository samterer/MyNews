package com.hzpd.ui.fragments.action;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.ActionListFmAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.ActionItemBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnItemClick;

import java.util.List;

public class ActionListFragment extends BaseFragment {

	@ViewInject(R.id.news_item_listview)
	private PullToRefreshListView news_item_listview;
	@ViewInject(R.id.news_item_nonetwork)
	private ImageView news_item_nonetwork;

	private boolean mFlagRefresh = false;//是否首次加载
	private int page = 1;
	private static final int pageSize = 15;

	private ActionListFmAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.zq_html_prlistview, container, false);
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		news_item_listview.setMode(Mode.PULL_FROM_START);
		news_item_listview.setEmptyView(news_item_nonetwork);

		adapter = new ActionListFmAdapter(activity);
		news_item_listview.setAdapter(adapter);
		news_item_listview.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				LogUtils.i("下拉刷新");

				//下拉刷新
//				refreshView.getLoadingLayoutProxy().setLoadingDrawable(
//						getResources().getDrawable(R.drawable.default_ptr_flip));
				refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_pull_label));
//				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("hh");
				page = 1;
				mFlagRefresh = true;
				getDbList();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				//上拉加载
				LogUtils.i("上拉加载");
//				refreshView.getLoadingLayoutProxy().setLoadingDrawable(
//						getResources().getDrawable(R.drawable.default_ptr_flip));
				refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_from_bottom_pull_label));
//				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("hh");
				mFlagRefresh = false;
				page++;
				getDbList();
			}

		});

		news_item_listview.postDelayed(new Runnable() {
			@Override
			public void run() {
				news_item_listview.setRefreshing(true);
			}
		}, 600);

	}

	@OnItemClick(R.id.news_item_listview)
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ActionItemBean aib = (ActionItemBean) adapter.getItem(position - 1);
		if (null == aib) {
			return;
		}

		Intent intent = new Intent(activity, ActionDetailActivity.class);
		intent.putExtra("id", aib.getId());
		startActivity(intent);
		AAnim.ActivityStartAnimation(activity);

	}

	private void getDbList() {

		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("page", "" + page);
		params.addBodyParameter("pagesize", "" + pageSize);
		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.actionList
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("action list result-->" + responseInfo.result);
				news_item_listview.onRefreshComplete();
				news_item_listview.setMode(Mode.PULL_FROM_START);
				JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
				if (null == obj) {
					return;
				}
				if (200 == obj.getIntValue("code")) {
					List<ActionItemBean> list = FjsonUtil.parseArray(obj.getString("data")
							, ActionItemBean.class);
					if (null != list && list.size() > 0) {
						adapter.appendData(list, mFlagRefresh);
						adapter.notifyDataSetChanged();
						if (list.size() >= pageSize) {
							news_item_listview.setMode(Mode.BOTH);
						}
					}
				} else {
					TUtils.toast(obj.getString("msg"));
				}

			}

			@Override
			public void onFailure(HttpException error, String msg) {
				LogUtils.i("action list failed");
				news_item_listview.onRefreshComplete();
				news_item_listview.setMode(Mode.PULL_FROM_START);

			}
		});


	}
}