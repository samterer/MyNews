package com.hzpd.services;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.BuildConfig;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.UserLog;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.joy.update.Utils;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.List;

public class InitService extends IntentService {

    public static final String InitAction = "initService";
    public static final String UserLogAction = "user.log.action";
    public static final String SHARE_KEY_AD = "key_ad";

    private HttpUtils httpUtils;
    private String rootPath;
    String siteid = null;
    String CACHE_url = null;
    String mAdPic_url = null;
    String CHANNELLIST_url = null;

    public InitService() {
        super("InitService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        httpUtils = SPUtil.getHttpUtils();
        rootPath = App.getInstance().getJsonFileCacheRootDir();
        String station = SharePreferecesUtils.getParam(this, StationConfig.STATION, "def").toString();

        siteid = InterfaceJsonfile.SITEID;
        CACHE_url = InterfaceJsonfile.CACHE;
        mAdPic_url = InterfaceJsonfile.mAdPic;
    }

    // 开屏图片
    private void GetWelcomePicJson() {
        App.getInstance().initAds();
        // 请求网络，获取开屏图片信息
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", siteid);
        SPUtil.addParams(params);
        ResponseStream rs = null;
        try {
            rs = httpUtils.sendSync(HttpMethod.POST, mAdPic_url, params);
        } catch (HttpException e) {
            Log.w("warning", e.toString());
        }

        // 读取请求结果
        if (null == rs) {
            return;
        }
        String response = null;
        try {
            response = rs.readString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(response)) {
            return;
        }
        LogUtils.i("---welcome-->" + response);

        JSONObject object = FjsonUtil.parseObject(response);

        if (null == object) {
            return;
        }

        // 解析JSON
        if (200 == object.getIntValue("code")) {
            JSONArray joData;
            try {
                joData = object.getJSONArray("data");
            } catch (Exception e1) {
                return;
            }
            // 缓存json到sd卡
            SPUtil.getInstance().setWelcome(object);

            App.getInstance().initAds();

            if (App.getInstance().welcomeAdbean != null) {
                // 广告图片
                downloadPic(App.getInstance().welcomeAdbean.getImgurl());
            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void downloadPic(final String imgUrl) {
        if (SPUtil.isImageUri(imgUrl)) {
            ImageLoader.getInstance().loadImage(imgUrl, null);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.i("initService");
        if (intent != null) {
            if (InitService.InitAction.equals(intent.getAction())) {
                GetWelcomePicJson();
            } else if (InitService.UserLogAction.equals(intent.getAction())) {
                Log.e("test", "send use log");
                sendUserLog();
            }
        }
    }

    private void sendUserLog() {
        if (BuildConfig.DEBUG || !Utils.isNetworkConnected(this)) {
            return;
        }
        UserBean user = SPUtil.getInstance().getUser();
        String uid = "";
        if (user != null && !TextUtils.isEmpty(user.getUid())) {
            uid = user.getUid();
        }
        final DbUtils dbUtils = DBHelper.getInstance(getApplicationContext()).getLogDbUtils();
        try {
            List<UserLog> logs = dbUtils.findAll(UserLog.class);
            if (logs.isEmpty() || logs.size() < 20) {
                return;
            }
            String json = FjsonUtil.toJsonString(logs);
            RequestParams params = RequestParamsUtils.getParams();
            params.addBodyParameter("uid", uid);
            params.addBodyParameter("json", json);
            SPUtil.addParams(params);
            ResponseStream rs = httpUtils.sendSync(HttpMethod.POST, InterfaceJsonfile.USER_LOG, params);
            String str = rs.readString();
            if (!TextUtils.isEmpty(str)) {
                Log.e("test", str);
                dbUtils.deleteAll(logs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
