package com.hzpd.modle.event;

public class WeatherMsgEvent {

	private String event;


	public WeatherMsgEvent(String event) {
		this.event = event;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}


}
