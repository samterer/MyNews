package com.hzpd.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.ThirdLoginBean;
import com.hzpd.modle.UserBean;
import com.hzpd.ui.activity.LoginActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

public class ZQ_LoginWelcomeFragment extends BaseFragment {

	@ViewInject(R.id.zq_loginwelbg_rl)
	private RelativeLayout zq_loginwelbg_rl;
	@ViewInject(R.id.zq_loginwel_iv_back)
	private ImageView zq_loginwel_iv_back;
	@ViewInject(R.id.zq_loginwel_iv_tenet)
	private ImageView zq_loginwel_iv_tenet;
	@ViewInject(R.id.zq_loginwel_iv_logo)
	private ImageView zq_loginwel_iv_logo;
	@ViewInject(R.id.zq_loginwel_iv_sina)
	private ImageView zq_loginwel_iv_sina;
	@ViewInject(R.id.zq_loginwel_iv_account)
	private ImageView zq_loginwel_iv_account;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 888: {
					Platform plat = (Platform) msg.obj;
					PlatformDb platDB = plat.getDb();
					// 通过DB获取各种数据
					String token = platDB.getToken();
					String gender = platDB.getUserGender();
					String usericon = platDB.getUserIcon();
					String userid = platDB.getUserId();
					String username = platDB.getUserName();
					String userplatform = platDB.getPlatformNname();

					LogUtils.i("token" + token + " gender:" + gender + " usericon:"
							+ usericon + " userid:" + userid + " username:"
							+ username + " userplatform:" + userplatform);
					ThirdLoginBean tlb = new ThirdLoginBean(userid, gender,
							username, usericon, userplatform);
					thirdLogin(tlb);
				}
				break;
				case 889: {
					TUtils.toast(getString(R.string.toast_app_not_installed));
				}
				break;
				case 890: {
					TUtils.toast(getString(R.string.toast_login_cancelled));
				}
				break;
			}

		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.zq_loginwelcome_layout,
				container, false);
		ViewUtils.inject(this, view);

		JSONObject welcomeObj = spu.getWelcome();
		if (null != welcomeObj) {
			String loginbg = welcomeObj.getString("loginbg");
			mImageLoader.loadImage(loginbg, new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					zq_loginwelbg_rl.setBackgroundResource(R.drawable.zy_bg_cehua);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view,
				                            FailReason failReason) {
					zq_loginwelbg_rl.setBackgroundResource(R.drawable.zy_bg_cehua);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view,
				                              Bitmap loadedImage) {
					BitmapDrawable bg = new BitmapDrawable(getResources(),
							loadedImage);
					if (null != loadedImage) {
						zq_loginwelbg_rl.setBackground(bg);
					}
				}

				@Override
				public void onLoadingCancelled(String imageUri, View view) {
				}
			});
		}

		return view;
	}

	@OnClick(R.id.zq_loginwel_iv_back)
	private void goback(View view) {
		activity.onBackPressed();
	}

	@OnClick(R.id.zq_loginwel_iv_account)
	private void accountLogin(View view) {
		((LoginActivity) activity).toLoginFm();
	}

	@OnClick({R.id.zq_loginwel_iv_tenet, R.id.zq_loginwel_iv_sina})
	private void thirdLogin(View v) {
		LogUtils.i("QQ.NAME" + QQ.NAME + "  Wechat.NAME：" + Wechat.NAME
				+ "  SinaWeibo.NAME:" + SinaWeibo.NAME);
		Platform platform = null;
		switch (v.getId()) {
			case R.id.zq_loginwel_iv_tenet: {
				platform = new Wechat(activity);
				// platform = new QQ(activity);
			}
			break;
			case R.id.zq_loginwel_iv_sina: {
				platform = new SinaWeibo(activity);
			}
			break;
		}

		platform.setPlatformActionListener(new PlatformActionListener() {
			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {
				LogUtils.i("onError ");
				arg0.removeAccount();
				handler.sendEmptyMessage(889);
			}

			@Override
			public void onComplete(Platform plat, int action,
			                       HashMap<String, Object> res) {
				// 用户资源都保存到res
				// 通过打印res数据看看有哪些数据是你想要的
				if (Platform.ACTION_USER_INFOR == action) {
					Message msg = handler.obtainMessage();
					msg.what = 888;
					msg.obj = plat;
					handler.sendMessage(msg);
				}
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				LogUtils.i("onCancel ");
				arg0.removeAccount();
				handler.sendEmptyMessage(890);
			}
		});

		platform.SSOSetting(false);
		platform.showUser(null);// 执行登录，登录后在回调里面获取用户资料

	}

	private void thirdLogin(ThirdLoginBean tlb) {
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("userid", tlb.getUserid());
		params.addBodyParameter("gender", tlb.getGender());
		params.addBodyParameter("nickname", tlb.getNickname());
		params.addBodyParameter("photo", tlb.getPhoto());
		params.addBodyParameter("third", tlb.getThird());
		params.addBodyParameter("is_ucenter", "1");

		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.thirdLogin, params,
				new RequestCallBack<String>() {
					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						LogUtils.i("result-->" + responseInfo.result);
						JSONObject obj = FjsonUtil
								.parseObject(responseInfo.result);
						if (null == obj) {
							return;
						}

						if (200 == obj.getIntValue("code")) {

							UserBean user = FjsonUtil.parseObject(
									obj.getString("data"), UserBean.class);
							spu.setUser(user);

							Intent intent = new Intent();
							intent.setAction(ZY_RightFragment.ACTION_USER);
							activity.sendBroadcast(intent);
							activity.finish();
							TUtils.toast(getString(R.string.toast_login_success));
						} else {
							TUtils.toast(obj.getString("msg"));
						}

					}

					@Override
					public void onFailure(HttpException error, String msg) {

					}
				});
	}

}