package com.hzpd.ui.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.hzpd.adapter.MainPagerAdapter;
import com.hzpd.custorm.DrawerArrowDrawable;
import com.hzpd.custorm.MyViewPager;
import com.hzpd.hflt.R;
import com.hzpd.modle.event.RestartEvent;
import com.hzpd.modle.event.SetThemeEvent;
import com.hzpd.services.InitService;
import com.hzpd.ui.App;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.ui.fragments.NewsFragment;
import com.hzpd.ui.fragments.ZY_DiscoveryFragment;
import com.hzpd.ui.fragments.ZY_RightFragment;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.ExitApplication;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.news.update.DownloadService;
import com.news.update.LocalUpdateDialogFragment;
import com.news.update.LocalUpdateEvent;
import com.news.update.UpdateUtils;
import com.news.update.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class
        MainActivity extends BaseActivity {

    public final static String TAG = "NEWS";
    public final static String TAG_DIALOG = "NEWS_DIALOG";

    public MainActivity() {
        super();
    }

    public static final int REQUEST_IMAGE = 2;
    private int itemSelectPositon = 0;
    private Fragment currentFrag;
    @ViewInject(R.id.title_content)
    private TextView mTextView;
    public final static int FILECHOOSER_RESULTCODE = 1900;

    private DrawerArrowDrawable drawerArrowDrawable;
    private float offset;
    private boolean flipped;

    List<HttpHandler> handlerList = new ArrayList<>();


    private MyViewPager viewPager;
    private MainPagerAdapter adapter;
    private TextView[] tv_menu;
    private BaseFragment[] fragments;

    @Override
    public void finish() {
        App.isStartApp = false;
        super.finish();
    }

    private View login_bg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        EventBus.getDefault().register(this);
        long start = System.currentTimeMillis();
        setContentView(R.layout.app_main);
        ViewUtils.inject(this);
        login_bg = findViewById(R.id.login_bg);
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
        fragments[0] = new NewsFragment();
        fragments[1] = new ZY_DiscoveryFragment();
        fragments[2] = new ZY_RightFragment();
        adapter.add(fragments[0]);
        adapter.add(fragments[1]);
        adapter.add(fragments[2]);
        viewPager.setOffscreenPageLimit(adapter.getCount());
        viewPager.setAdapter(adapter);
        onClickIndex(0);

        checkVersion();

        Thread.setDefaultUncaughtExceptionHandler(App.uncaughtExceptionHandler);

        setTitleText(getString(R.string.app_name));
        Log.e("MainActivity", System.currentTimeMillis() - start);
        App.isStartApp = true;
        EventUtils.sendStart(this);
        Log.e("MainActivity", System.currentTimeMillis() - start);
        Intent intent = new Intent(this, InitService.class);
        intent.setAction(InitService.UserLogAction);
        startService(intent);
        showLocalUpdateDialog(this);
    }

    public void onClickIndex(int index) {
        // TODO Auto-generated method stub
//        fragments[index].OnSelected();
        viewPager.setCurrentItem(index, false);

        for (int i = 0; i < tv_menu.length; i++) {

            if (index == i) {
                tv_menu[i].setSelected(true);
            } else {
                tv_menu[i].setSelected(false);
            }
        }
    }

    @OnClick({R.id.main_title_left, R.id.main_title_right})
    private void onclick(View view) {
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.main_title_left: {
            }
            break;
            case R.id.main_title_right: {
                Intent mIntent = new Intent();
                mIntent.setClass(MainActivity.this, SearchActivity.class);
                startActivity(mIntent);
                AAnim.ActivityStartAnimation(this);

            }
            break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //退出程序
        ExitApplication.exit(this);
    }

    public void setTitleText(String name) {
        SPUtil.setTitleFont(mTextView);
        mTextView.setText(name);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (2 == requestCode) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                String result = bundle.getString("result");
                LogUtils.i("result--->" + result);
                TUtils.toast(getString(R.string.toast_scan_content, result));
            }
        }
    }

    protected HttpUtils httpUtils;

    private void checkVersion() {

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        for (HttpHandler httpHandler : handlerList) {
            if (httpHandler.getState() == HttpHandler.State.LOADING || httpHandler.getState() == HttpHandler.State.STARTED) {
                httpHandler.setRequestCallBack(null);
                httpHandler.cancel();
            }
        }
        handlerList.clear();
        httpUtils = null;
        super.onDestroy();
    }

    public void onEventMainThread(SetThemeEvent event) {
        recreate();
    }

    private void restartApplication() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void onEventMainThread(RestartEvent event) {
//        restartApplication();
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

                if (UpdateUtils.isRomVersion(activity.getApplicationContext())) {
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
                        Log.e("update", " IS_DOWNLOADING ");
                        return;
                    }
                }
                boolean autoD = pref.getBoolean(UpdateUtils.KEY.KEY_AUTO_DOWNLOAD, false);
                int cVersion = pref.getInt(UpdateUtils.KEY.COMPLETED_VERSION_CODE, 0);
                Log.e("update", " autoD " + autoD);
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