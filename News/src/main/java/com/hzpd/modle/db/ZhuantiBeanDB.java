package com.hzpd.modle.db;

import com.hzpd.modle.SubjectItemBean;
import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Unique;

@Table(name = "zhuantibean")
public class ZhuantiBeanDB extends BaseDB {

	@Unique
	@Column(column = "sid")
	private String sid;//": "5",
	@Column(column = "title")
	private String title;//": "测试",
	@Column(column = "subdesc")
	private String subdesc;//": "11",
	@Column(column = "create_time")
	private String create_time;//": "2015-01-14 11:37:57",
	@Column(column = "mainphoto")
	private String mainphoto;//": "http://58.68.134.165:8081/",
	@Column(column = "json_url")
	private String json_url;//": "http://58.68.134.165:8081/./cms_json/zqzx/Subject/Content/5/latest"

	public ZhuantiBeanDB() {

	}

	public ZhuantiBeanDB(SubjectItemBean bean) {
		this.sid = bean.getSid();
		this.title = bean.getTitle();
		this.subdesc = bean.getSubdesc();
		this.create_time = bean.getCreate_time();
		this.mainphoto = bean.getMainphoto();
		this.json_url = bean.getJson_url();
	}

	public SubjectItemBean getSubjectItemBean() {
		SubjectItemBean bean = new SubjectItemBean();
		bean.setSid(sid);
		bean.setTitle(title);
		bean.setSubdesc(subdesc);
		bean.setCreate_time(create_time);
		bean.setMainphoto(mainphoto);
		bean.setJson_url(json_url);
		return bean;
	}

	public String getSid() {
		return sid;
	}

	public String getTitle() {
		return title;
	}

	public String getSubdesc() {
		return subdesc;
	}

	public String getCreate_time() {
		return create_time;
	}

	public String getMainphoto() {
		return mainphoto;
	}

	public String getJson_url() {
		return json_url;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setSubdesc(String subdesc) {
		this.subdesc = subdesc;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public void setMainphoto(String mainphoto) {
		this.mainphoto = mainphoto;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

}
