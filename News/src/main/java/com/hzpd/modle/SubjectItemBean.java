package com.hzpd.modle;

import java.io.Serializable;

public class SubjectItemBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private String sid;//": "5",
	private String title;//": "测试",
	private String subdesc;//": "11",
	private String create_time;//": "2015-01-14 11:37:57",
	private String mainphoto;//": "http://58.68.134.165:8081/",
	private String json_url;//": "http://58.68.134.165:8081/./cms_json/zqzx/Subject/Content/5/latest"

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubdesc() {
		return subdesc;
	}

	public void setSubdesc(String subdesc) {
		this.subdesc = subdesc;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getMainphoto() {
		return mainphoto;
	}

	public void setMainphoto(String mainphoto) {
		this.mainphoto = mainphoto;
	}

	public String getJson_url() {
		return json_url;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

}
