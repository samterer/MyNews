package com.hzpd.services;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.hzpd.hflt.BuildConfig;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.UserLog;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.util.LogUtils;
import com.news.update.Utils;
import com.squareup.okhttp.Request;

import java.util.Calendar;
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
        tag= OkHttpClientManager.getTag();
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
        final DbUtils dbUtils = DBHelper.getInstance(getApplicationContext()).getLogDbUtils();
        try {
            final List<UserLog> logs = dbUtils.findAll(UserLog.class);
            if (logs == null || logs.isEmpty() || logs.size() < 20) {
                return;
            }
            String json = FjsonUtil.toJsonString(logs);
            Map<String,String> params = RequestParamsUtils.getMaps();
            params.put("uid", uid);
            params.put("json", json);
            SPUtil.addParams(params);
            OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.USER_LOG, new OkHttpClientManager.ResultCallback() {
                @Override
                public void onSuccess(Object response) {
                    if (response.toString()!=null){
                        try {
                            dbUtils.deleteAll(logs);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Request request, Exception e) {

                }
            }, params);
//            ResponseStream rs = httpUtils.sendSync(HttpMethod.POST, InterfaceJsonfile.USER_LOG, params);
//            String str = rs.readString();
//            if (!TextUtils.isEmpty(str)) {
//                Log.e("test", str);
//                dbUtils.deleteAll(logs);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void debugTest() {
        if (BuildConfig.DEBUG) {
            UserLog userLog = new UserLog("12353", SPUtil.format(Calendar.getInstance()), 3);
            String json = FjsonUtil.toJsonString(userLog);
            Log.e("userLog", json);
        }
    }
}
