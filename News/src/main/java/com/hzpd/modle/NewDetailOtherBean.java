package com.hzpd.modle;

import java.io.Serializable;
import java.util.Arrays;

public class NewDetailOtherBean implements Serializable {

	private String columnid;
	private String comcount;
	private String comflag;
	private String copyfrom;
	private String[] imgs;
	private String json_url;
	private String nid;
	private String relation;
	private String rtype;
	private String rvalue;
	private String sectionid;
	private String sid;
	private String sort_order;
	private String status;
	private String subjectsort;
	private String thumbnail;
	private String tid;
	private String title;
	private String type;
	private String update_time;

	@Override
	public String toString() {
		return "NewDetailOtherBean{" +
				"columnid='" + columnid + '\'' +
				", comcount='" + comcount + '\'' +
				", comflag='" + comflag + '\'' +
				", copyfrom='" + copyfrom + '\'' +
				", imgs=" + Arrays.toString(imgs) +
				", json_url='" + json_url + '\'' +
				", nid='" + nid + '\'' +
				", relation='" + relation + '\'' +
				", rtype='" + rtype + '\'' +
				", rvalue='" + rvalue + '\'' +
				", sectionid='" + sectionid + '\'' +
				", sid='" + sid + '\'' +
				", sort_order='" + sort_order + '\'' +
				", status='" + status + '\'' +
				", subjectsort='" + subjectsort + '\'' +
				", thumbnail='" + thumbnail + '\'' +
				", tid='" + tid + '\'' +
				", title='" + title + '\'' +
				", type='" + type + '\'' +
				", update_time='" + update_time + '\'' +
				'}';
	}

	public String getColumnid() {
		return columnid;
	}

	public void setColumnid(String columnid) {
		this.columnid = columnid;
	}

	public String getComcount() {
		return comcount;
	}

	public void setComcount(String comcount) {
		this.comcount = comcount;
	}

	public String getComflag() {
		return comflag;
	}

	public void setComflag(String comflag) {
		this.comflag = comflag;
	}

	public String getCopyfrom() {
		return copyfrom;
	}

	public void setCopyfrom(String copyfrom) {
		this.copyfrom = copyfrom;
	}

	public String[] getImgs() {
		return imgs;
	}

	public void setImgs(String[] imgs) {
		this.imgs = imgs;
	}

	public String getJson_url() {
		return json_url;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getRtype() {
		return rtype;
	}

	public void setRtype(String rtype) {
		this.rtype = rtype;
	}

	public String getRvalue() {
		return rvalue;
	}

	public void setRvalue(String rvalue) {
		this.rvalue = rvalue;
	}

	public String getSectionid() {
		return sectionid;
	}

	public void setSectionid(String sectionid) {
		this.sectionid = sectionid;
	}

	public String getSid() {
		return sid;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSubjectsort() {
		return subjectsort;
	}

	public void setSubjectsort(String subjectsort) {
		this.subjectsort = subjectsort;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}
}
