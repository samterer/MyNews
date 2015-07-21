package com.hzpd.modle.vote;


import java.util.ArrayList;

public class VoteNewsEntity {
	private String vote_pic_url;
	private String vote_summary;
	private int vote_subjectid;
	private String vote_title;
	private String vote_time;
	private int Vote_type;// 0：单选
	private int Vote_status;// 状态0；没投过票
	private int vote_num;
	private String newid;

	private ArrayList<VoteEntity> list = new ArrayList<VoteEntity>();

	public VoteNewsEntity(String vote_pic_url, String vote_summary,
	                      int vote_subjectid, String vote_title, String vote_time,
	                      int vote_type, int vote_status, int vote_num, String newid,
	                      ArrayList<VoteEntity> list) {
		super();
		this.vote_pic_url = vote_pic_url;
		this.vote_summary = vote_summary;
		this.vote_subjectid = vote_subjectid;
		this.vote_title = vote_title;
		this.vote_time = vote_time;
		Vote_type = vote_type;
		Vote_status = vote_status;
		this.vote_num = vote_num;
		this.newid = newid;
		this.list = list;
	}

	public VoteNewsEntity() {
		super();
	}

	public String getNewid() {
		return newid;
	}

	public void setNewid(String newid) {
		this.newid = newid;
	}

	public int getVote_subjectid() {
		return vote_subjectid;
	}

	public void setVote_subjectid(int vote_subjectid) {
		this.vote_subjectid = vote_subjectid;
	}

	public String getVote_pic_url() {
		return vote_pic_url;
	}

	public void setVote_pic_url(String vote_pic_url) {
		this.vote_pic_url = vote_pic_url;
	}

	public String getVote_summary() {
		return vote_summary;
	}

	public void setVote_summary(String vote_summary) {
		this.vote_summary = vote_summary;
	}

	public String getVote_title() {
		return vote_title;
	}

	public void setVote_title(String vote_title) {
		this.vote_title = vote_title;
	}

	public String getVote_time() {
		return vote_time;
	}

	public void setVote_time(String vote_time) {
		this.vote_time = vote_time;
	}

	public int getVote_type() {
		return Vote_type;
	}

	public void setVote_type(int vote_type) {
		Vote_type = vote_type;
	}

	public int getVote_status() {
		return Vote_status;
	}

	public void setVote_status(int vote_status) {
		Vote_status = vote_status;
	}

	public int getVote_num() {
		return vote_num;
	}

	public void setVote_num(int vote_num) {
		this.vote_num = vote_num;
	}

	public ArrayList<VoteEntity> getList() {
		return list;
	}

	public void setList(ArrayList<VoteEntity> list) {
		this.list = list;
	}

}
