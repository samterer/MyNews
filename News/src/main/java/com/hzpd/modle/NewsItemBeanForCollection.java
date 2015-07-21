package com.hzpd.modle;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

import java.util.List;

@Table(name = "newsitemco")
public class NewsItemBeanForCollection {

	@Id
	private int id;
	@Column(column = "collectionid")
	private String collectionid;
	@Column(column = "type")
	private String type;//":"1",
	@Column(column = "datetime")
	private String datetime;//":"2014-12-31 12:12:12"
	@Column(column = "colldataid")
	private String colldataid;
	@Column(column = "tid")
	private String tid;
	@Column(column = "title")
	private String title;//":"111111111111111",
	@Column(column = "time")
	private String time;//":"",
	@Column(column = "thumb")
	private String thumb;//":"",
	@Column(column = "json_url")
	private String json_url;//":"",
	@Column(column = "rtype")
	private String rtype;//":""
	@Column(column = "collect_time")
	private long collect_time;


	public NewsItemBeanForCollection() {
	}

	public NewsItemBeanForCollection(NewsBean newsbean) {
		colldataid = newsbean.getNid();
		title = newsbean.getTitle();
		tid = newsbean.getTid();
		type = "1";
		time = newsbean.getUpdate_time();
		json_url = newsbean.getJson_url();
		String limgs[] = newsbean.getImgs();
		if (null != limgs && limgs.length > 0) {
			thumb = limgs[0];
		}
		collect_time = System.currentTimeMillis();
	}

	public NewsItemBeanForCollection(NewsBean newsbean, String html5) {
		colldataid = newsbean.getNid();
		title = newsbean.getTitle();
		tid = newsbean.getTid();
		type = "4";
		time = newsbean.getUpdate_time();
		json_url = newsbean.getJson_url();
		String limgs[] = newsbean.getImgs();
		if (null != limgs && limgs.length > 0) {
			thumb = limgs[0];
		}
		collect_time = System.currentTimeMillis();
	}

	public NewsItemBeanForCollection(ImgListBean imglistbean) {
		colldataid = imglistbean.getPid();
		title = imglistbean.getTitle();
		type = "2";
		time = imglistbean.getCreate_time();
		json_url = imglistbean.getJson_url();
		collect_time = System.currentTimeMillis();

		List<ImageListSubBean> li = imglistbean.getSubphoto();
		if (li.size() > 0) {
			thumb = li.get(0).getSubphoto();
		}

	}

	public NewsItemBeanForCollection(VideoItemBean vib) {
		colldataid = vib.getVid();
		title = vib.getTitle();
		type = "3";
		time = vib.getTime();
		json_url = vib.getJson_url();
		collect_time = System.currentTimeMillis();
		thumb = vib.getMainpic();
	}


	public NewsBean getNewsBean() {
		NewsBean nb = new NewsBean();
		nb.setNid(colldataid);
		nb.setJson_url(json_url);

		nb.setTid(tid);
		nb.setTitle(title);
		nb.setType(type);
		nb.setUpdate_time(time);
		if (null != thumb) {
			String s[] = new String[3];
			s[0] = thumb;
			nb.setImgs(s);
		}

		return nb;
	}

	public CollectionJsonBean getCollectionJsonBean() {
		CollectionJsonBean cb = new CollectionJsonBean();
		CollectionDataBean cdb = new CollectionDataBean(colldataid, tid, title, time, thumb, json_url, rtype);
		cb.setData(cdb);
		cb.setDatetime(time);
		cb.setType(type);

		return cb;
	}

	public VideoItemBean getVideoItemBean() {
		VideoItemBean vib = new VideoItemBean();
		vib.setJson_url(this.json_url);
		vib.setMainpic(this.thumb);
		vib.setTitle(this.title);
		vib.setVid(this.colldataid);
		vib.setTime(this.time);
		return vib;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCollectionid() {
		return collectionid;
	}

	public String getType() {
		return type;
	}

	public String getDatetime() {
		return datetime;
	}

	public String getColldataid() {
		return colldataid;
	}

	public String getTid() {
		return tid;
	}

	public String getTitle() {
		return title;
	}

	public String getTime() {
		return time;
	}

	public String getThumb() {
		return thumb;
	}

	public String getJson_url() {
		return json_url;
	}

	public String getRtype() {
		return rtype;
	}

	public void setCollectionid(String collectionid) {
		this.collectionid = collectionid;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	public void setColldataid(String colldataid) {
		this.colldataid = colldataid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

	public void setRtype(String rtype) {
		this.rtype = rtype;
	}


}
