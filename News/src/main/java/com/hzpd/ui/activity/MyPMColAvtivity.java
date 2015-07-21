package com.hzpd.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.CollectionAdapter;
import com.hzpd.adapter.PushmsgAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.CollectionDataBean;
import com.hzpd.modle.CollectionJsonBean;
import com.hzpd.modle.Jsonbean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.PushmsgBean;
import com.hzpd.modle.VideoItemBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;
import java.util.List;

/**
 * @author color
 *         推送和收藏
 */

public class MyPMColAvtivity extends MBaseActivity {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;

	@ViewInject(R.id.pushmsg_lv)
	private PullToRefreshListView pushmsg_lv;
	@ViewInject(R.id.pushmsg_tv_empty)
	private TextView pushmsg_tv_empty;

	private int Page = 1;//页数
	private static final int PageSize = 15; //每页大小

	private boolean mFlagRefresh = true;//刷新还是加载

	private PushmsgAdapter pmgadapter;
	private CollectionAdapter colladAdapter;

	private String type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mypushmsg_layout);
		ViewUtils.inject(this);

		init();
	}

	private void init() {
		Intent intent = getIntent();
		if (null == intent) {
			return;
		}
		type = intent.getStringExtra("type");
		LogUtils.i("type-->" + type);

		pushmsg_lv.setEmptyView(pushmsg_tv_empty);
		pushmsg_lv.setMode(Mode.PULL_FROM_START);

		if ("pushmsg".equals(type)) {
			stitle_tv_content.setText("我的消息");
			pushmsg_tv_empty.setText("没有消息");

			pmgadapter = new PushmsgAdapter(this);
			pushmsg_lv.setAdapter(pmgadapter);
		} else {

			stitle_tv_content.setText("我的收藏");
			pushmsg_tv_empty.setText("没有收藏");

			colladAdapter = new CollectionAdapter(this);
			pushmsg_lv.setAdapter(colladAdapter);
			pushmsg_lv.getRefreshableView().setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
				                               View view, int position, long id) {
					LogUtils.i("position-->" + position + " id-->" + id);
					CollectionJsonBean cb = (CollectionJsonBean) colladAdapter.getItem(position - 1);
					deletePop(view, cb, position - 1);
					return true;
				}
			});
		}

		pushmsg_lv.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				LogUtils.i("下拉刷新");
				//下拉刷新

				refreshView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
				Page = 1;
				mFlagRefresh = true;
				if ("pushmsg".equals(type)) {
					getPushmsgInfoFromServer();
				} else {
					colladAdapter.clear();
					getCollectionInfoFromServer();
				}
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				//上拉加载
				LogUtils.i("上拉加载");

//				refreshView.getLoadingLayoutProxy().setLoadingDrawable(
//						getResources().getDrawable(R.drawable.default_ptr_flip));
				refreshView.getLoadingLayoutProxy().setPullLabel("上拉加载更多");
//				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("hh");
				Page++;
				mFlagRefresh = false;
				if ("pushmsg".equals(type)) {
					getPushmsgInfoFromServer();
				} else {
					getCollectionInfoFromServer();
				}
			}
		});

		pushmsg_lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {
				if ("pushmsg".equals(type)) {
					pushmsgItemclick(parent, view, position, id);
				} else {
					mycollectionItemclick(parent, view, position, id);
				}
			}
		});

		pushmsg_lv.postDelayed(new Runnable() {
			@Override
			public void run() {
				pushmsg_lv.setRefreshing(true);
			}
		}, 500);

	}

	private void getPushmsgInfoFromServer() {
		if (null == spu.getUser()) {
			pushmsg_lv.onRefreshComplete();
			TUtils.toast("请登录");
			return;
		}
		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("Page", Page + "");
		params.addBodyParameter("PageSize", PageSize + "");

		httpUtils.send(HttpMethod.POST
				, ""
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				pushmsg_lv.onRefreshComplete();

				LogUtils.i("tsbl--list-->" + responseInfo.result);
				JSONObject obj = null;
				try {
					obj = JSONObject.parseObject(responseInfo.result);
				} catch (Exception e) {
					return;
				}

				if (200 == obj.getIntValue("code")) {
					JSONArray array = obj.getJSONArray("data");
					LogUtils.i("array-->" + array.toJSONString());
					ArrayList<PushmsgBean> list = (ArrayList<PushmsgBean>) JSONArray.parseArray(array.toJSONString(), PushmsgBean.class);
					LogUtils.i("listsize-->" + list.size());

					pmgadapter.appendData(list, mFlagRefresh);

					if (list.size() < PageSize) {
						pushmsg_lv.setMode(Mode.PULL_FROM_START);
					} else {
						pushmsg_lv.setMode(Mode.BOTH);
					}

				} else {
					TUtils.toast(obj.getString("msg"));
					if (!mFlagRefresh) {
						Page--;
					}
				}
				mFlagRefresh = false;
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				Log.i("push", msg);
				pushmsg_lv.onRefreshComplete();
				if (!mFlagRefresh) {
					Page--;
				}
				mFlagRefresh = false;
//					TUtils.toast("无法连接到服务器");
			}
		});
	}

	private void pushmsgItemclick(AdapterView<?> parent, View view,
	                              int position, long id) {
		PushmsgBean pb = (PushmsgBean) pmgadapter.getItem(position - 1);
		Intent intent = new Intent();
		boolean flag = false;//是否是预定类型

		if ("1".equals(pb.getAtype())) {//
			intent.setClass(MyPMColAvtivity.this, NewsDetailActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("nid", pb.getArticleid());//
			mBundle.putString("type", "1");
			mBundle.putString("commentCount", pb.getComcount());
			intent.putExtras(mBundle);
			flag = true;
		}

		if (!flag) {
			return;
		}

		startActivity(intent);
		AAnim.ActivityStartAnimation(activity);
	}

	private void mycollectionItemclick(AdapterView<?> parent, View view,
	                                   int position, long id) {
		CollectionJsonBean cb = (CollectionJsonBean) colladAdapter.getItem(position - 1);
		CollectionDataBean cdb = cb.getData();
		Intent intent = new Intent();
		intent.putExtra("from", "collection");
		boolean flag = false;//是否是预定类型
		//跳转脚标）1新闻  2图集  3视频 4html5
		LogUtils.i("type-->" + cb.getType());
		if ("1".equals(cb.getType())) {//
			intent.setClass(MyPMColAvtivity.this, NewsDetailActivity.class);

			NewsBean nb = new NewsBean();
			nb.setNid(cdb.getId());
			nb.setSid("0");
			nb.setTitle(cdb.getTitle());
			nb.setJson_url(cdb.getJson_url());
			nb.setType(cb.getType());
			nb.setTid(cdb.getTid());
			nb.setUpdate_time(cdb.getTime());
			String imgs[] = new String[3];
			imgs[0] = cdb.getThumb();
			nb.setImgs(imgs);

			intent.putExtra("newbean", nb);

			flag = true;
		} else if ("2".equals(cb.getType())) {
			intent.setClass(MyPMColAvtivity.this, NewsAlbumActivity.class);
			intent.putExtra("pid", cb.getId());
			intent.putExtra("json_url", cb.getData().getJson_url());
			flag = true;
		} else if ("3".equals(cb.getType())) {
			VideoItemBean vib = new VideoItemBean(cb);
			intent.setClass(MyPMColAvtivity.this, VideoPlayerActivity.class);
			intent.putExtra("vib", vib);
			flag = true;
		} else if ("4".equals(cb.getType())) {
			intent.setClass(MyPMColAvtivity.this, HtmlActivity.class);

			NewsBean nb = new NewsBean();
			nb.setNid(cdb.getId());
			nb.setSid("0");
			nb.setComflag("0");
			nb.setTitle(cb.getData().getTitle());
			nb.setJson_url(cb.getData().getJson_url());
			nb.setType(cb.getType());
			nb.setTid(cb.getData().getTid());
			nb.setUpdate_time(cb.getData().getTime());
			String imgs[] = new String[3];
			imgs[0] = cb.getData().getThumb();
			nb.setImgs(imgs);

			intent.putExtra("newbean", nb);

			flag = true;
		}

		if (flag) {
			startActivity(intent);
			AAnim.ActivityStartAnimation(activity);
		}

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pushmsg_lv.onRefreshComplete();

			if (1 == msg.what) {
				List<CollectionJsonBean> list = (List<CollectionJsonBean>) msg.obj;
				colladAdapter.appendData(list, mFlagRefresh);
				colladAdapter.notifyDataSetChanged();
				if (list.size() >= PageSize) {
					pushmsg_lv.setMode(Mode.BOTH);
				} else {
					pushmsg_lv.setMode(Mode.PULL_FROM_START);
				}
			}
			mFlagRefresh = false;
		}
	};

	private void getCollectionInfoFromServer() {
		if (null == spu.getUser()) {//未登录
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						List<NewsItemBeanForCollection> list = dbHelper.getCollectionDBUitls().findAll(Selector
								.from(NewsItemBeanForCollection.class)
								.where("id", "!=", null)
								.orderBy("id", true)
								.limit(PageSize)
								.offset((Page - 1) * PageSize));

						if (null != list) {
							LogUtils.i("list.size-->" + list.size());

							ArrayList<CollectionJsonBean> mlist = new ArrayList<CollectionJsonBean>();
							for (NewsItemBeanForCollection nifc : list) {
								mlist.add(nifc.getCollectionJsonBean());
							}

							Message msg = handler.obtainMessage();
							msg.what = 1;
							msg.obj = mlist;
							handler.sendMessage(msg);
						} else {
							handler.sendEmptyMessage(500);
						}
					} catch (Exception e) {
						e.printStackTrace();
						handler.sendEmptyMessage(500);
					}

				}
			}).start();

			return;
		}
		LogUtils.i("uid-->" + spu.getUser().getUid());

		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("page", Page + "");
		params.addBodyParameter("pagesize", PageSize + "");
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.COLLECTIONLIST//InterfaceApi.collection
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				pushmsg_lv.onRefreshComplete();
				pushmsg_lv.setMode(Mode.PULL_FROM_START);
				LogUtils.i("collection--list-->" + responseInfo.result);
				JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
				if (null == obj) {
					return;
				}
				if (200 == obj.getIntValue("code")) {

					List<CollectionJsonBean> mlist = FjsonUtil.parseArray(obj.getString("data")
							, CollectionJsonBean.class);

					if (null == mlist) {
						return;
					}

					LogUtils.i("listsize-->" + mlist.size());

					colladAdapter.appendData(mlist, mFlagRefresh);
					colladAdapter.notifyDataSetChanged();

					if (mlist.size() >= PageSize) {
						pushmsg_lv.setMode(Mode.BOTH);
					}

				} else {
					TUtils.toast("" + obj.getString("msg"));
					if (!mFlagRefresh) {
						Page--;
					}
				}
				mFlagRefresh = false;
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				pushmsg_lv.onRefreshComplete();
				pushmsg_lv.setMode(Mode.PULL_FROM_START);
				if (!mFlagRefresh) {
					Page--;
				}
				mFlagRefresh = false;
				TUtils.toast("无法连接到服务器");
			}
		});

	}

	private void deletePop(View v, final CollectionJsonBean cb, final int position) {

		final PopupWindow mPopupWindow = new PopupWindow(this);
		LinearLayout pv = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.comment_delete_pop, null);
		ImageView mTwo = (ImageView) pv.findViewById(R.id.comment_delete_img);//删除
		mTwo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
				if (null == spu.getUser()) {
					try {
						if ("2".equals(cb.getType())) {
							dbHelper.getCollectionDBUitls().delete(Jsonbean.class, WhereBuilder.b("fid", "=", cb.getId()));
						}
						dbHelper.getCollectionDBUitls().delete(NewsItemBeanForCollection.class, WhereBuilder.b("colldataid", "=", cb.getData().getId
								()));
						TUtils.toast("删除成功");
						colladAdapter.deleteItem(position);
					} catch (DbException e) {
						e.printStackTrace();
						TUtils.toast("删除失败");
					}
					return;
				}

				RequestParams pa = RequestParamsUtils.getParamsWithU();
				pa.addBodyParameter("id", cb.getId());

				httpUtils.send(HttpMethod.POST
						, InterfaceJsonfile.DELETECOLLECTION//InterfaceApi.deletecollection
						, pa
						, new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0,
					                      String arg1) {
						TUtils.toast("无法连接到服务器");
					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						LogUtils.i("delete reply-->" + arg0.result);
						JSONObject obj = null;
						try {
							obj = JSONObject.parseObject(arg0.result);
						} catch (Exception e) {
							return;
						}
						if (200 == obj.getIntValue("code")) {
							TUtils.toast("删除成功");
							colladAdapter.deleteItem(position);
						} else {
							TUtils.toast(obj.getString("msg"));
						}
					}
				});
			}
		});
		mPopupWindow.setContentView(pv);
		mPopupWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		ColorDrawable dw = new ColorDrawable(-00000);
		mPopupWindow.setBackgroundDrawable(dw);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);

		mPopupWindow.showAsDropDown(v,
				v.getWidth() / 2 - 30,
				-v.getHeight());
	}

	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		finish();
	}


	@Override
	protected void onDestroy() {
		handler.removeCallbacks(null);
		super.onDestroy();
	}
}
