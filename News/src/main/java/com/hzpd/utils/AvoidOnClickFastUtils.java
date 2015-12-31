package com.hzpd.utils;

import android.view.View;

/**
 * Created by taoshuang on 2015/9/15.
 */
public class AvoidOnClickFastUtils {

    private static long lastClickTime;
    private static int hash;

    /**
     * 防止同一个View过快点击
     */
    public static boolean isFastDoubleClick(final View view) {
        if (view.hashCode() != hash) {
            hash = view.hashCode();
            return false;
        }
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 1200) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


}