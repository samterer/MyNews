package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.hzpd.adapter.ZhuantiDetailListAdapter;
import com.hzpd.custorm.ListViewInScrollView;
import com.hzpd.hflt.R;
import com.hzpd.modle.CacheBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.SubjectItemColumnsBean;
import com.hzpd.modle.SubjectNumber;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.ui.interfaces.I_Control;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DataCleanManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.ZhuantiDetailListDbTask;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.lidroid.xutils.view.annotation.event.OnItemClick;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ZhuanTiActivity extends MBaseActivity implements I_Control {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;

	@ViewInject(R.id.zhuanti_item_sv)
	private PullToRefreshScrollView zhuanti_item_sv;
	@ViewInject(R.id.zhuanti_topview_myroot)
	private LinearLayout zhuanti_topview_myroot;
	@ViewInject(R.id.zhuanti_header_iv)
	private ImageView zhuanti_header_iv;
	@ViewInject(R.id.zhuanti_tv_title)
	private TextView zhuanti_tv_title;
	@ViewInject(R.id.zhuanti_tv_con)
	private TextView zhuanti_tv_con;

	@ViewInject(R.id.zhuanti_item_listview)
	private ListViewInScrollView mXListView;
	@ViewInject(R.id.zhuanti_item_iv)
	private ImageView zhuanti_item_iv;

	private ZhuantiDetailListAdapter adapter;

	private String from;//newsitem subject

	private boolean mFlagRefresh;
	private int page = 1;
	private static final int pageSize = 1500;//

	private NewsBean nb = null;//专题id

	private ZhuantiDetailListDbTask newsListDbTask; //zhuanti列表数据库

	private List<SubjectItemColumnsBean> columnList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.special_detail_layout);
		ViewUtils.inject(this);
		stitle_tv_content.setText(R.string.prompt_subject);
		init();

		mXListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				zhuanti_item_sv.setRefreshing(true);
			}
		}, 500);
		EventBus.getDefault().register(this);

	}

	private void init() {
		newsListDbTask = new ZhuantiDetailListDbTask(activity);
		Intent intent = getIntent();
		from = intent.getStringExtra("from");
		LogUtils.i("from-->" + from);

		if ("newsitem".equals(from)) {
			nb = (NewsBean) intent.getSerializableExtra("newbean");
		} else if ("subject".equals(from)) {
			nb = (NewsBean) intent.getSerializableExtra("nb");

		}
		if (null == nb) {
			return;
		}

		String imgs[] = nb.getImgs();
		String img = "";
		if (null != imgs && imgs.length > 0) {
			img = imgs[0];
		}
		SPUtil.displayImage(img, zhuanti_header_iv);
		zhuanti_tv_title.setText(nb.getTitle());
//		R.id.special_header_tv_con//数量

		mXListView.setEmptyView(zhuanti_item_iv);

		zhuanti_item_sv.setMode(Mode.PULL_FROM_START);
		adapter = new ZhuantiDetailListAdapter(activity);
		mXListView.setAdapter(adapter);
		zhuanti_item_sv.setOnRefreshListener(new OnRefreshListener2<ScrollView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
				LogUtils.i("下拉刷新");
				refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_pull_label));
				page = 1;
				mFlagRefresh = true;
				adapter.clearData();
				getColumns();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
				refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_from_bottom_pull_label));
				page++;
				mFlagRefresh = false;
				getColumns();
			}
		});

		//总共多少篇
		getCounts(nb.getNid());


	}

	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		finish();
	}

	@OnItemClick(R.id.zhuanti_item_listview)
	public void listviewonItemClick(AdapterView<?> parent, View view,
	                                int position, long id) {

		NewsBean nb = null;
		int type = adapter.getItemViewType(position);
		if (0 == type) {
			return;
		} else {
			nb = (NewsBean) adapter.getItem(position);
		}

		if (null == nb) {
			LogUtils.i("nb null");
			return;
		}

		String detailId = nb.getNid();

		LogUtils.i("detailId-->" + detailId);

		Intent in = new Intent(this, NewsDetailActivity.class);
		in.putExtra("newbean", nb);
		in.putExtra("from", "news");

		startActivity(in);
		AAnim.ActivityStartAnimation(this);
	}

	//专题栏目列表
	public void getColumns() {
		String station= SharePreferecesUtils.getParam(ZhuanTiActivity.this, StationConfig.STATION, "def").toString();
		String SUBJECTCOLUMNSLIST_url =null;
		if (station.equals(StationConfig.DEF)){
			SUBJECTCOLUMNSLIST_url =InterfaceJsonfile.SUBJECTCOLUMNSLIST;
		}else if (station.equals(StationConfig.YN)){
			SUBJECTCOLUMNSLIST_url = InterfaceJsonfile_YN.SUBJECTCOLUMNSLIST;
		}else if (station.equals(StationConfig.TW)){
			SUBJECTCOLUMNSLIST_url = InterfaceJsonfile_TW.SUBJECTCOLUMNSLIST;
		}
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("sid", nb.getNid());
		params.addBodyParameter("page", "" + page);
		params.addBodyParameter("pagesize", "" + pageSize);

		httpUtils.send(HttpMethod.POST
				, SUBJECTCOLUMNSLIST_url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String json = responseInfo.result;
				LogUtils.i("getColumns-->" + json);
				zhuanti_item_sv.onRefreshComplete();
				JSONObject obj = FjsonUtil
						.parseObject(responseInfo.result);
				if (null != obj) {
					if (200 == obj.getIntValue("code")) {
						JSONArray array = obj.getJSONArray("data");
						columnList = FjsonUtil.parseArray(array.toJSONString(), SubjectItemColumnsBean.class);

						if (null != columnList && columnList.size() > 0) {
							spu.setSubjectColumnList(array);
						} else {
							JSONArray oldarray = spu.getSubjectColumnList();
							columnList = FjsonUtil.parseArray(oldarray.toJSONString(), SubjectItemColumnsBean.class);
						}

						if (null != columnList && columnList.size() > 0) {
							for (SubjectItemColumnsBean sicb : columnList) {
								getDbList(sicb);
							}
						}

					} else {
						TUtils.toast(obj.getString("msg"));
					}
				} else {
					TUtils.toast(getString(R.string.toast_cannot_connect_network));
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				zhuanti_item_sv.onRefreshComplete();
			}
		});
	}

	//新闻列表
	@Override
	public void getDbList() {
		/*
		LogUtils.i("page-->"+page+"  pageSize-->"+pageSize);
		newsListDbTask.findList(sid, page, pageSize, new I_SetList<NewsBeanDB>() {
			@Override
			public void setList(List<NewsBeanDB> list) {
				String nids="";
				if(null!=list){
					StringBuilder sb=new StringBuilder();
					List<NewsBean> nbList=new ArrayList<NewsBean>();
					for(NewsBeanDB nbdb:list){
						sb.append(nbdb.getNid()+",");
						nbList.add(nbdb.getNewsBean());
					}
					
					adapter.appendData(nbList,mFlagRefresh);
					adapter.notifyDataSetChanged();
					if(sb.length()>1){
						nids=sb.substring(0, sb.length()-1);
					}
				}
				
				getServerList(nids);
			}
		});
		*/
	}

	//专题子分类列表
	public void getDbList(final SubjectItemColumnsBean columnid) {
		LogUtils.i("page-->" + page + "  pageSize-->" + pageSize);
		newsListDbTask.findList(columnid.getCid(), page, pageSize, new I_SetList<NewsBeanDB>() {
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

					adapter.appendData(columnid, nbList, mFlagRefresh);
					adapter.notifyDataSetChanged();
					if (sb.length() > 1) {
						nids = sb.substring(0, sb.length() - 1);
					}
				}

				getServerList(columnid, nids);
			}
		});
	}

	//获取新闻list
	@Override
	public void getServerList(String nids) {

	}


	//获取专题子分类list
	public void getServerList(final SubjectItemColumnsBean columnid, String nids) {
		LogUtils.i("nids-->" + nids);
		String station= SharePreferecesUtils.getParam(ZhuanTiActivity.this, StationConfig.STATION, "def").toString();
		String siteid=null;
		String NEWSLIST_url =null;
		if (station.equals(StationConfig.DEF)){
			siteid=InterfaceJsonfile.SITEID;
			NEWSLIST_url =InterfaceJsonfile.NEWSLIST;
		}else if (station.equals(StationConfig.YN)){
			siteid= InterfaceJsonfile_YN.SITEID;
			NEWSLIST_url = InterfaceJsonfile_YN.NEWSLIST;
		}else if (station.equals(StationConfig.TW)){
			siteid= InterfaceJsonfile_TW.SITEID;
			NEWSLIST_url = InterfaceJsonfile_TW.NEWSLIST;
		}
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", siteid);
		params.addBodyParameter("columnid", columnid.getCid());
		params.addBodyParameter("nids", nids);
		params.addBodyParameter("Page", "" + page);
		params.addBodyParameter("PageSize", "" + pageSize);
		params.addBodyParameter("update_time", spu.getCacheUpdatetime());

		httpUtils.send(HttpMethod.POST
				, NEWSLIST_url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("getServerList-->" + responseInfo.result);
				zhuanti_item_sv.onRefreshComplete();

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
								setData(columnid, obj);
							}
						});
					} else {
						setData(columnid, obj);
					}
				} else {
					TUtils.toast(getString(R.string.toast_cannot_connect_network));
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				zhuanti_item_sv.onRefreshComplete();
				zhuanti_item_sv.setMode(Mode.PULL_FROM_START);
				zhuanti_item_iv.setImageResource(R.drawable.zqzx_nonetwork);
			}
		});
	}

	//服务端返回数据处理
	@Override
	public void setData(JSONObject obj) {

	}

	public void setData(final SubjectItemColumnsBean columnid, JSONObject obj) {
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
						newsListDbTask.findList(columnid.getCid(), page, pageSize
								, new I_SetList<NewsBeanDB>() {
							@Override
							public void setList(List<NewsBeanDB> list) {

								List<NewsBean> nbList = new ArrayList<NewsBean>();
								if (null != list) {
									LogUtils.i("newsListDbTask.findList-->" + list.size());
									for (NewsBeanDB nbdb : list) {
										nbList.add(nbdb.getNewsBean());
									}

									adapter.appendData(columnid, nbList, mFlagRefresh);
									adapter.notifyDataSetChanged();
									zhuanti_item_sv.setMode(Mode.BOTH);
								}
							}
						});
					}
				});
			}
			break;
			case 201: {
				zhuanti_item_sv.setMode(Mode.PULL_FROM_START);
			}
			break;
			case 202: {
				zhuanti_item_sv.setMode(Mode.PULL_FROM_START);
				TUtils.toast(getString(R.string.pull_to_refresh_reached_end));
			}
			break;
			case 209: {
				zhuanti_item_sv.setMode(Mode.BOTH);
			}
			break;

			default: {
				TUtils.toast(obj.getString("msg"));
			}
			break;
		}
	}

	private void getCounts(String nid) {
		LogUtils.i("nid-->" + nid);
		String station= SharePreferecesUtils.getParam(ZhuanTiActivity.this, StationConfig.STATION, "def").toString();
		String subjectNum_url =null;
		if (station.equals(StationConfig.DEF)){
			subjectNum_url =InterfaceJsonfile.subjectNum;
		}else if (station.equals(StationConfig.YN)){
			subjectNum_url = InterfaceJsonfile_YN.subjectNum;
		}else if (station.equals(StationConfig.TW)){
			subjectNum_url = InterfaceJsonfile_TW.subjectNum;
		}
		RequestParams params = new RequestParams();
		params.addBodyParameter("nids", nid);

		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.POST
				, subjectNum_url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String json = responseInfo.result;
				LogUtils.i("getCounts-->" + json);

				JSONObject obj = FjsonUtil
						.parseObject(responseInfo.result);
				if (null == obj) {
					return;
				}

				if (200 == obj.getIntValue("code")) {
					List<SubjectNumber> list = FjsonUtil.parseArray(obj.getString("data"), SubjectNumber.class);
					for (SubjectNumber num : list) {
						if (nb.getNid().equals(num.getNid())) {
							zhuanti_tv_con.setVisibility(View.VISIBLE);
							zhuanti_tv_con.setText(getString(R.string.prompt_reports, num.getNum()));
						}
					}
				}

			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
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