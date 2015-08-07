package com.hzpd.modle.event;

public class DayNightEvent {
	private int daynightColor;

	public DayNightEvent() {
	}

	public DayNightEvent(int daynightColor) {
		super();
		this.daynightColor = daynightColor;
	}

	public int getDaynightColor() {
		return daynightColor;
	}

	public void setDaynightColor(int daynightColor) {
		this.daynightColor = daynightColor;
	}


}
