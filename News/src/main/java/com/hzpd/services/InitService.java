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
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.news.update.UpdateUtils;
import com.news.update.Utils;
import com.squareup.okhttp.Request;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public class InitService extends IntentService {

    public static final String InitAction = "initService";
    public static final String UserLogAction = "user.log.action";
    public static final String SHARE_KEY_AD = "key_ad";

    private String rootPath;
    String siteid = null;
    String CACHE_url = null;
    String mAdPic_url = null;
    private Object tag;

    public InitService() {
        super("InitService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rootPath = App.getInstance().getJsonFileCacheRootDir();
        siteid = InterfaceJsonfile.SITEID;
        CACHE_url = InterfaceJsonfile.CACHE;
        mAdPic_url = InterfaceJsonfile.mAdPic;
        tag = OkHttpClientManager.getTag();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.i("initService");
        if (intent != null) {
            if (InitService.InitAction.equals(intent.getAction())) {
                debugTest();
                //TODO 获取服务器配置  AD_KEY
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
        final UserLogDao dbUtils = DBHelper.getInstance(getApplicationContext()).getLog();
        try {
            final List<UserLog> logs = dbUtils.loadAll();
            if (logs == null || logs.isEmpty() || logs.size() < 20) {
                return;
            }
            String json = FjsonUtil.toJsonString(logs);
            Map<String, String> params = RequestParamsUtils.getMaps();
            params.put("uid", uid);
            params.put("json", json);
            SPUtil.addParams(params);
            String str = OkHttpClientManager.post(InterfaceJsonfile.USER_LOG, params);
            if (!str.isEmpty()) {
                dbUtils.deleteAll();
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

            //OkHttpClientManager.download(url, file, true, callBack);
        }
    }
}
