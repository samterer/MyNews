package com.hzpd.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.hzpd.hflt.R;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class AboutUsActivity extends MBaseActivity {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;
	@ViewInject(R.id.aboutus_wv)
	private PullToRefreshWebView aboutus_wv;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutus_layout);
		ViewUtils.inject(this);
		stitle_tv_content.setText("关于我们");

		aboutus_wv.setMode(Mode.DISABLED);

		WebSettings webSettings = aboutus_wv.getRefreshableView().getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setAppCacheEnabled(true);

		if (MyCommonUtil.isNetworkConnected(activity)) {
			aboutus_wv.getRefreshableView().clearHistory();
			aboutus_wv.getRefreshableView().clearCache(true);
		} else {
			webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		}

		aboutus_wv.getRefreshableView().setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});

		aboutus_wv.getRefreshableView().setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
			                         JsResult result) {
				TUtils.toast(message);
				result.cancel();
				return true;
			}
		});

		aboutus_wv.getRefreshableView().loadUrl(InterfaceJsonfile.ABOUTUS);
	}

	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		finish();
	}

}
