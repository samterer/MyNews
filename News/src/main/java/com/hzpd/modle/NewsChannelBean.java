package com.hzpd.modle;

import java.io.Serializable;

public class NewsChannelBean implements Comparable<NewsChannelBean>, Serializable {
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
}
