package com.hzpd.modle.event;

/**
 * 清除搜索历史
 */
public class ClassifItemEvent {

    public ClassifItemEvent(String key) {
        this.key = key;
    }

    public String key;
}
