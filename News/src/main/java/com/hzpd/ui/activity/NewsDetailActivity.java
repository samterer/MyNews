package com.hzpd.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.custorm.CustomProgressDialog;
import com.hzpd.custorm.VerticalSlideScrollView;
import com.hzpd.hflt.R;
import com.hzpd.hflt.wxapi.SharedUtil;
import com.hzpd.modle.CommentsCountBean;
import com.hzpd.modle.NewDetailOtherBean;
import com.hzpd.modle.NewDetailVedioBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsDetailBean;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.ReplayBean;
import com.hzpd.modle.event.FontSizeEvent;
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
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
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
import java.util.List;

import de.greenrobot.event.EventBus;

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
	private VerticalSlideScrollView mLayoutRoot;
	private WebSettings webSettings;
	private LinearLayout mBack;
	private LinearLayout mRoot;
	private LinearLayout mButtomLayout1;// 底部1
	private NewsDetailBean mBean;
	private ImageView mImageViewBack;

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
					SPUtil.getInstance().setTextSize(CODE.textSize_big);
					setWebViewTextSize(CODE.textSize_big);
					FontSizeEvent event = new FontSizeEvent(CODE.textSize_big);
					EventBus.getDefault().post(event);
				}
				break;
				case CODE.font_mid: {
					SPUtil.getInstance().setTextSize(CODE.textSize_normal);
					setWebViewTextSize(CODE.textSize_normal);
					FontSizeEvent event = new FontSizeEvent(CODE.textSize_normal);
					EventBus.getDefault().post(event);
				}
				break;
				case CODE.font_small: {
					SPUtil.getInstance().setTextSize(CODE.textSize_small);
					setWebViewTextSize(CODE.textSize_small);
					FontSizeEvent event = new FontSizeEvent(CODE.textSize_small);
					EventBus.getDefault().post(event);
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
		if (null != action && Intent.ACTION_VIEW.equals(action)) {
			Uri uri = intent.getData();
			if (uri != null) {
				nb = new NewsBean();
				String tid = uri.getPath();
				tid = tid.replace(File.separator, "");
				nb.setTid(tid);
				nb.setType("news");
				from = "browser";
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

		}
		if ("0".equals(nb.getTid())) {
			detailPathRoot = App.getInstance().getJsonFileCacheRootDir() + File.separator + "subject" + File.separator
					+ "notid" + File.separator;
		} else {
			detailPathRoot = App.getInstance().getJsonFileCacheRootDir() + File.separator + "newsdetail"
					+ File.separator;
		}

	}

	private void initNew() {
		init();

		newdetail_tv_comm = (TextView) findViewById(R.id.newdetail_tv_comm);
		newdetail_fontsize = (ImageView) findViewById(R.id.newdetail_fontsize);
		newdetail_share = (ImageView) findViewById(R.id.newdetail_share);
		newdetail_collection = (ImageView) findViewById(R.id.newdetail_collection);
		news_detail_nonetwork = (ImageView) findViewById(R.id.news_detail_nonetwork);

		if ("yes".equals(isVideo)) {
			newdetail_collection.setVisibility(View.GONE);
			newdetail_share.setVisibility(View.GONE);
		}
		newdetail_tv_comm.setOnClickListener(this);
		newdetail_fontsize.setOnClickListener(this);
		newdetail_share.setOnClickListener(this);
		newdetail_collection.setOnClickListener(this);

		newsdetails_title_comment = (LinearLayout) findViewById(R.id.newsdetails_title_comment);
		newsdetails_title_comment.setOnClickListener(this);
		initPopu();
		mButtomLayout1 = (LinearLayout) findViewById(R.id.news_detail_ll_bottom1);

		if ("browser".equals(from)) {
			getNewsDetails(nb.getTid());
		} else {
			getNewsDetails();
		}

		isCollection();
	}

	private void init() {
		newInstance();
		initRootParams();
		initRootTitle();
		initTextViewContent();
		dbUtils = DbUtils.create(this, App.getInstance().getJsonFileCacheRootDir(), App.collectiondbname);

		mRelativeLayoutTitleRoot = (RelativeLayout) findViewById(R.id.news_detail_layout);
		mBack = (LinearLayout) findViewById(R.id.news_detail_bak);
		mRoot = (LinearLayout) findViewById(R.id.news_detail_main_root_id);
		newsdetails_title_num = (TextView) findViewById(R.id.newsdetails_title_num);

		mLayoutRoot = (VerticalSlideScrollView) findViewById(R.id.news_detail_root_id);
		mImageViewBack = (ImageView) findViewById(R.id.news_detail_bak_img);
		mBack.setOnClickListener(this);

		mLayoutRoot.setVerticalScrollBarEnabled(true);
		mLayoutRoot.setHorizontalScrollBarEnabled(false);

		initPopu();

	}

	private void newInstance() {
		mWebView = new WebView(this);
		root = new LinearLayout(this);
		rootTitle = new LinearLayout(this);
		mLayoutInflater = LayoutInflater.from(this);
	}

	private void initPopu() {
		View mPopupMenu = LayoutInflater.from(this).inflate(R.layout.text_size_popu_layout, null);
		mPopupWindow = new PopupWindow(mPopupMenu, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ColorDrawable dw = new ColorDrawable(-00000);
		mPopupWindow.setBackgroundDrawable(dw);
		mPopupWindow.setOutsideTouchable(true);

		mRelativeLayoutPopuBig = (RelativeLayout) mPopupMenu.findViewById(R.id.news_textsize_big_id);
		mRelativeLayoutPopuCenter = (RelativeLayout) mPopupMenu.findViewById(R.id.text_size_popu_center_root_id);
		mRelativeLayoutPopuSmaill = (RelativeLayout) mPopupMenu.findViewById(R.id.text_size_popu_smail_root_id);
		mRelativeLayoutPopuBig.setOnClickListener(this);
		mRelativeLayoutPopuCenter.setOnClickListener(this);
		mRelativeLayoutPopuSmaill.setOnClickListener(this);

	}

	private void dissmissPopupWindows() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
			mFlagPopuShow = false;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.news_textsize_big_id:
				spu.setTextSize(CODE.textSize_big);
				setWebViewTextSize(CODE.textSize_big);
				dissmissPopupWindows();
				break;
			case R.id.text_size_popu_center_root_id:
				spu.setTextSize(CODE.textSize_normal);
				setWebViewTextSize(CODE.textSize_normal);
				dissmissPopupWindows();
				break;
			case R.id.text_size_popu_smail_root_id:
				spu.setTextSize(CODE.textSize_small);
				setWebViewTextSize(CODE.textSize_small);
				dissmissPopupWindows();
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
					TUtils.toast("请登录");
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

		webview.setVerticalScrollBarEnabled(false);
		webview.setHorizontalScrollBarEnabled(false);

	}

	// 总的布局
	private LinearLayout root;
	private LinearLayout rootTitle;
	private TextView title;
	private TextView time;

	private WebView mWebView;

	/**
	 * 设置webview的字体大小
	 *
	 * @param textSize
	 */
	private void setWebViewTextSize(int textSize) {

		mWebView = null;
		mWebView = new WebView(this);
		mWebView.setBackgroundColor(getResources().getColor(R.color.zqzx_bg_light));

		webViewChangeProgress(mWebView);
		if (mBean != null) {
			initScroolParams();
			setContentData(textSize);
		}

	}

	private void initScroolParams() {
		LinearLayout.LayoutParams rootPa = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mLayoutRoot.setLayoutParams(rootPa);
	}

	private void initRootParams() {
		LinearLayout.LayoutParams rootPa = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setLayoutParams(rootPa);
		root.setPadding(20, 20, 19, 10);
	}

	private void initRootTitle() {
		LinearLayout.LayoutParams rootTitlePa = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		rootTitle.setOrientation(LinearLayout.VERTICAL);
		rootTitle.setLayoutParams(rootTitlePa);
		rootTitle.setPadding(10, 0, 10, 0);
	}

	private void initTextViewContent() {
		title = new TextView(this);
		time = new TextView(this);
		title.setTextSize(20);
		title.setTextColor(getResources().getColor(R.color.zqzx_font_dark));
		time.setTextSize(13);
		time.setTextColor(getResources().getColor(R.color.zqzx_font_normal));
		// time.setGravity(Gravity.CENTER);
		// title.setGravity(Gravity.CENTER);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.bottomMargin = (int) MyCommonUtil.dp2px(getResources(), 10f);
		title.setLayoutParams(params);
		time.setLayoutParams(params);

	}

	private void setContentData(int textSize) {
		mLayoutRoot.removeAllViews();
		root.removeAllViews();
		rootTitle.removeAllViews();
		mWebView.removeAllViews();
		String content = mBean.getContent();

		mWebView.loadDataWithBaseURL(null, formatStringToHtml(content, textSize), "text/html", "utf-8", null);

		title.setText(mBean.getTitle());
		String au = "";
		if (null != mBean.getAuthorname() && !"".equals(mBean.getAuthorname())) {
			au = mBean.getAuthorname() + "\t";
		}
		time.setText(au + mBean.getCopyfrom() + "（" + mBean.getUpdate_time() + "）");

		rootTitle.addView(title);
		rootTitle.addView(time);
		root.addView(rootTitle);
		addVedioView();
		root.addView(mWebView);
		mLayoutRoot.addView(root);

		jsInterface.setNewsDetailBean(mBean);

		EventUtils.sendReadAtical(activity);
	}

	private List<NewDetailOtherBean> listTemp;

	private LayoutInflater mLayoutInflater;
	private String isVideo;

	private void setDetailOther() {
		listTemp = mBean.getRealtion();
		if (listTemp != null && listTemp.size() > 0) {
			TextView txt = new TextView(this);
			txt.setText("   相关新闻");
			txt.setTextSize(22);
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			txt.setLayoutParams(lp);
			root.addView(txt);
			for (int i = 0; i < listTemp.size(); i++) {
				View view = mLayoutInflater.inflate(R.layout.news_detail_other_layout, null);
				TextView title = (TextView) view.findViewById(R.id.news_detail_other_title_id);
				LinearLayout otherRoot = (LinearLayout) view.findViewById(R.id.news_detail_other_title_root);
				ImageView img = (ImageView) view.findViewById(R.id.other_detail_id);

				title.setText(listTemp.get(i).getTitle());
				otherRoot.setOnClickListener(new MyOtherClickListener(i));
				root.addView(view);
			}
		}
		int h = MyCommonUtil.getDensityRatio(this) * 50;

		View view = new View(App.getInstance());
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, h);
		view.setLayoutParams(lp);
		root.addView(view);
	}

	private void addVedioView() {
		LogUtils.i("add video");
		final NewDetailVedioBean bean = mBean.getVideo();
		if (bean != null) {
			View view = mLayoutInflater.inflate(R.layout.news_detail_vedio_layout, root, false);
			ImageView img = (ImageView) view.findViewById(R.id.news_detail_vedio_img_id);
			int videoheight = MyCommonUtil.getDisplayMetric(getResources()).widthPixels / 16 * 9;
			img.setLayoutParams(new android.widget.RelativeLayout.LayoutParams(
					android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, videoheight));
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
			root.addView(view);
		}
	}

	private String formatStringToHtml(String content, int textSize) {
		String data = "";
		if (mBean != null) {
			if (null != content && !"".equals(content)) {
				switch (textSize) {
					case CODE.textSize_small:
						data = content.replaceAll("<p>",
								"<p style=\"color:#231815;font-size:14px;text-indent: 0em;line-height: 1.55em;margin-bottom: 0.5em;letter-spacing:0" +
										".05em\">");
						break;
					case CODE.textSize_normal:
						data = content.replaceAll("<p>",
								"<p style=\"color:#231815;font-size:18px;text-indent: 0em;line-height: 1.55em;margin-bottom: 0.5em;letter-spacing:0" +
										".05em\">");
						break;
					case CODE.textSize_big:
						data = content.replaceAll("<p>",
								"<p style=\"color:#231815;font-size:26px;text-indent: 0em;line-height: 1.55em;margin-bottom: 0.5em;letter-spacing:0" +
										".05em;\">");
						break;
				}
			}
		}
		return data;
	}

	private void getNewsDetails() {

		File pageFile = App.getFile(detailPathRoot + "detail_" + nb.getNid());

		if (GetFileSizeUtil.getInstance().getFileSizes(pageFile) > 30) {

			String data = App.getFileContext(pageFile);
			LogUtils.i("data-->2" + data);
			JSONObject obj = JSONObject.parseObject(data);
			mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), NewsDetailBean.class);

			int textSize = spu.getTextSize();

			setWebViewTextSize(textSize);
			mRoot.setVisibility(View.VISIBLE);
			mLayoutRoot.setVisibility(View.VISIBLE);
			mButtomLayout1.setVisibility(View.VISIBLE);
			news_detail_nonetwork.setVisibility(View.GONE);

			getCommentsCounts();
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
				if (TextUtils.isEmpty(data)) {
					TUtils.toast("请求失败");
					return;
				}
				LogUtils.i("http data-->2" + data);
				JSONObject obj = FjsonUtil.parseObject(data);
				if (null == obj) {
					responseInfo.result.delete();
					TUtils.toast("缓存失效，请重新打开");
					return;
				}
				mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), NewsDetailBean.class);

				int textSize = spu.getTextSize();

				setWebViewTextSize(textSize);
				mRoot.setVisibility(View.VISIBLE);
				mLayoutRoot.setVisibility(View.VISIBLE);
				mButtomLayout1.setVisibility(View.VISIBLE);
				news_detail_nonetwork.setVisibility(View.GONE);

				getCommentsCounts();
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				news_detail_nonetwork.setVisibility(View.VISIBLE);
				TUtils.toast("服务器未响应");
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
					setWebViewTextSize(textSize);
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
		ColorDrawable dw = new ColorDrawable(-00000);
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
					dissmissPopupWindows();
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
					TUtils.toast("收藏成功");
					long co = dbUtils.count(NewsItemBeanForCollection.class);
					LogUtils.i("num:" + co);
					LogUtils.i("type-->" + nibfc.getType());
					newdetail_collection.setImageResource(R.drawable.zqzx_collection);
				} else {
					dbUtils.delete(NewsItemBeanForCollection.class, WhereBuilder.b("colldataid", "=", nb.getNid()));
					TUtils.toast("收藏取消");
					newdetail_collection.setImageResource(R.drawable.zqzx_nd_collection);
				}
			} catch (Exception e) {
				e.printStackTrace();
				TUtils.toast("收藏失败");
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
							TUtils.toast("收藏成功");
						} else {
							newdetail_collection.setImageResource(R.drawable.zqzx_nd_collection);
							TUtils.toast("收藏取消");
						}
					} else {
						TUtils.toast("收藏失败");
					}
				} catch (Exception e) {
					TUtils.toast("收藏失败");
					return;
				}

			}

			@Override
			public void onFailure(HttpException error, String msg) {
				TUtils.toast("无法连接到服务器");
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
