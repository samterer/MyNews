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
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.facebook.Profile;
import com.hzpd.custorm.switchbutton.SwitchButton;
import com.hzpd.hflt.R;
import com.hzpd.modle.UpdateBean;
import com.hzpd.modle.event.FontSizeEvent;
import com.hzpd.modle.event.LoginOutEvent;
import com.hzpd.modle.event.RestartEvent;
import com.hzpd.modle.event.SetThemeEvent;
import com.hzpd.services.ClearCacheService;
import com.hzpd.ui.App;
import com.hzpd.ui.widget.FontTextView;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.CODE;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.GetFileSizeUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sithagi.countrycodepicker.CountryPicker;
import com.sithagi.countrycodepicker.CountryPickerListener;

import java.io.File;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import de.greenrobot.event.EventBus;

public class SettingActivity extends MBaseActivity {
    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.setting;
    }

    @ViewInject(R.id.stitle_tv_content)
    private TextView stitle_tv_content;

    @ViewInject(R.id.zqzx_setting_skin)
    private RelativeLayout zqzx_setting_skin;
    @ViewInject(R.id.setting_chosse_skin)
    private TextView setting_chosse_skin;


    @ViewInject(R.id.zqzx_setting_textsize)
    private RelativeLayout zqzx_setting_textsize;
    @ViewInject(R.id.setting_chosse_textsize)
    private FontTextView setting_chosse_textsize;


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
    //站点设置
    @ViewInject(R.id.sb_use_listener)
    private SwitchButton sb_use_listener;

    private View loadingView;
    private RelativeLayout zqzx_setting_login_out;
    private AlertDialog.Builder mDeleteDialog;
    private TextView setting_choose_country;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zqzx_setting_layout);

        try {
            ViewUtils.inject(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.changeStatusBar();
        loadingView = findViewById(R.id.app_progress_bar);
        zqzx_setting_login_out = (RelativeLayout) findViewById(R.id.zqzx_setting_login_out);
        if (spu.getUser() != null) {
            zqzx_setting_login_out.setVisibility(View.VISIBLE);
        }

        skin = new String[]{this.getResources().getString(R.string.skin_style_blue), this.getResources().getString(R.string.skin_style_red), "黑夜"};//"黑夜"
        switch (App.getInstance().getThemeName()) {
            case "0": {
                setting_chosse_skin.setText(this.getResources().getString(R.string.skin_style_blue));
            }
            break;
            case "1": {
                setting_chosse_skin.setText(this.getResources().getString(R.string.skin_style_red));
            }
            break;
            case "2": {
//                setting_chosse_skin.setText(this.getResources().getString(R.string.skin_style_red));
                setting_chosse_skin.setText("护眼模式");
            }
            break;
        }
        textSize = new String[]{this.getResources().getString(R.string.settings_option_font_large), this.getResources().getString(R.string.settings_option_font_medium), this.getResources().getString(R.string.settings_option_font_small)};//"Besar", "Sedang", "Kecil"
        zqzx_setting_skin.setOnClickListener(new ChooseSkinClickListener());

        stitle_tv_content.setText(R.string.title_settings);

        //设置字体大小
        switch (spu.getTextSize()) {
            case CODE.textSize_big: {
                setting_chosse_textsize.setText(getString(R.string.settings_option_font_large));
            }
            break;
            case CODE.textSize_normal: {
                setting_chosse_textsize.setText(getString(R.string.settings_option_font_medium));
            }
            break;
            case CODE.textSize_small: {
                setting_chosse_textsize.setText(getString(R.string.settings_option_font_small));
            }
            break;
        }
        zqzx_setting_textsize.setOnClickListener(new TextSizeAlertClickListener());

        //推送获取当前状态
        Log.i("", "spu.getOffTuiSong()" + spu.getOffTuiSong());
        if (spu.getOffTuiSong()) {
            LogUtils.e("设置推送状态");
            sb_use_listener.setChecked(true);
        } else {
            sb_use_listener.setChecked(false);
        }
        //推送消息
        sb_use_listener.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pushSwitch(isChecked);
                } else {
                    pushSwitch(isChecked);
                }
            }
        });

        //获取版本信息
        zqzx_setting_tv_version.setText(App.getInstance().getVersionName());

        //设置国家
        setting_choose_country = (TextView) findViewById(R.id.setting_choose_country);
        Object country_Name = SharePreferecesUtils.getParam(SettingActivity.this, "CountryName", "Indonesia");
        setting_choose_country.setText("" + country_Name.toString());
        findViewById(R.id.zqzx_setting_choose_country).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CountryPicker picker = CountryPicker.newInstance("SelectCountry");
                picker.setListener(new CountryPickerListener() {

                    @Override
                    public void onSelectCountry(String name, String code, String dialCode) {
                        Log.i("Setting", "CountryName:" + name + "\nCode: " + code + "\nCurrency: " + CountryPicker.getCurrencyCode(code) + "\nDial Code: " + dialCode);
                        setting_choose_country.setText("" + name);
                        SharePreferecesUtils.setParam(SettingActivity.this, "CountryName", name);
//                        activity.startService(new Intent(activity, ClearCacheService.class));
                        SPUtil.setCountry(code);
//                      重新设置
                        EventBus.getDefault().post(new RestartEvent());
                        restartApplication();
                        finish();
                        picker.dismiss();
                    }
                });

                picker.show(getSupportFragmentManager(), "COUNTRY_CODE_PICKER");
            }
        });

        getCacheSize();
    }

    private void restartApplication() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                                Log.i("", "arg0-->" + arg0 + " arg1-->" + arg1);
                                if (arg2 != null) {
                                    for (String s : arg2) {
                                        Log.i("", "arg2->" + s);
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
        if (AvoidOnClickFastUtils.isFastDoubleClick())
            return;
        Intent mIntent = new Intent(this, AboutUsActivity.class);
        startActivity(mIntent);
        AAnim.ActivityStartAnimation(this);
    }

    private String[] skin;

    /**
     * 菜单弹出窗口
     */
    class ChooseSkinClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
//            @string/setting_skin
            new AlertDialog.Builder(SettingActivity.this).setTitle(getResources().getString(R.string.setting_skin)).setItems(skin, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (App.getInstance().getThemeName().equals("" + which)) {
                        return;
                    }
                    setting_chosse_skin.setText(skin[which]);
                    Log.e("which", "which" + which);
                    App.getInstance().setThemeName("" + which);
                    EventBus.getDefault().post(new SetThemeEvent());
                    stitle_tv_content.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 300);
                }
            }).show();
        }
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


    private String[] areas = new String[]{"Indonesia"};
    private String[] areas1 = new String[]{"def"};


    public void getCacheSize() {
        GetFileSizeUtil fz = GetFileSizeUtil.getInstance();
        String cacheSize = "0B";
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
                zqzx_setting_cache.setText("0B");
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
                        //刪除SharedPreference
//                        DataCleanManager.cleanSharedPreference(SettingActivity.this);
//                        App.getInstance().newTimeMap.put(channelbean.getTid(), obj.getString("newTime"));
                        App.getInstance().newTimeMap.clear();
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
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
            return;
        }

        Intent intent = new Intent(this, ZQ_FeedBackActivity.class);
        startActivity(intent);
        AAnim.ActivityStartAnimation(this);
    }

    @OnClick(R.id.stitle_ll_back)
    private void goback(View v) {
        finish();
    }

    @OnClick(R.id.zqzx_setting_login_out)
    private void loginOut(View v) {
        String logout = getResources().getString(
                R.string.com_facebook_loginview_log_out_action);
        String cancel = getResources().getString(
                R.string.com_facebook_loginview_cancel_action);
        String message;
        Profile profile = Profile.getCurrentProfile();
        if (profile != null && profile.getName() != null) {
            message = String.format(
                    getResources().getString(
                            R.string.com_facebook_loginview_logged_in_as),
                    profile.getName());
        } else {
            message = getResources().getString(
                    R.string.com_facebook_loginview_logged_in_using_facebook);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton(logout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        EventBus.getDefault().post(new LoginOutEvent());
                        finish();
                    }
                })
                .setNegativeButton(cancel, null);
        builder.create().show();
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
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("plat", "Android");
        params.addBodyParameter("version", "" + version);
        SPUtil.addParams(params);
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
                if (200 == obj.getIntValue("code")) {
                    UpdateBean mBean = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), UpdateBean.class);
                    SPUtil.updateDialog(mBean.getDescription(), SettingActivity.this);
                } else {
                    Toast.makeText(SettingActivity.this, getString(R.string.update_no_version), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Toast.makeText(SettingActivity.this, getString(R.string.toast_cannot_connect_to_server), Toast.LENGTH_SHORT).show();
            }
        });
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