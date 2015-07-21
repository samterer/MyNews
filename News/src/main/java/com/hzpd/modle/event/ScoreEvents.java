package com.hzpd.modle.event;

import java.util.ArrayList;
import java.util.List;

public class ScoreEvents {
	private String uid;//":"123",
	private List<ScoreEvent> events;//

	public ScoreEvents(String uid) {
		this.uid = uid;
		events = new ArrayList<ScoreEvent>();
	}

	public void addEvent(ScoreEvent event) {
		events.add(event);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public List<ScoreEvent> getEvents() {
		return events;
	}

	public void setEvents(List<ScoreEvent> events) {
		this.events = events;
	}


}
