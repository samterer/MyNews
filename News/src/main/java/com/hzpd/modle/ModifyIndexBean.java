package com.hzpd.modle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.ui.App;

import java.util.HashMap;
import java.util.List;


public class ModifyIndexBean {

	private String channel;
	private HashMap<String, ModifyPageBean> page;
	private HashMap<String, ModifyDetailPageBean> content;

	public ModifyIndexBean(JSONObject obj) {
		page = new HashMap<String, ModifyPageBean>();
		content = new HashMap<String, ModifyDetailPageBean>();

		channel = obj.getString("channel");

		List<ModifyPageBean> ll = JSONArray.parseArray(
				obj.getJSONArray("page").toJSONString()
				, ModifyPageBean.class);

		if (ll != null && ll.size() > 0) {
			for (ModifyPageBean mpb : ll) {
				page.put(mpb.getPageNum(), mpb);
			}
		}
		List<ModifyDetailPageBean> mdpbll = JSONArray.parseArray(
				obj.getJSONArray("content").toJSONString()
				, ModifyDetailPageBean.class);
		if (mdpbll != null && mdpbll.size() > 0) {
			for (ModifyDetailPageBean mdpb : mdpbll) {
				content.put(mdpb.getPageNum(), mdpb);
			}
		}
	}


	public String getChannel() {
		return channel;
	}


	public long getPageUpdateTime(String pageNum) {
		ModifyPageBean mpb = page.get(pageNum);
		if (mpb == null) {
			return 0l;
		}
		long time = App.getTimeStamp(mpb.getUpdate_time(), "yyyy-MM-dd HH:mm:ss");
		return time;
	}

	public long getContentUpdateTime(String nid) {
		ModifyDetailPageBean mdpb = content.get(nid);
		if (mdpb == null) {
			return 0l;
		}
		long time = App.getTimeStamp(mdpb.getUpdate_time(), "yyyy-MM-dd HH:mm:ss");
		return time;
	}


	public String getPageUpdateTimeString(String pageNum) {
		ModifyPageBean mpb = page.get(pageNum);
		if (mpb == null) {
			return null;
		}
		return mpb.getUpdate_time();
	}

	public String getContentUpdateTimeString(String nid) {
		ModifyDetailPageBean mdpb = content.get(nid);
		if (mdpb == null) {
			return null;
		}
		return mdpb.getUpdate_time();
	}


}
