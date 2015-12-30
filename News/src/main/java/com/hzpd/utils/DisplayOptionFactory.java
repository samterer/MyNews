package com.hzpd.utils;

import android.graphics.Bitmap.Config;

import com.hzpd.hflt.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class DisplayOptionFactory {
    public enum OptionTp {
        Small, Big, Paper, Logo, Circllogin, retRound, Avatar, XF_Avatar, Personal_center_News
    }

    public static DisplayImageOptions getOption(OptionTp type) {
        DisplayImageOptions option;
        switch (type) {
            case Big: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .showImageOnLoading(R.drawable.zy_thumbnail_small)
                        .showImageOnFail(R.drawable.zy_thumbnail_small)
                        .showImageForEmptyUri(R.drawable.zy_thumbnail_small)
                        .cacheInMemory(true).cacheOnDisk(true)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .build();
            }
            break;
            case Paper: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .bitmapConfig(Config.RGB_565)
                        .showImageOnLoading(R.drawable.default_bg)
                        .showImageOnFail(R.drawable.default_bg)
                        .showImageForEmptyUri(R.drawable.default_bg)
                        .cacheInMemory(true).cacheOnDisk(true)
                        .displayer(new FadeInBitmapDisplayer(300))
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .build();
            }
            break;
            case retRound: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .showImageOnLoading(R.drawable.default_bg)
                        .showImageOnFail(R.drawable.default_bg)
                        .showImageForEmptyUri(R.drawable.default_bg)
                        .cacheInMemory(true).cacheOnDisk(true)
                        .displayer(new FadeInBitmapDisplayer(300))
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .build();
            }
            break;
            case Logo: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .showImageOnFail(R.drawable.logo)
                        .showImageForEmptyUri(R.drawable.logo)
                        .showImageOnLoading(R.drawable.logo)
                        .cacheInMemory(true).cacheOnDisk(true)
                        .displayer(new FadeInBitmapDisplayer(300))
                        .build();
            }
            break;
            case Circllogin: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .showImageOnFail(R.drawable.zy_pic_touxiang)
                        .showImageForEmptyUri(R.drawable.zy_pic_touxiang)
                        .showImageOnLoading(R.drawable.zy_pic_touxiang)
                        .cacheInMemory(true).cacheOnDisk(true)
                        .bitmapConfig(Config.RGB_565)
                        .displayer(new CircleBitmapDisplayer())
                        .build();
            }
            break;
            case Avatar: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .showImageOnFail(R.drawable.zy_pic_main_info_profile_bg)
                        .showImageForEmptyUri(R.drawable.zy_pic_main_info_profile_bg)
                        .showImageOnLoading(R.drawable.zy_pic_main_info_profile_bg)
                        .displayer(new FadeInBitmapDisplayer(300))
                        .cacheInMemory(true).cacheOnDisk(true)
                        .build();
            }
            break;
            case XF_Avatar: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .showImageOnFail(R.drawable.details_comment_user_avatar)
                        .showImageForEmptyUri(R.drawable.details_comment_user_avatar)
                        .showImageOnLoading(R.drawable.details_comment_user_avatar)
                        .cacheInMemory(true).cacheOnDisk(true)
                        .build();
            }
            break;
            case Personal_center_News: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .showImageOnFail(R.drawable.urlicon_loadingpicture_dynamic)
                        .showImageForEmptyUri(R.drawable.urlicon_loadingpicture_dynamic)
                        .showImageOnLoading(R.drawable.urlicon_loadingpicture_dynamic)
                        .cacheInMemory(true).cacheOnDisk(true)
                        .bitmapConfig(Config.RGB_565)
                        .build();
            }
            break;
            default: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                        .showImageOnFail(R.drawable.zy_thumbnail_small)
                        .showImageForEmptyUri(R.drawable.zy_thumbnail_small)
                        .showImageOnLoading(R.drawable.zy_thumbnail_small)
                        .displayer(new FadeInBitmapDisplayer(300))
                        .cacheInMemory(true).cacheOnDisk(true)
                        .build();
            }
            break;
        }
        return option;
    }
}