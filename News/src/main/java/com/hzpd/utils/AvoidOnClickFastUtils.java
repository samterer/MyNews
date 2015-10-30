package com.hzpd.utils;

/**
 * Created by taoshuang on 2015/9/15.
 */
public class AvoidOnClickFastUtils {

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

}