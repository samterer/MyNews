package com.hzpd.ui.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.custorm.SlideSwitch;
import com.hzpd.hflt.R;
import com.hzpd.modle.UpdateBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.modle.event.RestartEvent;
import com.hzpd.services.ClearCacheService;
import com.hzpd.ui.App;
import com.hzpd.ui.widget.FontTextView;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.CODE;
import com.hzpd.utils.DataCleanManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.GetFileSizeUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import de.greenrobot.event.EventBus;

public class SettingActivity extends MBaseActivity {
    @Override
    public String getAnalyticPageName() {
        return "设置页";
    }

    @ViewInject(R.id.stitle_tv_content)
    private TextView stitle_tv_content;


    @ViewInject(R.id.zqzx_setting_textsize)
    private RelativeLayout zqzx_setting_textsize;
    @ViewInject(R.id.setting_chosse_textsize)
    private FontTextView setting_chosse_textsize;

    //字体大小设置
    @ViewInject(R.id.zqzx_setting_rb1)
    private RadioButton zqzx_setting_rb1;
    @ViewInject(R.id.zqzx_setting_rb2)
    private RadioButton zqzx_setting_rb2;
    @ViewInject(R.id.zqzx_setting_rb3)
    private RadioButton zqzx_setting_rb3;

    @ViewInject(R.id.zqzx_setting_push)
    private SlideSwitch zqzx_setting_push;
    @ViewInject(R.id.zqzx_setting_deletecache)
    private RelativeLayout zqzx_setting_deletecache;
    @ViewInject(R.id.zqzx_setting_feedback)
    private RelativeLayout zqzx_setting_feedback;
    @ViewInject(R.id.zqzx_setting_cache)
    private FontTextView zqzx_setting_cache;
    @ViewInject(R.id.zqzx_setting_update)
    private RelativeLayout zqzx_setting_update;
    @ViewInject(R.id.zqzx_setting_tv_version)
    private FontTextView zqzx_setting_tv_version;

    @ViewInject(R.id.rl_test)
    private RelativeLayout rl_test;

    @ViewInject(R.id.zqzx_setting_choose)
    private RelativeLayout zqzx_setting_choose;
    @ViewInject(R.id.setting_chosse_station)
    private FontTextView setting_chosse_station;
    //站点设置
    @ViewInject(R.id.zqzx_setting_rb4)
    private RadioButton zqzx_setting_rb4;
    private View loadingView;
    private AlertDialog.Builder mDeleteDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zqzx_setting_layout);

        try {
            ViewUtils.inject(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //测试用
        rl_test.setVisibility(View.GONE);
//        rl_test.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent mIntent = new Intent(SettingActivity.this, CollapseActivity.class);
//                startActivity(mIntent);
//            }
//        });

        textSize= new String[]{this.getResources().getString(R.string.settings_option_font_large), this.getResources().getString(R.string.settings_option_font_medium), this.getResources().getString(R.string.settings_option_font_small)};//"Besar", "Sedang", "Kecil"


        loadingView = findViewById(R.id.app_progress_bar);
        stitle_tv_content.setText(R.string.title_settings);

        switch (spu.getTextSize()) {
            case CODE.textSize_big: {
                zqzx_setting_rb1.setChecked(true);
                setting_chosse_textsize.setText(getString(R.string.settings_option_font_large));
            }
            break;
            case CODE.textSize_normal: {
                zqzx_setting_rb2.setChecked(true);
                setting_chosse_textsize.setText(getString(R.string.settings_option_font_medium));
            }
            break;
            case CODE.textSize_small: {
                zqzx_setting_rb3.setChecked(true);
                setting_chosse_textsize.setText(getString(R.string.settings_option_font_small));
            }
            break;
        }


        zqzx_setting_textsize.setOnClickListener(new TextSizeAlertClickListener());


        //弹出站点选择单选框
        zqzx_setting_choose.setOnClickListener(new AlertClickListener());

        Object obj = SharePreferecesUtils.getParam(SettingActivity.this, "STATION", "def");
        String station = obj.toString();
        if (station == null || station.equals("def")) {
            setting_chosse_station.setText("Indonesia");
        } else if (station.equals("yn")) {
            setting_chosse_station.setText("Indonesia");
        } else if (station.equals("tw")) {
            setting_chosse_station.setText("test");
        }


//		获取当前状态
        LogUtils.e("spu.getOffTuiSong()" + spu.getOffTuiSong());
        if (spu.getOffTuiSong()) {
            LogUtils.e("设置");
            zqzx_setting_push.setState(true);
            zqzx_setting_push.setSelected(true);
        } else {
            zqzx_setting_push.setState(false);
            zqzx_setting_push.setSelected(false);
        }

//		推送消息
        zqzx_setting_push.setSlideListener(new SlideSwitch.SlideListener() {
                                               @Override
                                               public void open() {
                                                   pushSwitch(true);
                                                   LogUtils.e("pushSwitch" + true);
                                               }

                                               @Override
                                               public void close() {
                                                   pushSwitch(false);
                                                   LogUtils.e("pushSwitch" + false);
                                               }
                                           }

        );

        zqzx_setting_tv_version.setText(App.getInstance().

                        getVersionName()

        );

        getCacheSize();


    }

    //开关状态
    private void pushSwitch(boolean isChecked) {
//		保存推送开关状态
        spu.setOffTuiSong(isChecked);
        if (JPushInterface.isPushStopped(SettingActivity.this)) {
            JPushInterface.init(SettingActivity.this);
        }
        if (isChecked) {
            if (null != spu.getUser()) {
                JPushInterface.setAlias(SettingActivity.this, spu.getUser().getUid(),
                        new TagAliasCallback() {
                            @Override
                            public void gotResult(int arg0, String arg1,
                                                  Set<String> arg2) {
                                LogUtils.i("arg0-->" + arg0 + " arg1-->" + arg1);
                                if (arg2 != null) {
                                    for (String s : arg2) {
                                        LogUtils.i("arg2->" + s);
                                    }
                                }
                            }
                        });
            }

        } else {
            JPushInterface.stopPush(SettingActivity.this);
        }

    }

    //	关于我们
    @OnClick(R.id.zqzx_setting_aboutus)
    private void aboutus(View v) {
        Intent mIntent = new Intent(this, AboutUsActivity.class);
        startActivity(mIntent);
        AAnim.ActivityStartAnimation(this);
    }

    private String[] textSize;
    private int[] textSize1 = new int[]{CODE.textSize_big, CODE.textSize_normal, CODE.textSize_small};

    /**
     * 菜单弹出窗口
     */
    class TextSizeAlertClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(SettingActivity.this).setTitle(getString(R.string.language)).setItems(textSize, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setting_chosse_textsize.setText(textSize[which]);
                    spu.setTextSize(textSize1[which]);
                    FontSizeEvent event = new FontSizeEvent();
                    event.setFontSize(textSize1[which]);
                    EventBus.getDefault().post(event);
                    dialog.dismiss();
                }
            }).show();
        }
    }


    //	设置字体大小
    @OnClick({R.id.zqzx_setting_rb1, R.id.zqzx_setting_rb2,
            R.id.zqzx_setting_rb3})
    private void onRadioCheck(View v) {
        FontSizeEvent event = new FontSizeEvent();
        switch (v.getId()) {
            case R.id.zqzx_setting_rb1: {
                zqzx_setting_rb1.setChecked(true);
                spu.setTextSize(CODE.textSize_big);
                event.setFontSize(CODE.textSize_big);
            }
            break;
            case R.id.zqzx_setting_rb2: {
                zqzx_setting_rb2.setChecked(true);
                spu.setTextSize(CODE.textSize_normal);
                event.setFontSize(CODE.textSize_normal);
            }
            break;
            case R.id.zqzx_setting_rb3: {
                zqzx_setting_rb3.setChecked(true);
                spu.setTextSize(CODE.textSize_small);
                event.setFontSize(CODE.textSize_small);
            }
            break;
        }

        EventBus.getDefault().post(event);
    }


    /**
     * 站点设置*
     */
    @OnClick({R.id.zqzx_setting_rb4})
    private void onRadioCheckStation(View v) {

        switch (v.getId()) {
            case R.id.zqzx_setting_rb4: {
//                checkStation(zqzx_setting_rb4, "def", "http://221.130.163.77/99cms");
            }
            break;
        }

    }

    private void checkStation(final RadioButton v, final String str, final String url) {
        mDeleteDialog = new Builder(this);
        mDeleteDialog.setTitle("站点设置");
        mDeleteDialog.setMessage("设置站点信息需要重新启动");
        mDeleteDialog.setNegativeButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        v.setChecked(true);
                        SharePreferecesUtils.setParam(SettingActivity.this, StationConfig.STATION, str);
                        SharePreferecesUtils.setParam(SettingActivity.this, StationConfig.STATION_URL, url);
                        LogUtils.e("设置成功");
                        activity.startService(new Intent(activity, ClearCacheService.class));

                    }
                });
        mDeleteDialog.setPositiveButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        v.setChecked(false);
                        defaultStation();
                    }
                });
        mDeleteDialog.show();
    }

    private void defaultStation() {
        Object obj = SharePreferecesUtils.getParam(SettingActivity.this, "STATION", "def");
        String station = obj.toString();
        if (station == null || station.equals("def")) {
            zqzx_setting_rb4.setChecked(true);
            LogUtils.e("" + station);
        }
    }

    private String[] areas = new String[]{"Indonesia"};
    private String[] areas1 = new String[]{"def"};

    /**
     * 菜单弹出窗口
     */
    class AlertClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(SettingActivity.this).setTitle(getString(R.string.language)).setItems(areas, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (areas1[which].equals(SharePreferecesUtils.getParam(SettingActivity.this, StationConfig.STATION, "def").toString())) {
                        Log.e("areas1", "areas1" + areas1[which]);
                        return;
                    }
                    setting_chosse_station.setText(areas[which]);
                    SharePreferecesUtils.setParam(SettingActivity.this, StationConfig.STATION, areas1[which]);
                    activity.startService(new Intent(activity, ClearCacheService.class));
                    DataCleanManager.cleanCustomCache(App.getInstance().getAllDiskCacheDir()
                            + File.separator
                            + App.mTitle);
                    EventBus.getDefault().post(new RestartEvent());
                    finish();
                    dialog.dismiss();
                }
            }).show();
        }
    }

    public void getCacheSize() {
        GetFileSizeUtil fz = GetFileSizeUtil.getInstance();
        String cacheSize = "0K";
        try {

            cacheSize = "" + fz.FormetFileSize(fz.getFileDirSize(ImageLoader.getInstance().getDiskCache().getDirectory())
                    + fz.getFileDirSize(new File(App.getInstance().getJsonFileCacheRootDir())));

        } catch (Exception e) {
            e.printStackTrace();
        }
        zqzx_setting_cache.setText(cacheSize);
    }

    private void deleteSuccess() {
        loadingView.setVisibility(View.GONE);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (111 == msg.what) {
                zqzx_setting_cache.setText("0K");
                loadingView.setVisibility(View.VISIBLE);
                deleteSuccess();
            } else if (112 == msg.what) {
                TUtils.toast(getString(R.string.toast_bind_failed));
            } else if (113 == msg.what) {
                TUtils.toast(getString(R.string.toast_bind_success));
            }
        }
    };


    @OnClick(R.id.zqzx_setting_deletecache)
    private void deleteCache(View v) {
        mDeleteDialog = new Builder(this);
        mDeleteDialog.setTitle(R.string.prompt_clear_cache_dialog_title);
        mDeleteDialog.setMessage(R.string.prompt_clear_cache_dialog_msg);
        mDeleteDialog.setNegativeButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        loadingView.setVisibility(View.VISIBLE);
                        activity.startService(new Intent(activity, ClearCacheService.class));
                        handler.sendEmptyMessageDelayed(111, 2000);
                    }
                });
        mDeleteDialog.setPositiveButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        loadingView.setVisibility(View.GONE);
                    }
                });
        mDeleteDialog.show();
    }

    @OnClick(R.id.zqzx_setting_feedback)
    private void feedBack(View v) {
//        EventBus.getDefault().post(new UpdateNewsBeanDbEvent("Update_OK"));
        Intent intent = new Intent(this, ZQ_FeedBackActivity.class);
        startActivity(intent);
        AAnim.ActivityStartAnimation(this);
    }

    @OnClick(R.id.stitle_ll_back)
    private void goback(View v) {
        finish();
    }

    @OnClick(R.id.zqzx_setting_update)
    private void checkUpdate(View v) {
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
            return;
        }
        int version = 0;
        try {
            version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.e("test", "version--->" + version);
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("plat", "Android");
        params.addBodyParameter("version", "" + version);
        Log.e("test", "GET_VERSION--->" + InterfaceJsonfile.GET_VERSION);
        httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.GET_VERSION
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (obj == null) {
                    return;
                }
                Log.e("onSuccess", "onSuccess" + obj.toString());
                if (200 == obj.getIntValue("code")) {
                    UpdateBean mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), UpdateBean.class);
                    updateDialog(mBean.getDescription());
                    showNotification(mBean.getDescription());
                } else {
                }

            }

            @Override
            public void onFailure(HttpException error, String msg) {
            }
        });
    }

    private AlertDialog.Builder mUpdateDialog;

    private void updateDialog(String description) {
        mUpdateDialog = new AlertDialog.Builder(this);
        mUpdateDialog.setTitle(R.string.update_version);
        mUpdateDialog.setMessage(description);
        mUpdateDialog.setNegativeButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final String appPackageName = getPackageName();
                        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)); // 点击该通知后要跳转的Activity
                        startActivity(notificationIntent);
                    }
                });
        mUpdateDialog.setPositiveButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        mUpdateDialog.show();
    }

    /**
     * 在状态栏显示通知更新
     */
    private void showNotification(String description) {
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.logo,
                getString(R.string.app_name), System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.defaults = Notification.DEFAULT_LIGHTS;
        notification.ledARGB = Color.BLUE;
        notification.ledOnMS = 5000; //闪光时间，毫秒
        CharSequence contentTitle = getString(R.string.app_name); // 通知栏标题
        CharSequence contentText = description; // 通知栏内容
        final String appPackageName = getPackageName();
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)); // 点击该通知后要跳转的Activity
        PendingIntent contentItent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, contentTitle, contentText, contentItent);
        // 把Notification传递给NotificationManager
        notificationManager.notify(0, notification);
    }

}