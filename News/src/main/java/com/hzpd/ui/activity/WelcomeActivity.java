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

	private String welcomeJsonPath;// 欢迎界面数据路径
	private volatile int done;
	private FragmentManager fm;
	private long startTime;
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
		startTime = System.currentTimeMillis();

		tran.commit();

		welcomeJsonPath = App.getInstance().getJsonFileCacheRootDir();

		getChannelJson();

		// 初始化服务
		Intent service = new Intent(this, InitService.class);
		service.setAction(InitService.InitAction);
		this.startService(service);

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

		final File title = new File(App.getInstance().getJsonFileCacheRootDir()
				+ File.separator + App.mTitle);
		final File target = App.getFile(welcomeJsonPath + File.separator
				+ "News");

		if (title.exists()) {
			loadMainUI();
		} else {
			httpUtils.download(InterfaceJsonfile.CHANNELLIST + "News",
					target.getAbsolutePath(), new RequestCallBack<File>() {
						@Override
						public void onSuccess(ResponseInfo<File> responseInfo) {
							LogUtils.i("write  channel to file success");
							String data = App
									.getFileContext(responseInfo.result);
							if (data != null) {
								LogUtils.i("channel-->" + data);
								JSONObject obj = null;

								obj = FjsonUtil.parseObject(data);
								if (null == obj) {
									return;
								}

								JSONArray array = obj.getJSONArray("data");
								List<NewsChannelBean> mList = JSONArray
										.parseArray(array.toJSONString(),
												NewsChannelBean.class);

								SerializeUtil<List<NewsChannelBean>> mSu = new SerializeUtil<List<NewsChannelBean>>();
								List<NewsChannelBean> stl = mSu
										.readyDataToFile(title
												.getAbsolutePath());
								HashMap<String, NewsChannelBean> saveTitleMap = new HashMap<String, NewsChannelBean>();

								if (null == stl || stl.size() < 1) {
									LogUtils.i("setTitleData");

									if (mList != null && mList.size() > 0) {
										LogUtils.i("list != null && list.size() > 0");

										mSu.writeDataToFile(mList, App
												.getInstance()
												.getJsonFileCacheRootDir()
												+ File.separator + App.mTitle);
										LogUtils.i("write title data to disk!");
									}
								} else {
									for (NewsChannelBean stb : mList) {
										saveTitleMap.put(stb.getTid(), stb);
									}

									for (int i = 0; i < stl.size(); i++) {
										NewsChannelBean stb = stl.get(i);
										NewsChannelBean stbq = saveTitleMap
												.get(stb.getTid());
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
							loadMainUI();
						}

						@Override
						public void onFailure(HttpException error, String msg) {
							LogUtils.i("write  channel to file Failed");
							if (GetFileSizeUtil.getInstance().getFileSizes(
									target) > 10) {
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


}
