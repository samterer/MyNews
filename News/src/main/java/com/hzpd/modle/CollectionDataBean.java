package com.hzpd.modle;

public class CollectionDataBean {
	private String id;
	private String tid;
	private String title;//":"111111111111111",
	private String time;//":"",
	private String thumb;//":"",
	private String json_url;//":"",
	private String rtype;//":""


	public CollectionDataBean() {
	}

	public CollectionDataBean(String id, String tid, String title, String time,
	                          String thumb, String json_url, String rtype) {
		super();
		this.id = id;
		this.tid = tid;
		this.title = title;
		this.time = time;
		this.thumb = thumb;
		this.json_url = json_url;
		this.rtype = rtype;
	}

	public String getTitle() {
		return title;
	}

	public String getTime() {
		return time;
	}

	public String getId() {
		return id;
	}

	public String getTid() {
		return tid;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getThumb() {
		return thumb;
	}

	public String getJson_url() {
		return json_url;
	}

	public String getRtype() {
		return rtype;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

	public void setRtype(String rtype) {
		this.rtype = rtype;
	}

}
