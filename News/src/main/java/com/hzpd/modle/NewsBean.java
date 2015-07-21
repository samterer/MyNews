package com.hzpd.modle;

import java.io.Serializable;


public class NewsBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private String nid;
	private String title;
	private String sid;
	private String tid;//频道
	private String outline;//子标题
	private String type;//item样式  4三联图  其他图文
	private String update_time;
	private String json_url;//": "http://demo.99cms.cn/cms_json/bj/News/Content/201410/20/9446"
	private String[] imgs;
	private String rtype;//（跳转脚标）1新闻  2图集  3直播 4专题  5关联新闻 6视频 7引用
	private String comcount;//": "0",
	private String sort_order;//": "1736823498",
	private String status;//": "0"删除
	private String comflag;//": "0",//0不能评论
	private String subjectsort;//": "0",//0不能评论
	private String columnid;


	public String getTid() {
		return tid;
	}

	public String getOutline() {
		return outline;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public void setOutline(String outline) {
		this.outline = outline;
	}

	public String getJson_url() {
		return json_url;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public String[] getImgs() {
		return imgs;
	}

	public void setImgs(String[] imgs) {
		this.imgs = imgs;
	}

	public String getRtype() {
		return rtype;
	}

	public void setRtype(String rtype) {
		this.rtype = rtype;
	}

	public String getComcount() {
		return comcount;
	}

	public void setComcount(String comcount) {
		this.comcount = comcount;
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

	public String getComflag() {
		return comflag;
	}

	public void setComflag(String comflag) {
		this.comflag = comflag;
	}

	public String getSubjectsort() {
		return subjectsort;
	}

	public void setSubjectsort(String subjectsort) {
		this.subjectsort = subjectsort;
	}

	public String getColumnid() {
		return columnid;
	}

	public void setColumnid(String columnid) {
		this.columnid = columnid;
	}

}
