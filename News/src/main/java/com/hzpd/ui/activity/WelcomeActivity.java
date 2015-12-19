package com.hzpd.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.NewsChannelBeanDB;
import com.hzpd.services.InitService;
import com.hzpd.ui.App;
import com.hzpd.ui.fragments.welcome.AdFlashFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.db.NewsListDbTask;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author color
 */
public class WelcomeActivity extends MWBaseActivity {

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.welcome;
    }

    public WelcomeActivity() {
        Log.e("test", "WelcomeActivity new ");
    }

    private volatile int done;
    private volatile boolean exists;
    private FragmentManager fm;
    private boolean isFirstStartApp;
    private View welcome_top_view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        Log.e("test", "  WelcomeActivity " + getResources().getBoolean(R.bool.isRom));
        exists = false;
        setContentView(R.layout.frame_welcome);
        welcome_top_view = findViewById(R.id.welcome_top_view);
        fm = getSupportFragmentManager();
        FragmentTransaction tran = fm.beginTransaction();
        isFirstStartApp = spu.getIsTodayFistStartApp();
        tran.replace(R.id.welcome_frame, new AdFlashFragment());
        tran.commit();
        getChooseNewsJson();
        getChannelJson();
        // 初始化服务
        Intent service = new Intent(this, InitService.class);
        service.setAction(InitService.InitAction);
        this.startService(service);
        Log.d(getLogTag(), "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void loadMainUI() {
        done++;
        if (done > 2) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            AAnim.ActivityFinish(this);
        }

    }

    public void getChannelJson() {
        final String channelCachePath = App.getInstance().getAllDiskCacheDir()
                + File.separator
                + SPUtil.getCountry()
                + File.separator
                + App.mTitle;
        Log.i("", "WelcomeActivity存储--->" + channelCachePath);
        final File channelCacheFile = new File(channelCachePath);
        final File target = App.getFile(App.getInstance().getAllDiskCacheDir() + File.separator + SPUtil.getCountry() + File.separator + "News");

        if (channelCacheFile.exists() && channelCacheFile.length() > 30) {
            exists = true;
            Log.e("loadMainUI()", "loadMainUI()");
            loadMainUI();
        }
        String urlChannelList = InterfaceJsonfile.CHANNELLIST + "News";
        String country = SPUtil.getCountry();
        urlChannelList = urlChannelList.replace("#country#", country.toLowerCase());
//			下载信息并保存
        HttpHandler httpHandler = httpUtils.download(urlChannelList,
                target.getAbsolutePath(),
                new RequestCallBack<File>() {
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        try {
                            String json = App.getFileContext(responseInfo.result);
//							LogUtils.e("WelcomeActivity数据源问题--->"+json);
                            if (json != null) {
                                JSONObject obj = FjsonUtil.parseObject(json);
                                if (null == obj) {
                                    if (!exists) {
                                        loadMainUI();
                                    }
                                    return;
                                }
                                List<NewsChannelBean> newestChannels = new ArrayList<>();
                                List<NewsChannelBeanDB> dbs = null;
                                try {
                                    if (obj.getIntValue("code") == 200) {
                                        // 读取json，获取频道信息
                                        JSONArray array = obj.getJSONArray("data");
                                        newestChannels = JSONArray
                                                .parseArray(array.toJSONString(),
                                                        NewsChannelBean.class);
                                        for (int i = 0; i < newestChannels.size(); i++) {
                                            NewsChannelBean newsChannelBean = newestChannels.get(i);
                                            newsChannelBean.getCnname();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (newestChannels == null) {
                                    newestChannels = new ArrayList<>();
                                }
                                dbs = dbHelper.getChannelDbUtils().findAll(NewsChannelBeanDB.class);
                                // 如果没有缓存
                                if (null == dbs || dbs.size() < 1) {
                                    Log.i("Welcome", "如果没有缓存");
                                    addLocalChannels(newestChannels);
                                    dbs = new ArrayList<>();
                                    for (NewsChannelBean bean : newestChannels) {
                                        dbs.add(new NewsChannelBeanDB(bean));
                                    }
                                    dbHelper.getChannelDbUtils().saveAll(dbs);
                                    SPUtil.updateChannel();
                                } else if (newestChannels.size() > 0) { // 如果有缓存
                                    Log.i("Welcome", "如果有缓存" + dbs.size());
                                    //TODO
                                    boolean change = false;
                                    for (int ii = 0; ii < dbs.size(); ii++) {
                                        NewsChannelBeanDB beanDB = dbs.get(ii);
                                        boolean delete = true;
                                        if (!TextUtils.isEmpty(beanDB.getTid()) && beanDB.getType() != NewsChannelBean.TYPE_RECOMMEND) {
                                            for (NewsChannelBean bean : newestChannels) {
                                                if (bean.getTid().equals(beanDB.getTid())) {
                                                    delete = false;
                                                    newestChannels.remove(bean);
                                                    break;
                                                }
                                            }
                                            if (delete) {
                                                change = true;
                                                dbs.remove(beanDB);
                                                --ii;
                                            }
                                        }
                                    }
                                    if (change || newestChannels.size() > 0) {
                                        for (NewsChannelBean bean : newestChannels) {
                                            dbs.add(new NewsChannelBeanDB(bean));
                                        }
                                        dbHelper.getChannelDbUtils().deleteAll(NewsChannelBeanDB.class);
                                        dbHelper.getChannelDbUtils().saveAll(dbs);
                                        SPUtil.updateChannel();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!exists) {
                            loadMainUI();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        if (!exists) {
                            loadMainUI();
                        }
                    }
                });
        handlerList.add(httpHandler);
    }

    public void jump(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    //TODO 提前获取推荐频道第一页
    public void getChooseNewsJson() {
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("tid", "" + NewsChannelBean.TYPE_RECOMMEND);
        params.addBodyParameter("Page", "1");
        params.addBodyParameter("PageSize", "15");
        UserBean user = SPUtil.getInstance().getUser();
        if (user != null && !TextUtils.isEmpty(user.getUid())) {
            params.addBodyParameter("uid", "" + user.getUid());
            params.addBodyParameter("tagIndex", "1");
            params.addBodyParameter("pageIndex", "1");
        }
        SPUtil.addParams(params);
        HttpHandler httpHandler = httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.CHANNEL_RECOMMEND_NEW
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                final JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj && obj.getIntValue("code") == 200) {
                    try {
                        App.getInstance().newTime = obj.getString("newTime");
                        App.getInstance().oldTime = obj.getString("oldTime");
                    } catch (Exception e) {
                    }
                    List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);
                    if (list != null) {
                        for (NewsBean bean : list) {
                            bean.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
                        }
                    }
                    if (null != list) {
                        LogUtils.i(" getChooseNewsJson --> " + list.size());
                        new NewsListDbTask(getApplicationContext()).saveList(list, null);
                    }
                }
                loadMainUI();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                loadMainUI();
            }
        });
        handlerList.add(httpHandler);
    }

    //	直接添加本地频道
    private void addLocalChannels(List<NewsChannelBean> list) {

//        NewsChannelBean channelSubject = new NewsChannelBean();
//        channelSubject.setTid("" + NewsChannelBean.TYPE_SUBJECT);
//        channelSubject.setType(NewsChannelBean.TYPE_SUBJECT);
//        channelSubject.setCnname(getString(R.string.menu_subject));
//        if (!list.contains(channelSubject)) {
//            list.add(0, channelSubject);
//            Log.d(getLogTag(), "add channelSubject");
//        }
//        NewsChannelBean channelVideo = new NewsChannelBean();
//        channelVideo.setTid("" + NewsChannelBean.TYPE_VIDEO);
//        channelVideo.setType(NewsChannelBean.TYPE_VIDEO);
//        channelVideo.setCnname(getString(R.string.menu_video));
//        if (!list.contains(channelVideo)) {
//            list.add(0, channelVideo);
//            Log.d(getLogTag(), "add channelVideo");
//        }
//        NewsChannelBean channelImageAlbum = new NewsChannelBean();
//        channelImageAlbum.setTid("" + NewsChannelBean.TYPE_IMAGE_ALBUM);
//        channelImageAlbum.setType(NewsChannelBean.TYPE_IMAGE_ALBUM);
//        channelImageAlbum.setCnname(getString(R.string.menu_album));
//        if (!list.contains(channelImageAlbum)) {
//            list.add(0, channelImageAlbum);
//            Log.d(getLogTag(), "add channelImageAlbum");
//        }


        NewsChannelBean channelRecommend = new NewsChannelBean();
        channelRecommend.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setType(NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setCnname(getString(R.string.recommend));
        channelRecommend.setDefault_show("1");
        // 添加推荐频道
        if (!list.contains(channelRecommend)) {
            list.add(0, channelRecommend);
        }
    }


}