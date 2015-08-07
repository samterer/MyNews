package com.hzpd.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.hzpd.hflt.R;
import com.hzpd.hflt.wxapi.SharedUtil;
import com.hzpd.modle.CommentsCountBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsDetailBean;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.ReplayBean;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.Constant;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
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

import java.io.File;
import java.util.List;

public class HtmlActivity extends MBaseActivity {

	private static final String BASEURL = InterfaceJsonfile.ROOT + "index.php?s=/Public/newsview/nid/";

	@ViewInject(R.id.html_wv)
	private PullToRefreshWebView html_wv;
	@ViewInject(R.id.news_detail_ll_bottom1)
	private LinearLayout mButtomLayout1;// 底部1
	@ViewInject(R.id.newsdetails_title_comment)
	private LinearLayout newsdetails_title_comment;// 跳转到评论
	@ViewInject(R.id.newsdetails_title_num)
	private TextView newsdetails_title_num;
	@ViewInject(R.id.newdetail_tv_comm)
	private TextView newdetail_tv_comm;
	@ViewInject(R.id.newdetail_share)
	private ImageView newdetail_share;
	@ViewInject(R.id.newdetail_collection)
	private ImageView newdetail_collection;

	private String from;
	private NewsBean nb;
	private NewsDetailBean ndb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.htmlactivity_layout);
		ViewUtils.inject(this);

		init();

	}

	private void init() {

		html_wv.setMode(Mode.DISABLED);

		WebSettings webSettings = html_wv.getRefreshableView().getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setAppCacheEnabled(true);
		webSettings.setAppCachePath(App.getInstance().getAllDiskCacheDir());

		if (MyCommonUtil.isNetworkConnected(activity)) {
			webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		} else {
			webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		}
		webSettings.setAllowFileAccess(true);

		html_wv.getRefreshableView().setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				html_wv.onRefreshComplete();

				mButtomLayout1.setVisibility(View.VISIBLE);

				getCommentsCounts();

			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				html_wv.onRefreshComplete();
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				html_wv.onRefreshComplete();
			}
		});
		html_wv.setOnRefreshListener(new OnRefreshListener<WebView>() {
			@Override
			public void onRefresh(PullToRefreshBase<WebView> refreshView) {
				html_wv.getRefreshableView().loadUrl(nb.getJson_url());
			}
		});
		// ======

		Intent intent = getIntent();
		from = intent.getStringExtra("from");
		String action = intent.getAction();
		LogUtils.i("from-->" + from);
		if (null != action && Intent.ACTION_VIEW.equals(action)) {
			Uri uri = intent.getData();
			if (uri != null) {
				nb = new NewsBean();
				String tid = uri.getPath();
				tid = tid.replace(File.separator, "");
				nb.setTid(tid);
				nb.setType("news");
				from = "browser";

				ndb = new NewsDetailBean(nb);
				getNewsDetails(nb.getTid());
			}
		} else {
			nb = (NewsBean) intent.getSerializableExtra("newbean");
			try {
				from = intent.getStringExtra("from");
			} catch (Exception e) {
				from = null;
			}

			LogUtils.i("url-->" + nb.getJson_url());

			String url = nb.getJson_url();
			if (null != spu.getUser()) {
				if (url.contains("?")) {
					url = url + "&uid=" + spu.getUser().getUid();
				} else {
					url = url + "?uid=" + spu.getUser().getUid();
				}
			}
			html_wv.getRefreshableView().loadUrl(url);

			if ("zw".equals(from)) {
				LogUtils.i("from zw");
				getNewsBean(nb.getNid());
			}
		}

		isCollection();
	}

	private void getNewsDetails(String nid) {
		LogUtils.i("url-->" + BASEURL + nid);
		html_wv.getRefreshableView().loadUrl(BASEURL + nid);
	}

	private void getNewsBean(String nid) {
		if (TextUtils.isEmpty(nid)) {
			return;
		}

		LogUtils.i("getNewsBean");
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("nid", nid);
		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.bnewsItem, params, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("result-->" + responseInfo.result);
				JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
				if (null == obj) {

				}
				if (200 == obj.getIntValue("code")) {
					JSONObject object = obj.getJSONObject("data");
					String url = nb.getJson_url();
					nb = JSONObject.parseObject(object.toJSONString(), NewsBean.class);
					nb.setJson_url(url);

				} else {
					TUtils.toast(obj.getString("msg"));
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
	}

	@OnClick(R.id.news_detail_bak)
	private void goback(View v) {
		finish();
	}

	@OnClick({R.id.news_detail_bak, R.id.newsdetails_title_comment, R.id.newdetail_tv_comm, R.id.newdetail_share,
			R.id.newdetail_collection})
	public void myClick(View v) {
		switch (v.getId()) {
			case R.id.news_detail_bak:
				this.finish();
				break;
			case R.id.newsdetails_title_comment: {
				// 跳转到评论页
				if (!MyCommonUtil.isNetworkConnected(this)) {
					TUtils.toast(getString(R.string.toast_check_network));
					return;
				}
				if (null == nb) {
					return;
				}

				Intent intent = new Intent(this, CheckCommenthotActivity.class);
				intent.putExtra("id", nb.getNid());
				intent.putExtra("mNewtype", "Html");

				LogUtils.i("nit-->" + nb.getNid() + "  mBean.getLink()-->" + nb.getJson_url());
				startActivity(intent);
				AAnim.ActivityStartAnimation(this);

			}
			break;
			case R.id.newdetail_tv_comm: {
				if (null == spu.getUser()) {
					TUtils.toast(getString(R.string.toast_please_login));
					Intent intent = new Intent(this, LoginActivity.class);
					startActivity(intent);
					AAnim.ActivityStartAnimation(this);
					return;
				}
				if (null == nb) {
					return;
				}
				if (!"0".equals(nb.getComflag())) {
					String smallimg = "";
					if (null != nb.getImgs() && nb.getImgs().length > 0) {
						smallimg = nb.getImgs()[0];
					}
					ReplayBean bean = new ReplayBean(nb.getNid(), nb.getTitle(), "Html", nb.getJson_url(), smallimg);
					Intent intent = new Intent(this, ZQ_ReplyActivity.class);
					intent.putExtra("replay", bean);
					startActivity(intent);
					AAnim.bottom2top(this);
				}
			}
			break;
			case R.id.newdetail_share: {
				LogUtils.i("click share");
				String imgurl = null;
				if (null != nb.getImgs() && nb.getImgs().length > 0) {
					imgurl = nb.getImgs()[0];
				}

				SharedUtil.showShares(true, null, nb.getTitle(), nb.getJson_url(), imgurl, this);

			}
			break;
			case R.id.newdetail_collection: {
				addCollection();
			}
			break;

		}
	}

	// 是否收藏
	private void isCollection() {
		if (null == spu.getUser()) {
			RequestParams params = RequestParamsUtils.getParamsWithU();
			params.addBodyParameter("typeid", nb.getNid());
			params.addBodyParameter("type", "4");

			httpUtils.send(HttpMethod.POST, InterfaceJsonfile.ISCELLECTION, params, new RequestCallBack<String>() {
				@Override
				public void onSuccess(ResponseInfo<String> responseInfo) {
					LogUtils.i("isCollection result-->" + responseInfo.result);
					JSONObject obj = null;
					try {
						obj = JSONObject.parseObject(responseInfo.result);
					} catch (Exception e) {
						return;
					}

					if (200 == obj.getIntValue("code")) {
						JSONObject object = obj.getJSONObject("data");
						if ("1".equals(object.getString("status"))) {
							newdetail_collection.setImageResource(R.drawable.zqzx_collection);
						}
					}
				}

				@Override
				public void onFailure(HttpException error, String msg) {
					LogUtils.i("isCollection failed");
				}
			});
		} else {
			try {
				NewsItemBeanForCollection nbfc = dbHelper.getCollectionDBUitls()
						.findFirst(Selector.from(NewsItemBeanForCollection.class).where("colldataid", "=", nb.getNid())
								.and("type", "=", "4"));
				if (null != nbfc) {
					newdetail_collection.setImageResource(R.drawable.zqzx_collection);
				}
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}

	// 添加收藏
	private void addCollection() {
		if (null == spu.getUser()) {
			NewsItemBeanForCollection nibfc = new NewsItemBeanForCollection(nb, "");
			try {
				NewsItemBeanForCollection mnbean = dbHelper.getCollectionDBUitls().findFirst(
						Selector.from(NewsItemBeanForCollection.class).where("colldataid", "=", nb.getNid()));
				if (mnbean == null) {
					dbHelper.getCollectionDBUitls().save(nibfc);
					TUtils.toast(getString(R.string.toast_collect_success));
					newdetail_collection.setImageResource(R.drawable.zqzx_collection);
				} else {
					dbHelper.getCollectionDBUitls().delete(NewsItemBeanForCollection.class,
							WhereBuilder.b("colldataid", "=", nb.getNid()));
					TUtils.toast(getString(R.string.toast_collect_cancelled));
					newdetail_collection.setImageResource(R.drawable.zqzx_nd_collection);
				}
			} catch (DbException e) {
				e.printStackTrace();
				TUtils.toast(getString(R.string.toast_collect_failed));
			}
			return;
		}
		LogUtils.i("Type-->" + nb.getType() + "  Fid-->" + nb.getNid());
		RequestParams params = RequestParamsUtils.getParamsWithU();
		///
		params.addBodyParameter("type", "4");
		params.addBodyParameter("typeid", nb.getNid());
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("data", nb.getJson_url());

		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.ADDCOLLECTION// InterfaceApi.addcollection
				, params, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("result-->" + responseInfo.result);
				JSONObject obj = null;

				try {
					obj = JSONObject.parseObject(responseInfo.result);
					if (200 == obj.getIntValue("code")) {
						JSONObject object = obj.getJSONObject("data");
						// 1:收藏操作成功 2:取消收藏操作成功
						if ("1".equals(object.getString("status"))) {
							newdetail_collection.setImageResource(R.drawable.zqzx_collection);
						} else {
							newdetail_collection.setImageResource(R.drawable.zqzx_nd_collection);
						}
					}
				} catch (Exception e) {
					TUtils.toast(getString(R.string.toast_collect_failed));
					return;
				}

				TUtils.toast(obj.getString("msg"));
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				TUtils.toast(getString(R.string.toast_cannot_connect_to_server));
			}
		});
	}

	private void getCommentsCounts() {
		EventUtils.sendReadAtical(activity);
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("type", Constant.TYPE.NewsA.toString());
		params.addBodyParameter("nids", nb.getNid());

		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.commentsConts, params, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("loginSubmit-->" + responseInfo.result);

				JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
				if (null == obj) {
					return;
				}
				if (200 == obj.getIntValue("code")) {

					List<CommentsCountBean> li = JSONObject.parseArray(obj.getString("data"), CommentsCountBean.class);
					if (null == li) {
						return;
					}
					for (CommentsCountBean cc : li) {
						if (nb.getNid().equals(cc.getNid())) {
							String snum = cc.getC_num();
							if (TextUtils.isDigitsOnly(snum)) {
								int num = Integer.parseInt(snum);
								// TUtils.toast(num+"");
								if (num > 0) {
									newsdetails_title_comment.setVisibility(View.VISIBLE);
								}
								nb.setComcount(num + "");
								newsdetails_title_num.setText(cc.getC_num());// 设置评论数量
							}
							nb.setComflag(cc.getComflag());
							if (!"0".equals(cc.getComflag())) {
								newdetail_tv_comm.setVisibility(View.VISIBLE);
							}
						}
					}
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
	}

	protected void onPause() {
		try {
			html_wv.getClass().getMethod("onPause").invoke(html_wv, (Object[]) null);
		} catch (Exception e) {

		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			html_wv.getClass().getMethod("onResume").invoke(html_wv, (Object[]) null);
		} catch (Exception e) {

		}
	}

	@Override
	public void finish() {
		if ("push".equals(from) || "browser".equals(from)) {
			LogUtils.i("push  browser");
			Intent in = new Intent();
			in.setClass(this, WelcomeActivity.class);
			startActivity(in);
		}
		super.finish();
	}

}