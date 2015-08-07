package com.hzpd.hflt.wxapi;

import android.content.Context;

import com.hzpd.hflt.R;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.util.LogUtils;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;


public class SharedUtil {

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
				TUtils.toast(context.getString(R.string.share_failed));
			}

			@Override
			public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
				TUtils.toast(context.getString(R.string.share_completed));
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				TUtils.toast(context.getString(R.string.share_canceled));
			}
		});
		// 在自动授权时可以禁用SSO方式
		oks.disableSSOWhenAuthorize();
		oks.show(context);
	}

	public static void showImgShares(boolean silent, String platform
			, String title, String imagePath, String nid, final Context context) {
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
				TUtils.toast(context.getString(R.string.share_failed));
			}

			@Override
			public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
				TUtils.toast(context.getString(R.string.share_completed));
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				TUtils.toast(context.getString(R.string.share_canceled));
			}
		});
		// 在自动授权时可以禁用SSO方式
		oks.disableSSOWhenAuthorize();
		oks.show(context);
	}

}