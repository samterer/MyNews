package com.hzpd.modle;


public class CollectionJsonBean {
	private String id;
	private CollectionDataBean data;//
	private String type;//":"1",
	private String datetime;//":"2014-12-31 12:12:12"

	public CollectionDataBean getData() {
		return data;
	}

	public void setData(CollectionDataBean data) {
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDatetime() {
		return datetime;
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
