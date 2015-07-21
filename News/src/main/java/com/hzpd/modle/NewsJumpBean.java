package com.hzpd.modle;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "newsjumpbean")
public class NewsJumpBean {

	@Id
	private int id;
	@Column(column = "url")
	private String url;
	@Column(column = "content")
	private String content;

	public NewsJumpBean() {
	}

	public NewsJumpBean(String url, String content) {
		super();
		this.url = url;
		this.content = content;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
