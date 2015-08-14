package com.hzpd.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.custorm.CircleImageView;
import com.hzpd.hflt.R;
import com.hzpd.modle.UserBean;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.PersonalInfoActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.CipherUtils;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
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
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

public class ZQ_PersonalInfoFragment extends BaseFragment {

	@ViewInject(R.id.zq_pinfo_rl_bg)
	private RelativeLayout zq_pinfo_rl_bg;
	@ViewInject(R.id.pi_login_name_tr)
	private TableRow pi_login_name_tr;
	@ViewInject(R.id.pi_modifypwd_tr)
	private TableRow pi_modifypwd_tr;
	@ViewInject(R.id.pi_nick_name_tr)
	private TableRow pi_nick_name_tr;
	@ViewInject(R.id.pi_gender_tr)
	private TableRow pi_gender_tr;


	@ViewInject(R.id.lg_pi_iv_touxiang)
	private CircleImageView lg_pi_iv_touxiang;//头像
	@ViewInject(R.id.lg_pi_tv_name)
	private TextView lg_pi_tv_name;//用户名
	@ViewInject(R.id.lg_pi_nick)
	private TextView lg_pi_nick;//昵称
	@ViewInject(R.id.lg_pi_tv_sex)
	private TextView lg_pi_tv_sex;//性别
	@ViewInject(R.id.lg_pi_bt_quite)
	private Button lg_pi_bt_quite;//退出登录


	/* 请求码 */
	private static final int IMAGE_REQUEST_CODE = 450;
	private static final int CAMERA_REQUEST_CODE = 451;
	private static final int RESULT_REQUEST_CODE = 452;
	private String[] items = new String[]{App.getInstance().getString(R.string.prompt_choose_local_photo),
			App.getInstance().getString(R.string.prompt_take_photo)};
	private final String IMAGE_FILE_NAME = "faceImage.jpg";
	private boolean isThirdpart = false;//是否是第三方登陆
	private File imgFile;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.personal_info, container, false);
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	private void init() {
		LogUtils.i("userimg-->" + spu.getUser().getAvatar_path());
		LogUtils.i("usertoken-->" + spu.getUser().getToken());
		LogUtils.i("uid-->" + spu.getUser().getUid());
		JSONObject obj = spu.getWelcome();

		String pinfobg = null;
		if (null != obj) {
			pinfobg = obj.getString("userinfobg");
		}
		if (TextUtils.isEmpty(pinfobg)) {
			zq_pinfo_rl_bg.setBackgroundResource(R.drawable.welcome);
		} else {
			mImageLoader.loadImage(pinfobg, new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
				}

				@Override
				public void onLoadingFailed(String imageUri, View view,
				                            FailReason failReason) {
					zq_pinfo_rl_bg.setBackgroundResource(R.drawable.welcome);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					BitmapDrawable bg = new BitmapDrawable(getResources(), loadedImage);
					zq_pinfo_rl_bg.setBackground(bg);
				}

				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					zq_pinfo_rl_bg.setBackgroundResource(R.drawable.welcome);
				}
			});
		}


		mImageLoader.displayImage(spu.getUser().getAvatar_path(), lg_pi_iv_touxiang, DisplayOptionFactory.getOption(OptionTp.Logo));

		String username = spu.getUser().getUsername();
		if (QQ.NAME.equals(username)) {
			username = getString(R.string.prompt_qq_user);
//			pi_modifypwd_tr.setVisibility(View.GONE);
			isThirdpart = true;
		} else if (Wechat.NAME.equals(username)) {
			username = getString(R.string.prompt_weixin_user);
//			pi_modifypwd_tr.setVisibility(View.GONE);
			isThirdpart = true;
		} else if (SinaWeibo.NAME.equals(username)) {
			username = getString(R.string.prompt_weibo_user);
//			pi_modifypwd_tr.setVisibility(View.GONE);
			isThirdpart = true;
		} else {
			username = getString(R.string.prompt_register_user);
		}

//		String ph=spu.getMobile();
		lg_pi_tv_name.setText(username);
		lg_pi_nick.setText(spu.getUser().getNickname());

		if ("1".equals(spu.getUser().getSex())) {
			lg_pi_tv_sex.setText(R.string.prompt_male);
		} else if ("2".equals(spu.getUser().getSex())) {
			lg_pi_tv_sex.setText(R.string.prompt_female);
		} else if ("3".equals(spu.getUser().getSex())) {
			lg_pi_tv_sex.setText(R.string.prompt_keep_secret);
		}

		imgFile = getImgPath();

		play();
	}

	@OnClick(R.id.lg_pi_bt_quite)
	private void exit(View v) {
		spu.setUser(null);

		if (isThirdpart = true) {
			for (Platform pl : ShareSDK.getPlatformList()) {
				if (pl.isValid()) {
					pl.SSOSetting(true);
					pl.removeAccount();
				}
			}
		}

		Intent intent = new Intent();
		if (v != null) {
			intent.setAction(ZY_RightFragment.ACTION_QUIT);
		} else {
			intent.setAction(ZY_RightFragment.ACTION_QUIT_LOGIN);
		}
		activity.sendBroadcast(intent);
		activity.finish();
	}

	@OnClick(R.id.zq_pinfo_iv_back)
	private void goBack(View v) {
		activity.onBackPressed();
	}


	@OnClick(R.id.pi_modifypwd_tr)
	private void modifyPwd(View view) {
		if (isThirdpart) {
			TUtils.toast(getString(R.string.toast_cannot_modify_password));
			return;
		}
		((PersonalInfoActivity) activity).toFindbackpwdFm();
	}

	@OnClick(R.id.pi_nick_name_tr)
	private void modifyNickName(View v) {
		if (isThirdpart) {
			TUtils.toast(getString(R.string.toast_cannot_modify_nickname));
			return;
		}
		((PersonalInfoActivity) activity).toModifyPinfoFm(InterfaceJsonfile.NICKNAME);
	}

	@OnClick(R.id.pi_gender_tr)
	private void modifyGender(View v) {
		if (isThirdpart) {
			TUtils.toast(getString(R.string.toast_cannot_modify_sex));
			return;
		}
		((PersonalInfoActivity) activity).toModifyPinfoFm(InterfaceJsonfile.GENDER);
	}

	/**
	 * 显示选择对话框
	 */
	@OnClick(R.id.lg_pi_iv_touxiang)
	private void showDialog(View v) {
		new AlertDialog.Builder(activity)
				.setTitle(R.string.prompt_set_avatar)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0: {
								Intent intentFromGallery = new Intent();
								intentFromGallery.setType("image/*"); // 设置文件类型
								intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
								activity.startActivityForResult(intentFromGallery,
										IMAGE_REQUEST_CODE);
								AAnim.ActivityStartAnimation(activity);
							}
							break;
							case 1: {
								Intent intentFromCapture = new Intent(
										MediaStore.ACTION_IMAGE_CAPTURE);
								intentFromCapture.putExtra(
										MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(imgFile));
								activity.startActivityForResult(intentFromCapture,
										CAMERA_REQUEST_CODE);
								AAnim.ActivityStartAnimation(activity);
							}
							break;
						}
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();

	}

	public void modify() {
		lg_pi_nick.setText(spu.getUser().getNickname());

		if ("1".equals(spu.getUser().getSex())) {
			lg_pi_tv_sex.setText(R.string.prompt_male);
		} else if ("2".equals(spu.getUser().getSex())) {
			lg_pi_tv_sex.setText(R.string.prompt_female);
		} else if ("3".equals(spu.getUser().getSex())) {
			lg_pi_tv_sex.setText(R.string.prompt_keep_secret);
		}
	}

	//图片裁剪
	public void startPhotoZoom(Uri uri) {
		if (null == uri) {
			return;
		}

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 320);
		intent.putExtra("outputY", 320);
		intent.putExtra("return-data", true);
		activity.startActivityForResult(intent, RESULT_REQUEST_CODE);
		AAnim.ActivityStartAnimation(activity);
	}

	public void startPhotoZoom() {
		Uri uri = Uri.fromFile(imgFile);
		startPhotoZoom(uri);
	}

	//上传头像
	public void uploadPhoto(Bitmap photo) {

		try {
			photo.compress(CompressFormat.PNG, 100, new FileOutputStream(imgFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		lg_pi_iv_touxiang.setImageBitmap(photo);

		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("token", spu.getUser().getToken());
		params.addBodyParameter("avatar", CipherUtils.base64Encode(photo));

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.CHANGEPINFO//InterfaceApi.modify_gender
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				TUtils.toast(getString(R.string.toast_server_no_response));
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.i("-->" + arg0.result);
				JSONObject obj = FjsonUtil.parseObject(arg0.result);
				if (null == obj) {
					return;
				}

				if (200 == obj.getIntValue("code")) {
					TUtils.toast(getString(R.string.toast_modify_success));
					UserBean user = FjsonUtil.parseObject(obj.getString("data"), UserBean.class);
					spu.setUser(user);

					Intent intent = new Intent();
					intent.setAction(ZY_RightFragment.ACTION_USER);
					activity.sendBroadcast(intent);
				} else {
					TUtils.toast(obj.getString("msg"));
				}
			}
		});

	}

	public File getImgPath() {
		File cacheFileDir;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
				!Environment.isExternalStorageRemovable()) {
			cacheFileDir = Environment.getExternalStorageDirectory();
		} else {
			cacheFileDir = activity.getApplicationContext().getCacheDir();
		}
		if (null != cacheFileDir && !cacheFileDir.exists()) {
			cacheFileDir.mkdirs();
		}
		File cacheFile = App.getFile(cacheFileDir.getAbsolutePath()
				+ File.separator + "cyol"
				+ File.separator + IMAGE_FILE_NAME);

		return cacheFile;
	}

	private void play() {
		PropertyValuesHolder scaleX = PropertyValuesHolder
				.ofFloat("scaleX", 1f, 1.3f, 1f);
		PropertyValuesHolder scaleY = PropertyValuesHolder
				.ofFloat("scaleY", 1f, 1.3f, 1f);

		ObjectAnimator obj = ObjectAnimator.ofPropertyValuesHolder(lg_pi_iv_touxiang, scaleX, scaleY)
				.setDuration(200);
		obj.setStartDelay(1000);
		obj.start();

	}

}