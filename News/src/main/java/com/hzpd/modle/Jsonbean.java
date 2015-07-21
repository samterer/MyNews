package com.hzpd.modle;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * @author lixinyuan
 */
@Table(name = "jsonbean")
public class Jsonbean {

	@Id
	private int id;
	@Column(column = "fid")
	private String fid;
	//	@Column(column="fidtype")
//	private String fidtype;//新闻1，图片2，视频3)9评论
	@Column(column = "data")
	private String data;

	public Jsonbean() {
	}

	public Jsonbean(String mfid, String mdata) {
		this.fid = mfid;
		this.data = mdata;
	}


	public int getId() {
		return id;
	}

	public String getFid() {
		return fid;
	}

	public String getData() {
		return data;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public void setData(String data) {
		this.data = data;
	}

}
