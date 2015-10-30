package com.hzpd.modle;

import java.util.List;

public class Adbean {
    int type;
    String imgurl;
    String link;
    String facebookid;
    int position;
    int timesize;
    String width;
    String height;
    private List<String> tid; //": [],

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFacebookid() {
        return facebookid;
    }

    public void setFacebookid(String facebookid) {
        this.facebookid = facebookid;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        position = position > 2 ? position : 2;
        this.position = position;
    }

    public int getTimesize() {
        return timesize;
    }

    public void setTimesize(int timesize) {
        this.timesize = timesize;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public List<String> getTid() {
        return tid;
    }

    public void setTid(List<String> tid) {
        this.tid = tid;
    }
}
