package com.hzpd.utils;

import android.view.View;

public class AvoidOnClickFastUtils {

    private static long lastClickTime;
    private static int hash;

    /**
     * 防止同一个View过快点击
     */
    public static boolean isFastDoubleClick(final View view) {
        long time = System.currentTimeMillis();
        if (view.hashCode() != hash) {
            hash = view.hashCode();
            lastClickTime = time;
            return false;
        }
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 1200) {
            return true;
        }
        return false;
    }


}