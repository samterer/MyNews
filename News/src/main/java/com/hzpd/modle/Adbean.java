package com.hzpd.modle;

import java.util.List;

public class Adbean {
	private String imgurl;//": "http://www.99cms_hb.info//Public/Uploads/ad/1418219068.jpg",
	private List<String> tid;//": [],
	private String link;//": "fsad"

	public String getImgurl() {
		return imgurl;
	}

	public List<String> getTid() {
		return tid;
	}

	public String getLink() {
		return link;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}

	public void setTid(List<String> tid) {
		this.tid = tid;
	}

	public void setLink(String link) {
		this.link = link;
	}

}
