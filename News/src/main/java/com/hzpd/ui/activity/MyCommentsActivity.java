package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.MycommentsAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.MycommentsBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.VideoItemBean;
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
import com.lidroid.xutils.view.annotation.event.OnItemClick;

import java.util.List;

public class MyCommentsActivity extends MBaseActivity {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;
	@ViewInject(R.id.pushmsg_lv)
	private PullToRefreshListView pushmsg_lv;
	@ViewInject(R.id.pushmsg_tv_empty)
	private TextView pushmsg_tv_empty;

	private int Page = 1;
	private static final int PageSize = 15;
	private boolean mFlagRefresh;
	private MycommentsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mypushmsg_layout);
		ViewUtils.inject(this);

		stitle_tv_content.setText("我的评论");
		pushmsg_tv_empty.setText("没有评论");
		pushmsg_lv.setEmptyView(pushmsg_tv_empty);
		adapter = new MycommentsAdapter(this);
		pushmsg_lv.setAdapter(adapter);

		pushmsg_lv.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				LogUtils.i("下拉刷新");
				refreshView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
				Page = 1;
				mFlagRefresh = true;
				getInfoFromServer();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				//上拉加载
				LogUtils.i("上拉加载");
				refreshView.getLoadingLayoutProxy().setPullLabel("上拉加载更多");
				Page++;
				mFlagRefresh = false;
				getInfoFromServer();
			}
		});

		pushmsg_lv.postDelayed(new Runnable() {
			@Override
			public void run() {
				pushmsg_lv.setRefreshing(true);
			}
		}, 600);

	}

	@OnItemClick(R.id.pushmsg_lv)
	private void myonItemClick(AdapterView<?> parent, View view,
	                           int position, long id) {

		boolean flag = false;//是否是预定类型
		MycommentsBean bean = (MycommentsBean) adapter.getItem(position - 1);
		Intent intent = new Intent();
		if ("1".equals(bean.getType())) {
			intent.setClass(this, NewsDetailActivity.class);
			NewsBean nb = new NewsBean();
			nb.setNid(bean.getNid());
			nb.setSid("0");
			nb.setTitle(bean.getTitle());
			nb.setJson_url(bean.getUrl());
			nb.setType(bean.getType());
			nb.setRtype(bean.getType());
			nb.setTid(bean.getTid());
			String[] imgs = {bean.getSmallimgurl()};
			nb.setImgs(imgs);
			nb.setUpdate_time(bean.getUpdate_time());
			intent.putExtra("newbean", nb);
			intent.putExtra("from", "mycomments");
			flag = true;

		} else if ("2".equals(bean.getType())) {
			intent.setClass(this, NewsAlbumActivity.class);
			intent.putExtra("from", "collection");
			intent.putExtra("pid", bean.getNid());
			intent.putExtra("json_url", bean.getUrl());
			flag = true;
		} else if ("3".equals(bean.getType())
				|| "7".equals(bean.getType())) {
			intent.setClass(this, HtmlActivity.class);

			NewsBean nb = new NewsBean();
			nb.setNid(bean.getNid());
			nb.setSid("0");
			nb.setTitle(bean.getTitle());
			nb.setJson_url(bean.getUrl());
			nb.setRtype(bean.getType());
			nb.setType("1");
			nb.setTid(bean.getTid());
			String[] imgs = {bean.getSmallimgurl()};
			nb.setImgs(imgs);
			nb.setUpdate_time(bean.getUpdate_time());
			intent.putExtra("newbean", nb);
			intent.putExtra("from", "mycomments");
			flag = true;
		} else if ("4".equals(bean.getType())) {
			intent.setClass(this, VideoPlayerActivity.class);
			VideoItemBean vib = new VideoItemBean();
			vib.setVid(bean.getNid());
			vib.setTitle(bean.getTitle());
			vib.setTime(bean.getUpdate_time());
			;
			vib.setMainpic(bean.getSmallimgurl());
			vib.setJson_url(bean.getUrl());
			intent.putExtra("from", "collection");
			intent.putExtra("vib", vib);
			flag = true;
		}
		if (!flag) {
			return;
		}
		startActivity(intent);
		AAnim.ActivityStartAnimation(this);

	}

	private void getInfoFromServer() {
		if (null == spu.getUser()) {
			pushmsg_lv.onRefreshComplete();
			TUtils.toast("请登录");
			return;
		}
		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("PageSize", PageSize + "");
		params.addBodyParameter("Page", Page + "");
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.myComm
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				pushmsg_lv.onRefreshComplete();
				pushmsg_lv.setMode(Mode.PULL_FROM_START);
				LogUtils.i("data-->" + responseInfo.result);
				JSONObject obj = FjsonUtil.parseObject(responseInfo.result);

				if (null == obj) {
					return;
				}

				if (200 == obj.getIntValue("code")) {
					List<MycommentsBean> mlist = FjsonUtil.parseArray(obj.getString("data"), MycommentsBean.class);
					if (null == mlist) {
						return;
					}
					if (mlist.size() >= PageSize) {
						pushmsg_lv.setMode(Mode.BOTH);
					}
					adapter.appendData(mlist, mFlagRefresh);
					adapter.notifyDataSetChanged();

				} else {
					TUtils.toast(obj.getString("msg"));
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				pushmsg_lv.onRefreshComplete();
				pushmsg_lv.setMode(Mode.PULL_FROM_START);
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
