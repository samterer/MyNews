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
import com.hzpd.adapter.VideoAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.CacheBean;
import com.hzpd.modle.VideoItemBean;
import com.hzpd.modle.db.VideoItemBeanDb;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.ui.activity.VideoPlayerActivity;
import com.hzpd.ui.interfaces.I_Control;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DataCleanManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.VideoListDbTask;
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

public class VideoListFragment extends BaseFragment implements I_Control {

	@ViewInject(R.id.video_lv)
	private PullToRefreshListView mXListView;
	@ViewInject(R.id.video_nonetwork)
	private ImageView video_nonetwork;

	private VideoAdapter adapter;

	private boolean mFlagRefresh = false;
	private int page = 1;
	private static final int pageSize = 15;
	private VideoListDbTask videoListdbTask;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.video_list_fragment_layout, container, false);
		ViewUtils.inject(this, mView);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	private void init() {
		videoListdbTask = new VideoListDbTask(activity);

		mXListView.setEmptyView(video_nonetwork);
		mXListView.setMode(Mode.PULL_FROM_START);
		adapter = new VideoAdapter(getActivity());
		mXListView.setAdapter(adapter);

		mXListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				refreshView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
				mFlagRefresh = true;
				page = 1;
				adapter.clear();
				getDbList();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				LogUtils.i("上拉加载");
				refreshView.getLoadingLayoutProxy().setPullLabel("上拉加载更多");
				mFlagRefresh = false;
				page++;
				getDbList();
			}
		});

		//
		mXListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mXListView.setRefreshing(true);
			}
		}, 500);

	}

	@Override
	public void getDbList() {
		LogUtils.i("page-->" + page + "  pageSize-->" + pageSize);
		videoListdbTask.findList(page, pageSize, new I_SetList<VideoItemBeanDb>() {
			@Override
			public void setList(List<VideoItemBeanDb> list) {
				String vids = "";
				if (null != list) {
					StringBuilder sb = new StringBuilder();
					List<VideoItemBean> abList = new ArrayList<VideoItemBean>();
					for (VideoItemBeanDb abdb : list) {
						sb.append(abdb.getVid() + ",");
						abList.add(abdb.getVideoItemBean());
					}

					adapter.appendData(abList, mFlagRefresh);
					adapter.notifyDataSetChanged();
					if (sb.length() > 0) {
						vids = sb.substring(0, sb.length() - 1);
					}
					LogUtils.i("vids-->" + vids);
				} else {
					LogUtils.i("list null");
				}

				getServerList(vids);
			}
		});
	}

	@Override
	public void getServerList(String ids) {
		LogUtils.i("ids-->" + ids);
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("ids", ids);
		params.addBodyParameter("Page", "" + page);
		params.addBodyParameter("PageSize", "" + pageSize);
		params.addBodyParameter("update_time", spu.getCacheUpdatetime());

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.VIDEOLIST
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("getVideoList-->" + responseInfo.result);
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
					TUtils.toast("服务器错误");
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				mXListView.onRefreshComplete();
				video_nonetwork.setImageResource(R.drawable.zqzx_nonetwork);
			}
		});
	}

	@OnItemClick(R.id.video_lv)
	private void lvonItemClick(AdapterView<?> parent, View view,
	                           int position, long id) {

		VideoItemBean bean = (VideoItemBean) adapter.getItem(position - 1);
		Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
		intent.putExtra("VideoItemBean", bean);
		intent.putExtra("from", "videofragment");
		getActivity().startActivity(intent);
		AAnim.ActivityStartAnimation(getActivity());
	}

	//服务端返回数据处理
	@Override
	public void setData(JSONObject obj) {

		if (200 == obj.getIntValue("code")) {
			List<VideoItemBean> list = FjsonUtil.parseArray(obj.getString("data"), VideoItemBean.class);

			videoListdbTask.saveList(list, new I_Result() {
				@Override
				public void setResult(Boolean flag) {
					if (!flag) {
						return;
					}
					LogUtils.i("" + flag);

					videoListdbTask.findList(page, pageSize, new I_SetList<VideoItemBeanDb>() {
						@Override
						public void setList(List<VideoItemBeanDb> list) {

							List<VideoItemBean> abList = new ArrayList<VideoItemBean>();
							if (null != list) {
								LogUtils.i("videoListDbTask.findList-->" + list.size());
								for (VideoItemBeanDb abdb : list) {
									abList.add(abdb.getVideoItemBean());
								}

								adapter.removeOld();
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
			TUtils.toast("已到最后");
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
