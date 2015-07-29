package com.hzpd.ui.fragments.welcome;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.ui.activity.WelcomeActivity;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import java.io.File;

public class AdFlashFragment extends BaseFragment {

	@ViewInject(R.id.adflash_img_ad)
	private ImageView adflash_img_ad;
	@ViewInject(R.id.adflash_img_logo)
	private ImageView adflash_img_logo;
	@ViewInject(R.id.adflash_tv_slogan)
	private TextView adflash_tv_slogan;

	private AnimatorSet animSetFadeout;
	private AnimatorSet animSetFadein;
	private JSONObject obj;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.adflash_frag_layout, container,
				false);
		ViewUtils.inject(this, view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		obj = spu.getWelcome();
		setImageViewAnimation();
	}

	private void setImageViewAnimation() {
		// 获取动画时间
		String duration = null;
		if (null != obj) {
			duration = obj.getString("timesize");
		}
		int dur = 2000;
		if (!TextUtils.isEmpty(duration)) {
			if (TextUtils.isDigitsOnly(duration)) {
				try {
					dur = Integer.parseInt(duration);
				} catch (Exception e) {

				}
			}
		}

		// 初始化
		animSetFadein = new AnimatorSet();
		ObjectAnimator oanim = ObjectAnimator.ofFloat(adflash_img_ad, "alpha",
				0.0f, 1.0f).setDuration(dur);
		oanim.setInterpolator(new DecelerateInterpolator());

		animSetFadein.play(oanim);
		animSetFadein.setDuration(dur);
		animSetFadein.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				((WelcomeActivity) getActivity()).loadMainUI();
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {

			}
		});

		animSetFadeout = new AnimatorSet();
		ObjectAnimator oanimFadeout = ObjectAnimator.ofFloat(adflash_img_ad,
				"alpha", 1.0f, 0.0f).setDuration(dur);
		oanimFadeout.setInterpolator(new AccelerateInterpolator());

		animSetFadeout.play(oanimFadeout);
		animSetFadeout.setDuration(2000);
		animSetFadeout.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				Bitmap bm = null;
				if (null != obj) {
					if (!TextUtils.isEmpty(obj.getString("imgurl"))) {

						File fbitmap = DiskCacheUtils.findInCache(
								obj.getString("imgurl"),
								mImageLoader.getDiskCache());

						if (null != fbitmap) {
							bm = BitmapFactory.decodeFile(fbitmap
									.getAbsolutePath());
						} else {
							mImageLoader.loadImage(obj.getString("imgurl"),
									DisplayOptionFactory
											.getOption(OptionTp.Big),
									new ImageLoadingListener() {
										@Override
										public void onLoadingStarted(
												String imageUri, View view) {
										}

										@Override
										public void onLoadingFailed(
												String imageUri, View view,
												FailReason failReason) {
										}

										@Override
										public void onLoadingComplete(
												String imageUri, View view,
												Bitmap loadedImage) {
											LogUtils.i("download welcome pic succuss");
										}

										@Override
										public void onLoadingCancelled(
												String imageUri, View view) {
										}
									});
						}
					} else {

					}
				}

				if (null != bm) {
					adflash_img_ad.setImageBitmap(bm);
				} else {
					adflash_img_ad.setImageResource(R.drawable.welcome);
				}

				animSetFadein.start();
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {

			}
		});

		animSetFadeout.start();

	}

	@OnClick(R.id.adflash_img_ad)
	private void adJump(View view) {
		String url = null;
		if (null != obj) {
			url = obj.getString("link");
		}
		if (!TextUtils.isEmpty(url)) {
			((WelcomeActivity) getActivity()).jump(url);
		}
	}

	@Override
	public void onDestroy() {
		if (null != animSetFadeout) {
			animSetFadeout.removeAllListeners();
		}
		if (null != animSetFadein) {
			animSetFadein.removeAllListeners();
		}
		super.onDestroy();
	}

}
