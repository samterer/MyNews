package com.hzpd.ui.activity;


import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.PushBuilder;
import de.greenrobot.event.EventBus;


public class MainActivity extends BaseActivity implements I_ChangeFm {

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
        Thread.setDefaultUncaughtExceptionHandler(App.uncaughtExceptionHandler);
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

        setStyleCustom();

    }

    private void setStyleCustom() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String date = sDateFormat.format(curDate);
//        CustomPushNotificationBuilder builder = new CustomPushNotificationBuilder(MainActivity.this,
//                R.layout.customer_notitfication_layout, R.id.icon, R.id.title, R.id.text);  // 指定定制的 Notification Layout
//        builder.statusBarDrawable = R.drawable.details_related_news;      // 指定最顶层状态栏小图标
//        builder.layoutIconDrawable = R.drawable.logo;   // 指定下拉状态栏时显示的通知图标
        PushBuilder builder1 = new PushBuilder(MainActivity.this,
                R.layout.customer_notitfication_layout, R.id.icon, R.id.title, R.id.text, R.id.time);
        builder1.layoutIconDrawable = R.drawable.logo;   // 指定下拉状态栏时显示的通知图标
        JPushInterface.setPushNotificationBuilder(1, builder1);
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
        httpUtils = SPUtil.getHttpUtils();
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
                    SPUtil.showNotification(mBean.getDescription(), getApplicationContext());
                    SPUtil.updateDialog(mBean.getDescription(), MainActivity.this);
                } else {
                }

            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.e("test", "onFailure");
            }
        });
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