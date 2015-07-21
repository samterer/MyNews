package com.hzpd.modle.db;

import android.text.TextUtils;

import com.hzpd.modle.VideoItemBean;
import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Unique;


@Table(name = "videoitemdb")
public class VideoItemBeanDb extends BaseDB {

	@Unique
	@Column(column = "vid")
	private int vid;//": 
	@Column(column = "title")
	private String title;//": "3333",
	@Column(column = "time")
	private String time;//": "2014-12-18 18:41:13",
	@Column(column = "mainpic")
	private String mainpic;//": "http://58.68.134.165:8081/",
	@Column(column = "json_url")
	private String json_url;//": "http://58.68.134.165:8081/./cms_json/zqzx/Video/Content/201412/18/8"

	public VideoItemBeanDb() {
	}

	public VideoItemBeanDb(VideoItemBean bean) {
		if (!TextUtils.isEmpty(bean.getVid())) {
			if (TextUtils.isDigitsOnly(bean.getVid())) {
				try {
					vid = Integer.parseInt(bean.getVid());
				} catch (Exception e) {
					vid = -1;
				}
			}
		}
		this.time = bean.getTime();
		this.title = bean.getTitle();
		this.mainpic = bean.getMainpic();
		this.json_url = bean.getJson_url();
	}

	public VideoItemBean getVideoItemBean() {
		VideoItemBean bean = new VideoItemBean();
		bean.setVid(vid + "");
		bean.setTitle(title);
		bean.setTime(time);
		bean.setMainpic(mainpic);
		bean.setJson_url(json_url);

		return bean;
	}

	public int getVid() {
		return vid;
	}

	public String getTitle() {
		return title;
	}

	public String getTime() {
		return time;
	}

	public String getMainpic() {
		return mainpic;
	}

	public String getJson_url() {
		return json_url;
	}

	public void setVid(int vid) {
		this.vid = vid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setMainpic(String mainpic) {
		this.mainpic = mainpic;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

}
