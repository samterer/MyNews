package com.hzpd.modle;

import java.io.Serializable;

public class VideoItemBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private String vid;
	private String title;
	private String time;
	private String mainpic;
	private String json_url;

	public VideoItemBean() {

	}

	public VideoItemBean(CollectionJsonBean cb) {
		this.vid = cb.getData().getId();
		this.title = cb.getData().getTitle();
		this.time = cb.getData().getTime();
		this.mainpic = cb.getData().getThumb();
		this.json_url = cb.getData().getJson_url();
	}

	public String getVid() {
		return vid;
	}

	public void setVid(String vid) {
		this.vid = vid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getMainpic() {
		return mainpic;
	}

	public void setMainpic(String mainpic) {
		this.mainpic = mainpic;
	}

	public String getJson_url() {
		return json_url;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}


}
