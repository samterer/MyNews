package com.hzpd.modle.db;

import android.text.TextUtils;

import com.hzpd.modle.NewsBean;
import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Unique;


@Table(name = "newslist")
public class NewsBeanDB extends BaseDB implements Comparable<NewsBeanDB> {

    @Unique
    @Column(column = "nid")
    private int nid;
    @Column(column = "title")
    private String title;
    @Column(column = "sid")
    private String sid;
    @Column(column = "tid")
    private String tid;//频道
    @Column(column = "outline")
    private String outline;
    @Column(column = "type")
    private String type;//item样式  4三联图  其他图文
    @Column(column = "update_time")
    private String update_time;
    @Column(column = "json_url")
    private String json_url;//": "http://demo.99cms.cn/cms_json/bj/News/Content/201410/20/9446"
    @Column(column = "imgs")
    private String imgs;
    @Column(column = "rtype")
    private String rtype;//（跳转脚标）1新闻  2图集  3直播 4专题  5关联新闻 6视频 7引用
    @Column(column = "comcount")
    private String comcount;//": "0",
    @Column(column = "sort_order")
    private String sort_order;//": "1736823498",
    @Column(column = "subjectsort")
    private String subjectsort;//": "1736823498",
    @Column(column = "status")
    private String status;//": "0"删除
    @Column(column = "comflag")
    private String comflag;//": "0",//0不能评论
    @Column(column = "isreaded", defaultValue = "0")
    private int isreaded;//shi否阅读过
    @Column(column = "columnid", defaultValue = "0")
    private String columnid;
    @Column(column = "copyfrom")
    private String copyfrom;
    @Column(column = "fav")
    private String fav;
    @Column(column = "attname")
    private String attname;
    @Column(column = "like")
    private String like;
    @Column(column = "unlike")
    private String unlike;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public String getUnlike() {
        return unlike;
    }

    public void setUnlike(String unlike) {
        this.unlike = unlike;
    }

    public NewsBeanDB() {

    }

    public NewsBeanDB(NewsBean nb) {

        nid = Integer.parseInt(nb.getNid());
        title = nb.getTitle();
        sid = nb.getSid();
        tid = nb.getTid();
        outline = nb.getOutline();
        type = nb.getType();
        update_time = nb.getUpdate_time();
        json_url = nb.getJson_url();
        subjectsort = nb.getSubjectsort();
        columnid = nb.getColumnid();
        String[] nbImgs = nb.getImgs();
        StringBuilder sb = new StringBuilder();
        if (null != nbImgs) {
            for (String s : nbImgs) {
                sb.append(s);
                sb.append(",");
            }
            if (sb.length() > 1) {
                imgs = sb.substring(0, sb.length() - 1);
            }
        }
        rtype = nb.getRtype();
        comcount = nb.getComcount();
        sort_order = nb.getSort_order();
        status = nb.getStatus();
        comflag = nb.getComflag();
        copyfrom = nb.getCopyfrom();
        fav = nb.getFav();
        attname = nb.getAttname();
        like = nb.getLike();
        unlike = nb.getUnlike();
    }

    public NewsBean getNewsBean() {
        NewsBean nb = new NewsBean();

        nb.setNid(nid + "");
        nb.setTid(tid);
        nb.setComcount(comcount);
        nb.setComflag(comflag);
        nb.setJson_url(json_url);
        nb.setOutline(outline);
        nb.setRtype(rtype);
        nb.setSort_order(sort_order);
        nb.setSid(sid);
        nb.setType(type);
        nb.setUpdate_time(update_time);
        nb.setTitle(title);
        nb.setStatus(status);
        nb.setSubjectsort(subjectsort);
        nb.setColumnid(columnid);
        nb.setCopyfrom(copyfrom);
        nb.setFav(fav);
        nb.setAttname(attname);
        nb.setLike(like);
        nb.setUnlike(unlike);
        if (!TextUtils.isEmpty(imgs)) {
            String[] nbImgs = imgs.split(",");
            nb.setImgs(nbImgs);
        }

        return nb;
    }


    public int getNid() {
        return nid;
    }

    public String getTitle() {
        return title;
    }

    public String getSid() {
        return sid;
    }

    public String getTid() {
        return tid;
    }

    public String getOutline() {
        return outline;
    }

    public String getType() {
        return type;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public String getJson_url() {
        return json_url;
    }

    public String getImgs() {
        return imgs;
    }

    public String getRtype() {
        return rtype;
    }

    public String getComcount() {
        return comcount;
    }

    public String getSort_order() {
        return sort_order;
    }

    public String getStatus() {
        return status;
    }

    public String getComflag() {
        return comflag;
    }

    public String getCopyfrom() {
        return copyfrom;
    }

    public String getFav() {
        return fav;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public void setJson_url(String json_url) {
        this.json_url = json_url;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public void setRtype(String rtype) {
        this.rtype = rtype;
    }

    public void setComcount(String comcount) {
        this.comcount = comcount;
    }

    public void setSort_order(String sort_order) {
        this.sort_order = sort_order;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setComflag(String comflag) {
        this.comflag = comflag;
    }

    public int getIsreaded() {
        return isreaded;
    }

    public void setIsreaded(int isreaded) {
        this.isreaded = isreaded;
    }

    public String getSubjectsort() {
        return subjectsort;
    }

    public void setSubjectsort(String subjectsort) {
        this.subjectsort = subjectsort;
    }


    public String getColumnid() {
        return columnid;
    }

    public void setColumnid(String columnid) {
        this.columnid = columnid;
    }

    public void setCopyfrom(String copyfrom) {
        this.copyfrom = copyfrom;
    }

    public void setFav(String fav) {
        this.fav = fav;
    }

    public String getAttname() {
        return attname;
    }

    public void setAttname(String attname) {
        this.attname = attname;
    }

    @Override
    public int compareTo(NewsBeanDB another) {

        if (nid > another.getNid()) {
            return 1;
        } else if (nid < another.getNid()) {
            return -1;
        }
        return 0;
    }

}
