package com.hzpd.utils.showwebview;

import android.webkit.WebView;

public class JsToJava {

	private WebView mWebView;

	public JsToJava(WebView web) {
		this.mWebView = web;
		addImageClickListner();
	}

	// 注入js函数监听
	private void addImageClickListner() {
		// 这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，在还是执行的时候调用本地接口传递url过去
		mWebView.loadUrl("javascript:(function(){"
				+ "var objs = document.getElementsByTagName(\"img\"); "
				+ "for(var i=0;i<objs.length;i++)  " + "{"
				+ "    objs[i].onclick=function()  " + "{"
				+ "        window.imagelistner.openImage(this.src);  "
				+ "}"
				+ "}"
				+ "})()");
	}

}
