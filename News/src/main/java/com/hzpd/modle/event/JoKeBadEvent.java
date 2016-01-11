package com.hzpd.modle.event;

/**
 * 刷新当前新闻列表页
 */
public class JoKeBadEvent {
    public boolean isJokeBad = false;
    public String nid;

    public JoKeBadEvent() {

    }

    public JoKeBadEvent(boolean force,String nid) {
        this.isJokeBad = force;
        this.nid=nid;
    }

}
