package com.hzpd.modle;

import java.io.Serializable;

public class FavoritesListBean implements Serializable {

	private String id;

	private String fid;

	private String fidtype;

	private String create_time;

	private String title;

	private String imgurl;

	private String aid;

	private int isPl = -1;

	private int savePosition;

	public int getSavePosition() {
		return savePosition;
	}

	public void setSavePosition(int savePosition) {
		this.savePosition = savePosition;
	}

	public String getAid() {
		return aid;
	}

	public void setAid(String aid) {
		this.aid = aid;
	}

	public int getIsPl() {
		return isPl;
	}

	public void setIsPl(int isPl) {
		this.isPl = isPl;
	}

	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public String getFidtype() {
		return fidtype;
	}

	public void setFidtype(String fidtype) {
		this.fidtype = fidtype;
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

}
