package com.hzpd.utils;

import android.graphics.Bitmap.Config;

import com.hzpd.hflt.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class DisplayOptionFactory {
	public enum OptionTp {
		Small, Big, Paper, Logo, Circllogo, retRound
	}

	public static DisplayImageOptions getOption(OptionTp type) {
		DisplayImageOptions option;
		switch (type) {
			case Big: {
				option = new DisplayImageOptions.Builder()
						.showImageOnLoading(R.drawable.default_bg)
						.showImageOnFail(R.drawable.default_bg)
						.showImageForEmptyUri(R.drawable.default_bg)
						.cacheInMemory(true).cacheOnDisk(true)
						.imageScaleType(ImageScaleType.EXACTLY)
						.bitmapConfig(Config.RGB_565)
						.build();
			}
			break;
			case Paper: {
				option = new DisplayImageOptions.Builder()
						.showImageOnLoading(R.drawable.default_bg)
						.showImageOnFail(R.drawable.default_bg)
						.showImageForEmptyUri(R.drawable.default_bg)
						.cacheInMemory(true).cacheOnDisk(true)
//						.displayer(new FadeInBitmapDisplayer(300))
						.imageScaleType(ImageScaleType.EXACTLY)
						.bitmapConfig(Config.RGB_565)
						.build();
			}
			break;
			case retRound: {
				option = new DisplayImageOptions.Builder()
						.showImageOnLoading(R.drawable.default_bg)
						.showImageOnFail(R.drawable.default_bg)
						.showImageForEmptyUri(R.drawable.default_bg)
						.cacheInMemory(true).cacheOnDisk(true)
						.displayer(new RoundedBitmapDisplayer(4))//是否设置为圆角，弧度为多少
//			.displayer(new FadeInBitmapDisplayer(300))
						.imageScaleType(ImageScaleType.EXACTLY)
						.bitmapConfig(Config.RGB_565)
						.build();
			}
			break;
			case Logo: {
				option = new DisplayImageOptions.Builder()
						.imageScaleType(ImageScaleType.EXACTLY)
						.showImageOnFail(R.drawable.logo)
						.showImageForEmptyUri(R.drawable.logo)
						.showImageOnLoading(R.drawable.logo)
						.cacheInMemory(true).cacheOnDisk(true)
						.bitmapConfig(Config.RGB_565)
//			.displayer(new FadeInBitmapDisplayer(300))
						.build();
			}
			break;
			case Circllogo: {
				option = new DisplayImageOptions.Builder()
						.imageScaleType(ImageScaleType.EXACTLY)
						.showImageOnFail(R.drawable.logo)
						.showImageForEmptyUri(R.drawable.logo)
						.showImageOnLoading(R.drawable.logo)
						.cacheInMemory(true).cacheOnDisk(true)
						.bitmapConfig(Config.RGB_565)
						.displayer(new CircleBitmapDisplayer())
						.build();
			}
			break;
			default: {
				option = new DisplayImageOptions.Builder()
						.imageScaleType(ImageScaleType.EXACTLY)
						.showImageOnFail(R.drawable.zy_thumbnail_small)
						.showImageForEmptyUri(R.drawable.zy_thumbnail_small)
						.showImageOnLoading(R.drawable.zy_thumbnail_small)
						.cacheInMemory(true).cacheOnDisk(true)
						.bitmapConfig(Config.RGB_565)
//			.displayer(new FadeInBitmapDisplayer(300))
						.build();
			}
			break;
		}
		return option;
	}
}
