package com.hzpd.modle.event;

/**
 * Created by taoshuang on 2015/10/19.
 */
public class UpdateNewsBeanDbEvent {

    private String mMsg;

    public UpdateNewsBeanDbEvent(String msg) {
        this.mMsg = msg;
    }

    public String getmMsg(){
        return mMsg;
    }


}
