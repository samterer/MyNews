package com.hzpd.ui.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.hzpd.adapter.MainPagerAdapter;
import com.hzpd.custorm.MyViewPager;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.event.LoginEvent;
import com.hzpd.modle.event.RefreshEvent;
import com.hzpd.modle.event.RestartEvent;
import com.hzpd.modle.event.SetThemeEvent;
import com.hzpd.services.InitService;
import com.hzpd.ui.App;
import com.hzpd.ui.ConfigBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.ui.fragments.FeedbackTagFragment;
import com.hzpd.ui.fragments.LoginTagFragment;
import com.hzpd.ui.fragments.NewsFragment;
import com.hzpd.ui.fragments.NewsItemFragment;
import com.hzpd.ui.fragments.ZY_DiscoveryFragment;
import com.hzpd.ui.fragments.ZY_RightFragment;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.ExitApplication;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.news.update.DownloadService;
import com.news.update.LocalUpdateDialogFragment;
import com.news.update.LocalUpdateEvent;
import com.news.update.UpdateUtils;
import com.news.update.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;


public class MainActivity extends BaseActivity {
    public final static String TAG = "NEWS";
    public final static String TAG_DIALOG = "NEWS_DIALOG";

    public MainActivity() {
        super();
    }

    private MyViewPager viewPager;
    private MainPagerAdapter adapter;
    private TextView[] tv_menu;
    private BaseFragment[] fragments;

    @Override
    public void finish() {
        App.isStartApp = false;
        super.finish();
    }

    private AlertDialog.Builder mDeleteDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        EventBus.getDefault().register(this);
        setContentView(R.layout.app_main);
        tag = OkHttpClientManager.getTag();
        viewPager = (MyViewPager) findViewById(R.id.main_pager);
        adapter = new MainPagerAdapter(getSupportFragmentManager());
        fragments = new BaseFragment[3];
        tv_menu = new TextView[3];
        tv_menu[0] = (TextView) findViewById(R.id.tv_tab_menu0);
        tv_menu[1] = (TextView) findViewById(R.id.tv_tab_menu1);
        tv_menu[2] = (TextView) findViewById(R.id.tv_tab_menu2);
        for (int i = 0; i < tv_menu.length; i++) {
            final int cur = i;
            tv_menu[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    onClickIndex(cur);
                }
            });
        }
        int index = 0;
        if (ConfigBean.getInstance().open_channel.contains(SPUtil.getCountry())) {
            fragments[index] = new NewsFragment();
            adapter.add(fragments[index++]);
        } else {
            NewsChannelBean channelBean = new NewsChannelBean();
            channelBean.setTid("1");
            channelBean.setCnname("NEWS");
            fragments[index] = new NewsItemFragment(channelBean);
            adapter.add(fragments[index++]);
        }
        if (ConfigBean.getInstance().open_tag.contains(SPUtil.getCountry())) {
            fragments[index] = new ZY_DiscoveryFragment();
            adapter.add(fragments[index++]);
        } else {
            tv_menu[1].setVisibility(View.GONE);
        }
        fragments[index] = new ZY_RightFragment();
        adapter.add(fragments[index++]);
        viewPager.setOffscreenPageLimit(adapter.getCount());
        viewPager.setAdapter(adapter);
        onClickIndex(0);
        Thread.setDefaultUncaughtExceptionHandler(App.uncaughtExceptionHandler);
        App.isStartApp = true;
        EventUtils.sendStart(this);
        showLocalUpdateDialog(this);
        showFeedback();
//        showLoginFragment();
//        showLogin();
    }

    private void showLogin() {
        mDeleteDialog = new AlertDialog.Builder(this);
        mDeleteDialog.setTitle("FaceBook登录");
        mDeleteDialog.setMessage("登录以后更加精彩");
        mDeleteDialog.setNegativeButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.i("MainActivity", "FaceBook登录");
                        EventBus.getDefault().post(new LoginEvent());
                    }
                });
        mDeleteDialog.setPositiveButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (spu.getUser() == null)
            mDeleteDialog.show();
    }

    public void onClickIndex(int index) {
        int current = viewPager.getCurrentItem();
        viewPager.setCurrentItem(index, false);
        for (int i = 0; i < tv_menu.length; i++) {
            if (index == i) {
                tv_menu[i].setSelected(true);
            } else {
                tv_menu[i].setSelected(false);
            }
        }
        if (index == 0 && current == 0) {
            EventBus.getDefault().post(new RefreshEvent());
        }
    }

    @Override
    public void onBackPressed() {
        //退出程序
        ExitApplication.exit(this);
    }

    private static final String FEEDBACK_TIME = "feedback_time";
    public static final String FEEDBACK_COUNT = "feedback_count";

    private static final String LOGIN_TIME = "feedback_time";
    public static final String LOGIN_COUNT = "feedback_count";

    public void showFeedback() {
        if (FeedbackTagFragment.shown || LoginTagFragment.shown) {
            return;
        }
        int countLogin = (int) SPUtil.getGlobal(LOGIN_COUNT, 0L);
        if (countLogin < 3) {
            long lastLogin = SPUtil.getGlobal(LOGIN_TIME, 0L);
            ++countLogin;
            if (lastLogin == 0 || System.currentTimeMillis() - lastLogin > countLogin * ConfigBean.getInstance().rate_time) {
                SPUtil.setGlobal(LOGIN_COUNT, countLogin);
                SPUtil.setGlobal(LOGIN_TIME, System.currentTimeMillis());
                LoginTagFragment fragment = new LoginTagFragment();
                fragment.show(getSupportFragmentManager(), LoginTagFragment.TAG);
                return;
            }
        }


        int countFeedBack = (int) SPUtil.getGlobal(FEEDBACK_COUNT, 0L);
        if (countFeedBack < 3) {
            long last = SPUtil.getGlobal(FEEDBACK_TIME, 0L);
            if (last == 0) {
                SPUtil.setGlobal(FEEDBACK_TIME, System.currentTimeMillis());
                return;
            }
            ++countFeedBack;
            if (System.currentTimeMillis() - last > countFeedBack * ConfigBean.getInstance().rate_time) {
                SPUtil.setGlobal(FEEDBACK_COUNT, countFeedBack);
                SPUtil.setGlobal(FEEDBACK_TIME, System.currentTimeMillis());
                FeedbackTagFragment fragment = new FeedbackTagFragment();
                fragment.show(getSupportFragmentManager(), FeedbackTagFragment.TAG);
            }
        }


    }


    public void showLoginFragment() {
        if (LoginTagFragment.shown) {
            return;
        }
        int count = (int) SPUtil.getGlobal(LOGIN_COUNT, 0L);
        if (count >= 3) {
            return;
        }
        long last = SPUtil.getGlobal(LOGIN_TIME, 0L);
        if (last == 0) {
            SPUtil.setGlobal(LOGIN_TIME, System.currentTimeMillis());
            return;
        }
        ++count;
        if (System.currentTimeMillis() - last > count * ConfigBean.getInstance().rate_time) {
            SPUtil.setGlobal(LOGIN_COUNT, count);
            SPUtil.setGlobal(LOGIN_TIME, System.currentTimeMillis());
            LoginTagFragment fragment = new LoginTagFragment();
            fragment.show(getSupportFragmentManager(), LoginTagFragment.TAG);
        }

    }

    private Object tag;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(this, InitService.class);
        intent.setAction(InitService.UserLogAction);
        startService(intent);
        // 初始化服务
        Intent service = new Intent(this, InitService.class);
        service.setAction(InitService.InitAction);
        startService(service);
        App.uiService.shutdownNow();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        OkHttpClientManager.cancel(tag);
        super.onDestroy();
    }

    public void onEventMainThread(SetThemeEvent event) {
        recreate();

    }

    public void onEventMainThread(RestartEvent event) {
        finish();
    }


    public void onEventMainThread(LocalUpdateEvent event) {
        Log.e("UPDATE", null);
        showLocalUpdateDialog(this);
    }

    /**
     * 弹出更新提示对话框
     */
    public static void showLocalUpdateDialog(FragmentActivity activity) {

        if (LocalUpdateDialogFragment.shown) {
            return;
        }

        SharedPreferences pref = activity.getSharedPreferences(
                UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        try {
            if (pref.getBoolean(UpdateUtils.KEY.KEY_SILENCE_INSTALL, false)) {
                return;
            }
            long later = pref.getLong(UpdateUtils.KEY.UPDATE_LATER_TIME, 0L);
            if (later > 10000) {
                if (System.currentTimeMillis() - later > UpdateUtils.UPDATE_LATER_TIME) {
                    pref.edit()
                            .putLong(UpdateUtils.KEY.UPDATE_LATER_TIME, 0L)
                            .apply();
                } else {
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (pref.getBoolean(UpdateUtils.KEY.KEY_HAS_NEW, false)) {
                boolean forcing = pref.getBoolean(UpdateUtils.KEY.KEY_FORCING_UPDATE, false);

                int versionCode = pref.getInt(UpdateUtils.KEY.KEY_VERSION_CODE, 0);
                if (versionCode <= Utils.getVersionCode(activity)) {
                    Log.e("update", "versionCode "
                            + versionCode + ":" + Utils.getVersionCode(activity));
                    pref.edit().putBoolean(UpdateUtils.KEY.KEY_HAS_NEW,
                            false);
                    return;
                }

                if (!UpdateUtils.isRomVersion(activity.getApplicationContext())) {
                    try {
                        String dialogContent = pref.getString(UpdateUtils.KEY.KEY_DIALOG_CONTENT, "");
                        LocalUpdateDialogFragment fragment = (LocalUpdateDialogFragment) Fragment.instantiate(activity, LocalUpdateDialogFragment.class.getName
                                ());
                        Bundle bundle = new Bundle();
                        bundle.putString(LocalUpdateDialogFragment.DIALOG_CONTENT, dialogContent);
                        bundle.putBoolean(LocalUpdateDialogFragment.FORCING, forcing);
                        fragment.setArguments(bundle);
                        fragment.show(activity.getSupportFragmentManager(), TAG_DIALOG);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }


                if (!forcing) {
                    if (pref.getBoolean(UpdateUtils.KEY.IS_DOWNLOADING, false)
                            || pref.getBoolean(UpdateUtils.KEY.IS_WIFI_DOWNLOADING, false)) {
                        return;
                    }
                }
                boolean autoD = pref.getBoolean(UpdateUtils.KEY.KEY_AUTO_DOWNLOAD, false);
                int cVersion = pref.getInt(UpdateUtils.KEY.COMPLETED_VERSION_CODE, 0);
                if (autoD) {
                    File root = Environment.getExternalStorageDirectory();
                    if (root != null) {
                        File fold = new File(root, UpdateUtils.PATH_SAVE);
                        File target = new File(fold, UpdateUtils.getFileName(activity.getApplicationContext()));
                        Intent intent = new Intent(activity, DownloadService.class);
                        if (pref.getBoolean(UpdateUtils.KEY.KEY_MOBILE_AUTO, false)) {
                            intent.putExtra(DownloadService.TAG, UpdateUtils.KEY.IS_DOWNLOADING);
                        } else {
                            intent.putExtra(DownloadService.TAG, UpdateUtils.KEY.IS_WIFI_DOWNLOADING);
                        }
                        if (!target.exists() || cVersion < versionCode) {
                            Log.e("update", " target is not exists ");
                            activity.startService(intent);
                            if (!forcing) {
                                return;
                            }
                        } else if (target.exists()) {
                            String md5 = null;
                            md5 = UpdateUtils.getHash(target.getAbsolutePath(), "MD5").toLowerCase();
                            if (!md5.equals(pref.getString(UpdateUtils.KEY.KEY_MD5, ""))) {
                                target.delete();
                                pref.edit().putInt(UpdateUtils.KEY.COMPLETED_VERSION_CODE, 0).apply();
                                activity.startService(intent);
                                if (!forcing) {
                                    return;
                                }
                            }
                        }
                    }
                }
                String dialogContent = pref.getString(UpdateUtils.KEY.KEY_DIALOG_CONTENT, "");
                LocalUpdateDialogFragment fragment = (LocalUpdateDialogFragment) Fragment.instantiate(activity, LocalUpdateDialogFragment.class.getName
                        ());
                Bundle bundle = new Bundle();
                bundle.putString(LocalUpdateDialogFragment.DIALOG_CONTENT, dialogContent);
                bundle.putBoolean(LocalUpdateDialogFragment.FORCING, forcing);
                bundle.putBoolean(LocalUpdateDialogFragment.AUTO_DOWNLOAD, autoD);
                fragment.setArguments(bundle);
                fragment.show(activity.getSupportFragmentManager(), TAG_DIALOG);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}