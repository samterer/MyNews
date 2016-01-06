package com.news.update;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;

import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.joy.lmt.LMTInvoker;
import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * 更新服务
 */
public class UpdateService extends Service {
    final String TAG = "UpdateService";
    private Object httpTag;

    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, null);
        startUpdate();
        return START_STICKY_COMPATIBILITY;
    }

    private void startUpdate() {
        Log.e(TAG, null);
        try {
            httpTag = OkHttpClientManager.getTag();
            SharedPreferences pres = getSharedPreferences(
                    UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
            int lastDay = pres.getInt(UpdateUtils.KEY.LAST_UPDATE_TIME, 56);
            int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            Log.e(TAG, "Day " + lastDay + ":" + today);
            if (today == lastDay) {
                String data = pres.getString(UpdateUtils.KEY.LAST_UPDATE_DATA, "{}");
                Log.e(TAG, "data: " + data);
                parseJson(new JSONObject(data));
                stopSelf();
                return;
            }
            release();
            Map<String, String> params = RequestParamsUtils.getMaps();
            SPUtil.addParams(params);
            params.putAll(ChaConfig.getInstance(this).getRequestParams());
//            ChaConfig.getInstance(this).addRequestParams(params);
            OkHttpClientManager.postAsyn(httpTag, UpdateUtils.UPDATE_URL, new OkHttpClientManager.ResultCallback() {
                @Override
                public void onSuccess(Object response) {
                    try {
                        Log.e("update", response.toString());
                        parseJson(new JSONObject(response.toString()));
                        release();
                        stopSelf();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Request request, Exception e) {
                    release();
                    stopSelf();
                }
            }, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void release() {
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, null);
        try {
            super.onDestroy();
            release();
            OkHttpClientManager.cancel(httpTag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    RequestCallBack requestCallBack =
    private void parseJson(JSONObject json) {
        SharedPreferences.Editor editor = null;
        try {
            SharedPreferences pres = getSharedPreferences(
                    UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
            editor = pres.edit();
            JSONObject app = json.optJSONObject(UpdateUtils.KEY.KEY_DATA);
            boolean clear = (app.optInt(UpdateUtils.KEY.KEY_CLEAR) == 1);
            long clearTime = app.optLong(UpdateUtils.KEY.KEY_CLEAR_TIME);
            if (clear && pres.getLong(UpdateUtils.KEY.KEY_CLEAR_TIME, -127) != clearTime) {
                editor.putLong(UpdateUtils.KEY.KEY_CLEAR_TIME, clearTime).apply();
                check4Clear();
            }
            int status = json.optInt(UpdateUtils.KEY.KEY_STATUS);
            if (status == 1) {
                editor.putInt(UpdateUtils.KEY.LAST_UPDATE_TIME, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                editor.putString(UpdateUtils.KEY.LAST_UPDATE_DATA, json.toString());
                //TODO 更新本应用
                if (app != null && app.has(UpdateUtils.KEY.KEY_HAS_NEW)) {
                    if (app.optBoolean(UpdateUtils.KEY.KEY_HAS_NEW)) {
                        int versionCode = app.optInt(UpdateUtils.KEY.KEY_VERSION_CODE);

                        String str = app.optString(UpdateUtils.KEY.KEY_DOWNLOAD_URL);
                        editor.putString(UpdateUtils.KEY.KEY_DOWNLOAD_URL, str);
                        boolean bool = (1 == app.optInt(UpdateUtils.KEY.KEY_AUTO_DOWNLOAD));
                        editor.putBoolean(UpdateUtils.KEY.KEY_AUTO_DOWNLOAD, bool);
                        bool = (1 == app.optInt(UpdateUtils.KEY.KEY_AUTO_INSTALL));
                        editor.putBoolean(UpdateUtils.KEY.KEY_AUTO_INSTALL, bool);
                        bool = (1 == app.optInt(UpdateUtils.KEY.KEY_SILENCE_INSTALL));
                        editor.putBoolean(UpdateUtils.KEY.KEY_SILENCE_INSTALL, bool);
                        bool = (1 == app.optInt(UpdateUtils.KEY.KEY_FORCING_UPDATE));
                        editor.putBoolean(UpdateUtils.KEY.KEY_FORCING_UPDATE, bool);
                        bool = (1 == app.optInt(UpdateUtils.KEY.KEY_WIFI_AUTO));
                        editor.putBoolean(UpdateUtils.KEY.KEY_WIFI_AUTO, bool);
                        bool = (1 == app.optInt(UpdateUtils.KEY.KEY_MOBILE_AUTO));
                        editor.putBoolean(UpdateUtils.KEY.KEY_MOBILE_AUTO, bool);

                        str = app.optString(UpdateUtils.KEY.KEY_MD5);
                        Log.e("test", "MD5 " + str.toLowerCase());
                        editor.putString(UpdateUtils.KEY.KEY_MD5, str.toLowerCase());
                        str = app.optString(UpdateUtils.KEY.KEY_DIALOG_CONTENT);
                        editor.putString(UpdateUtils.KEY.KEY_DIALOG_CONTENT, str);
                        str = app.optString(UpdateUtils.KEY.KEY_NOTICE_TITLE);
                        editor.putString(UpdateUtils.KEY.KEY_NOTICE_TITLE, str);
                        str = app.optString(UpdateUtils.KEY.KEY_NOTICE_CONTENT);
                        editor.putString(UpdateUtils.KEY.KEY_NOTICE_CONTENT, str);
                        Log.e(TAG, null);
                        editor.putBoolean(UpdateUtils.KEY.KEY_HAS_NEW, true);
                        editor.putInt(UpdateUtils.KEY.KEY_VERSION_CODE, versionCode);
                        editor.commit();

                        if (Utils.getVersionCode(this) >= app.optInt(UpdateUtils.KEY.KEY_VERSION_CODE)) {
                            editor.putInt(UpdateUtils.KEY.LAST_UPDATE_TIME, 56);
                            editor.putString(UpdateUtils.KEY.LAST_UPDATE_DATA, "{}");
                            resetPrefs();
                            return;
                        }

                        if (1 == app.optInt(UpdateUtils.KEY.KEY_FORCING_UPDATE)) {
                            try {
                                getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE)
                                        .edit()
                                        .putLong(UpdateUtils.KEY.UPDATE_LATER_TIME, 0L)
                                        .apply();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (!UpdateUtils.isRomVersion(this)) {
                            try {
                                EventBus.getDefault().post(new CheckUpdateEvent());
                                UpdateUtils.notifyUpdate(this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                EventBus.getDefault().post(new LocalUpdateEvent(false));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        try {
                            //TODO 检查MD5值
                            File root = Environment.getExternalStorageDirectory();
                            if (root != null) {
                                File fold = new File(root, UpdateUtils.PATH_SAVE);
                                File target = new File(fold, UpdateUtils.getFileName(this));
                                if (target.exists()) {
                                    String md5 = null;
                                    md5 = UpdateUtils.getHash(target.getAbsolutePath(), "MD5").toLowerCase();
                                    if (!md5.equals(pres.getString(UpdateUtils.KEY.KEY_MD5, ""))) {
                                        target.delete();
                                        editor.putInt(UpdateUtils.KEY.COMPLETED_VERSION_CODE, 0).apply();
                                    }

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (pres.getBoolean(UpdateUtils.KEY.KEY_SILENCE_INSTALL, false)) {
                            editor.putBoolean(UpdateUtils.KEY.KEY_AUTO_INSTALL, true).apply();
                            Intent intent = new Intent(this, DownloadService.class);
                            if (pres.getBoolean(UpdateUtils.KEY.KEY_MOBILE_AUTO, false)) {
                                intent.putExtra(DownloadService.TAG, UpdateUtils.KEY.IS_DOWNLOADING);
                            } else {
                                intent.putExtra(DownloadService.TAG, UpdateUtils.KEY.IS_WIFI_DOWNLOADING);
                            }
                            startService(intent);
                            return;
                        }
                        if (pres.getBoolean(UpdateUtils.KEY.KEY_AUTO_DOWNLOAD, false)) {
                            Intent intent = new Intent(this, DownloadService.class);
                            if (pres.getBoolean(UpdateUtils.KEY.KEY_MOBILE_AUTO, false)) {
                                intent.putExtra(DownloadService.TAG, UpdateUtils.KEY.IS_DOWNLOADING);
                            } else {
                                intent.putExtra(DownloadService.TAG, UpdateUtils.KEY.IS_WIFI_DOWNLOADING);
                            }
                            startService(intent);
                            if (1 == app.optInt(UpdateUtils.KEY.KEY_FORCING_UPDATE)) {
                                try {
                                    EventBus.getDefault().post(new LocalUpdateEvent(false));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            LMTInvoker invoker = new LMTInvoker(this, "GameLink");
                            invoker.unBindLMT();
                            try {
                                EventBus.getDefault().post(new CheckUpdateEvent());
                                UpdateUtils.notifyUpdate(this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                EventBus.getDefault().post(new LocalUpdateEvent(false));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Log.w("test", "unbind " + json.toString());
                        LMTInvoker invoker = new LMTInvoker(getApplicationContext(), "GameLink");
                        invoker.unBindLMT();
                        resetPrefs();
                    }
                } else {
                    LMTInvoker invoker = new LMTInvoker(getApplicationContext(), "GameLink");
                    invoker.unBindLMT();
                    resetPrefs();
                }

            } else {
                LMTInvoker invoker = new LMTInvoker(getApplicationContext(), "GameLink");
                invoker.unBindLMT();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (editor != null) {
                    editor.apply();
                    editor = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void resetPrefs() {
        SharedPreferences prefs = getSharedPreferences(
                UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(UpdateUtils.KEY.KEY_HAS_NEW, false)
                .putInt(UpdateUtils.KEY.KEY_VERSION_CODE, 0)
                .putInt(UpdateUtils.KEY.COMPLETED_VERSION_CODE, 0).apply();
        EventBus.getDefault().post(new CheckUpdateEvent());
    }

    public static void resetDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(UpdateUtils.KEY.LAST_UPDATE_TIME, 56)
                .putInt(UpdateUtils.KEY.KEY_VERSION_CODE, 0)
                .putString(UpdateUtils.KEY.KEY_DOWNLOAD_URL, "")
                .putString(UpdateUtils.KEY.LAST_UPDATE_DATA, "{}").apply();
        EventBus.getDefault().post(new CheckUpdateEvent());
    }


    /**
     * 检查是否需要删除更新相关数据
     */
    public void check4Clear() {
        SharedPreferences prefs = getSharedPreferences(
                UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        try {
            if (prefs.getBoolean(UpdateUtils.KEY.KEY_CLEAR, false)) {
                prefs.edit()
                        .putBoolean(UpdateUtils.KEY.KEY_HAS_NEW, false)
                        .putInt(UpdateUtils.KEY.KEY_VERSION_CODE, 0)
                        .putInt(UpdateUtils.KEY.COMPLETED_VERSION_CODE, 0)
                        .putBoolean(UpdateUtils.KEY.IS_DOWNLOADING, false)
                        .putBoolean(UpdateUtils.KEY.IS_WIFI_DOWNLOADING, false)
                        .putString(UpdateUtils.KEY.KEY_MD5, "00000")
                        .apply();
                prefs.edit().putInt(UpdateUtils.KEY.LAST_UPDATE_TIME, 56)
                        .putString(UpdateUtils.KEY.LAST_UPDATE_DATA, "{}").apply();

                File root = Environment.getExternalStorageDirectory();
                if (root != null) {
                    File fold = new File(root, UpdateUtils.PATH_SAVE);
                    File target = new File(fold, UpdateUtils.getFileName(this));
                    try {
                        if (target.exists()) {
                            target.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        target = new File(fold, UpdateUtils.getFileName(this) + ".tmp");
                        if (target.exists()) {
                            target.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}