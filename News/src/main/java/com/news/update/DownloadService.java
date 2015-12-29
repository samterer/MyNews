package com.news.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;

import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.joy.lmt.LMTInvoker;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * 下载服务
 */
public class DownloadService extends Service {
    public static final String TAG = "DownloadService";

    public DownloadService() {
    }

    HttpUtils httpUtils;
    HttpHandler httpHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (!UpdateUtils.isRomVersion(this)) {
                stopSelf();
                return START_NOT_STICKY;
            }
            Log.e(TAG, intent);
            retry = 0;
            SharedPreferences.Editor editor = getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE)
                    .edit();
            editor.putBoolean(UpdateUtils.KEY.IS_WIFI_DOWNLOADING, false);
            editor.putBoolean(UpdateUtils.KEY.IS_DOWNLOADING, false);
            String key = intent.getStringExtra(TAG);
            if (TextUtils.isEmpty(key)) {
                key = UpdateUtils.KEY.IS_WIFI_DOWNLOADING;
            }
            editor.putBoolean(key, true)
                    .commit();
            startDownload();
        } catch (Exception e) {
            Log.e("test", e.toString());
            e.printStackTrace();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkReceiver, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Log.e("test", null);
        try {
            super.onDestroy();
            if (httpHandler != null) {
                retry = 100;
                Log.e("test", " --- stop --- ");
                httpHandler.cancel(true);
                httpHandler = null;
            }
            unregisterReceiver(networkReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String url;
    int retry = 0;


    private void startDownload() {
        try {
            if (!getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE).getBoolean(UpdateUtils.KEY.KEY_HAS_NEW, false)) {
                stopSelf();
                return;
            }
            if (httpHandler == null || httpHandler.isCancelled()) {

                url = getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE)
                        .getString(UpdateUtils.KEY.KEY_DOWNLOAD_URL, "");
//				url = "http://gdown.baidu.com/data/wisegame/7c31d95af688e9e4/menghuanxiyou_10140.apk";
                Log.e("test", url);
                if (TextUtils.isEmpty(url) || !url.toLowerCase().startsWith("http")) {
                    stopSelf();
                    return;
                }
                NetworkInfo networkInfo = UpdateUtils.getNetworkInfo(this);
                if (networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED) {
                    Log.e("test", " no connection ");
                    stopSelf();
                    return;
                }
                SharedPreferences pref = getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE);
                if (pref.getBoolean(UpdateUtils.KEY.KEY_AUTO_INSTALL, false)
                        && pref.getBoolean(UpdateUtils.KEY.KEY_SILENCE_INSTALL, false)) {
                    silence = true;
                } else {
                    silence = false;
                }

                //TODO 判断下载类型以及网络类型
                boolean bool = pref.getBoolean(UpdateUtils.KEY.IS_WIFI_DOWNLOADING, false);
                if (bool && networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                    return;
                }

                fileLength = pref.getLong(UpdateUtils.KEY.KEY_FILE_SIZE, 0L);
                autoDownload = pref.getBoolean(UpdateUtils.KEY.KEY_AUTO_DOWNLOAD, false);
                File root = Environment.getExternalStorageDirectory();
                File fold = new File(root, UpdateUtils.PATH_SAVE);
                if (!fold.exists()) {
                    fold.mkdirs();
                }
                File target = new File(fold, UpdateUtils.getFileName(this));
                //TODO 判断文件版本
                if (target.exists()) {
                    if (pref.getInt(UpdateUtils.KEY.COMPLETED_VERSION_CODE, 0) == pref.getInt(UpdateUtils.KEY.KEY_VERSION_CODE, 0)) {
                        done(target);
                        return;
                    } else {
                        target.delete();
                    }
                }

                target = new File(fold, UpdateUtils.getFileName(this) + ".tmp");
                if (!isResume && target.exists()) {
                    target.delete();
                }
                String path = target.getAbsolutePath();
                Log.e("test", path);
                if (httpUtils == null) {
                    httpUtils = SPUtil.getHttpUtils();
                }
                httpHandler = httpUtils.download(HttpRequest.HttpMethod.GET, pref.getString(UpdateUtils.KEY.KEY_DOWNLOAD_URL, ""), target.getAbsolutePath(), null, true, false, requestCallBack);

                isResume = true;

                if (pref.getBoolean(UpdateUtils.KEY.KEY_FORCING_UPDATE, false)) {
                    try {
                        EventBus.getDefault().post(new LocalUpdateEvent(false));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("test", e.toString());
            e.printStackTrace();
        }
    }

    private final void done(File file) {
        Log.e("test", null);
        try {
            getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE)
                    .edit().putBoolean(UpdateUtils.KEY.IS_DOWNLOADING, false)
                    .putBoolean(UpdateUtils.KEY.IS_WIFI_DOWNLOADING, false)
                    .commit();
            if (notification != null) {
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(UpdateUtils.REQUEST_CODE);
            }
            if (httpHandler != null) {
                Log.e("test", " --- end --- ");
                httpHandler.cancel(true);
                httpHandler = null;
            }
            SharedPreferences pref = getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE);
            String md5 = null;
            md5 = UpdateUtils.getHash(file.getAbsolutePath(), "MD5").toLowerCase();
            Log.e("test", "MD5 " + pref.getString(UpdateUtils.KEY.KEY_MD5, ""));
            Log.e("test", "MD5 " + md5);
            Log.e("test", "MD5 " + md5.equals(pref.getString(UpdateUtils.KEY.KEY_MD5, "")));
            //TODO 检查文件完整性
            if (md5.equals(pref.getString(UpdateUtils.KEY.KEY_MD5, ""))
                    ) {
                Log.e("test", " GOOD ");
                try {
                    LMTInvoker invoker = new LMTInvoker(getApplicationContext(), "GameLink");
                    invoker.unBindLMT();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pref.edit().putInt(UpdateUtils.KEY.COMPLETED_VERSION_CODE, pref.getInt(UpdateUtils.KEY.KEY_VERSION_CODE, 0)).commit();
                if (silence) {
                    UpdateUtils.installApk(this, file, pref.getBoolean(UpdateUtils.KEY.KEY_AUTO_INSTALL, false));
                    return;
                }

                if (pref.getBoolean(UpdateUtils.KEY.KEY_AUTO_DOWNLOAD, false)) {
                    UpdateUtils.notifyUpdate(this);
                    try {
                        Log.e("test", "CheckUpdateEvent");
                        EventBus.getDefault().post(new CheckUpdateEvent());
                    } catch (Exception e) {
                    }
                    try {
                        Log.e("test", "LocalUpdateEvent");
                        EventBus.getDefault().post(new LocalUpdateEvent(true));
                    } catch (Exception e) {
                    }
                } else {
                    UpdateUtils.installApk(this, file, pref.getBoolean(UpdateUtils.KEY.KEY_AUTO_INSTALL, false));
                }
            } else {
                Log.e("test", " BAD ");
                file.delete();
                startDownload();
            }
        } catch (Exception e) {
            Log.e("Exception", e.toString());
            Log.e("test", " BAD ");
            file.delete();
            startDownload();
            e.printStackTrace();
        }
    }

    private void resetStatus() {
        getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE)
                .edit().putBoolean(UpdateUtils.KEY.IS_DOWNLOADING, false)
                .putBoolean(UpdateUtils.KEY.IS_WIFI_DOWNLOADING, false)
                .commit();
    }

    boolean checkFile = true;
    long fileLength = 0L;
    boolean isResume = true;
    boolean autoDownload = false;
    boolean silence = false;
    public static int progress = 0;

    RequestCallBack<File> requestCallBack = new RequestCallBack<File>() {

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            try {
                File file = responseInfo.result;
                retry = 0;
                Log.e(TAG, file);
                File target = new File(file.getParent(), UpdateUtils.getFileName(getApplicationContext()));
                file.renameTo(target);
                done(target);
            } catch (Exception e) {
                Log.e(TAG, e);
                e.printStackTrace();
            }
        }


        @Override
        public void onFailure(HttpException error, String msg) {
            Log.e("test", error + ":" + msg);
            ++retry;
            if (httpHandler != null) {
                Log.e("test", " --- stop --- ");
                httpHandler.cancel(true);
                httpHandler = null;
                //TODO 继续下载
                if (retry < 5) {
                    startDownload();
                } else {
                    LMTInvoker invoker = new LMTInvoker(getApplicationContext(), "GameLink");
                    invoker.unBindLMT();
                    resetStatus();
                }
            }
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            progress = (int) (1.0f * current / total * 100);
            Log.e("progress", "progress -- " + progress);
            try {
                EventBus.getDefault().post(new ProgressUpdateEvent(progress));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (silence) {
                return;
            }
            if (!autoDownload) {
                notification = UpdateUtils.notifyProgress(DownloadService.this, progress, notification);
            }
        }


        @Override
        public void onStart() {
            Log.e("test", null);
            checkFile = true;
        }
    };

    // 检查文件大小是否一致
    private void checkLength(long count) {
        if (!checkFile) {
            return;
        }
        if (fileLength == 0L && count > 1) {
            getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE).edit()
                    .putLong(UpdateUtils.KEY.KEY_FILE_SIZE, count)
                    .commit();
            fileLength = count;
        } else {
            if (fileLength != count) {
                getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE).edit()
                        .putLong(UpdateUtils.KEY.KEY_FILE_SIZE, count)
                        .apply();
                isResume = false;
                if (httpHandler != null) {
                    Log.e("test", " --- stop --- ");
                    httpHandler.cancel(true);
                    httpHandler = null;
                    //TODO 继续下载
                    retry = 0;
                    startDownload();

                }
            } else {
                checkFile = false;
            }
        }
    }

    NetworkReceiver networkReceiver = new NetworkReceiver();

    class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("test", intent.getAction());
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE).getBoolean(UpdateUtils.KEY.IS_DOWNLOADING, false)
                        || getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, MODE_PRIVATE).getBoolean(UpdateUtils.KEY.IS_WIFI_DOWNLOADING,
                        false)) {
                    if (httpHandler == null || httpHandler.isCancelled()) {
                        Log.e("test", "handler start");
                        httpHandler = null;
                        startDownload();
                    }
                }
            }
        }
    }

    Notification notification;
}