package com.hzpd.modle;

import java.util.List;

public class ZhuanTiListBean {

	private String sid;
	private String title;
	private String outline;
	private String mainphoto;
	private List<NewsBean> relation_news;

	public String getSid() {
		return sid;
	}

	public String getTitle() {
		return title;
	}

	public String getOutline() {
		return outline;
	}

	public String getMainphoto() {
		return mainphoto;
	}

	public List<NewsBean> getRelation_news() {
		return relation_news;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setOutline(String outline) {
		this.outline = outline;
	}

	public void setMainphoto(String mainphoto) {
		this.mainphoto = mainphoto;
	}

	public void setRelation_news(List<NewsBean> relation_news) {
		this.relation_news = relation_news;
	}


}
