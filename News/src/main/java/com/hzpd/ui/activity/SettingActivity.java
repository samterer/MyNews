package com.hzpd.ui.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzpd.custorm.CustomProgressDialog;
import com.hzpd.custorm.SlideSwitch;
import com.hzpd.custorm.SlideSwitch.SlideListener;
import com.hzpd.hflt.R;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.services.ClearCacheService;
import com.hzpd.ui.App;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.CODE;
import com.hzpd.utils.GetFileSizeUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import de.greenrobot.event.EventBus;

public class SettingActivity extends MBaseActivity {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;

	@ViewInject(R.id.zqzx_setting_rb1)
	private RadioButton zqzx_setting_rb1;
	@ViewInject(R.id.zqzx_setting_rb2)
	private RadioButton zqzx_setting_rb2;
	@ViewInject(R.id.zqzx_setting_rb3)
	private RadioButton zqzx_setting_rb3;
	@ViewInject(R.id.zqzx_setting_push)
	private SlideSwitch zqzx_setting_push;
	@ViewInject(R.id.zqzx_setting_weibo)
	private SlideSwitch zqzx_setting_weibo;
	@ViewInject(R.id.zqzx_setting_deletecache)
	private RelativeLayout zqzx_setting_deletecache;
	@ViewInject(R.id.zqzx_setting_feedback)
	private RelativeLayout zqzx_setting_feedback;
	@ViewInject(R.id.zqzx_setting_cache)
	private TextView zqzx_setting_cache;
	@ViewInject(R.id.zqzx_setting_update)
	private RelativeLayout zqzx_setting_update;
	@ViewInject(R.id.zqzx_setting_tv_version)
	private TextView zqzx_setting_tv_version;

	private CustomProgressDialog dialog;
	private AlertDialog.Builder mDeleteDialog;

	private Platform sinaPlatform;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zqzx_setting_layout);
		ViewUtils.inject(this);

		stitle_tv_content.setText(R.string.title_settings);

		switch (spu.getTextSize()) {
			case CODE.textSize_big: {
				zqzx_setting_rb1.setChecked(true);
			}
			break;
			case CODE.textSize_normal: {
				zqzx_setting_rb2.setChecked(true);
			}
			break;
			case CODE.textSize_small: {
				zqzx_setting_rb3.setChecked(true);
			}
			break;
		}

		if (spu.getOffTuiSong()) {
			zqzx_setting_push.setState(true);
		} else {
			zqzx_setting_push.setState(false);
		}

		sinaPlatform = ShareSDK.getPlatform(SinaWeibo.NAME);

		sinaPlatform.setPlatformActionListener(new PlatformActionListener() {
			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {
				handler.sendEmptyMessage(112);
			}

			@Override
			public void onComplete(Platform arg0, int arg1,
			                       HashMap<String, Object> arg2) {

				if (arg1 == Platform.ACTION_AUTHORIZING) {
					LogUtils.i("Platform.ACTION_AUTHORIZING-->"
							+ Platform.ACTION_AUTHORIZING);
				}
				handler.sendEmptyMessage(113);
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				handler.sendEmptyMessage(112);
			}
		});
		if (sinaPlatform.isValid()) {
			zqzx_setting_weibo.setState(true);
		} else {
			zqzx_setting_weibo.setState(false);
		}

		zqzx_setting_push.setSlideListener(new SlideListener() {
			                                   @Override
			                                   public void open() {
				                                   pushSwitch(true);
			                                   }

			                                   @Override
			                                   public void close() {
				                                   pushSwitch(false);
			                                   }
		                                   }
		);

		zqzx_setting_weibo.setSlideListener(new SlideListener() {
			                                    @Override
			                                    public void open() {
				                                    sinaPlatform.authorize();
			                                    }

			                                    @Override
			                                    public void close() {
				                                    sinaPlatform.SSOSetting(true);
				                                    sinaPlatform.removeAccount();
			                                    }
		                                    }
		);

		zqzx_setting_tv_version.setText(App.getInstance().getVersionName());

		getCacheSize();
	}

	private void pushSwitch(boolean isChecked) {
		spu.setOffTuiSong(isChecked);
		if (JPushInterface.isPushStopped(SettingActivity.this)) {
			JPushInterface.init(SettingActivity.this);
		}
		if (isChecked) {
			if (null != spu.getUser()) {
				JPushInterface.setAlias(SettingActivity.this, spu.getUser().getUid(),
						new TagAliasCallback() {
							@Override
							public void gotResult(int arg0, String arg1,
							                      Set<String> arg2) {
								LogUtils.i("arg0-->" + arg0 + " arg1-->" + arg1);
								if (arg2 != null) {
									for (String s : arg2) {
										LogUtils.i("arg2->" + s);
									}
								}
							}
						});
			}

		} else {
			JPushInterface.stopPush(SettingActivity.this);
		}

	}

	@OnClick(R.id.zqzx_setting_aboutus)
	private void aboutus(View v) {
		Intent mIntent = new Intent(this, AboutUsActivity.class);
		startActivity(mIntent);
		AAnim.ActivityStartAnimation(this);
	}

	@OnClick(R.id.zqzx_setting_update)
	private void checkUpdate(View v) {
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			@Override
			public void onUpdateReturned(int updateStatus,
			                             UpdateResponse updateInfo) {
				switch (updateStatus) {
					case UpdateStatus.Yes: // has update
						UmengUpdateAgent.showUpdateDialog(SettingActivity.this,
								updateInfo);
						break;
					case UpdateStatus.No: // has no update
						TUtils.toast(getString(R.string.toast_no_update));
						break;
					case UpdateStatus.NoneWifi: // none wifi
						TUtils.toast(getString(R.string.toast_update_only_in_wifi));
						break;
					case UpdateStatus.Timeout: // time out
						TUtils.toast(getString(R.string.toast_timeout));
						break;
				}
			}
		});
		UmengUpdateAgent.forceUpdate(this);
	}

	@OnClick({R.id.zqzx_setting_rb1, R.id.zqzx_setting_rb2,
			R.id.zqzx_setting_rb3})
	private void onRadioCheck(View v) {
		FontSizeEvent event = new FontSizeEvent();
		switch (v.getId()) {
			case R.id.zqzx_setting_rb1: {
				zqzx_setting_rb1.setChecked(true);
				spu.setTextSize(CODE.textSize_big);
				event.setFontSize(CODE.textSize_big);
			}
			break;
			case R.id.zqzx_setting_rb2: {
				zqzx_setting_rb2.setChecked(true);
				spu.setTextSize(CODE.textSize_normal);
				event.setFontSize(CODE.textSize_normal);
			}
			break;
			case R.id.zqzx_setting_rb3: {
				zqzx_setting_rb3.setChecked(true);
				spu.setTextSize(CODE.textSize_small);
				event.setFontSize(CODE.textSize_small);
			}
			break;
		}

		EventBus.getDefault().post(event);
	}

	public void getCacheSize() {
		GetFileSizeUtil fz = GetFileSizeUtil.getInstance();
		String cacheSize = "0K";
		try {

			cacheSize = "" + fz.FormetFileSize(fz.getFileDirSize(ImageLoader.getInstance().getDiskCache().getDirectory())
					+ fz.getFileDirSize(new File(App.getInstance().getJsonFileCacheRootDir())));

		} catch (Exception e) {
			e.printStackTrace();
		}
		zqzx_setting_cache.setText(cacheSize);
	}

	private void showDialog() {
		dialog = CustomProgressDialog.createDialog(this, false);
		dialog.show();
	}

	private void deleteSuccess() {
		AlertDialog.Builder dilaog = new Builder(this);
		dilaog.setMessage(R.string.toast_delete_success);
		dilaog.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();


			}
		});
		dilaog.show();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (111 == msg.what) {
				zqzx_setting_cache.setText("0K");
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				deleteSuccess();
			} else if (112 == msg.what) {
				zqzx_setting_weibo.setState(false);
				TUtils.toast(getString(R.string.toast_bind_failed));
			} else if (113 == msg.what) {
				TUtils.toast(getString(R.string.toast_bind_success));
			}
		}
	};

	@OnClick(R.id.zqzx_setting_deletecache)
	private void deleteCache(View v) {
		mDeleteDialog = new Builder(this);
		mDeleteDialog.setTitle(R.string.prompt_clear_cache_dialog_title);
		mDeleteDialog.setMessage(R.string.prompt_clear_cache_dialog_msg);
		mDeleteDialog.setNegativeButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showDialog();

						activity.startService(new Intent(activity, ClearCacheService.class));
						handler.sendEmptyMessageDelayed(111, 2000);
					}
				});
		mDeleteDialog.setPositiveButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		mDeleteDialog.show();
	}

	@OnClick(R.id.zqzx_setting_feedback)
	private void feedBack(View v) {
		Intent intent = new Intent(this, ZQ_FeedBackActivity.class);
		startActivity(intent);
		AAnim.ActivityStartAnimation(this);
	}

	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		finish();
	}

}
