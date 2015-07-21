package com.hzpd.hflt.wxapi;

import android.content.Context;
import android.os.Handler;

import com.hzpd.hflt.R;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.util.LogUtils;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;


public class SharedUtil {
	private static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (112 == msg.what) {
				TUtils.toast("分享失败");
			} else if (113 == msg.what) {
				TUtils.toast("分享成功");
				EventUtils.sendShareAtival(App.getInstance());
			} else if (114 == msg.what) {
				TUtils.toast("分享取消");
			}
		}
	};

	public static void showShares(boolean silent, String platform
			, String title, String link, String imagePath
			, final Context context) {
		OnekeyShare oks = new OnekeyShare();

		oks.setTheme(OnekeyShareTheme.CLASSIC);
		if (null == link || "".equals(link)) {
			link = context.getString(R.string.shared_site);
		}
		LogUtils.e("link-->" + link);
		String data = title + link;
//		oks.setAddress("");

		oks.setTitle(title);
		oks.setTitleUrl(link);
		oks.setUrl(link);// 分享内容的url、在微信和易信中也使用为视频文件地址
		oks.setText(data);
		if (null != imagePath) {
			oks.setImageUrl(imagePath);//待分享的网络图片
		} else {
			oks.setImageUrl(context.getString(R.string.shared_img));
		}
		//

		oks.setSilent(silent);
		if (platform != null) {
			oks.setPlatform(platform);
		}

		oks.setCallback(new PlatformActionListener() {
			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {
				handler.sendEmptyMessage(112);
			}

			@Override
			public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
				handler.sendEmptyMessage(113);
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				handler.sendEmptyMessage(114);
			}
		});
		// 在自动授权时可以禁用SSO方式
		oks.disableSSOWhenAuthorize();
		oks.show(context);
	}

	public static void showImgShares(boolean silent, String platform
			, String title, String imagePath, String nid, Context context) {
		OnekeyShare oks = new OnekeyShare();
		oks.setTheme(OnekeyShareTheme.CLASSIC);

		oks.setTitle(title);
		String link = InterfaceJsonfile.ROOT + "index.php?s=/Public/photoview/id/" + nid;
		oks.setTitleUrl(link);
		oks.setText(title + link);
		if (null != imagePath) {
			oks.setImageUrl(imagePath);
		} else {
			oks.setImageUrl(context.getString(R.string.shared_img));
		}
		//
		oks.setUrl(link);

		oks.setSilent(silent);
		if (platform != null) {
			oks.setPlatform(platform);
		}
		oks.setCallback(new PlatformActionListener() {
			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {
				handler.sendEmptyMessage(112);
			}

			@Override
			public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
				handler.sendEmptyMessage(113);
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				handler.sendEmptyMessage(114);
			}
		});
		// 在自动授权时可以禁用SSO方式
		oks.disableSSOWhenAuthorize();
		oks.show(context);
	}

}
