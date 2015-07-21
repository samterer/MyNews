package com.hzpd.modle;


public class CheckCommentNews {

	private String nid;
	private String create_time;
	private String title;
	private String copyfrom;
	private String comcount;
	private String coverpicid;
	private String dateline;

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCopyfrom() {
		return copyfrom;
	}

	public void setCopyfrom(String copyfrom) {
		this.copyfrom = copyfrom;
	}

	public String getComcount() {
		if (null == comcount || "".equals(comcount)) {
			return "0";
		}
		return comcount;
	}

	public void setComcount(String comcount) {
		this.comcount = comcount;
	}

	public String getCoverpicid() {
		return coverpicid;
	}

	public void setCoverpicid(String coverpicid) {
		this.coverpicid = coverpicid;
	}

	public String getDateline() {
		return dateline;
	}

	public void setDateline(String dateline) {
		this.dateline = dateline;
	}

}
