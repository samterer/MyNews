package com.hzpd.modle;

import java.io.Serializable;

public class NewDetailOtherBean implements Serializable {

	private String title;

	private String nid;

	private String type;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
