package com.hzpd.modle;

public class O2oUserBean {

	private String username;
	private String pwd;

	public O2oUserBean() {
	}

	public O2oUserBean(String username, String pwd) {
		super();
		this.username = username;
		this.pwd = pwd;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
}
