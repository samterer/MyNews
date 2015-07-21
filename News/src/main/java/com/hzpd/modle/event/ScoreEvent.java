package com.hzpd.modle.event;

public class ScoreEvent {

	private String eventid;//":""
	private String time;//":"1234567"

	public ScoreEvent(String eventid) {
		this.eventid = eventid;
		time = "" + (System.currentTimeMillis() / 1000);
	}

	public String getEventid() {
		return eventid;
	}

	public void setEventid(String eventid) {
		this.eventid = eventid;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}


}
