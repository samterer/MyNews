package com.hzpd.modle.vote;


/**
 * 直播item信息
 *
 * @author Administrator
 */
public class LiveItemEntity {

	private int id;
	private int nid;
	private String text;
	private String img;
	private String time;
	private String author;

	public LiveItemEntity() {
		super();
	}

	public LiveItemEntity(int id, int nid, String text, String img,
	                      String time, String author) {
		super();
		this.id = id;
		this.nid = nid;
		this.text = text;
		this.img = img;
		this.time = time;
		this.author = author;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

}
