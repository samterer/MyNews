package com.hzpd.modle.db;

import com.hzpd.modle.ImageListSubBean;
import com.hzpd.modle.ImgListBean;
import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Finder;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Unique;

import java.util.ArrayList;
import java.util.List;


@Table(name = "albumbean")
public class AlbumBeanDB extends BaseDB {


	@Unique
	@Column(column = "pid")
	private String pid;
	@Column(column = "title")
	private String title;
	@Column(column = "count")
	private String count;
	@Column(column = "siteid")
	private String siteid;
	@Column(column = "create_time")
	private String create_time;
	@Column(column = "json_url")
	private String json_url;

	@Finder(valueColumn = "id", targetColumn = "albumbeanid")
	private List<AlbumItemBeanDB> subphoto;


	public AlbumBeanDB() {
	}

	public AlbumBeanDB(ImgListBean bean) {
		this.pid = bean.getPid();
		this.title = bean.getTitle();
		this.count = bean.getCount();
		this.siteid = bean.getSiteid();
		this.create_time = bean.getCreate_time();
		this.json_url = bean.getJson_url();

		subphoto = new ArrayList<AlbumItemBeanDB>();
		if (null != bean.getSubphoto()) {
			for (ImageListSubBean ilsb : bean.getSubphoto()) {
				AlbumItemBeanDB aibdb = new AlbumItemBeanDB(ilsb, this);
				subphoto.add(aibdb);
			}
		}

	}

	public ImgListBean getImgListBean() {
		ImgListBean bean = new ImgListBean();
		bean.setPid(pid);
		bean.setTitle(title);
		bean.setCount(count);
		bean.setSiteid(siteid);
		bean.setCreate_time(create_time);
		bean.setJson_url(json_url);

		List<ImageListSubBean> ilsbList = new ArrayList<ImageListSubBean>();
		if (null != subphoto) {
			for (AlbumItemBeanDB ilsb : subphoto) {
				ilsbList.add(ilsb.getImageListSubBean());
			}
		}
		bean.setSubphoto(ilsbList);

		return bean;
	}

	public String getPid() {
		return pid;
	}

	public String getTitle() {
		return title;
	}

	public String getCount() {
		return count;
	}

	public String getSiteid() {
		return siteid;
	}

	public String getCreate_time() {
		return create_time;
	}

	public String getJson_url() {
		return json_url;
	}

	public List<AlbumItemBeanDB> getSubphoto() {
		return subphoto;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public void setSiteid(String siteid) {
		this.siteid = siteid;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

	public void setSubphoto(List<AlbumItemBeanDB> subphoto) {
		this.subphoto = subphoto;
	}

}
