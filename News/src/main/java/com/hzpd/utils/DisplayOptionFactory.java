package com.hzpd.utils;

import android.graphics.Bitmap.Config;

import com.hzpd.hflt.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public enum DisplayOptionFactory {
    Small, Big, Logo, Avatar, XF_Avatar, Personal_center_News;
    public DisplayImageOptions options;

    DisplayOptionFactory() {
        options = getOption(ordinal());
    }

    private static DisplayImageOptions getOption(int type) {
        DisplayImageOptions option;
        switch (type) {
            case 0: {
                option = new DisplayImageOptions.Builder()
                        .bitmapConfig(Config.RGB_565)
                        .showImageOnLoading(R.drawable.default_bg)
                        .showImageOnFail(R.drawable.default_bg)
                        .showImageForEmptyUri(R.drawable.default_bg)
                        .cacheInMemory(true).cacheOnDisk(true)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .build();
            }
            break;
            case 1: {
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
            case 2: {
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
            case 3: {
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
            case 4: {
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
                        .showImageOnFail(R.drawable.default_bg)
                        .showImageForEmptyUri(R.drawable.default_bg)
                        .showImageOnLoading(R.drawable.default_bg)
                        .displayer(new FadeInBitmapDisplayer(300))
                        .cacheInMemory(true).cacheOnDisk(true)
                        .build();
            }
            break;
        }
        return option;
    }
}