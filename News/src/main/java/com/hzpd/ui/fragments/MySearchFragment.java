package com.hzpd.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.ui.activity.HtmlActivity;
import com.hzpd.ui.activity.NewsAlbumActivity;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.ZhuanTiActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.FjsonUtil;
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

import java.util.List;

public class MySearchFragment extends BaseFragment {

	@ViewInject(R.id.search_listview_id)
	private PullToRefreshListView search_listview_id;
	@ViewInject(R.id.search_edittext_id)
	private EditText search_edittext_id;
	@ViewInject(R.id.search_tv_search)
	private TextView search_tv_search;

	@ViewInject(R.id.search_ll1_root)
	private LinearLayout search_ll1_root;
	@ViewInject(R.id.search_ll2_search)
	private LinearLayout search_ll2_search;

	@ViewInject(R.id.search_ll3_root)
	private LinearLayout search_ll3_root;
	@ViewInject(R.id.zq_search_iv_clean)
	private ImageView zq_search_iv_clean;

	private NewsItemListViewAdapter adapter;

	private boolean isSearch = false;//是否已有搜索结果
	private boolean isRefresh = false;
	private int page = 1;
	private static final int pageSize = 15;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.search_main_layout, container, false);
		ViewUtils.inject(this, mView);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	private void init() {
		adapter = new NewsItemListViewAdapter(activity);
		search_listview_id.setAdapter(adapter);
		search_listview_id.setMode(Mode.DISABLED);

		search_listview_id.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				String con = search_edittext_id.getText().toString();
				if (null == con || "".equals(con)) {
					TUtils.toast("请输入内容");
					search_listview_id.setMode(Mode.DISABLED);
					return;
				}
				isRefresh = true;
				page = 1;
				getSearchData(con);
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				String con = search_edittext_id.getText().toString();
				if (null == con || "".equals(con)) {
					TUtils.toast("请输入内容");
					return;
				}
				isRefresh = false;
				page++;
				getSearchData(con);
			}
		});

		search_edittext_id.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
			                              int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String ss = s.toString();
				if (TextUtils.isEmpty(ss)) {
					search_tv_search.setText("取消");
					zq_search_iv_clean.setVisibility(View.GONE);
					isSearch = true;
				} else {
					search_tv_search.setText("搜索");
					zq_search_iv_clean.setVisibility(View.VISIBLE);
					isSearch = false;
				}
			}
		});

		search_listview_id.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {

				NewsBean nb = (NewsBean) adapter.getItem(position - 1);
				Intent mIntent = new Intent();
				mIntent.putExtra("newbean", nb);
				mIntent.putExtra("from", "newsitem");
				TextView title = (TextView) view.findViewById(R.id.newsitem_title);
				if (null != title) {
					title.setTextColor(getResources().getColor(R.color.grey_font));
				}

				////////////////////////////
				//1新闻  2图集  3直播 4专题  5关联新闻 6视频 
				if ("1".equals(nb.getRtype())) {
					mIntent.setClass(getActivity(), NewsDetailActivity.class);
				} else if ("2".equals(nb.getRtype())) {
					mIntent.setClass(getActivity(), NewsAlbumActivity.class);
				} else if ("3".equals(nb.getRtype())) {
					mIntent.setClass(getActivity(), HtmlActivity.class);//直播界面
				} else if ("4".equals(nb.getRtype())) {
					mIntent.setClass(getActivity(), ZhuanTiActivity.class);
				} else if ("5".equals(nb.getRtype())) {
					mIntent.setClass(getActivity(), NewsDetailActivity.class);
				} else if ("6".equals(nb.getRtype())) {
//					mIntent.setClass(getActivity(),VideoPlayerActivity.class);
					mIntent.setClass(getActivity(), NewsDetailActivity.class);
				} else if ("7".equals(nb.getRtype())) {
					mIntent.setClass(getActivity(), HtmlActivity.class);
				} else {
					return;
				}

				getActivity().startActivityForResult(mIntent, 0);
				AAnim.ActivityStartAnimation(getActivity());
			}
		});

	}

	@OnClick(R.id.search_ll2_search)
	private void search(View view) {
		search_ll1_root.setVisibility(View.GONE);
		search_ll3_root.setVisibility(View.VISIBLE);
	}

	@OnClick(R.id.zq_search_iv_clean)
	private void clean(View view) {
		search_edittext_id.setText("");
	}

	@OnClick(R.id.search_tv_search)
	private void searchDel(View v) {
		if (isSearch) {
			search_edittext_id.setText("");
			adapter.clear();
			adapter.notifyDataSetChanged();
			search_listview_id.setMode(Mode.DISABLED);
			search_ll1_root.setVisibility(View.VISIBLE);
			search_ll3_root.setVisibility(View.GONE);
			return;
		}

		String con = search_edittext_id.getText().toString();
		if (null == con || "".equals(con)) {
			TUtils.toast("请输入内容");
			search_listview_id.setMode(Mode.DISABLED);
			return;
		}

		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager.isActive()) {
			inputMethodManager.toggleSoftInput(
					InputMethodManager.SHOW_IMPLICIT
					, InputMethodManager.HIDE_NOT_ALWAYS);
		}
		search_listview_id.setMode(Mode.PULL_FROM_START);
		search_listview_id.postDelayed(new Runnable() {
			@Override
			public void run() {
				search_listview_id.setRefreshing(true);
			}
		}, 500);
		search_tv_search.setText("取消");
		isSearch = true;
	}

	private void getSearchData(String content) {
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("content", content);
		params.addBodyParameter("Page", "" + page);
		params.addBodyParameter("PageSize", "" + pageSize);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.SEARCH
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("result-->" + responseInfo.result);
				search_listview_id.onRefreshComplete();

				JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
				if (null == obj) {
					TUtils.toast("暂无数据");
					return;
				}

				if (200 == obj.getIntValue("code")) {

					List<NewsBean> l = FjsonUtil.parseArray(
							obj.getString("data"), NewsBean.class);
					if (null == l) {
						TUtils.toast("暂无数据");
						return;
					}
					LogUtils.i("l size-->" + l.size());

					adapter.appendData(l, isRefresh);

					if (l.size() < pageSize) {
						LogUtils.i("PULL_FROM_START");
						search_listview_id.setMode(Mode.PULL_FROM_START);
					} else {
						LogUtils.i("both");
						search_listview_id.setMode(Mode.BOTH);
					}
					adapter.notifyDataSetChanged();
				} else {
					TUtils.toast(obj.getString("msg"));
					if (!isRefresh) {
						page--;
					}
					search_listview_id.setMode(Mode.PULL_FROM_START);
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				TUtils.toast("服务器未响应");
				search_listview_id.onRefreshComplete();
				if (!isRefresh) {
					page--;
				}
				search_listview_id.setMode(Mode.PULL_FROM_START);
			}
		});
	}


}
