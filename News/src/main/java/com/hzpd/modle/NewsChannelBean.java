package com.hzpd.modle;

import android.text.TextUtils;

import java.io.Serializable;

public class NewsChannelBean implements Comparable<NewsChannelBean>, Serializable {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_RECOMMEND = 0x9999;
    public static final int TYPE_IMAGE_ALBUM = 0x11111;
    public static final int TYPE_VIDEO = 0x22222;
    public static final int TYPE_SUBJECT = 0x33333;

    private static final long serialVersionUID = 1L;

    private String tid;//": "16",
    private String cnname;//": "898195fb",
    private String sort_order;//": "11",
    private String fid;//": "14",
    private String path;//": "0-14",
    private String source;//": "http://www.tianshannet.com/wap/rss/toutiao.xml",
    private String style;//": "2",
    private String status;//": "1",
    private String siteid;//": "1"
    private String default_show;

    @Override
    public String toString() {
        return "NewsChannelBean{" +
                "tid='" + tid + '\'' +
                ", cnname='" + cnname + '\'' +
                ", sort_order='" + sort_order + '\'' +
                ", fid='" + fid + '\'' +
                ", path='" + path + '\'' +
                ", source='" + source + '\'' +
                ", style='" + style + '\'' +
                ", status='" + status + '\'' +
                ", siteid='" + siteid + '\'' +
                ", default_show='" + default_show + '\'' +
                ", type=" + type +
                '}';
    }

    public String getDefault_show() {
        return default_show;
    }

    public void setDefault_show(String default_show) {
        this.default_show = default_show;
    }

    private int type = TYPE_NORMAL;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTid() {
        return tid;
    }

    public String getCnname() {
        return cnname;
    }

    public String getSort_order() {
        return sort_order;
    }

    public String getFid() {
        return fid;
    }

    public String getPath() {
        return path;
    }

    public String getSource() {
        return source;
    }

    public String getStyle() {
        return style;
    }

    public String getStatus() {
        return status;
    }

    public String getSiteid() {
        return siteid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public void setCnname(String cnname) {
        this.cnname = cnname;
    }

    public void setSort_order(String sort_order) {
        this.sort_order = sort_order;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSiteid(String siteid) {
        this.siteid = siteid;
    }

    @Override
    public int compareTo(NewsChannelBean another) {
        int my = Integer.parseInt(sort_order);
        int an = Integer.parseInt(another.getSort_order());
        if (my > an) {
            return 1;
        } else if (my < an) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NewsChannelBean) {
            NewsChannelBean another = (NewsChannelBean) o;
            return !TextUtils.isEmpty(getTid()) && getTid().equals(another.getTid());
        }
        return super.equals(o);
    }

}
