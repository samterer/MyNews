package com.hzpd.ui.activity;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.custorm.DrawerArrowDrawable;
import com.hzpd.hflt.R;
import com.hzpd.modle.UpdateBean;
import com.hzpd.modle.event.RestartEvent;
import com.hzpd.modle.event.SetThemeEvent;
import com.hzpd.services.InitService;
import com.hzpd.ui.App;
import com.hzpd.ui.fragments.MySearchFragment;
import com.hzpd.ui.fragments.NewsAlbumFragment;
import com.hzpd.ui.fragments.NewsFragment;
import com.hzpd.ui.fragments.VideoListFragment;
import com.hzpd.ui.fragments.WebviewFragment;
import com.hzpd.ui.fragments.ZhuantiFragment;
import com.hzpd.ui.fragments.action.ActionListFragment;
import com.hzpd.ui.interfaces.I_ChangeFm;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.CODE;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.ExitApplication;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import de.greenrobot.event.EventBus;


public class MainActivity extends BaseActivity implements I_ChangeFm {
    @Override
    public String getAnalyticPageName() {
        return "新闻主页";
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

    @Override
    public void finish() {
        App.isStartApp = false;
        super.finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        EventBus.getDefault().register(this);
        long start = System.currentTimeMillis();
        setContentView(R.layout.app_main);
        ViewUtils.inject(this);
        checkVersion();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("Alert", "MainActivity  UncaughtException !!!");
                paramThrowable.printStackTrace();
                System.exit(0);
            }
        });
        FragmentTransaction ft = fm.beginTransaction();
        currentFrag = new NewsFragment();
        ft.add(R.id.root, currentFrag, App.menuList.get(CODE.MENU_NEWS).getName());
        ft.commit();
        setTitleText(getString(R.string.app_name));
//        setTitleText(App.menuList.get(CODE.MENU_NEWS).getName());
        Log.e("MainActivity", System.currentTimeMillis() - start);
        App.isStartApp = true;
        EventUtils.sendStart(this);
        Log.e("MainActivity", System.currentTimeMillis() - start);

        Intent intent = new Intent(this, InitService.class);
        intent.setAction(InitService.UserLogAction);
        startService(intent);

    }

    @OnClick({R.id.main_title_left, R.id.main_title_right})
    private void onclick(View view) {
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.main_title_left: {
                slidingMenu.showMenu();
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
        if (getSlidingMenu().isMenuShowing() || getSlidingMenu().isSecondaryMenuShowing()) {
            getSlidingMenu().toggle(true);
            return;
        }
        if (currentFrag instanceof WebviewFragment) {
            if (((WebviewFragment) currentFrag).canback()) {
                return;
            }
        }

        //退出程序
        ExitApplication.exit(this);
    }

    public void setTitleText(String name) {
        SPUtil.setTitleFont(mTextView);
        mTextView.setText(name);
    }

    @Override
    public void changeFm(int position) {

        if (itemSelectPositon == position) {
            getSlidingMenu().toggle(true);
            return;
        }

        itemSelectPositon = position;
        Fragment fragment = null;
        setTitleText(App.menuList.get(position).getName());

        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(currentFrag);
        fragment = fm.findFragmentByTag(App.menuList.get(position).getName());
        if (null == fragment) {
            switch (position) {
                case CODE.MENU_NEWS:// 新闻
                    fragment = new NewsFragment();
                    break;
                case CODE.MENU_ALBUM:// 图集
                    fragment = new NewsAlbumFragment();
                    break;
                case CODE.MENU_VIDEO_RECORDING:// 视频
                    fragment = new VideoListFragment();
                    break;
                case CODE.MENU_SPECIAL: { // 专题(专题报道)
                    fragment = new ZhuantiFragment();
                }
                break;
                case CODE.MENU_ACTION: {// 活动
                    fragment = new ActionListFragment();
                }
                break;
                case CODE.MENU_SEARCH: {// 搜索
                    fragment = new MySearchFragment();
                }
                break;
                default: {

                }
                break;
            }
            ft.add(R.id.root, fragment, App.menuList.get(position).getName());
        } else {
            ft.show(fragment);
        }
        ft.commit();
        currentFrag = fragment;
        getSlidingMenu().toggle(true);

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
        httpUtils = new HttpUtils();
        int version = 0;
        try {
            version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("plat", "Android");
        params.addBodyParameter("version", "" + version);
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
                    showNotification(mBean.getDescription());
                    updateDialog(mBean.getDescription());
                } else {
                }

            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.e("test", "onFailure");
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
     * 在状态栏显示通知
     */
    private void showNotification(String description) {
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.logo,
                getString(R.string.app_name), System.currentTimeMillis());
//        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.defaults = Notification.DEFAULT_LIGHTS;
        notification.ledARGB = Color.BLUE;
        notification.ledOnMS = 5000; //闪光时间，毫秒
        CharSequence contentTitle = getString(R.string.app_name); // 通知栏标题
        CharSequence contentText = description; // 通知栏内容
        final String appPackageName = getPackageName();
        Log.e("test", "test" + appPackageName);
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)); // 点击该通知后要跳转的Activity
        PendingIntent contentItent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, contentTitle, contentText, contentItent);
        notificationManager.notify(0, notification);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(RestartEvent event) {
        restartApplication();
    }

    public void onEventMainThread(SetThemeEvent event) {
        recreate();
//        finish();
    }



    private void restartApplication() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}