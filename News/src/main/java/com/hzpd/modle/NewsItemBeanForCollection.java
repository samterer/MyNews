package com.hzpd.modle;

import android.text.TextUtils;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

import java.util.Arrays;
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
	@Column(column = "nid")
	private String nid;
	@Column(column = "tid")
	private String tid;
	@Column(column = "title")
	private String title;//":"111111111111111",
	@Column(column = "time")
	private String time;//":"",
	@Column(column = "imgs")
	private String imgs;//":"",
	@Column(column = "json_url")
	private String json_url;//":"",
	@Column(column = "rtype")
	private String rtype;//":""
	@Column(column = "collect_time")
	private long collect_time;
	@Column(column = "copyfrom")
	private String copyfrom;//新闻来源
	@Column(column = "fav")
	private String fav;//收藏数目
	@Column(column = "comcount")
	private String comcount;//": "0",
	@Column(column = "like")
	private String like;
	@Column(column = "unlike")
	private String unlike;



	public NewsItemBeanForCollection() {
	}

	public NewsItemBeanForCollection(NewsBean newsbean) {
		nid = newsbean.getNid();
		title = newsbean.getTitle();
		tid = newsbean.getTid();
		type = "1";
		time = newsbean.getUpdate_time();
		json_url = newsbean.getJson_url();
		copyfrom=newsbean.getCopyfrom();
		fav=newsbean.getFav();
		comcount=newsbean.getComcount();
		like=newsbean.getLike();
		unlike=newsbean.getUnlike();
//		String limgs[] = newsbean.getImgs();
//		if (null != limgs && limgs.length > 0) {
//			imgs = limgs[0];
//		}
		String[] nbImgs = newsbean.getImgs();
		StringBuilder sb = new StringBuilder();
		if (null != nbImgs) {
			for (String s : nbImgs) {
				sb.append(s);
				sb.append(",");
			}
			if (sb.length() > 1) {
				imgs = sb.substring(0, sb.length() - 1);
			}
		}
		collect_time = System.currentTimeMillis();
	}

	public NewsItemBeanForCollection(NewsBean newsbean, String html5) {
		nid = newsbean.getNid();
		title = newsbean.getTitle();
		tid = newsbean.getTid();
		type = "4";
		time = newsbean.getUpdate_time();
		json_url = newsbean.getJson_url();
		copyfrom=newsbean.getCopyfrom();
		fav=newsbean.getFav();
		comcount=newsbean.getComcount();
		like=newsbean.getLike();
		unlike=newsbean.getUnlike();
//		String limgs[] = newsbean.getImgs();
//		if (null != limgs && limgs.length > 0) {
//			imgs = limgs[0];
//		}
		String[] nbImgs = newsbean.getImgs();
		StringBuilder sb = new StringBuilder();
		if (null != nbImgs) {
			for (String s : nbImgs) {
				sb.append(s);
				sb.append(",");
			}
			if (sb.length() > 1) {
				imgs = sb.substring(0, sb.length() - 1);
			}
		}

		collect_time = System.currentTimeMillis();
	}

	public NewsItemBeanForCollection(ImgListBean imglistbean) {
		nid = imglistbean.getPid();
		title = imglistbean.getTitle();
		type = "2";
		time = imglistbean.getCreate_time();
		json_url = imglistbean.getJson_url();
		collect_time = System.currentTimeMillis();
		copyfrom=imglistbean.getCopyfrom();
		fav=imglistbean.getFav();
		comcount=imglistbean.getComcount();
		List<ImageListSubBean> li = imglistbean.getSubphoto();
		if (li.size() > 0) {
			imgs= li.get(0).getSubphoto();
		}

	}

	public NewsItemBeanForCollection(VideoItemBean vib) {
		nid = vib.getVid();
		title = vib.getTitle();
		type = "3";
		time = vib.getTime();
		json_url = vib.getJson_url();
		collect_time = System.currentTimeMillis();
		imgs = vib.getMainpic();
		copyfrom=vib.getCopyfrom();
		fav=vib.getFav();
		comcount=vib.getComcount();
	}


	public NewsBean getNewsBean() {
		NewsBean nb = new NewsBean();
		nb.setNid(nid);
		nb.setJson_url(json_url);

		nb.setTid(tid);
		nb.setTitle(title);
		nb.setType(type);
		nb.setUpdate_time(time);

		nb.setCopyfrom(copyfrom);
		nb.setFav(fav);
		nb.setComcount(comcount);

//		if (null != imgs) {
//			String s[] = new String[3];
//			s[0] = imgs;
//			nb.setImgs(s);
//		}

		if (!TextUtils.isEmpty(imgs)) {
			String[] nbImgs = imgs.split(",");
			nb.setImgs(nbImgs);
		}

		return nb;
	}

	public CollectionJsonBean getCollectionJsonBean() {
		CollectionJsonBean cb = new CollectionJsonBean();
		String[] nbImgs=null;
		if (!TextUtils.isEmpty(imgs)) {
			 nbImgs = imgs.split(",");
		}
		CollectionDataBean cdb = new CollectionDataBean(nid, tid, title, time, nbImgs, json_url, rtype,copyfrom,fav,comcount);
//		CollectionDataBean cdb = new CollectionDataBean(nid, tid, title, time, thumb, json_url, rtype);
		cb.setData(cdb);
		cb.setDatetime(time);
		cb.setType(type);

		return cb;
	}

	public VideoItemBean getVideoItemBean() {
		VideoItemBean vib = new VideoItemBean();
		vib.setJson_url(this.json_url);
		vib.setMainpic(this.imgs);
		vib.setTitle(this.title);
		vib.setVid(this.nid);
		vib.setTime(this.time);
		vib.setCopyfrom(this.copyfrom);
		vib.setFav(this.fav);
		vib.setComcount(this.comcount);
		return vib;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getCollect_time() {
		return collect_time;
	}

	public void setCollect_time(long collect_time) {
		this.collect_time = collect_time;
	}

	public String getCopyfrom() {
		return copyfrom;
	}

	public void setCopyfrom(String copyfrom) {
		this.copyfrom = copyfrom;
	}

	public String getFav() {
		return fav;
	}

	public void setFav(String fav) {
		this.fav = fav;
	}

	public String getComcount() {
		return comcount;
	}

	public void setComcount(String comcount) {
		this.comcount = comcount;
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


	public String getTid() {
		return tid;
	}

	public String getTitle() {
		return title;
	}

	public String getTime() {
		return time;
	}

	public String getImgs() {
		return imgs;
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

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
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

	public void setImgs(String imgs) {
		this.imgs = imgs;
	}

	public void setJson_url(String json_url) {
		this.json_url = json_url;
	}

	public void setRtype(String rtype) {
		this.rtype = rtype;
	}



	public String getLike() {
		return like;
	}

	public void setLike(String like) {
		this.like = like;
	}

	public String getUnlike() {
		return unlike;
	}

	public void setUnlike(String unlike) {
		this.unlike = unlike;
	}

	@Override
	public String toString() {
		return "NewsItemBeanForCollection{" +
				"id=" + id +
				", collectionid='" + collectionid + '\'' +
				", type='" + type + '\'' +
				", datetime='" + datetime + '\'' +
				", nid='" + nid + '\'' +
				", tid='" + tid + '\'' +
				", title='" + title + '\'' +
				", time='" + time + '\'' +
				", imgs='" + imgs + '\'' +
				", json_url='" + json_url + '\'' +
				", rtype='" + rtype + '\'' +
				", collect_time=" + collect_time +
				", copyfrom='" + copyfrom + '\'' +
				", fav='" + fav + '\'' +
				", comcount='" + comcount + '\'' +
				", like='" + like + '\'' +
				", unlike='" + unlike + '\'' +
				'}';
	}

}
