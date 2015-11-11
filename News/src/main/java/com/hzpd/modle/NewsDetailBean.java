package com.hzpd.modle;

import android.text.Html;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 */

/**
 * @author Administrator
 */
public class NewsDetailBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nid;
    private String tid;
    private String title;
    private String copyfrom;
    private String update_time;
    private String comcount;
    private String content;
    private String outline;
    private String realtion;
    private NewDetailVedioBean video;
    private List<NewsDetailImgList> pic;
    private String link;
    private String authorname;
    private int vote;// 判断新闻是否有投票选项 0没有 1有
    private String type;//": "2",
    private String audiourl;//": "",
    private String videoid;//": "",
    private String[] tag;
    private List<NewsBean> ref;

    public NewsDetailBean() {
    }

    public NewsDetailBean(NewsBean nb) {
        this.nid = nb.getNid();
        this.tid = nb.getTid();
        this.title = nb.getTitle();
        this.type = nb.getType();
        this.outline = nb.getOutline();
        this.update_time = nb.getUpdate_time();
        this.copyfrom = nb.getCopyfrom();
        this.fav = nb.getFav();
        this.comcount = nb.getComcount();
    }

    private String fav;

    public String getFav() {
        return fav;
    }

    public void setFav(String fav) {
        this.fav = fav;
    }

    public List<NewsBean> getRef() {
        return ref;
    }

    public void setRef(List<NewsBean> ref) {
        this.ref = ref;
    }

    public String[] getTag() {
        return tag;
    }

    public void setTag(String[] tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "NewsDetailBean{" +
                "nid='" + nid + '\'' +
                ", tid='" + tid + '\'' +
                ", title='" + title + '\'' +
                ", copyfrom='" + copyfrom + '\'' +
                ", update_time='" + update_time + '\'' +
                ", comcount='" + comcount + '\'' +
                ", content='" + content + '\'' +
                ", outline='" + outline + '\'' +
                ", realtion='" + realtion + '\'' +
                ", video=" + video +
                ", pic=" + pic +
                ", link='" + link + '\'' +
                ", authorname='" + authorname + '\'' +
                ", vote=" + vote +
                ", type='" + type + '\'' +
                ", audiourl='" + audiourl + '\'' +
                ", videoid='" + videoid + '\'' +
                ", tag=" + Arrays.toString(tag) +
                ", ref=" + ref +
                ", fav='" + fav + '\'' +
                '}';
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public List<NewsDetailImgList> getPic() {
        return pic;
    }

    public void setPic(List<NewsDetailImgList> pic) {
        this.pic = pic;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public NewDetailVedioBean getVideo() {
        return video;
    }

    public void setVideo(NewDetailVedioBean video) {
        this.video = video;
    }

    public String getRealtion() {
        return realtion;
    }

    public void setRealtion(String realtion) {
        this.realtion = realtion;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getOutline() {
        return outline;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Html.fromHtml(title).toString();
    }

    public String getCopyfrom() {
        return copyfrom;
    }

    public void setCopyfrom(String copyfrom) {
        this.copyfrom = copyfrom;
    }

    public String getComcount() {
        return comcount;
    }

    public void setComcount(String comcount) {
        this.comcount = comcount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorname() {
        return authorname;
    }

    public void setAuthorname(String authorname) {
        this.authorname = authorname;
    }

    public String getType() {
        return type;
    }

    public String getAudiourl() {
        return audiourl;
    }

    public String getVideoid() {
        return videoid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAudiourl(String audiourl) {
        this.audiourl = audiourl;
    }

    public void setVideoid(String videoid) {
        this.videoid = videoid;
    }

}
