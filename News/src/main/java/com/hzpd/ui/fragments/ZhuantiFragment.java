package com.hzpd.ui.fragments;

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
import com.hzpd.adapter.ZhuantiListAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.CacheBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.ZhuanTiActivity;
import com.hzpd.ui.interfaces.I_Control;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.DataCleanManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.ZhuantiListDbTask;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnItemClick;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ZhuantiFragment extends BaseFragment implements I_Control {
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (isVisible != isVisibleToUser) {
			isVisible = isVisibleToUser;
			if (isVisibleToUser) {
				AnalyticUtils.sendGaEvent(getActivity(), AnalyticUtils.CATEGORY.newsType, AnalyticUtils.ACTION.viewPage, "专题",
						0L);
				AnalyticUtils.sendUmengEvent(getActivity(), AnalyticUtils.CATEGORY.newsType, "专题");
			}
		}
	}

	@ViewInject(R.id.subjectlist_lv)
	private PullToRefreshListView mXListView;
	@ViewInject(R.id.subjectlist_nonetwork)
	private ImageView subjectlist_nonetwork;

	private ZhuantiListAdapter adapter;

	private boolean mFlagRefresh;// 是刷新

	private int page = 1;
	private static final int pageSize = 15;//
	private ZhuantiListDbTask zhuantiListDbTask;

	public ZhuantiFragment() {
		setTitle(App.getInstance().getString(R.string.menu_subject));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.specialfm_layout, container,
				false);
		ViewUtils.inject(this, mView);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
		AnalyticUtils.sendGaEvent(getActivity(), AnalyticUtils.CATEGORY.newsType, AnalyticUtils.ACTION.viewPage, "专题", 0L);
		AnalyticUtils.sendUmengEvent(getActivity(), AnalyticUtils.CATEGORY.newsType, "专题");
	}

	private void init() {
		zhuantiListDbTask = new ZhuantiListDbTask(activity);
		mXListView.setEmptyView(subjectlist_nonetwork);
		mXListView.setMode(Mode.PULL_FROM_START);

		adapter = new ZhuantiListAdapter(getActivity());
		mXListView.setAdapter(adapter);

		mXListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				LogUtils.i("下拉刷新");
				refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_pull_label));
				page = 1;
				mFlagRefresh = true;
				getDbList();
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_from_bottom_pull_label));
				page++;
				mFlagRefresh = false;
				getDbList();
			}
		});

		mXListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mXListView.setRefreshing(true);
			}
		}, 500);

	}

	@OnItemClick(R.id.subjectlist_lv)
	private void onitemclick(AdapterView<?> parent, View view, int position,
	                         long id) {

		LogUtils.i("position:" + position);
		NewsBean kdbean = (NewsBean) adapter
				.getItem(position - 1);
		Intent intent = new Intent(getActivity(), ZhuanTiActivity.class);
		intent.putExtra("from", "subject");
		intent.putExtra("nb", kdbean);
		getActivity().startActivity(intent);
		AAnim.ActivityStartAnimation(getActivity());
	}

	// 专题列表
	@Override
	public void getDbList() {
		LogUtils.i("page-->" + page + "  pageSize-->" + pageSize);
		zhuantiListDbTask.findList(page, pageSize,
				new I_SetList<NewsBeanDB>() {
					@Override
					public void setList(List<NewsBeanDB> list) {
						String nids = "";
						if (null != list) {
							StringBuilder sb = new StringBuilder();
							List<NewsBean> nbList = new ArrayList<NewsBean>();
							for (NewsBeanDB nbdb : list) {
								sb.append(nbdb.getNid() + ",");
								nbList.add(nbdb.getNewsBean());
							}

							adapter.appendData(nbList, mFlagRefresh);
							adapter.notifyDataSetChanged();
							if (sb.length() > 1) {
								nids = sb.substring(0, sb.length() - 1);
							}
						}

						getServerList(nids);
					}
				});
	}

	// 获取新闻list
	@Override
	public void getServerList(String nids) {
		LogUtils.i("nids-->" + nids);
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("rtype", "4");
		params.addBodyParameter("nids", nids);
		params.addBodyParameter("Page", "" + page);
		params.addBodyParameter("PageSize", "" + pageSize);
		params.addBodyParameter("update_time", spu.getCacheUpdatetime());
		SPUtil.addParams(params);
		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.SUBJECTLIST, params,
				new RequestCallBack<String>() {
					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						if(!isAdded()){
							return;
						}
						mXListView.onRefreshComplete();

						final JSONObject obj = FjsonUtil
								.parseObject(responseInfo.result);
						if (null != obj) {
							// 缓存更新
							JSONObject cache = obj.getJSONObject("cachetime");
							if (null != cache) {
								spu.setCacheUpdatetime(cache
										.getString("update_time"));
								List<CacheBean> cacheList = FjsonUtil
										.parseArray(cache.getString("data"),
												CacheBean.class);
								DataCleanManager dcm = new DataCleanManager();
								dcm.deleteDb(cacheList, activity,
										new I_Result() {
											@Override
											public void setResult(Boolean flag) {
												setData(obj);
											}
										});
							} else {
								setData(obj);
							}
						} else {
							TUtils.toast(getString(R.string.toast_server_error));
						}
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						if(!isAdded()){
							return;
						}
						mXListView.onRefreshComplete();
						mXListView.setMode(Mode.PULL_FROM_START);
						subjectlist_nonetwork.setImageResource(R.drawable.zqzx_nonetwork);
					}
				});
	}

	// 服务端返回数据处理
	@Override
	public void setData(JSONObject obj) {
		// 数据处理
		LogUtils.i("obj-->" + obj);
		switch (obj.getIntValue("code")) {
			case 200: {
				List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"),
						NewsBean.class);

				zhuantiListDbTask.saveList(list, new I_Result() {
					@Override
					public void setResult(Boolean flag) {
						if (!flag) {
							return;
						}
						zhuantiListDbTask.findList(page,
								pageSize, new I_SetList<NewsBeanDB>() {
									@Override
									public void setList(List<NewsBeanDB> list) {

										List<NewsBean> zbList = new ArrayList<NewsBean>();
										if (null != list) {
											LogUtils.i("newsListDbTask.findList-->"
													+ list.size());
											for (NewsBeanDB zbdb : list) {
												zbList.add(zbdb.getNewsBean());
											}
											adapter.removeOld();
											adapter.appendData(zbList, mFlagRefresh);
											adapter.notifyDataSetChanged();
											mXListView.setMode(Mode.BOTH);
										}
									}
								});
					}
				});
			}
			break;
			case 201: {
				mXListView.setMode(Mode.PULL_FROM_START);
			}
			break;
			case 202: {
				mXListView.setMode(Mode.PULL_FROM_START);
				TUtils.toast(getString(R.string.pull_to_refresh_reached_end));
			}
			break;
			case 209: {
				mXListView.setMode(Mode.BOTH);
			}
			break;

			default: {
				TUtils.toast(obj.getString("msg"));
			}
			break;
		}
	}

	public void onEventMainThread(FontSizeEvent event) {
		adapter.setFontSize(event.getFontSize());
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
}