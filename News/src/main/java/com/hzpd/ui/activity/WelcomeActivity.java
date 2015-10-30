package com.hzpd.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.db.AlbumBeanDB;
import com.hzpd.modle.db.AlbumItemBeanDB;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.db.VideoItemBeanDb;
import com.hzpd.modle.db.ZhuantiBeanDB;
import com.hzpd.services.InitService;
import com.hzpd.ui.App;
import com.hzpd.ui.fragments.welcome.AdFlashFragment;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SerializeUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.db.NewsListDbTask;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * @author color
 */
public class WelcomeActivity extends MWBaseActivity {

    @Override
    public String getAnalyticPageName() {
        return "欢迎页";
    }

    private volatile int done;
    private volatile boolean exists;
    private FragmentManager fm;
    private boolean isFirstStartApp;
    private View welcome_top_view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
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
        createDb();
    }

    // 创建数据库
    private void createDb() {
        try {
            DbUtils newsListDb = DBHelper.getInstance(getApplicationContext()).getNewsListDbUtils();
            newsListDb.count(AlbumBeanDB.class);
            newsListDb.count(AlbumItemBeanDB.class);
            newsListDb.count(NewsBeanDB.class);
            newsListDb.count(VideoItemBeanDb.class);
            newsListDb.count(ZhuantiBeanDB.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                + App.mTitle;
//		LogUtils.e("WelcomeActivity存储--->" + channelCachePath);
        final File channelCacheFile = new File(channelCachePath);
        final File target = App.getFile(App.getInstance().getAllDiskCacheDir() + File.separator + "News");

        if (channelCacheFile.exists() && channelCacheFile.length() > 30) {
            exists = true;
            loadMainUI();
        }
        String station = SharePreferecesUtils.getParam(WelcomeActivity.this, StationConfig.STATION, "def").toString();
        String CHANNELLIST_url = null;
        if (station.equals(StationConfig.DEF)) {
            CHANNELLIST_url = InterfaceJsonfile.CHANNELLIST;
        } else if (station.equals(StationConfig.YN)) {
            CHANNELLIST_url = InterfaceJsonfile_YN.CHANNELLIST;
        } else if (station.equals(StationConfig.TW)) {
            CHANNELLIST_url = InterfaceJsonfile_TW.CHANNELLIST;
        }
        String urlChannelList = CHANNELLIST_url + "News";
//			下载信息并保存
        httpUtils.download(urlChannelList,
                target.getAbsolutePath(),
                new RequestCallBack<File>() {
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        String json = App.getFileContext(responseInfo.result);
//							LogUtils.e("WelcomeActivity数据源问题--->"+json);
                        if (json != null) {
                            LogUtils.i("channel-->" + json);
                            JSONObject obj = FjsonUtil.parseObject(json);
                            if (null == obj) {
                                return;
                            }

                            // 读取json，获取频道信息
                            JSONArray array = obj.getJSONArray("data");
                            List<NewsChannelBean> newestChannels = JSONArray
                                    .parseArray(array.toJSONString(),
                                            NewsChannelBean.class);
                            for (int i = 0; i < newestChannels.size(); i++) {
                                NewsChannelBean newsChannelBean = newestChannels.get(i);
                                newsChannelBean.getCnname();
//									LogUtils.e("WelcomeActivity数据源问题--->"+newsChannelBean.getCnname());
                            }

                            // 读取频道信息的本地缓存
                            SerializeUtil<List<NewsChannelBean>> serializeUtil = new SerializeUtil<List<NewsChannelBean>>();
                            List<NewsChannelBean> cacheChannels = serializeUtil
                                    .readyDataToFile(channelCacheFile.getAbsolutePath());

                            // 如果没有缓存
                            if (null == cacheChannels || cacheChannels.size() < 1) {

                                if (newestChannels != null && newestChannels.size() > 0) {

                                    addLocalChannels(newestChannels);
                                    // 缓存频道信息到SD卡上
                                    serializeUtil.writeDataToFile(newestChannels, channelCachePath);
                                }
                            } else { // 如果有缓存
                                HashMap<String, NewsChannelBean> channelMap = new HashMap<String, NewsChannelBean>();
                                for (NewsChannelBean stb : newestChannels) {
                                    channelMap.put(stb.getTid(), stb);
                                }

                                for (int i = 0; i < cacheChannels.size(); i++) {
                                    // 缓存的频道信息
                                    NewsChannelBean cacheChannel = cacheChannels.get(i);
                                    // 最新获取的频道信息
                                    NewsChannelBean newestChannel = channelMap.get(cacheChannel.getTid());

                                    if (null != newestChannel) {
                                        // 最新的数据中有和缓存中对应的频道，则更新频道信息
                                        cacheChannel.setStyle(newestChannel.getStyle());
                                        cacheChannel.setCnname(newestChannel.getCnname());
                                    } else {
                                        // 最新的数据中没有和缓存中对应的频道，删除该频道信息
                                        cacheChannels.remove(i);
                                    }
                                }
                                addLocalChannels(cacheChannels);

                                // 更新后信息再次保存到SD卡中
                                serializeUtil.writeDataToFile(cacheChannels, channelCachePath);

                            }
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
    }

    public void jump(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    //TODO 提前获取推荐频道第一页
    public void getChooseNewsJson() {
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("tid", "" + NewsChannelBean.TYPE_RECOMMEND);
        params.addBodyParameter("nids", "0");
        params.addBodyParameter("Page", "1");
        params.addBodyParameter("PageSize", "15");

        httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.CHANNEL_RECOMMEND
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                final JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj) {
                    List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);
                    if (list != null) {
                        for (NewsBean bean : list) {
                            bean.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
                        }
                    }
                    if (null != list) {
                        LogUtils.i(" getChooseNewsJson --> " + list.size());
                        new NewsListDbTask(getApplicationContext()).saveList(list, new I_Result() {
                            @Override
                            public void setResult(Boolean flag) {
                                loadMainUI();
                            }
                        });
                    } else {
                        loadMainUI();
                    }
                } else {
                    loadMainUI();
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                loadMainUI();
            }
        });
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


        // 添加推荐频道
        NewsChannelBean channelRecommend = new NewsChannelBean();
        channelRecommend.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setType(NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setCnname(getString(R.string.recommend));
        if (!list.contains(channelRecommend)) {
            list.add(0, channelRecommend);
        }

    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent intent = new Intent(this, InitService.class);
//        stopService(intent);
//        finish();
//    }
}