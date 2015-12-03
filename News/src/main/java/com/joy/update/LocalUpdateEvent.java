package com.joy.update;

/**
 * 本地更新通知
 */
public class LocalUpdateEvent {
	//已下载完成
	public boolean done = false;

	public LocalUpdateEvent(boolean bool) {
		done = bool;
	}
}
