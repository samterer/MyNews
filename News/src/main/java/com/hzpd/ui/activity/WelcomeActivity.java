package com.hzpd.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.NewsChannelBeanDB;
import com.hzpd.services.InitService;
import com.hzpd.ui.App;
import com.hzpd.ui.ConfigBean;
import com.hzpd.ui.fragments.welcome.AdFlashFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.db.NewsListDbTask;

import com.news.update.UpdateService;
import com.news.update.Utils;
import com.squareup.okhttp.Request;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author color
 */
public class WelcomeActivity extends MWBaseActivity {

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.welcome;
    }

    private volatile int done;
    private volatile boolean exists;
    private Fragment fragment;
    private Object tag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        Log.e("test", "  WelcomeActivity " + getResources().getBoolean(R.bool.isRom));
        exists = false;
        setContentView(R.layout.frame_welcome);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tran = fm.beginTransaction();
        fragment = new AdFlashFragment();
        tran.replace(R.id.welcome_frame, fragment);
        tran.commit();
        // 初始化服务
        Intent service = new Intent(this, InitService.class);
        service.setAction(InitService.InitAction);
        this.startService(service);
        // 更新服务
        service = new Intent(this, UpdateService.class);
        this.startService(service);
        if (ConfigBean.getInstance().open_channel.equals(SPUtil.getCountry())) {
            getChooseNewsJson();
            getChannelJson();
        } else {
            done += 2;
        }
        tag = OkHttpClientManager.getTag();
    }

    @Override
    protected void onDestroy() {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment);
            fragment = null;
        }
        super.onDestroy();
        OkHttpClientManager.cancel(tag);
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
        List<NewsChannelBeanDB> channelBeanDBs = dbHelper.getChannel().loadAll();
        if (channelBeanDBs != null && channelBeanDBs.size() > 0) {
            loadMainUI();
        }

        if (!Utils.isNetworkConnected(this)) {
            loadMainUI();
            return;
        }
        String urlChannelList = InterfaceJsonfile.CHANNELLIST + "News";
        String country = SPUtil.getCountry();
        urlChannelList = urlChannelList.replace("#country#", country.toLowerCase());
        Log.i("channelCache", "channelCache  000");
        OkHttpClientManager.getAsyn(tag, urlChannelList,
                new OkHttpClientManager.ResultCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            String json = response.toString();
                            if (json != null) {
                                JSONObject obj = null;
                                Log.i("channelCache", "channelCache" + json);
                                try {
                                    obj = FjsonUtil.parseObject(json);
                                } catch (Exception e) {
                                    obj = FjsonUtil.parseObject("{\"code\":209}");
                                }
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
                                dbs = dbHelper.getChannel().loadAll();
                                // 如果没有缓存
                                if (null == dbs || dbs.size() < 1) {
                                    addLocalChannels(newestChannels);
                                    dbs = new ArrayList<>();
                                    for (NewsChannelBean bean : newestChannels) {
                                        dbs.add(new NewsChannelBeanDB(bean));
                                    }
                                    for (int i = 0; i < dbs.size(); i++) {
                                        dbs.get(i).setId((long) i);
                                    }
                                    dbHelper.getChannel().insertInTx(dbs);
                                    SPUtil.updateChannel();
                                } else if (newestChannels.size() > 0) { // 如果有缓存
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
                                        dbHelper.getChannel().deleteAll();
                                        for (int i = 0; i < dbs.size(); i++) {
                                            dbs.get(i).setId((long) i);
                                        }
                                        dbHelper.getChannel().insertInTx(dbs);
                                        SPUtil.updateChannel();
                                    }
                                }
                            } else {
                                Log.i("channelCache", "channelCache  null");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!exists) {
                            loadMainUI();
                        }
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        Log.i("channelCache", "channelCache  onFailure");
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
        if (!Utils.isNetworkConnected(this)) {
            loadMainUI();
            return;
        }
        Map<String, String> params = RequestParamsUtils.getMaps();
        params.put("siteid", InterfaceJsonfile.SITEID);
        params.put("tid", "" + NewsChannelBean.TYPE_RECOMMEND);
        params.put("Page", "1");
        params.put("PageSize", "15");
        UserBean user = SPUtil.getInstance().getUser();
        if (user != null && !TextUtils.isEmpty(user.getUid())) {
            params.put("uid", "" + user.getUid());
            params.put("tagIndex", "1");
            params.put("pageIndex", "1");
        }
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.CHANNEL_RECOMMEND_NEW
                , new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                final JSONObject obj = FjsonUtil
                        .parseObject(response.toString());
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
                        new NewsListDbTask(getApplicationContext()).saveList(list, null);
                    }
                }
                loadMainUI();
            }

            @Override
            public void onFailure(Request request, Exception e) {
                loadMainUI();
            }
        }, params);
    }

    //	直接添加本地频道
    private void addLocalChannels(List<NewsChannelBean> list) {
        NewsChannelBean channelRecommend = new NewsChannelBean();
        channelRecommend.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setType(NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setCnname(getString(R.string.recommend));
        channelRecommend.setDefault_show("1");
        channelRecommend.setSort_order("0");
        // 添加推荐频道
        if (!list.contains(channelRecommend)) {
            list.add(0, channelRecommend);
        }
    }

}