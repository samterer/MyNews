package com.hzpd.services;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.modle.Adbean;
import com.hzpd.modle.CacheBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.ui.App;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.DataCleanManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SerializeUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class InitService extends IntentService {

	public static final String InitAction = "initService";

	private HttpUtils httpUtils;
	private String rootPath;

	public InitService() {
		super("InitService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		httpUtils = new HttpUtils();
		rootPath = App.getInstance().getJsonFileCacheRootDir();
		LogUtils.i("InitService onCreate");
	}

	private void getAppModify() {
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("update_time", SPUtil.getInstance()
				.getCacheUpdatetime());

		ResponseStream rs = null;
		try {
			rs = httpUtils.sendSync(HttpMethod.POST, InterfaceJsonfile.CACHE,
					params);
		} catch (HttpException e) {
			e.printStackTrace();
		}

		if (null == rs) {
			return;
		}
		String responseResult = null;
		try {
			responseResult = rs.readString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (TextUtils.isEmpty(responseResult)) {
			return;
		}

		LogUtils.i("getAppModify-->" + responseResult);

		JSONObject obj = FjsonUtil.parseObject(responseResult);
		if (null != obj) {
			SPUtil.getInstance().setCacheUpdatetime(
					obj.getString("update_time"));
			switch (obj.getIntValue("code")) {
				// TODO 为什么要删除？
				case 200: {
					List<CacheBean> cacheList = FjsonUtil.parseArray(
							obj.getString("data"), CacheBean.class);
					DataCleanManager dcm = new DataCleanManager();
					dcm.deleteDb(cacheList, InitService.this, new I_Result() {
						@Override
						public void setResult(Boolean flag) {
							LogUtils.i("cacheList clean " + flag);
						}
					});
				}
				break;
				case 209: {// 初始化

				}
				break;
				case 201: {// 超时一个月清缓存
					startService(new Intent(InitService.this,
							ClearCacheService.class));
				}
				break;
				default: {
					LogUtils.i(obj.getString("msg"));
				}
				break;
			}

		} else {
			LogUtils.i("服务器错误");
		}
	}

	// 新闻频道更新
	private void updateChannel() {
		LogUtils.i("update channel");
		final File target = App.getFile(rootPath + File.separator + "News");

		HttpHandler<File> handler = httpUtils.download(
				InterfaceJsonfile.CHANNELLIST + "News",
				target.getAbsolutePath(), new RequestCallBack<File>() {
					@Override
					public void onSuccess(ResponseInfo<File> responseInfo) {
						String data = App.getFileContext(responseInfo.result);
						if (data != null) {
							LogUtils.i("Channel result-->" + data);
							JSONObject obj = null;
							try {
								obj = JSONObject.parseObject(data);
							} catch (Exception e) {
								return;
							}

							JSONArray array = obj.getJSONArray("data");
							List<NewsChannelBean> mList = FjsonUtil.parseArray(
									array.toJSONString(), NewsChannelBean.class);
							File title = new File(App.getInstance()
									.getJsonFileCacheRootDir()
									+ File.separator
									+ App.mTitle);
							SerializeUtil<List<NewsChannelBean>> mSu = new SerializeUtil<List<NewsChannelBean>>();
							List<NewsChannelBean> stl = mSu
									.readyDataToFile(title.getAbsolutePath());
							HashMap<String, NewsChannelBean> saveTitleMap = new HashMap<String, NewsChannelBean>();

							if (null == stl || stl.size() < 1) {
								LogUtils.i("setTitleData");

								if (mList != null && mList.size() > 0) {
									LogUtils.i("list != null && list.size() > 0");
									mSu.writeDataToFile(mList, App
											.getInstance().getJsonFileCacheRootDir()
											+ File.separator + App.mTitle);
								}
							} else {
								for (NewsChannelBean stb : mList) {
									saveTitleMap.put(stb.getTid(), stb);
								}

								for (int i = 0; i < stl.size(); i++) {
									NewsChannelBean stb = stl.get(i);
									NewsChannelBean stbq = saveTitleMap.get(stb
											.getTid());
									if (null != stbq) {
										stb.setStyle(stbq.getStyle());
										stb.setCnname(stbq.getCnname());
									} else {
										stl.remove(i);
									}
								}
								mSu.writeDataToFile(stl, App.getInstance()
										.getJsonFileCacheRootDir()
										+ File.separator
										+ App.mTitle);
								LogUtils.i("uptate title data to disk!");
							}
						}
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						LogUtils.i("write  channel to file Failed");
					}
				});
	}

	// 开屏图片
	private void GetWelcomePicJson() {
		LogUtils.i("GetWelcomePicJson");

		// 请求网络，获取开屏图片信息
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		ResponseStream rs = null;
		try {
			rs = httpUtils.sendSync(HttpMethod.POST, InterfaceJsonfile.mAdPic, params);
		} catch (HttpException e) {
			e.printStackTrace();
		}

		// 读取请求结果
		if (null == rs) {
			return;
		}
		String response = null;
		try {
			response = rs.readString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (TextUtils.isEmpty(response)) {
			return;
		}
		LogUtils.i("---welcome-->" + response);

		JSONObject object = FjsonUtil.parseObject(response);

		if (null == object) {
			return;
		}

		// 解析JSON
		if (200 == object.getIntValue("code")) {
			JSONObject joData;
			try {
				joData = object.getJSONObject("data");
			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}
			// 缓存json到sd卡
			SPUtil.getInstance().setWelcome(joData);

			// 强制更新
			/**
			 * String oldTime=SPUtil.getInstance().getForceUpdateTime (); String
			 * newTime=obj.getString(""); if(TextUtils.isEmpty(oldTime)
			 * ||-1==App.compareTimeString(oldTime, newTime)){
			 *
			 * SPUtil.getInstance().setForceUpdateTime(newTime);
			 * UmengUpdateAgent.silentUpdate(InitService.this); }
			 */
			// 广告图片
			downloadPic(joData.getString("imgurl"));

			// 登录背景
			downloadPic(joData.getString("loginbg"));

			// 个人信息背景
			downloadPic(joData.getString("userinfobg"));

			// 广告
			List<Adbean> arrayChannel;
			try {
				arrayChannel = JSONObject.parseArray(joData.getString("type"),
						Adbean.class);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			for (Adbean adb : arrayChannel) {
				for (String tid : adb.getTid()) {
					App.getInstance().channelADMap.put(tid, adb);
				}
			}
			List<Adbean> arrayNewsdetail;
			try {
				arrayNewsdetail = JSONObject.parseArray(
						joData.getJSONArray("content").toJSONString(),
						Adbean.class);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			for (Adbean adb : arrayNewsdetail) {
				for (String tid : adb.getTid()) {
					App.getInstance().newsDetailADMap.put(tid, adb);
				}
			}
			LogUtils.i("welcome done GlobalUtils.channelMap-->"
					+ App.getInstance().channelADMap.size() + "\n"
					+ "newsDetailMap-->"
					+ App.getInstance().newsDetailADMap.size());

		}

	}

	@Override
	public void onDestroy() {
		LogUtils.i("initService destroy");
		super.onDestroy();
	}

	private void downloadPic(final String imgUrl) {
		if (TextUtils.isEmpty(imgUrl)) {
			return;
		}
		ImageLoader.getInstance().loadImage(imgUrl, null);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		LogUtils.i("initService");
		if (intent != null) {
			if (InitService.InitAction.equals(intent.getAction())) {
				GetWelcomePicJson();
				getAppModify();
			}
		}
	}
}
