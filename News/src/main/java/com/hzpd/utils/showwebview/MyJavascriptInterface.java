package com.hzpd.utils.showwebview;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.hzpd.modle.NewsDetailBean;
import com.hzpd.ui.activity.NewsAlbumActivity;

public class MyJavascriptInterface {

	private Activity context;
	private NewsDetailBean ndb;

	public MyJavascriptInterface(Activity context) {
		this.context = context;

	}

	public void setNewsDetailBean(NewsDetailBean ndb) {
		this.ndb = ndb;
	}

	@JavascriptInterface
	public void openImage(String img) {
		Intent intent = new Intent();
		if (null == ndb || null == ndb.getPic()) {
			return;
		}

		int position = 0;
		for (int i = 0; i < ndb.getPic().size(); i++) {
			if (img.equals(ndb.getPic().get(i)
					.getSubphoto())) {
				position = i;
				break;
			}
		}
		intent.putExtra("from", "news");
		intent.putExtra("position", position);
		intent.putExtra("ndb", ndb);
		intent.setClass(context, NewsAlbumActivity.class);
		context.startActivity(intent);

	}

	public void openArrayImage(String[] img) {
		for (int i = 0; i < img.length; i++) {
			System.out.println("img-->>" + img[i]);
		}
	}
}