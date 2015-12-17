package com.hzpd.modle.db;

import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.TagBean;
import com.lidroid.xutils.db.annotation.Column;

public class NewsChannelBeanDB extends BaseDB {
    @Column(column = "tid")
    private String tid;
    @Column(column = "cnname")
    private String cnname;
    @Column(column = "sort_order")
    private String sort_order;
    @Column(column = "fid")
    private String fid;
    @Column(column = "path")
    private String path;
    @Column(column = "source")
    private String source;
    @Column(column = "style")
    private String style;
    @Column(column = "status")
    private String status;
    @Column(column = "siteid")
    private String siteid;
    @Column(column = "default_show")
    private String default_show;

    @Column(column = "icon")
    private String icon;
    @Column(column = "tagid")
    private String tagid;
    @Column(column = "name")
    private String name;
    @Column(column = "num")
    private String num;
    @Column(column = "type")
    private int type;

    public NewsChannelBeanDB() {

    }

    public NewsChannelBeanDB(NewsChannelBean bean) {
        setTid(bean.getTid());
        setCnname(bean.getCnname());
        setSort_order(bean.getSort_order());
        setFid(bean.getFid());
        setPath(bean.getPath());
        setSource(bean.getSource());
        setStyle(bean.getStyle());
        setStatus(bean.getStatus());
        setSiteid(bean.getSiteid());
        setDefault_show(bean.getDefault_show());
        setIcon(bean.getIcon());
        setTagid(bean.getId());
        setName(bean.getName());
        setNum(bean.getNum());
        setType(bean.getType());
    }

    public NewsChannelBeanDB(TagBean bean) {
        setCnname(bean.getName());
        setDefault_show("1");
        setIcon(bean.getIcon());
        setTagid(bean.getId());
        setName(bean.getName());
        setNum(bean.getNum());
        setType(NewsChannelBean.TYPE_NORMAL);
    }


    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getCnname() {
        return cnname;
    }

    public void setCnname(String cnname) {
        this.cnname = cnname;
    }

    public String getSort_order() {
        return sort_order;
    }

    public void setSort_order(String sort_order) {
        this.sort_order = sort_order;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSiteid() {
        return siteid;
    }

    public void setSiteid(String siteid) {
        this.siteid = siteid;
    }

    public String getDefault_show() {
        return default_show;
    }

    public void setDefault_show(String default_show) {
        this.default_show = default_show;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTagid() {
        return tagid;
    }

    public void setTagid(String tagid) {
        this.tagid = tagid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
