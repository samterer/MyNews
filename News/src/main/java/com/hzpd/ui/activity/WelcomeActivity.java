package com.hzpd.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.services.InitService;
import com.hzpd.ui.App;
import com.hzpd.ui.fragments.welcome.AdFlashFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.GetFileSizeUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.SerializeUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;

/**
 * @author color
 */
public class WelcomeActivity extends MBaseActivity {

	private volatile int done;
	private FragmentManager fm;
	private boolean isFirstStartApp;
	private boolean isJump = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frame_welcome);

		ShareSDK.initSDK(this);
		ShareSDK.removeCookieOnAuthorize(true);

		fm = getSupportFragmentManager();
		FragmentTransaction tran = fm.beginTransaction();
		isFirstStartApp = spu.getIsTodayFistStartApp();
		LogUtils.i("isFirstStartApp->" + isFirstStartApp);

		tran.replace(R.id.welcome_frame, new AdFlashFragment());

		tran.commit();

		getChannelJson();

		// 初始化服务
		Intent service = new Intent(this, InitService.class);
		service.setAction(InitService.InitAction);
		this.startService(service);

		Log.d(getLogTag(), "");
	}

	public void loadMainUI() {
		done++;
		if (done > 1) {
			if (isFirstStartApp) {
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				finish();
			} else {
				if (!isJump) {
					Intent intent = new Intent(this, MainActivity.class);
					startActivity(intent);
					finish();
				} else {
					isJump = true;
				}
			}
		}
	}

	public void getChannelJson() {
		final String channelCachePath = App.getInstance().getJsonFileCacheRootDir()
				+ File.separator
				+ App.mTitle;
		final File channelCacheFile = new File(channelCachePath);
		final File target = App.getFile(App.getInstance().getJsonFileCacheRootDir() + File.separator + "News");

		Log.d(getLogTag(), "channelCacheFile.exists():" + channelCacheFile.exists());
		if (channelCacheFile.exists()) {
			loadMainUI();
		} else {
			String urlChannelList = InterfaceJsonfile.CHANNELLIST + "News";
			httpUtils.download(urlChannelList,
					target.getAbsolutePath(),
					new RequestCallBack<File>() {
						@Override
						public void onSuccess(ResponseInfo<File> responseInfo) {
							LogUtils.i("write  channel to file success");
							String json = App.getFileContext(responseInfo.result);
							if (json != null) {
								LogUtils.i("channel-->" + json);
								JSONObject obj = FjsonUtil.parseObject(json);
								if (null == obj) {
									return;
								}

								// 读取json，获取频道信息
								JSONArray array = obj.getJSONArray("data");
								List<NewsChannelBean> newestChannels = JSONArray
										.parseArray(array.toJSONString(),
												NewsChannelBean.class);

								// 读取频道信息的本地缓存
								SerializeUtil<List<NewsChannelBean>> serializeUtil = new SerializeUtil<List<NewsChannelBean>>();
								List<NewsChannelBean> cacheChannels = serializeUtil
										.readyDataToFile(channelCacheFile.getAbsolutePath());

								// 如果没有缓存
								if (null == cacheChannels || cacheChannels.size() < 1) {
									LogUtils.i("setTitleData");

									if (newestChannels != null && newestChannels.size() > 0) {
										LogUtils.i("list != null && list.size() > 0");

										addLocalChannels(newestChannels);
										// 缓存频道信息到SD卡上
										serializeUtil.writeDataToFile(newestChannels, channelCachePath);
										LogUtils.i("write title data to disk!");
									}
								} else { // 如果有缓存
									HashMap<String, NewsChannelBean> channelMap = new HashMap<String, NewsChannelBean>();
									for (NewsChannelBean stb : newestChannels) {
										channelMap.put(stb.getTid(), stb);
									}

									for (int i = 0; i < cacheChannels.size(); i++) {
										// 缓存的频道信息
										NewsChannelBean cacheChannel = cacheChannels.get(i);
										// 最新获取的频道信息
										NewsChannelBean newestChannel = channelMap.get(cacheChannel.getTid());

										if (null != newestChannel) {
											// 最新的数据中有和缓存中对应的频道，则更新频道信息
											cacheChannel.setStyle(newestChannel.getStyle());
											cacheChannel.setCnname(newestChannel.getCnname());
										} else {
											// 最新的数据中没有和缓存中对应的频道，删除该频道信息
											cacheChannels.remove(i);
										}
									}
									addLocalChannels(cacheChannels);

									// 更新后信息再次保存到SD卡中
									serializeUtil.writeDataToFile(cacheChannels, channelCachePath);

									LogUtils.i("uptate title data to disk!");
								}
							}
							loadMainUI();
						}

						@Override
						public void onFailure(HttpException error, String msg) {
							LogUtils.i("write  channel to file Failed");
							if (GetFileSizeUtil.getInstance().getFileSizes(target) > 10) {
								loadMainUI();
								return;
							}
						}
					});
		}
	}

	public void jump(String url) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (!isJump) {
			loadMainUI();
		} else {
			isJump = false;
		}
	}

	private void addLocalChannels(List<NewsChannelBean> list) {
		NewsChannelBean channelSubject = new NewsChannelBean();
		channelSubject.setTid("" + NewsChannelBean.TYPE_SUBJECT);
		channelSubject.setType(NewsChannelBean.TYPE_SUBJECT);
		channelSubject.setCnname(getString(R.string.menu_subject));
		if (!list.contains(channelSubject)) {
			list.add(0, channelSubject);
			Log.d(getLogTag(), "add channelSubject");
		}

		NewsChannelBean channelVideo = new NewsChannelBean();
		channelVideo.setTid("" + NewsChannelBean.TYPE_VIDEO);
		channelVideo.setType(NewsChannelBean.TYPE_VIDEO);
		channelVideo.setCnname(getString(R.string.menu_video));
		if (!list.contains(channelVideo)) {
			list.add(0, channelVideo);
			Log.d(getLogTag(), "add channelVideo");
		}

		NewsChannelBean channelImageAlbum = new NewsChannelBean();
		channelImageAlbum.setTid("" + NewsChannelBean.TYPE_IMAGE_ALBUM);
		channelImageAlbum.setType(NewsChannelBean.TYPE_IMAGE_ALBUM);
		channelImageAlbum.setCnname(getString(R.string.menu_album));
		if (!list.contains(channelImageAlbum)) {
			list.add(0, channelImageAlbum);
			Log.d(getLogTag(), "add channelImageAlbum");
		}
	}

}