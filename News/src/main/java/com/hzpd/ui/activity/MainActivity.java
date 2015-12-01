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
import com.hzpd.ui.fragments.NewsFragment;
import com.hzpd.ui.fragments.WebviewFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
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
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class MainActivity extends BaseActivity {

    public final static String TAG = "NEWS";

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
        ft.add(R.id.root, currentFrag, TAG);
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
        if (httpUtils == null) {
            httpUtils = SPUtil.getHttpUtils();
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
        HttpHandler httpHandler = httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.GET_VERSION
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (responseInfo==null){
                    return;
                }
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
        handlerList.add(httpHandler);
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