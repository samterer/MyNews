package com.hzpd.modle;

import android.text.Html;

import java.io.Serializable;

public class NewsPageListBean implements Serializable, Comparable<NewsPageListBean> {
	private static final long serialVersionUID = -8736403233689482029L;

	private String title;//title
	private String imgurl;//smallimgurl
	private String newid;//nid
	private String sid;//sid
	private String sort_order;
	private String type;//: "2",;
	private String update_time;
	private String json_url;//": "http://demo.99cms.cn/cms_json/bj/News/Content/201410/20/9446"
	private String tid;


	//	private String []imgs;

	public String getSid() {
		return sid;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public String getJson_url() {
		return json_url;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getSort_order() {
		return sort_order;
	}

	public void setSort_order(String sort_order) {
		this.sort_order = sort_order;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = Html.fromHtml(title).toString();
	}

	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}

	public String getNewid() {
		return newid;
	}

	public void setNewid(String newid) {
		this.newid = newid;
	}

	@Override
	public int compareTo(NewsPageListBean another) {
		try {
			int i = Integer.parseInt(sort_order);
			int j = Integer.parseInt(another.getSort_order());
			if (i > j) {
				return 1;
			} else if (i == j) {
				return 0;
			} else {
				return -1;
			}
		} catch (Exception e) {

		}
		return 0;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}


}
