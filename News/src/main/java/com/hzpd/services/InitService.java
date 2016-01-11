package com.hzpd.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.color.tools.mytools.LogUtils;
import com.hzpd.hflt.BuildConfig;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.UserLog;
import com.hzpd.modle.db.UserLogDao;
import com.hzpd.ui.ConfigBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.news.update.UpdateUtils;
import com.news.update.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitService extends IntentService {

    public static final String InitAction = "initService";
    public static final String UserLogAction = "user.log.action";
    public static final String SHARE_KEY_AD_CONFIG = "key_config";
    public static final String SHARE_CONFIG_ETAG = "key_config_etag";
    public static final String SHARE_SEND_LOG = "key_send_log";

    public static long SEND_TIME = 1000 * 60 * 2;

    private Object tag;

    public InitService() {
        super("InitService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (ConfigBean.getInstance().send_log_time > 1000) {
            SEND_TIME = ConfigBean.getInstance().send_log_time;
        }
        tag = OkHttpClientManager.getTag();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OkHttpClientManager.cancel(tag);
        Log.e("test", "News: " + "onDestroy");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.i("initService");
        if (intent != null) {
            if (InitService.InitAction.equals(intent.getAction())) {
                debugTest();
                //TODO 获取服务器配置  AD_KEY
                getConfig();
            } else if (InitService.UserLogAction.equals(intent.getAction())) {
                Log.e("test", "send use log");
                sendUserLog();
            }
        }
    }

    private void getConfig() {
        try {
            if (!Utils.isNetworkConnected(this)) {
                return;
            }
            Map<String, String> params = new HashMap<>();
            SPUtil.addParams(params);
            Request request = new Request.Builder().url(InterfaceJsonfile.AD_CONFIG).head().build();
            Response response = new OkHttpClient().newCall(request).execute();
            Log.e("test", "News: " + response.headers());
            String etag = response.header("ETag");
            String cEtag = SPUtil.getGlobal(SHARE_CONFIG_ETAG, "");
            if (!TextUtils.isEmpty(cEtag) && cEtag.equals(etag)) {
                Log.e("test", "News: CONFIG NOT UPDATE.");
                return;
            }
            String data = OkHttpClientManager.get(InterfaceJsonfile.AD_CONFIG);
            if (!TextUtils.isEmpty(data)) {
                Log.e("test", "News: " + data);
                com.alibaba.fastjson.JSONObject json = FjsonUtil.parseObject(data);
                if (json.getString("code").equals("200")) {
                    SharePreferecesUtils.setParam(this, SHARE_KEY_AD_CONFIG, data);
                    ConfigBean.getInstance().update();
                    SPUtil.setGlobal(SHARE_CONFIG_ETAG, etag);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendUserLog() {
        if (BuildConfig.DEBUG || !Utils.isNetworkConnected(this)) {
            return;
        }
        long send_user_log_time = SPUtil.getGlobal(SHARE_SEND_LOG, System.currentTimeMillis());
        if (System.currentTimeMillis() - send_user_log_time < SEND_TIME) {
            return;
        }

        UserBean user = SPUtil.getInstance().getUser();
        String uid = "";
        if (user != null && !TextUtils.isEmpty(user.getUid())) {
            uid = user.getUid();
        }
        final UserLogDao dbUtils = DBHelper.getInstance(getApplicationContext()).getLog();
        try {
            final List<UserLog> logs = dbUtils.loadAll();
            if (logs == null || logs.isEmpty()) {
                return;
            }
            String json = FjsonUtil.toJsonString(logs);
            Map<String, String> params = RequestParamsUtils.getMaps();
            params.put("uid", uid);
            params.put("json", json);
            SPUtil.addParams(params);
            String str = OkHttpClientManager.post(InterfaceJsonfile.USER_LOG, params);
            if (!str.isEmpty() && str.contains("200")) {
                dbUtils.deleteAll();
                send_user_log_time = System.currentTimeMillis();
                SPUtil.setGlobal(SHARE_SEND_LOG, send_user_log_time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void debugTest() {
        if (BuildConfig.DEBUG) {
            String url = "http://s3-ap-southeast-1.amazonaws.com/ltcms-apk/com.joy.tl.news/GooglePlay/1/567c9e57069ba.apk";
            OkHttpClientManager.ResultCallback callBack = new OkHttpClientManager.ResultCallback<File>() {

                @Override
                public void onLoading(int total, int current) {
                    Log.e("test", "News " + total + ":" + current);
                    int progress = (int) (1.0f * current / total * 100);
                    Log.e("progress", "progress -- " + progress);
                }

                @Override
                public void onSuccess(File response) {
                    try {
                        Log.e("test", "News " + response.getAbsolutePath());
                        Log.e("test", "News " + UpdateUtils.getHash(response.getAbsolutePath(), "MD5"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Request request, Exception e) {
                    Log.e("test", "News " + request + ":" + e.toString());
                }
            };
            File file = Environment.getExternalStorageDirectory();
            file = new File(file, "news.test.apk");
        }
    }
}
