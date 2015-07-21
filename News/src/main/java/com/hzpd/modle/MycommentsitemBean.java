package com.hzpd.modle;

public class MycommentsitemBean {

	private String praise;//":"1",
	private String content;//": "555555555555555",
	private String dateline;//": "2014-12-17 18:25:15"
	private String status;//1审核通过 -2审核中

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPraise() {
		return praise;
	}

	public void setPraise(String praise) {
		this.praise = praise;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDateline() {
		return dateline;
	}

	public void setDateline(String dateline) {
		this.dateline = dateline;
	}

}
