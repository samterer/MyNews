package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.adapter.TopviewpagerAdapter;
import com.hzpd.custorm.ListViewInScrollView;
import com.hzpd.custorm.TopPicViewPager;
import com.hzpd.hflt.R;
import com.hzpd.modle.CacheBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.NewsPageListBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.DayNightEvent;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.HtmlActivity;
import com.hzpd.ui.activity.NewsAlbumActivity;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.ZhuanTiActivity;
import com.hzpd.ui.interfaces.I_Control;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DataCleanManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.NewsListDbTask;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class NewsItemFragment extends BaseFragment implements I_Control {
	@ViewInject(R.id.nil_root)
	private RelativeLayout nil_root;
	@ViewInject(R.id.news_item_sv)
	private PullToRefreshScrollView news_item_sv;
	@ViewInject(R.id.news_item_listview)
	private ListViewInScrollView mXListView;
	@ViewInject(R.id.news_item_iv)
	private ImageView news_item_iv;

	@ViewInject(R.id.test_pager)
	private TopPicViewPager topViewpager;//顶部轮播图
	@ViewInject(R.id.viewpage_txt_id)
	private TextView mTextView;
	@ViewInject(R.id.viewpage_txt_Molecular)
	private TextView viewpage_txt_Molecular;
	@ViewInject(R.id.viewpage_txt_Denominator)
	private TextView viewpage_txt_Denominator;
	@ViewInject(R.id.news_viewpage_myroot)
	private LinearLayout news_viewpage_myroot;//顶部轮播图根布局

	private TopviewpagerAdapter topviewAdapter;//轮播图适配器
	private NewsItemListViewAdapter adapter;

	private NewsChannelBean channelbean;//本频道
	private String newsItemPath;//本频道根目录flash

	private int page = 1;
	private static final int pageSize = 15;//

	private NewsListDbTask newsListDbTask; //新闻列表数据库

	// 是否刷新最新数据
	private boolean mFlagRefresh;
	private boolean isRefresh = false;//是否首次加载
	private boolean isNeedRefresh = false;
	private int position = -1;

	private MyRun switchTask;//切换线程

	private boolean isContinue = true;//是否可以自动切换
	private int interval;//播放间隔时间
	private Handler viewHandler = new Handler();

	public NewsItemFragment() {

	}

	public NewsItemFragment(NewsChannelBean channelbean, int position) {
		this.channelbean = channelbean;
		this.position = position;
		setTitle(channelbean.getCnname());
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		newsItemPath = App.getInstance().getJsonFileCacheRootDir();
		newsListDbTask = new NewsListDbTask(activity);
		EventBus.getDefault().register(this);
	}

	public String getTitle() {
		return channelbean.getCnname();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.news_prlistview, container, false);
		ViewUtils.inject(this, view);

		return view;
	}

	public void setIsNeedRefresh() {
		isNeedRefresh = true;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mXListView.setEmptyView(news_item_iv);
		news_item_sv.setMode(Mode.PULL_FROM_START);

		adapter = new NewsItemListViewAdapter(activity);
		mXListView.setAdapter(adapter);

		news_item_sv.setOnRefreshListener(new OnRefreshListener2<ScrollView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
				LogUtils.i("下拉刷新");

				//下拉刷新
//				refreshView.getLoadingLayoutProxy().setLoadingDrawable(
//						getResources().getDrawable(R.drawable.default_ptr_flip));
				refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_pull_label));
//				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("hh");
				page = 1;
				mFlagRefresh = true;
				getDbList();
				getFlash();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
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
		mXListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {

				LogUtils.i("position-->" + position + "  id-->" + id);

				TextView title = (TextView) view.findViewById(R.id.newsitem_title);
				if (null != title) {
					title.setTextColor(getResources().getColor(R.color.grey_font));
				}

				NewsBean nb = (NewsBean) adapter.getItem(position);
				Intent mIntent = new Intent();
				mIntent.putExtra("newbean", nb);
				mIntent.putExtra("from", "newsitem");

				LogUtils.i("rtype-->" + nb.getRtype());

				adapter.setReadedId(nb.getNid());
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

				activity.startActivityForResult(mIntent, 0);
				AAnim.ActivityStartAnimation(getActivity());
			}
		});

		initFlash();

		if (0 == position || isNeedRefresh) {
			mXListView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mFlagRefresh = true;
					getDbList();
					getFlash();
				}
			}, 800);
			isRefresh = true;
			isNeedRefresh = false;
		}
	}

	public void init() {

		if (!isRefresh && null != mXListView) {
			mXListView.postDelayed(new Runnable() {
				@Override
				public void run() {
					news_item_sv.setRefreshing(true);
					isRefresh = true;
				}
			}, 600);
		}
	}

	//新闻列表
	@Override
	public void getDbList() {
		LogUtils.i("page-->" + page + "  pageSize-->" + pageSize);
		newsListDbTask.findList(channelbean.getTid(), page, pageSize, new I_SetList<NewsBeanDB>() {
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

	//获取新闻list
	@Override
	public void getServerList(String nids) {
		LogUtils.i("nids-->" + nids);

		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("tid", channelbean.getTid());
		params.addBodyParameter("nids", nids);
		params.addBodyParameter("Page", "" + page);
		params.addBodyParameter("PageSize", "" + pageSize);
		params.addBodyParameter("update_time", spu.getCacheUpdatetime());

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.NEWSLIST
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("getNewsList-->" + responseInfo.result);
				Log.i("getNewsList", responseInfo.result);
				news_item_sv.onRefreshComplete();

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
				news_item_sv.onRefreshComplete();
				news_item_sv.setMode(Mode.PULL_FROM_START);
				news_item_iv.setImageResource(R.drawable.zqzx_nonetwork);
			}
		});
	}

	//服务端返回数据处理
	@Override
	public void setData(JSONObject obj) {
		//数据处理
		switch (obj.getIntValue("code")) {
			case 200: {
				List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);

				newsListDbTask.saveList(list, new I_Result() {
					@Override
					public void setResult(Boolean flag) {
						if (!flag) {
							return;
						}
						newsListDbTask.findList(channelbean.getTid(), page, pageSize
								, new I_SetList<NewsBeanDB>() {
							@Override
							public void setList(List<NewsBeanDB> list) {

								List<NewsBean> nbList = new ArrayList<NewsBean>();
								if (null != list) {
									LogUtils.i("newsListDbTask.findList-->" + list.size());
									for (NewsBeanDB nbdb : list) {
										nbList.add(nbdb.getNewsBean());
									}
									adapter.removeOld();
									adapter.appendData(nbList, mFlagRefresh);
									adapter.notifyDataSetChanged();
									news_item_sv.setMode(Mode.BOTH);
								}
							}
						});
					}
				});
			}
			break;
			case 201: {
				news_item_sv.setMode(Mode.PULL_FROM_START);
			}
			break;
			case 202: {
				news_item_sv.setMode(Mode.PULL_FROM_START);
				TUtils.toast(getString(R.string.pull_to_refresh_reached_end));
			}
			break;
			case 209: {
				news_item_sv.setMode(Mode.BOTH);
			}
			break;

			default: {
				TUtils.toast(obj.getString("msg"));
			}
			break;
		}
	}

	//获取幻灯
	private void getFlash() {
		final File pageFile = App.getFile(newsItemPath + File.separator
				+ "channel_" + channelbean.getTid()
				+ File.separator + "flash");
		String path = InterfaceJsonfile.FLASH + channelbean.getTid();
		LogUtils.i("getFlash-->" + path);

		httpUtils.download(
				path
				, pageFile.getAbsolutePath()
				, new RequestCallBack<File>() {
					@Override
					public void onSuccess(ResponseInfo<File> responseInfo) {
						String data = App.getFileContext(responseInfo.result);

						LogUtils.i("flash-->" + data);
						JSONObject obj = FjsonUtil.parseObject(data);

						if (null == obj) {
							responseInfo.result.delete();
							return;
						}

						List<NewsPageListBean> mViewPagelist = null;
						if (200 == obj.getIntValue("code")) {
							JSONObject object = obj.getJSONObject("data");

							mViewPagelist = FjsonUtil.parseArray(object.getString("flash"), NewsPageListBean.class);

							try {
								interval = Integer.parseInt(object.getString("interval"));
							} catch (Exception e) {
								interval = 4;
							}

						}
						setFlashData(mViewPagelist);
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						LogUtils.i("getFlash-failed");

						String data = App.getFileContext(pageFile);
						JSONObject obj = FjsonUtil.parseObject(data);

						if (null == obj) {
							pageFile.delete();
							return;
						}
						List<NewsPageListBean> mViewPagelist = null;
						if (200 == obj.getIntValue("code")) {
							JSONObject object = obj.getJSONObject("data");
							mViewPagelist = FjsonUtil.parseArray(object.getString("flash"), NewsPageListBean.class);
							try {
								interval = Integer.parseInt(object.getString("interval"));
							} catch (Exception e) {
								interval = 4;
							}
						}
						setFlashData(mViewPagelist);
					}
				});
	}

	private void initFlash() {

		DisplayMetrics dm = activity.getResources().getDisplayMetrics();
		int height = (int) (dm.widthPixels * 0.58);
		LogUtils.i("height-->" + height);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, height);
		news_viewpage_myroot.setLayoutParams(params);

		topviewAdapter = new TopviewpagerAdapter(activity);
		topviewAdapter.setTid(channelbean.getTid());
		topViewpager.setAdapter(topviewAdapter);

		topViewpager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {

				if (arg0 == topviewAdapter.getCount() - 1) {
					topViewpager.setCurrentItem(1, false);
					return;
				} else if (0 == arg0) {
					topViewpager.setCurrentItem(topviewAdapter.getCount() - 2, false);
					return;
				}
				mTextView.setText(topviewAdapter.getBean(arg0).getTitle());//设置小标题
				viewpage_txt_Molecular.setText(topviewAdapter.getPosition(arg0) + "");
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		topViewpager.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE:
						isContinue = false;
						break;
					case MotionEvent.ACTION_UP: {
						isContinue = true;
					}
					break;
					default:
						isContinue = true;
						break;
				}
				return false;
			}
		});

	}

	private void setFlashData(List<NewsPageListBean> mViewPagelist) {
		if (null == mViewPagelist || mViewPagelist.size() < 1) {
			news_viewpage_myroot.setVisibility(View.GONE);
			return;
		}
		news_viewpage_myroot.setVisibility(View.VISIBLE);
		viewHandler.removeCallbacksAndMessages(null);
		switchTask = new MyRun();
		topViewpager.setCurrentItem(0);

		LogUtils.i("mViewPagelist-->" + mViewPagelist.size());
		mTextView.setText(mViewPagelist.get(0).getTitle());

		topviewAdapter.setData(mViewPagelist);

		viewpage_txt_Molecular.setText("1");
		if (mViewPagelist.size() > 1) {
			viewpage_txt_Denominator.setText("/" + (mViewPagelist.size() - 2));
			topViewpager.setCurrentItem(1, false);
		} else {
			viewpage_txt_Denominator.setText("/" + mViewPagelist.size());
		}

		if (topviewAdapter.getListSize() > 1) {
			switchTask.run();
		}
	}

	@Override
	public void onDestroy() {
		if (null != viewHandler) {
			viewHandler.removeCallbacks(switchTask);
		}
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onEventMainThread(FontSizeEvent event) {
		adapter.setFontSize(event.getFontSize());
	}

	public void onEventMainThread(DayNightEvent event) {
		nil_root.setBackgroundColor(event.getDaynightColor());
	}

	private class MyRun implements Runnable {
		private boolean isStart = false;

		public void run() {
			if (isContinue && isStart) {

				int current = topViewpager.getCurrentItem();
				int total = topViewpager.getAdapter().getCount();
				if (total == current + 1) {
					topViewpager.setCurrentItem(0, false);
				} else {
					topViewpager.setCurrentItem(current + 1, true);
				}
//				LogUtils.i("interval-->"+interval+"   i-->"+i+"  "+System.currentTimeMillis());
			} else {
				isStart = true;
			}

			viewHandler.postDelayed(switchTask, interval * 1000);
		}

	}


}
