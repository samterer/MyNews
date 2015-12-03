package com.hzpd.ui.activity;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.MainPagerAdapter;
import com.hzpd.custorm.DrawerArrowDrawable;
import com.hzpd.custorm.MyViewPager;
import com.hzpd.hflt.R;
import com.hzpd.modle.UpdateBean;
import com.hzpd.modle.event.RestartEvent;
import com.hzpd.modle.event.SetThemeEvent;
import com.hzpd.services.InitService;
import com.hzpd.ui.App;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.ui.fragments.NewsFragment;
import com.hzpd.ui.fragments.WebviewFragment;
import com.hzpd.ui.fragments.ZY_FindFragment;
import com.hzpd.ui.fragments.ZY_RightFragment;
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


    private MyViewPager viewPager;
    private MainPagerAdapter adapter;
    private TextView[] tv_menu;
    private BaseFragment[] fragments;

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
        fragments[1] = new ZY_FindFragment();
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
                if (responseInfo == null) {
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
    }

    private void restartApplication() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}