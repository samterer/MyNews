package com.hzpd.utils;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.modle.UserBean;
import com.hzpd.ui.App;
import com.lidroid.xutils.util.LogUtils;

/**
 * @author color
 *         程序配置
 */
public class SPUtil {
	/** 配置名称 * */
	private static final String SETTINGS = "HZPD";
	private ACache msp;
	private static SPUtil mSPutil;

	private UserBean user;

	private SPUtil() {
		msp = ACache.get(App.getInstance()
				.getApplicationContext(), SETTINGS);
	}

	public static synchronized SPUtil getInstance() {
		if (mSPutil == null) {
			mSPutil = new SPUtil();
		}
		return mSPutil;
	}

	public int getVersionCode() {
		String svc = msp.getAsString("versioncode");
		int vc = 0;
		if (!TextUtils.isEmpty(svc)) {
			try {
				if (TextUtils.isDigitsOnly(svc)) {
					vc = Integer.parseInt(svc);
				}
			} catch (Exception e) {
			}
		}
		setVersionCode(vc);
		return vc;
	}

	private void setVersionCode(int vc) {
		if (vc > 0) {
			msp.put("versioncode", vc + "");
		}
	}

	public boolean getOffTuiSong() {
		boolean start = msp.getAsBoolean("off_ts", true);
		return start;
	}

	public void setOffTuiSong(boolean flag) {
		msp.put("off_ts", flag);
	}

	public boolean getIsTodayFistStartApp() {
		String oldDay = msp.getAsString("addate");
		String newDay = CalendarUtil.getToday("yyyy-MM-dd");
		LogUtils.e("newDay-->" + newDay);

		setDate(newDay);

		if (null != oldDay) {
			if (oldDay.equals(newDay)) {
				return false;
			}
		}
		return true;
	}

	private void setDate(String d) {
		msp.put("addate", d);
	}

	public void setTextSize(int size) {
		msp.put("textsize", size + "");
	}

	public int getTextSize() {
		int textSize = 19;
		String ts = msp.getAsString("textsize");
		if (!TextUtils.isEmpty(ts)) {
			try {
				if (TextUtils.isDigitsOnly(ts)) {
					textSize = Integer.parseInt(ts);
				}
			} catch (Exception e) {
			}
		}
		return textSize;
	}

	public void setUser(UserBean user) {
		this.user = user;
		String dsuser = FjsonUtil.toJsonString(user);
		if (TextUtils.isEmpty(dsuser)) {
			return;
		}
		String suser = null;
		try {
			suser = CipherUtils.encrypt(dsuser, CipherUtils.getDESKey("fuckbitch".getBytes()), "DES");
		} catch (Exception e) {
			e.printStackTrace();
		}
		msp.put("user", suser);
	}

	public UserBean getUser() {
		if (null != user) {
			return user;
		}
		String dsuser = msp.getAsString("user");
		String suser = null;
		if (!TextUtils.isEmpty(dsuser)) {
			try {
				suser = CipherUtils.decrypt(dsuser, CipherUtils.getDESKey("fuckbitch".getBytes()), "DES");
			} catch (Exception e) {
				e.printStackTrace();
			}
			user = FjsonUtil.parseObject(suser, UserBean.class);
		}
		return user;
	}

	public void setWelImg(String img) {
		msp.put("wel_img", img);
	}

	public String getWelImg() {
		String img = msp.getAsString("wel_img");
		return img;
	}

	public JSONObject getWelcome() {
		return msp.getAsJSONObject("welcomeString");
	}

	public void setWelcome(JSONObject obj) {
		msp.put("welcomeString", obj);
	}

	public void setForceUpdateTime(String updateTime) {
		msp.put("updateTime", updateTime);
	}

	public String getForceUpdateTime() {
		String updateTime = msp.getAsString("updateTime");
		return updateTime;
	}

	public String getCacheUpdatetime() {
		String updateTime = msp.getAsString("cacheupdateTime");
		return updateTime;
	}

	public void setCacheUpdatetime(String cacheupdateTime) {
		msp.put("cacheupdateTime", cacheupdateTime);
	}

	public void setSubjectColumnList(JSONArray array) {
		msp.put("subjectcolumnlist", array);
	}

	public JSONArray getSubjectColumnList() {
		JSONArray array = msp.getAsJSONArray("subjectcolumnlist");
		return array;
	}

	public String getForumTitle() {
		return msp.getAsString("forumTitle");
	}

	public void setForumTitle(String forumTitle) {
		msp.put("forumTitle", forumTitle);
	}

}
