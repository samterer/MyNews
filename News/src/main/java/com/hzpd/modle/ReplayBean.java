package com.hzpd.modle;

import java.io.Serializable;

public class ReplayBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String title;
	private String type;
	private String jsonUrl;
	private String imgUrl;

	public ReplayBean() {
	}

	public ReplayBean(String id, String title, String type, String jsonUrl,
	                  String imgUrl) {
		super();
		this.id = id;
		this.title = title;
		this.type = type;
		this.jsonUrl = jsonUrl;
		this.imgUrl = imgUrl;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public String getJsonUrl() {
		return jsonUrl;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setJsonUrl(String jsonUrl) {
		this.jsonUrl = jsonUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

}
