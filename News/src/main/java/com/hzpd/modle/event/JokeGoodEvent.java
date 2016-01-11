package com.hzpd.modle.event;

/**
 * 刷新当前新闻列表页
 */
public class JokeGoodEvent {
    public boolean isJokeGood = false;
    public String nid;

    public JokeGoodEvent() {

    }

    public JokeGoodEvent(boolean force,String nid) {
        this.isJokeGood = force;
        this.nid=nid;
    }

}
