package com.hzpd.services;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;

public class InitService extends IntentService {

    public static final String InitAction = "initService";
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
        httpUtils = new HttpUtils();
        rootPath = App.getInstance().getJsonFileCacheRootDir();
        String station = SharePreferecesUtils.getParam(this, StationConfig.STATION, "def").toString();

        if (station.equals(StationConfig.DEF)) {
            siteid = InterfaceJsonfile.SITEID;
            CACHE_url = InterfaceJsonfile.CACHE;
            mAdPic_url = InterfaceJsonfile.mAdPic;
            CHANNELLIST_url = InterfaceJsonfile.CHANNELLIST;
        } else if (station.equals(StationConfig.YN)) {
            siteid = InterfaceJsonfile_YN.SITEID;
            CACHE_url = InterfaceJsonfile_YN.CACHE;
            mAdPic_url = InterfaceJsonfile_YN.mAdPic;
            CHANNELLIST_url = InterfaceJsonfile_YN.CHANNELLIST;
        } else if (station.equals(StationConfig.TW)) {
            siteid = InterfaceJsonfile_TW.SITEID;
            CACHE_url = InterfaceJsonfile_TW.CACHE;
            mAdPic_url = InterfaceJsonfile_TW.mAdPic;
            CHANNELLIST_url = InterfaceJsonfile_TW.CHANNELLIST;
        }
    }

    // 开屏图片
    private void GetWelcomePicJson() {
        App.getInstance().initAds();
        // 请求网络，获取开屏图片信息
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", siteid);
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
//                getAppModify();
            }
        }
    }
}
