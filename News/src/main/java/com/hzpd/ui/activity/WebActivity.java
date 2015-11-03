package com.hzpd.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.hzpd.hflt.R;
import com.hzpd.ui.App;

/**
 * WevView third url
 */
public class WebActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity_layout);
        checkIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent();
    }

    private void checkIntent() {
        loading = true;
        Intent intent = getIntent();
        url = intent.getStringExtra(KEY_URL);
        if (TextUtils.isEmpty(url)) {
            finish();
        }
        initViews();
    }

    public final static String KEY_URL = "key_url";
    private String url;
    private WebView webView;
    private ProgressBar load_progress_bar;
    private View background_empty;
    private View backView;

    private void initViews() {
        webView = (WebView) findViewById(R.id.webview);
        load_progress_bar = (ProgressBar) findViewById(R.id.load_progress_bar);
        background_empty = findViewById(R.id.background_empty);
        backView = findViewById(R.id.btn_back);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        load_progress_bar.postDelayed(runnable, 50);
        load_progress_bar.setProgress(0);
        webView.loadUrl(url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setDomStorageEnabled(true);

        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(App.getInstance().getAllDiskCacheDir());
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowFileAccess(true);


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                background_empty.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loading = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                background_empty.setVisibility(View.GONE);
                wProgress = 100;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
    }

    boolean loading = true;
    int wProgress = 0;
    int MIDDLE_PROGRESS = 95;
    int progress = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (loading) {
                if (wProgress < MIDDLE_PROGRESS && progress < MIDDLE_PROGRESS) {
                    progress += 1;
                } else if (wProgress > MIDDLE_PROGRESS) {
                    progress += 3;
                    if (progress > 100) {
                        progress = 100;
                        wProgress = 101;
                    }
                }
                if (wProgress > 100) {
                    loading = false;
                    load_progress_bar.setVisibility(View.GONE);
                    return;
                }
                load_progress_bar.setProgress(progress);
                load_progress_bar.postDelayed(runnable, 20);
            }
        }
    };

}
