package com.hzpd.utils;

import android.content.Context;

import com.hzpd.modle.event.ScoreEvent;
import com.hzpd.modle.event.ScoreEvents;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;

public class EventUtils {

	public static void sendStart(final Context context) {
		if (null == SPUtil.getInstance().getUser()) {
			return;
		}
		ScoreEvent event = new ScoreEvent("3");
		ScoreEvents events = new ScoreEvents(SPUtil.getInstance().getUser().getUid());
		events.addEvent(event);
		String station= SharePreferecesUtils.getParam(context, StationConfig.STATION, "def").toString();
		String siteid=null;
		String XF_UPLOADEVENT_url =null;
		if (station.equals(StationConfig.DEF)){
			siteid=InterfaceJsonfile.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.YN)){
			siteid= InterfaceJsonfile_YN.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_YN.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.TW)){
			siteid= InterfaceJsonfile_TW.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_TW.XF_UPLOADEVENT;
		}
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("siteid", siteid);
		params.addBodyParameter("jsonstr", FjsonUtil.toJsonString(events));

		httpUtils.send(HttpMethod.POST
				, XF_UPLOADEVENT_url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("sendStart-->" + responseInfo.result);
			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
	}

	public static void sendReadAtical(final Context context) {
		if (null == SPUtil.getInstance().getUser()) {
			return;
		}

		ScoreEvent event = new ScoreEvent("4");
		ScoreEvents events = new ScoreEvents(SPUtil.getInstance().getUser().getUid());
		events.addEvent(event);
		String station= SharePreferecesUtils.getParam(context, StationConfig.STATION, "def").toString();
		String siteid=null;
		String XF_UPLOADEVENT_url =null;
		if (station.equals(StationConfig.DEF)){
			siteid=InterfaceJsonfile.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.YN)){
			siteid= InterfaceJsonfile_YN.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_YN.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.TW)){
			siteid= InterfaceJsonfile_TW.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_TW.XF_UPLOADEVENT;
		}
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("siteid", siteid);
		params.addBodyParameter("jsonstr", FjsonUtil.toJsonString(events));

		httpUtils.send(HttpMethod.POST
				, XF_UPLOADEVENT_url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("sendReadAtical-->" + responseInfo.result);
			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
	}

	public static void sendShareAtival(final Context context) {
		if (null == SPUtil.getInstance().getUser()) {
			return;
		}

		ScoreEvent event = new ScoreEvent("5");
		ScoreEvents events = new ScoreEvents(SPUtil.getInstance().getUser().getUid());
		events.addEvent(event);
		String station= SharePreferecesUtils.getParam(context, StationConfig.STATION, "def").toString();
		String siteid=null;
		String XF_UPLOADEVENT_url =null;
		if (station.equals(StationConfig.DEF)){
			siteid=InterfaceJsonfile.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.YN)){
			siteid= InterfaceJsonfile_YN.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_YN.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.TW)){
			siteid= InterfaceJsonfile_TW.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_TW.XF_UPLOADEVENT;
		}
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("siteid", siteid);
		params.addBodyParameter("jsonstr", FjsonUtil.toJsonString(events));

		httpUtils.send(HttpMethod.POST
				, XF_UPLOADEVENT_url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("sendShareAtival-->" + responseInfo.result);
			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
	}

	public static void sendComment(final Context context) {
		if (null == SPUtil.getInstance().getUser()) {
			return;
		}
		if (null == SPUtil.getInstance().getUser()) {
			return;
		}
		ScoreEvent event = new ScoreEvent("6");
		ScoreEvents events = new ScoreEvents(SPUtil.getInstance().getUser().getUid());
		events.addEvent(event);
		String station= SharePreferecesUtils.getParam(context, StationConfig.STATION, "def").toString();
		String siteid=null;
		String XF_UPLOADEVENT_url =null;
		if (station.equals(StationConfig.DEF)){
			siteid=InterfaceJsonfile.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.YN)){
			siteid= InterfaceJsonfile_YN.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_YN.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.TW)){
			siteid= InterfaceJsonfile_TW.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_TW.XF_UPLOADEVENT;
		}
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("siteid", siteid);
		params.addBodyParameter("jsonstr", FjsonUtil.toJsonString(events));

		httpUtils.send(HttpMethod.POST
				, XF_UPLOADEVENT_url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("sendComment-->" + responseInfo.result);
			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
	}

	public static void sendPraise(final Context context) {
		if (null == SPUtil.getInstance().getUser()) {
			return;
		}

		ScoreEvent event = new ScoreEvent("7");
		ScoreEvents events = new ScoreEvents(SPUtil.getInstance().getUser().getUid());
		events.addEvent(event);
		String station= SharePreferecesUtils.getParam(context, StationConfig.STATION, "def").toString();
		String siteid=null;
		String XF_UPLOADEVENT_url =null;
		if (station.equals(StationConfig.DEF)){
			siteid=InterfaceJsonfile.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.YN)){
			siteid= InterfaceJsonfile_YN.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_YN.XF_UPLOADEVENT;
		}else if (station.equals(StationConfig.TW)){
			siteid= InterfaceJsonfile_TW.SITEID;
			XF_UPLOADEVENT_url =InterfaceJsonfile_TW.XF_UPLOADEVENT;
		}
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("siteid", siteid);
		params.addBodyParameter("jsonstr", FjsonUtil.toJsonString(events));

		httpUtils.send(HttpMethod.POST
				, XF_UPLOADEVENT_url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("sendPraise-->" + responseInfo.result);
			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
	}

}
