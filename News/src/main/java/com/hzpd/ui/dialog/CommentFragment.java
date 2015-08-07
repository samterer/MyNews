package com.hzpd.ui.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.hzpd.hflt.R;
import com.hzpd.modle.ReplayBean;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.LoginActivity;
import com.hzpd.ui.activity.ZQ_ReplyActivity;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.Log;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.lang.reflect.InvocationTargetException;

public class CommentFragment extends BaseFragment {
	private static final String HTMLURL = InterfaceJsonfile.PATH_ROOT + "/Comment/showcommentv3/nid/";

	@ViewInject(R.id.xf_comments_wv_detail)
	private PullToRefreshWebView xf_comments_wv_detail;
	@ViewInject(R.id.xf_newscomm_npb)
	private NumberProgressBar npb;

	@ViewInject(R.id.magazine_tv_comm)
	private TextView magazine_tv_comm;
	private ReplayBean bean;

	private String commentItemCid;
	private String isLoaded = "";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.xf_newscomments_layout, container, false);
		ViewUtils.inject(this, view);
		return view;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initData();
	}

	protected void initData() {

		Bundle bundle = getArguments();
		if (null != bundle) {
			bean = (ReplayBean) bundle.getSerializable("reply");
		}

		xf_comments_wv_detail.setMode(Mode.DISABLED);
		WebSettings webSettings = xf_comments_wv_detail.getRefreshableView().getSettings();
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

		xf_comments_wv_detail.getRefreshableView().setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				TUtils.toast(message);
				result.cancel();
				return true;
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				npb.setVisibility(View.VISIBLE);
				npb.setProgress(newProgress);
				if (newProgress > 95) {
					npb.setVisibility(View.GONE);
				}
			}
		});

		xf_comments_wv_detail.getRefreshableView().setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// xfzj://userinfo/uid/113
				// xfzj://comment?nid=66&uid=1&cid=5&type=News&siteid=1
				if (TextUtils.isEmpty(url)) {
					return true;
				}
				if (url.startsWith("xfzj://")) {
					url = url.replace("xfzj://", "");

					if (url.startsWith("comment?")) {
						url = url.replace("comment?", "");
						LogUtils.i("query-->" + url);
						String[] querys = url.split("&");
						for (String s : querys) {
							String[] kv = s.split("=");
							if ("cid".equals(kv[0])) {
								commentItemCid = kv[1];
								break;
							}
						}

					}

				}

				return true;
			}
		});

		setBean(bean);
	}

	public void setBean(ReplayBean bean) {

		if (null == bean) {
			return;
		}

		this.bean = bean;
		LogUtils.i("nid---" + bean.getId() + " mNewtype-->" + bean.getType());
		String url = HTMLURL + bean.getId() + "/type/" + bean.getType() + "/siteid/" + InterfaceJsonfile.SITEID
				+ "/page/1/pagesize/10";
		if (null != spu.getUser()) {
			url += "/uid/" + spu.getUser().getUid();
		}
		if (isLoaded.equals(url)) {
			return;
		}
		Log.d(getLogTag(), "url-->" + url);
		LogUtils.i("url-->" + url);
		xf_comments_wv_detail.getRefreshableView().loadUrl(url);
		isLoaded = url;
	}


	@OnClick(R.id.magazine_tv_comm)
	private void comment(View view) {

		if (null == spu.getUser()) {
			TUtils.toast(getString(R.string.toast_please_login));
			Intent intent = new Intent(activity, LoginActivity.class);
			startActivity(intent);
			AAnim.ActivityStartAnimation(activity);
			return;
		}
		if (null == bean) {
			return;
		}

		Intent intent = new Intent(activity, ZQ_ReplyActivity.class);
		intent.putExtra("replay", bean);
		startActivity(intent);
		AAnim.bottom2top(activity);

	}

	@Override
	public void onPause() {
		try {
			xf_comments_wv_detail.getRefreshableView().getClass().getMethod("onPause")
					.invoke(xf_comments_wv_detail.getRefreshableView(), (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			xf_comments_wv_detail.getRefreshableView().getClass().getMethod("onResume")
					.invoke(xf_comments_wv_detail.getRefreshableView(), (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

}
