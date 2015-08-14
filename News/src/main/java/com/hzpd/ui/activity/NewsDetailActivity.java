package com.hzpd.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.avatarqing.loadmore.lib.LoadMoreContainer;
import com.avatarqing.loadmore.lib.LoadMoreHandler;
import com.avatarqing.loadmore.lib.LoadMoreListViewContainer;
import com.hzpd.adapter.CommentListAdapter;
import com.hzpd.custorm.CustomProgressDialog;
import com.hzpd.custorm.CustomScrollView;
import com.hzpd.hflt.R;
import com.hzpd.hflt.wxapi.SharedUtil;
import com.hzpd.modle.CommentsCountBean;
import com.hzpd.modle.CommentzqzxBean;
import com.hzpd.modle.NewDetailOtherBean;
import com.hzpd.modle.NewDetailVedioBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsDetailBean;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.ReplayBean;
import com.hzpd.ui.App;
import com.hzpd.ui.dialog.FontsizePop;
import com.hzpd.ui.dialog.SharePop;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.CODE;
import com.hzpd.utils.Constant;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.GetFileSizeUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.showwebview.JsToJava;
import com.hzpd.utils.showwebview.MyJavascriptInterface;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewsDetailActivity extends MBaseActivity implements OnClickListener {

	private static final String BASEURL = InterfaceJsonfile.ROOT + "index.php?s=/Public/newsview/nid/";
	private PopupWindow mPopupWindow;
	/**
	 * popo的布局
	 */
	private RelativeLayout mRelativeLayoutPopuBig;
	private RelativeLayout mRelativeLayoutPopuCenter;
	private RelativeLayout mRelativeLayoutPopuSmaill;
	private RelativeLayout mRelativeLayoutTitleRoot;
	private boolean mFlagPopuShow;
	private CustomScrollView mLayoutRoot;
	private WebSettings webSettings;
	private LinearLayout mBack;
	private LinearLayout mRoot;
	private LinearLayout mButtomLayout1;// 底部1
	private NewsDetailBean mBean;
	private ListView mCommentListView;
	private LoadMoreListViewContainer mLoadMoreContainer;

	private CommentListAdapter mCommentListAdapter;

	private ImageView news_detail_nonetwork;

	// ---------------------------

	private TextView newdetail_tv_comm;// 评论
	private ImageView newdetail_fontsize;// 字体
	private ImageView newdetail_share;// 分享
	private ImageView newdetail_collection;// 收藏

	private LinearLayout newsdetails_title_comment;// 跳转到评论
	private TextView newsdetails_title_num;
	// -------------------------
	private SharePop shapop;
	private FontsizePop fontpop;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CODE.font_big: {
//					SPUtil.getInstance().setTextSize(CODE.textSize_big);
					setupWebView(CODE.textSize_big);
//					FontSizeEvent event = new FontSizeEvent(CODE.textSize_big);
//					EventBus.getDefault().post(event);
				}
				break;
				case CODE.font_mid: {
//					SPUtil.getInstance().setTextSize(CODE.textSize_normal);
					setupWebView(CODE.textSize_normal);
//					FontSizeEvent event = new FontSizeEvent(CODE.textSize_normal);
//					EventBus.getDefault().post(event);
				}
				break;
				case CODE.font_small: {
//					SPUtil.getInstance().setTextSize(CODE.textSize_small);
					setupWebView(CODE.textSize_small);
//					FontSizeEvent event = new FontSizeEvent(CODE.textSize_small);
//					EventBus.getDefault().post(event);
				}
				break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_details_layout);
		// ViewUtils.inject(this);
		// ----------------------
		getThisIntent();
		initNew();
		// ----------------------
	}

	private String from;
	private String detailPathRoot;
	private NewsBean nb;
	private DbUtils dbUtils;

	private boolean isDetail = false;
	private MyJavascriptInterface jsInterface;// 跳转到图集接口

	private void getThisIntent() {
		Intent intent = getIntent();
		String action = intent.getAction();
		Log.d(getLogTag(), "action:" + action);
		if (null != action && Intent.ACTION_VIEW.equals(action)) {
			Uri uri = intent.getData();
			if (uri != null) {
				nb = new NewsBean();
				String tid = uri.getPath();
				tid = tid.replace(File.separator, "");
				nb.setTid(tid);
				nb.setType("news");
				from = "browser";
				Log.d(getLogTag(), "uri:" + uri + ",tid:" + tid);
				getNewsDetails(nb.getTid());
			}
		} else {
			nb = (NewsBean) intent.getSerializableExtra("newbean");
			LogUtils.i("nit-->" + nb.getNid() + " type-->" + nb.getType() + "  titleid-->" + nb.getTid() + " jsonurl-->"
					+ nb.getJson_url());
			try {
				from = intent.getStringExtra("from");
			} catch (Exception e) {
				from = null;
			}

			try {
				isVideo = intent.getStringExtra("isVideo");
			} catch (Exception e) {
				isVideo = null;
			}
			Log.d(getLogTag(), "from:" + from + ",isVideo:" + isVideo + ",NewsBean:" + nb);
		}
		if ("0".equals(nb.getTid())) {
			detailPathRoot = App.getInstance().getJsonFileCacheRootDir() + File.separator + "subject" + File.separator
					+ "notid" + File.separator;
		} else {
			detailPathRoot = App.getInstance().getJsonFileCacheRootDir() + File.separator + "newsdetail"
					+ File.separator;
		}
		Log.d(getLogTag(), "detailPathRoot->" + detailPathRoot);
	}

	private void initNew() {
		dbUtils = DbUtils.create(this, App.getInstance().getJsonFileCacheRootDir(), App.collectiondbname);
		initViews();
		initPopupWindows();
		if ("browser".equals(from)) {
			getNewsDetails(nb.getTid());
		} else {
			getNewsDetails();
		}
		isCollection();
	}

	private void initViews() {
		mLayoutInflater = LayoutInflater.from(this);

		mLoadMoreContainer = (LoadMoreListViewContainer) findViewById(R.id.load_more_list_view_container);
		mCommentListView = (ListView) findViewById(R.id.comment_listview);
		mWebView = (WebView) findViewById(R.id.webview);
		mRelativeLayoutTitleRoot = (RelativeLayout) findViewById(R.id.news_detail_layout);
		mBack = (LinearLayout) findViewById(R.id.news_detail_bak);
		mRoot = (LinearLayout) findViewById(R.id.news_detail_main_root_id);
		newsdetails_title_num = (TextView) findViewById(R.id.newsdetails_title_num);
		mLayoutRoot = (CustomScrollView) findViewById(R.id.news_detail_root_id);
		newdetail_tv_comm = (TextView) findViewById(R.id.newdetail_tv_comm);
		newdetail_fontsize = (ImageView) findViewById(R.id.newdetail_fontsize);
		newdetail_share = (ImageView) findViewById(R.id.newdetail_share);
		newdetail_collection = (ImageView) findViewById(R.id.newdetail_collection);
		news_detail_nonetwork = (ImageView) findViewById(R.id.news_detail_nonetwork);
		newsdetails_title_comment = (LinearLayout) findViewById(R.id.newsdetails_title_comment);
		mButtomLayout1 = (LinearLayout) findViewById(R.id.news_detail_ll_bottom1);

		mBack.setOnClickListener(this);
		newdetail_tv_comm.setOnClickListener(this);
		newdetail_fontsize.setOnClickListener(this);
		newdetail_share.setOnClickListener(this);
		newdetail_collection.setOnClickListener(this);
		newsdetails_title_comment.setOnClickListener(this);

		if ("yes".equals(isVideo)) {
			newdetail_collection.setVisibility(View.GONE);
			newdetail_share.setVisibility(View.GONE);
		}

		initCommentListView();
	}

	private void initCommentListView() {
		// 添加一个占位的HeaderView，避免ListView无任何子View
		TextView headerView = new TextView(this);
		ListView.LayoutParams lp = new AbsListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, 1);
		headerView.setLayoutParams(lp);
		mCommentListView.addHeaderView(headerView);

		// 加载更多容器的设置
		mLoadMoreContainer.useDefaultHeader();
		mLoadMoreContainer.setLoadMoreHandler(new LoadMoreHandler() {
			@Override
			public void onLoadMore(LoadMoreContainer loadMoreContainer) {
				getLatestComm();
			}
		});

		// 适配器设置
		mCommentListAdapter = new CommentListAdapter();
		mCommentListView.setAdapter(mCommentListAdapter);
	}

	private void initPopupWindows() {
		View mPopupMenu = LayoutInflater.from(this).inflate(R.layout.text_size_popu_layout, null);
		mPopupWindow = new PopupWindow(mPopupMenu, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
		mPopupWindow.setBackgroundDrawable(dw);
		mPopupWindow.setOutsideTouchable(true);

		mRelativeLayoutPopuBig = (RelativeLayout) mPopupMenu.findViewById(R.id.news_textsize_big_id);
		mRelativeLayoutPopuCenter = (RelativeLayout) mPopupMenu.findViewById(R.id.text_size_popu_center_root_id);
		mRelativeLayoutPopuSmaill = (RelativeLayout) mPopupMenu.findViewById(R.id.text_size_popu_smail_root_id);
		mRelativeLayoutPopuBig.setOnClickListener(this);
		mRelativeLayoutPopuCenter.setOnClickListener(this);
		mRelativeLayoutPopuSmaill.setOnClickListener(this);
	}

	private void dismissPopupWindows() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
			mFlagPopuShow = false;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.news_textsize_big_id:
//				spu.setTextSize(CODE.textSize_big);
				setupWebView(CODE.textSize_big);
				dismissPopupWindows();
				break;
			case R.id.text_size_popu_center_root_id:
//				spu.setTextSize(CODE.textSize_normal);
				setupWebView(CODE.textSize_normal);
				dismissPopupWindows();
				break;
			case R.id.text_size_popu_smail_root_id:
//				spu.setTextSize(CODE.textSize_small);
				setupWebView(CODE.textSize_small);
				dismissPopupWindows();
				break;
			case R.id.news_detail_bak:
				this.finish();
				break;
			case R.id.newsdetails_title_comment: {
				// 跳转到评论页
				if (null == nb) {
					return;
				}

				String img = "";
				if (null != nb.getImgs() && nb.getImgs().length > 0) {
					img = nb.getImgs()[0];
				}
				ReplayBean bean = new ReplayBean(nb.getNid(), nb.getTitle(),
						Constant.TYPE.News.toString(), nb.getJson_url(), img);

				Bundle bundle = new Bundle();
				bundle.putSerializable("reply", bean);
				Intent intent = new Intent(NewsDetailActivity.this, XF_NewsCommentsActivity.class);
//			Intent intent = new Intent(NewsDetailActivity.this, CheckCommenthotActivity.class);
				intent.putExtras(bundle);

				LogUtils.i("nit-->" + nb.getNid() + "  mBean.getLink()-->" + mBean.getLink());
				startActivity(intent);
				AAnim.ActivityStartAnimation(NewsDetailActivity.this);

			}
			break;
			case R.id.newdetail_tv_comm: {
//			Log.i("comflag", nb.getComflag());
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
					ReplayBean bean = new ReplayBean(nb.getNid(), nb.getTitle(), "News", nb.getJson_url(), smallimg);
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
				SharedUtil.showShares(true, null, mBean.getTitle(), BASEURL + mBean.getNid(), imgurl, this);

			}
			break;
			case R.id.newdetail_fontsize: {
				if (null == fontpop) {
					View view = this.getLayoutInflater().inflate(R.layout.nd_fontsize_pop, null);
					fontpop = new FontsizePop(view, handler);
					fontpop.showAsDropDown(mButtomLayout1, 0, -mButtomLayout1.getHeight() * 2 - 20);
				} else {
					if (fontpop.isShowing()) {
						fontpop.dismiss();
					} else {
						fontpop.showAsDropDown(mButtomLayout1, 0, -mButtomLayout1.getHeight() * 2 - 20);
					}
				}
			}
			break;
			case R.id.newdetail_collection: {
				addCollection();
			}
			break;
		}
	}

	private CustomProgressDialog dialog;

	private void showDialog() {
		dialog = CustomProgressDialog.createDialog(this, false);
		dialog.show();
	}

	private void webViewChangeProgress(final WebView webview) {
		webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setDomStorageEnabled(true);

		webSettings.setAppCacheEnabled(true);
		webSettings.setAppCachePath(App.getInstance().getAllDiskCacheDir());
		webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webSettings.setAllowFileAccess(true);

		jsInterface = new MyJavascriptInterface(this);
		// 添加js交互接口类，并起别名 imagelistner
		webview.addJavascriptInterface(jsInterface, "imagelistner");
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				isDetail = true;
				view.loadUrl(url);
				return true;

				// Uri uri = Uri.parse(url);
				// Intent it = new Intent(Intent.ACTION_VIEW, uri);
				// startActivity(it);
				// return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (!isDetail) {
					JsToJava jt = new JsToJava(webview);
					// jt.addImageClickListner();
					setOtherNews();
				}
			}
		});

		webview.setHorizontalScrollBarEnabled(false);
	}

	/** 容纳标题栏、视频、WebView */
	private TextView title;
	private TextView time;

	private WebView mWebView;

	/**
	 * 设置webview的字体大小
	 *
	 * @param textSize
	 */
	private void setupWebView(int textSize) {
		webViewChangeProgress(mWebView);
		if (mBean != null) {
			setContentData(textSize);
		}
	}

	private void setContentData(int textSize) {
		String content = mBean.getContent();

		mWebView.loadDataWithBaseURL(null, formatStringToHtml(content, textSize), "text/html", "utf-8", null);

		// 标题栏
		title = (TextView) findViewById(R.id.news_title);
		title.setText(mBean.getTitle());
		String au = "";
		if (null != mBean.getAuthorname() && !"".equals(mBean.getAuthorname())) {
			au = mBean.getAuthorname() + "\t";
		}
		time = (TextView) findViewById(R.id.news_time);
		time.setText(au + mBean.getCopyfrom() + "（" + mBean.getUpdate_time() + "）");
		// TODO 添加视频
//		addVedioView();

		jsInterface.setNewsDetailBean(mBean);

		EventUtils.sendReadAtical(activity);
	}

	private List<NewDetailOtherBean> listTemp;

	private LayoutInflater mLayoutInflater;
	private String isVideo;

	private void setDetailOther() {
		// TODO 相关新闻
		listTemp = mBean.getRealtion();
		if (listTemp != null && listTemp.size() > 0) {
			TextView txt = new TextView(this);
			txt.setText(R.string.prompt_relate_news);
			txt.setTextSize(22);
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			txt.setLayoutParams(lp);
//			root.addView(txt);
			for (int i = 0; i < listTemp.size(); i++) {
				View view = mLayoutInflater.inflate(R.layout.news_detail_other_layout, null);
				TextView title = (TextView) view.findViewById(R.id.news_detail_other_title_id);
				LinearLayout otherRoot = (LinearLayout) view.findViewById(R.id.news_detail_other_title_root);
				ImageView img = (ImageView) view.findViewById(R.id.other_detail_id);

				title.setText(listTemp.get(i).getTitle());
				otherRoot.setOnClickListener(new MyOtherClickListener(i));
//				root.addView(view);
			}
		}
		int h = MyCommonUtil.getDensityRatio(this) * 50;

		View view = new View(App.getInstance());
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, h);
		view.setLayoutParams(lp);
//		root.addView(view);
	}

	private void addVedioView() {
		LogUtils.i("add video");
		final NewDetailVedioBean bean = mBean.getVideo();
		if (bean != null) {
			View view = mLayoutInflater.inflate(R.layout.news_detail_vedio_layout, mWebView, false);
			ImageView img = (ImageView) view.findViewById(R.id.news_detail_vedio_img_id);
			int videoheight = MyCommonUtil.getDisplayMetric(getResources()).widthPixels / 16 * 9;
			img.setLayoutParams(new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, videoheight));
			mImageLoader.displayImage(bean.getMainpic(), img, DisplayOptionFactory.getOption(OptionTp.Big));
			img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(NewsDetailActivity.this, VideoPlayerActivity.class);

					LogUtils.i("path-->" + bean.getVideourl());
					LogUtils.i("title-->" + bean.getVideourl());

					intent.putExtra("path", bean.getVideourl());
					intent.putExtra("title", bean.getVideourl());
					intent.putExtra("from", "newsdetail");
					startActivity(intent);
					AAnim.ActivityStartAnimation(NewsDetailActivity.this);

				}
			});
//			root.addView(view);
		}
	}

	private String formatStringToHtml(String content, int textSize) {
		String data = "";
		if (mBean != null) {
			if (null != content && !"".equals(content)) {
				switch (textSize) {
					case CODE.textSize_small:
						data = content.replaceAll("<p>",
								"<p style=\"color:#231815;font-size:14px;text-indent: 0em;line-height: 1.55em;margin-bottom: 0.5em;" +
										"letter-spacing:0" +
										".05em\">");
						break;
					case CODE.textSize_normal:
						data = content.replaceAll("<p>",
								"<p style=\"color:#231815;font-size:18px;text-indent: 0em;line-height: 1.55em;margin-bottom: 0.5em;" +
										"letter-spacing:0" +
										".05em\">");
						break;
					case CODE.textSize_big:
						data = content.replaceAll("<p>",
								"<p style=\"color:#231815;font-size:26px;text-indent: 0em;line-height: 1.55em;margin-bottom: 0.5em;" +
										"letter-spacing:0" +
										".05em;\">");
						break;
				}
			}
		}
		return data;
	}

	private int mCurPage = 0;
	private int mPageSize = 20;

	private void getLatestComm() {
		if (mBean == null) {
			Log.d(getLogTag(), "mBean is null");
			return;
		}

		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("Page", "" + mCurPage);
		params.addBodyParameter("PageSize", "" + mPageSize);
		params.addBodyParameter("nid", mBean.getNid());
		params.addBodyParameter("type", "News");
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

		String url = InterfaceJsonfile.mLatestComm;
		// TODO 测试地址
		boolean test = true;
		if (test) {
			url = "http://api.locktheworld.com/custom/comments.php";
		}
		httpUtils.send(HttpMethod.POST
				, url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				parseCommentJson(responseInfo.result);
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				TUtils.toast(getString(R.string.toast_server_no_response));
				mLoadMoreContainer.loadMoreFinish(true, false);
			}
		});
	}

	private void parseCommentJson(String json) {
		mCurPage++;
		Log.d(getLogTag(), "Latest Comment Json->" + json);
		JSONObject obj = null;
		try {
			obj = JSONObject.parseObject(json);
		} catch (Exception e) {
			return;
		}
		boolean hasMore = true;
		if (obj != null && 200 == obj.getIntValue("code")) {
			ArrayList<CommentzqzxBean> latestList = (ArrayList<CommentzqzxBean>) JSONArray.parseArray(
					obj.getString("data"), CommentzqzxBean.class);
			Log.d(getLogTag(), "latest comment count:" + latestList.size());
			Log.d(getLogTag(), "latest comment list:" + latestList);
			// 添加数据到Adpater
			mCommentListAdapter.appendData(latestList);
			hasMore = true;
		} else {
			hasMore = false;
			TUtils.toast(obj.getString("msg"));
		}
		boolean emptyResult = mCommentListAdapter.isEmpty();
		mLoadMoreContainer.loadMoreFinish(emptyResult, hasMore);
	}

	private void getNewsDetails() {
		File pageFile = App.getFile(detailPathRoot + "detail_" + nb.getNid());

		if (GetFileSizeUtil.getInstance().getFileSizes(pageFile) > 30) {

			String data = App.getFileContext(pageFile);
			LogUtils.i("data-->2" + data);
			JSONObject obj = JSONObject.parseObject(data);
			mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), NewsDetailBean.class);
			Log.d(getLogTag(), "" + mBean);

			int textSize = spu.getTextSize();

			setupWebView(textSize);
			mRoot.setVisibility(View.VISIBLE);
			mLayoutRoot.setVisibility(View.VISIBLE);
			mButtomLayout1.setVisibility(View.VISIBLE);
			news_detail_nonetwork.setVisibility(View.GONE);

			getCommentsCounts();

			getLatestComm();
			return;
		}

		httpUtils.download(nb.getJson_url(), detailPathRoot + "detail_" + nb.getNid(), new RequestCallBack<File>() {
			@Override
			public void onStart() {
				super.onStart();
				showDialog();
			}

			@Override
			public void onSuccess(ResponseInfo<File> responseInfo) {
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				String data = App.getFileContext(responseInfo.result);
				Log.d(getLogTag(), "data->" + data);
				if (TextUtils.isEmpty(data)) {
					TUtils.toast(getString(R.string.toast_request_failed));
					return;
				}
				LogUtils.i("http data-->2" + data);
				JSONObject obj = FjsonUtil.parseObject(data);
				if (null == obj) {
					responseInfo.result.delete();
					TUtils.toast(getString(R.string.toast_cache_invalidate));
					return;
				}
				mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), NewsDetailBean.class);
				Log.d(getLogTag(), "" + mBean);

				int textSize = spu.getTextSize();

				setupWebView(textSize);
				mRoot.setVisibility(View.VISIBLE);
				mLayoutRoot.setVisibility(View.VISIBLE);
				mButtomLayout1.setVisibility(View.VISIBLE);
				news_detail_nonetwork.setVisibility(View.GONE);

				getCommentsCounts();
				getLatestComm();
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				news_detail_nonetwork.setVisibility(View.VISIBLE);
				TUtils.toast(getString(R.string.toast_server_no_response));
			}
		});
	}

	// 自浏览器打开
	private void getNewsDetails(String nid) {
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("nid", nid);
		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.bnewsItem, params, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("result-->" + responseInfo.result);
				JSONObject obj = null;
				try {
					obj = JSONObject.parseObject(responseInfo.result);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				if (200 == obj.getIntValue("code")) {
					JSONObject object = obj.getJSONObject("data");
					nb = JSONObject.parseObject(object.toJSONString(), NewsBean.class);

					getNewsDetails();

				} else {
					TUtils.toast(obj.getString("msg"));
				}

			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
	}

	@Override
	public void finish() {
		if (null != shapop && shapop.isShowing()) {
			shapop.dismiss();
			return;
		} else if (isDetail) {
			isDetail = false;
			if (null != mWebView) {
				if (mWebView.canGoBack()) {
					mWebView.goBack();
					int textSize = spu.getTextSize();
					setupWebView(textSize);
				}
			}
			return;
		}

		if (!App.isStartApp && ("push".equals(from) || "browser".equals(from))) {
			Intent in = new Intent();
			in.setClass(this, WelcomeActivity.class);
			startActivity(in);
		}

		super.finish();
	}

	class MyOtherClickListener implements OnClickListener {
		private int i;

		public MyOtherClickListener(int i) {
			this.i = i;
		}

		@Override
		public void onClick(View v) {
			Intent in = new Intent();
			Bundle bu = new Bundle();
			bu.putString("nid", listTemp.get(i).getNid());
			bu.putString("type", "1");
			in.putExtras(bu);
			in.setClass(NewsDetailActivity.this, NewsDetailActivity.class);
			startActivity(in);
			AAnim.ActivityStartAnimation(NewsDetailActivity.this);
		}
	}

	private void setOtherNews() {
		final Handler otherHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				setDetailOther();
			}
		};
		otherHandler.sendEmptyMessageDelayed(0, 1000);
	}

	private void popUpwindow() {
		final PopupWindow pinlunpop = new PopupWindow(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		View popRoot = inflater.inflate(R.layout.newsdetail_popupwindow_layout, null);
		pinlunpop.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		pinlunpop.setContentView(popRoot);
		ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
		pinlunpop.setBackgroundDrawable(dw);
		pinlunpop.setOutsideTouchable(true);
		ImageView pop_fenxiang_img = (ImageView) popRoot.findViewById(R.id.pop_fenxiang_img);
		ImageView pop_shoucang_img = (ImageView) popRoot.findViewById(R.id.pop_shoucang_img);
		ImageView pop_ziti_img = (ImageView) popRoot.findViewById(R.id.pop_ziti_img);
		ImageView pop_xiazai_iv = (ImageView) popRoot.findViewById(R.id.pop_xiazai_iv);

		pop_xiazai_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pinlunpop.dismiss();
			}
		});
		pop_fenxiang_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pinlunpop.dismiss();
				// SharedUtil.showShares(true, null, mBean.getTitle(),
				// mBean.getLink(),nb.getSmallimgurl(),NewsDetailActivity.this);
				String imgurl = null;
				if (null != nb.getImgs() && nb.getImgs().length > 0) {
					imgurl = nb.getImgs()[0];
				}
				SharedUtil.showShares(true, null, mBean.getTitle(), BASEURL + mBean.getNid(), imgurl,
						NewsDetailActivity.this);
			}
		});
		pop_shoucang_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pinlunpop.dismiss();
				addCollection();
			}
		});
		pop_ziti_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pinlunpop.dismiss();
				if (mFlagPopuShow) {
					dismissPopupWindows();
				} else {
					// mPopupWindow.showAsDropDown(mPinLun,-30,-15);
				}
				mFlagPopuShow = !mFlagPopuShow;
			}
		});
		// pinlunpop.showAsDropDown(mPinLun, -60,-15);

	}

	// ----------------------
	// 添加收藏
	private void addCollection() {
		if (null == spu.getUser()) {
			NewsItemBeanForCollection nibfc = new NewsItemBeanForCollection(nb);
			try {
				NewsItemBeanForCollection mnbean = dbUtils.findFirst(
						Selector.from(NewsItemBeanForCollection.class).where("colldataid", "=", nb.getNid()));

				if (mnbean == null) {
					dbUtils.save(nibfc);
					TUtils.toast(getString(R.string.toast_collect_success));
					long co = dbUtils.count(NewsItemBeanForCollection.class);
					LogUtils.i("num:" + co);
					LogUtils.i("type-->" + nibfc.getType());
					newdetail_collection.setImageResource(R.drawable.zqzx_collection);
				} else {
					dbUtils.delete(NewsItemBeanForCollection.class, WhereBuilder.b("colldataid", "=", nb.getNid()));
					TUtils.toast(getString(R.string.toast_collect_cancelled));
					newdetail_collection.setImageResource(R.drawable.zqzx_nd_collection);
				}
			} catch (Exception e) {
				e.printStackTrace();
				TUtils.toast(getString(R.string.toast_collect_failed));
			}
			return;
		}

		newdetail_collection.setImageResource(R.drawable.zqzx_nd_collection);

		LogUtils.i("Type-->" + nb.getType() + "  Fid-->" + nb.getNid());
		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("type", "1");
		params.addBodyParameter("typeid", nb.getNid());
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("data", nb.getJson_url());

		LogUtils.i("params-->" + params.toString());

		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.ADDCOLLECTION// InterfaceApi.addcollection
				, params, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("result-->" + responseInfo.result);
				Log.i("result", responseInfo.result);
				JSONObject obj = null;

				try {
					obj = JSONObject.parseObject(responseInfo.result);
					if (200 == obj.getIntValue("code")) {
						JSONObject object = obj.getJSONObject("data");
						// 1:收藏操作成功 2:取消收藏操作成功
						if ("1".equals(object.getString("status"))) {
							newdetail_collection.setImageResource(R.drawable.zqzx_collection);
							TUtils.toast(getString(R.string.toast_collect_success));
						} else {
							newdetail_collection.setImageResource(R.drawable.zqzx_nd_collection);
							TUtils.toast(getString(R.string.toast_collect_cancelled));
						}
					} else {
						TUtils.toast(getString(R.string.toast_collect_failed));
					}
				} catch (Exception e) {
					TUtils.toast(getString(R.string.toast_collect_failed));
					return;
				}

			}

			@Override
			public void onFailure(HttpException error, String msg) {
				TUtils.toast(getString(R.string.toast_cannot_connect_to_server));
			}
		});

	}

	// 是否收藏
	private void isCollection() {
		if (null != spu.getUser()) {
			RequestParams params = RequestParamsUtils.getParamsWithU();
			params.addBodyParameter("typeid", nb.getNid());
			params.addBodyParameter("type", "1");

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
								.and("type", "=", "1"));
				if (null != nbfc) {
					newdetail_collection.setImageResource(R.drawable.zqzx_collection);
				}
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}

	private CommentsCountBean ccBean;

	private void getCommentsCounts() {
		if (null == nb) {
			return;
		}
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("type", Constant.TYPE.NewsA.toString());
		params.addBodyParameter("nids", nb.getNid());
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.commentsConts, params, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("getCommentsCounts-->" + responseInfo.result);
				JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
				if (null == obj) {
					return;
				}
				Log.i("code", obj.getIntValue("code") + "");
				if (200 == obj.getIntValue("code")) {

					List<CommentsCountBean> li = JSONObject.parseArray(obj.getString("data"), CommentsCountBean.class);
					if (null == li) {
						return;
					}

					for (CommentsCountBean cc : li) {
						if (nb.getNid().equals(cc.getNid())) {
							ccBean = cc;
							String snum = cc.getC_num();
							LogUtils.i("count-->" + snum);
							Log.i("count", snum);
							if (TextUtils.isDigitsOnly(snum)) {
								int num = Integer.parseInt(snum);
								if (num > 0) {
									LogUtils.i("count-->" + cc.getC_num());
									newsdetails_title_comment.setVisibility(View.VISIBLE);
									nb.setComcount(cc.getC_num());
									newsdetails_title_num.setText(cc.getC_num());// 设置评论数量
								}
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
				Log.i("failure", msg);
			}
		});
	}

	@Override
	protected void onPause() {
		try {
			mWebView.getClass().getMethod("onPause").invoke(mWebView, (Object[]) null);
		} catch (Exception e) {

		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			mWebView.getClass().getMethod("onResume").invoke(mWebView, (Object[]) null);
		} catch (Exception e) {

		}
	}

}
