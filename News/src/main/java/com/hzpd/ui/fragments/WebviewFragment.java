package com.hzpd.ui.fragments;

import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.hzpd.hflt.R;
import com.hzpd.ui.App;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.lang.reflect.InvocationTargetException;

public class WebviewFragment extends BaseFragment {

	@ViewInject(R.id.zy_wv_pb)
	private NumberProgressBar zy_wv_pb;
	@ViewInject(R.id.zy_wv_wv)
	private PullToRefreshWebView zy_wv_wv;

	private String url;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.zy_webview_layout, container, false);
		ViewUtils.inject(this, view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);


		Bundle bundle = getArguments();
		if (null == bundle) {
			return;
		}
		url = bundle.getString("url");

		if (TextUtils.isEmpty(url)) {
			return;
		}

		zy_wv_wv.setMode(Mode.DISABLED);
		WebSettings webSettings = zy_wv_wv.getRefreshableView().getSettings();
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

		zy_wv_wv.getRefreshableView().setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				zy_wv_wv.onRefreshComplete();
				zy_wv_pb.setVisibility(View.GONE);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
			                            String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				zy_wv_wv.onRefreshComplete();
				zy_wv_pb.setVisibility(View.GONE);
			}

			@Override
			public void onReceivedSslError(WebView view,
			                               SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				zy_wv_wv.onRefreshComplete();

			}
		});
		zy_wv_wv.getRefreshableView().setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				zy_wv_pb.setVisibility(View.VISIBLE);
				zy_wv_pb.setProgress(newProgress);
				if (newProgress > 95) {
					zy_wv_pb.setVisibility(View.GONE);
				}
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
			                         JsResult result) {
				TUtils.toast(message);
				result.cancel();
				return true;
			}
		});
		zy_wv_wv.setOnRefreshListener(new OnRefreshListener<WebView>() {
			@Override
			public void onRefresh(PullToRefreshBase<WebView> refreshView) {
				zy_wv_wv.getRefreshableView().loadUrl(url);
			}
		});

		zy_wv_wv.getRefreshableView().loadUrl(url);

	}


	public boolean canback() {
		if (zy_wv_wv.getRefreshableView().canGoBack()) {
			zy_wv_wv.getRefreshableView().goBack();
			return true;
		}
		return false;
	}


	@Override
	public void onPause() {
		try {
			zy_wv_wv.getRefreshableView().getClass().getMethod("onPause")
					.invoke(zy_wv_wv.getRefreshableView(), (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			zy_wv_wv.getRefreshableView().getClass().getMethod("onResume")
					.invoke(zy_wv_wv.getRefreshableView(), (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
}
