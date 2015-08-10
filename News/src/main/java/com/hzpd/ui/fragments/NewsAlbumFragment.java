package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.ImgListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.CacheBean;
import com.hzpd.modle.ImgListBean;
import com.hzpd.modle.db.AlbumBeanDB;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.NewsAlbumActivity;
import com.hzpd.ui.interfaces.I_Control;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DataCleanManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.AlbumListDbTask;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class NewsAlbumFragment extends BaseFragment implements I_Control {
	@ViewInject(R.id.album_lv)
	private PullToRefreshListView mXListView;
	@ViewInject(R.id.album_nonetwork)
	private ImageView album_nonetwork;

	private ImgListViewAdapter adapter;

	private int page = 1;
	private static final int pageSize = 15;
	private AlbumListDbTask albumListdbTask;

	private boolean mFlagRefresh;

	public NewsAlbumFragment() {
		setTitle(App.getInstance().getString(R.string.menu_album));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.img_main_layout, container, false);
		ViewUtils.inject(this, mView);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}


	private void init() {
		albumListdbTask = new AlbumListDbTask(activity);
		mXListView.setEmptyView(album_nonetwork);
		mXListView.setMode(Mode.PULL_FROM_START);
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
		                                }
		);

		adapter = new ImgListViewAdapter(getActivity());
		mXListView.setAdapter(adapter);

		mXListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
			                        int position, long arg3) {
				if (position == 0) {
					return;
				}
				ImgListBean imgbean = (ImgListBean) adapter.getItem(position - 1);
				if (null == imgbean.getSubphoto() || imgbean.getSubphoto().size() < 1) {
					TUtils.toast(getString(R.string.toast_no_image));
					return;
				}
				Intent in = new Intent(getActivity(), NewsAlbumActivity.class);
				in.putExtra("from", "album");
				in.putExtra("imgbean", imgbean);
				getActivity().startActivity(in);
				AAnim.ActivityStartAnimation(getActivity());
			}
		});

		mXListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				LogUtils.i("get album");
				mXListView.setRefreshing(true);
			}
		}, 500);

	}

	@Override
	public void getDbList() {
		LogUtils.i("page-->" + page + "  pageSize-->" + pageSize);
		albumListdbTask.findList(page, pageSize, new I_SetList<AlbumBeanDB>() {
			@Override
			public void setList(List<AlbumBeanDB> list) {
				String pids = "";
				if (null != list) {
					StringBuilder sb = new StringBuilder();
					List<ImgListBean> abList = new ArrayList<ImgListBean>();
					for (AlbumBeanDB abdb : list) {
						sb.append(abdb.getPid() + ",");
						abList.add(abdb.getImgListBean());
					}

					adapter.appendData(abList, mFlagRefresh);
					adapter.notifyDataSetChanged();
					if (sb.length() > 0) {
						pids = sb.substring(0, sb.length() - 1);
					}
					LogUtils.i("pids-->" + pids);
				} else {
					LogUtils.i("list null");
				}

				getServerList(pids);
			}
		});
	}

	@Override
	public void getServerList(String nids) {
		LogUtils.i("ids-->" + nids);
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("ids", nids);
		params.addBodyParameter("Page", "" + page);
		params.addBodyParameter("PageSize", "" + pageSize);
		params.addBodyParameter("update_time", spu.getCacheUpdatetime());

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.ALBUMLIST
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("getAlbumFromServer-->" + responseInfo.result);
				Log.i("getAlbumFromServer", responseInfo.result);
				mXListView.onRefreshComplete();

				final JSONObject obj = FjsonUtil
						.parseObject(responseInfo.result);
				if (null != obj) {
					//缓存更新
					JSONObject cache = obj.getJSONObject("cachetime");
					if (null != cache) {
						spu.setCacheUpdatetime(cache.getString("update_time"));
						List<CacheBean> cacheList = FjsonUtil.parseArray(cache.getString("data"), CacheBean.class);
						DataCleanManager dcm = new DataCleanManager();
						dcm.deleteDb(cacheList, activity, new I_Result() {
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
				mXListView.onRefreshComplete();
			}
		});
	}

	//服务端返回数据处理
	@Override
	public void setData(JSONObject obj) {
		if (200 == obj.getIntValue("code")) {
			List<ImgListBean> list = FjsonUtil.parseArray(obj.getString("data"), ImgListBean.class);

			albumListdbTask.saveList(list, new I_Result() {
				@Override
				public void setResult(Boolean flag) {
					if (!flag) {
						return;
					}
					LogUtils.i("" + flag);

					albumListdbTask.findList(page, pageSize, new I_SetList<AlbumBeanDB>() {
						@Override
						public void setList(List<AlbumBeanDB> list) {

							List<ImgListBean> abList = new ArrayList<ImgListBean>();
							if (null != list) {
								LogUtils.i("albumListDbTask.findList-->" + list.size());
								for (AlbumBeanDB abdb : list) {
									abList.add(abdb.getImgListBean());
								}

								adapter.appendData(abList, mFlagRefresh);
								adapter.notifyDataSetChanged();
								mXListView.setMode(Mode.BOTH);

							}
						}
					});
				}
			});

		} else if (209 == obj.getIntValue("code")) {
			mXListView.setMode(Mode.BOTH);
		} else if (202 == obj.getIntValue("code")) {
			mXListView.setMode(Mode.PULL_FROM_START);
			TUtils.toast(getString(R.string.pull_to_refresh_reached_end));
		} else {
			TUtils.toast(obj.getString("msg"));
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