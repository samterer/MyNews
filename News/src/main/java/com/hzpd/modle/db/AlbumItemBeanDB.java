package com.hzpd.modle.db;

import com.hzpd.modle.ImageListSubBean;
import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.Table;


@Table(name = "albumitembean")
public class AlbumItemBeanDB extends BaseDB {

	@Column(column = "subdesc")
	private String subdesc;
	@Column(column = "subphoto")
	private String subphoto;//

	@Foreign(column = "albumbeanid", foreign = "id")
	public AlbumBeanDB parent;

	public AlbumItemBeanDB() {
	}

	public AlbumItemBeanDB(ImageListSubBean bean, AlbumBeanDB parent) {
		this.subdesc = bean.getSubdesc();
		this.subphoto = bean.getSubphoto();
		this.parent = parent;
	}

	public ImageListSubBean getImageListSubBean() {
		ImageListSubBean bean = new ImageListSubBean();

		bean.setSubdesc(subdesc);
		bean.setSubphoto(subphoto);

		return bean;
	}

	public String getSubdesc() {
		return subdesc;
	}

	public String getSubphoto() {
		return subphoto;
	}

	public AlbumBeanDB getParent() {
		return parent;
	}

	public void setSubdesc(String subdesc) {
		this.subdesc = subdesc;
	}

	public void setSubphoto(String subphoto) {
		this.subphoto = subphoto;
	}

	public void setParent(AlbumBeanDB parent) {
		this.parent = parent;
	}

}
