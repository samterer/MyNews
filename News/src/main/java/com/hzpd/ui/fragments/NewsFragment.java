package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.NewsFragmentPagerAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.TagBean;
import com.hzpd.modle.db.NewsChannelBeanDB;
import com.hzpd.modle.event.ChangeChannelEvent;
import com.hzpd.modle.event.TagEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.MyEditColumnActivity;
import com.hzpd.ui.activity.SearchActivity;
import com.hzpd.ui.widget.PagerSlidingTabStrip;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class NewsFragment extends BaseFragment {

    @Override
    public String getAnalyticPageName() {
        return null;
    }

    @ViewInject(R.id.news_pager)
    private ViewPager pager;
    @ViewInject(R.id.ll_news_button)
    private View ll_news_button;
    @ViewInject(R.id.psts_tabs_app)
    private PagerSlidingTabStrip tabStrip;
    @ViewInject(R.id.ll_main)
    private LinearLayout ll_main;
    @ViewInject(R.id.background_empty)
    private ImageView background_empty;
    @ViewInject(R.id.main_no_news)
    private View main_no_news;
    @ViewInject(R.id.app_progress_bar)
    private View app_progress_bar;
    @ViewInject(R.id.transparent_layout_id)
    private View transparent_layout_id;

    private NewsFragmentPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

    }

    private View main_top_search;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment_main, container, false);
        ViewUtils.inject(this, view);
        main_top_search = view.findViewById(R.id.main_top_search);
        main_top_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AvoidOnClickFastUtils.isFastDoubleClick())
                    return;
                Intent mIntent = new Intent();
                mIntent.setClass(getActivity(), SearchActivity.class);
                startActivity(mIntent);
                AAnim.ActivityStartAnimation(getActivity());
            }
        });
//        if (App.getInstance().getThemeName().equals("3")) {
//            transparent_layout_id.setVisibility(View.VISIBLE);
//        } else {
//            transparent_layout_id.setVisibility(View.GONE);
//        }
        return view;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            readTitleData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @OnClick(R.id.ll_news_button)
    private void editChannel1(View v) {
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
            return;
        }
        Intent in = new Intent();
        in.setClass(getActivity(), MyEditColumnActivity.class);
        startActivity(in);
        AAnim.ActivityStartAnimation(getActivity());
    }

    private List<NewsChannelBean> mList;

    /**
     * 读取频道信息
     */
    private void readTitleData() {
        // 频道信息即tab，在开屏的时候获取过了，现在取出来
        try {
            List<NewsChannelBeanDB> dbs = dbHelper.getChannelDbUtils().findAll(Selector.from(NewsChannelBeanDB.class).where("default_show", "=", "1"));
            mList = new ArrayList<>();
            for (NewsChannelBeanDB beanDB : dbs) {
                mList.add(new NewsChannelBean(beanDB));
            }
            if (null == mList) {
                mList = new ArrayList<NewsChannelBean>();
                ll_main.setVisibility(View.GONE);
                main_no_news.setVisibility(View.VISIBLE);
                app_progress_bar.setVisibility(View.GONE);
                main_no_news.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        app_progress_bar.setVisibility(View.VISIBLE);
                        main_no_news.setVisibility(View.GONE);
                        getChannelJson();
                    }
                });
            } else {
                ll_main.setVisibility(View.VISIBLE);
                main_no_news.setVisibility(View.GONE);
                app_progress_bar.setVisibility(View.GONE);
            }
            adapter = new NewsFragmentPagerAdapter(fm);
            pager.setAdapter(adapter);
            adapter.sortChannel(mList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                adapter.setSelectedPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setOffscreenPageLimit(PAGE_LIMIT);
        tabStrip.setViewPager(pager);
        tabStrip.setOnTabClickListener(new PagerSlidingTabStrip.TabClickListener() {

                                           public void onTabClicked(int position) {
                                               pager.setCurrentItem(position);
                                           }
                                       }

        );
    }

    public void getChannelJson() {
        final String channelCachePath = App.getInstance().getAllDiskCacheDir()
                + File.separator
                + App.mTitle;
        final File channelCacheFile = new File(channelCachePath);
        final File target = App.getFile(App.getInstance().getAllDiskCacheDir() + File.separator + "News");
        String urlChannelList = InterfaceJsonfile.CHANNELLIST + "News";
        String country = SPUtil.getCountry();
        urlChannelList = urlChannelList.replace("#country#", country.toLowerCase());
//			下载信息并保存
        httpUtils.download(urlChannelList,
                target.getAbsolutePath(),
                new RequestCallBack<File>() {
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        String json = App.getFileContext(responseInfo.result);
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
                            }
                            try {
                                List<NewsChannelBeanDB> dbs = null;
                                dbs = dbHelper.getChannelDbUtils().findAll(NewsChannelBeanDB.class);
                                // 如果没有缓存
                                if (null == dbs || dbs.size() < 1) {
                                    addLocalChannels(newestChannels);
                                    dbs = new ArrayList<>();
                                    for (NewsChannelBean bean : newestChannels) {
                                        dbs.add(new NewsChannelBeanDB(bean));
                                    }
                                    dbHelper.getChannelDbUtils().saveAll(dbs);
                                } else { // 如果有缓存
                                    //TODO
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            app_progress_bar.setVisibility(View.GONE);
                            readTitleData();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        app_progress_bar.setVisibility(View.GONE);
                        main_no_news.setVisibility(View.VISIBLE);
                    }
                });
    }

    //	直接添加本地频道
    private void addLocalChannels(List<NewsChannelBean> list) {

        // 添加推荐频道
        NewsChannelBean channelRecommend = new NewsChannelBean();
        channelRecommend.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setType(NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setCnname(getString(R.string.recommend));
        if (!list.contains(channelRecommend)) {
            list.add(0, channelRecommend);
        }

    }

    public void onEventMainThread(ChangeChannelEvent event) {
        SPUtil.updateChannel();
        if (event.csl != null) {
            SPUtil.updateChannel();
            pager.setOffscreenPageLimit(PAGE_LIMIT);
            mList = event.csl.getSaveTitleList();
            adapter.sortChannel(mList);
            tabStrip.notifyDataSetChanged();
        }
        if (event.position != -1) {
            pager.setCurrentItem(event.position);
            adapter.setSelectedPosition(event.position);
        }
    }

    public void onEventMainThread(TagEvent event) {
        try {
            TagBean tagBean = event.bean;
            if (SPUtil.checkTag(tagBean)) {
                return;
            }
            pager.setOffscreenPageLimit(PAGE_LIMIT);
            NewsChannelBeanDB beanDB = SPUtil.getTag(tagBean);
            if (beanDB == null) {
                beanDB = new NewsChannelBeanDB(tagBean);
                beanDB.setDefault_show("1");
                dbHelper.getChannelDbUtils().save(beanDB);
                SPUtil.updateChannel();
            } else {
                beanDB.setDefault_show("1");
                dbHelper.getChannelDbUtils().update(beanDB);
            }
            if (mList.size() > 2) {
                mList.add(2, new NewsChannelBean(beanDB));
            } else {
                mList.add(1, new NewsChannelBean(beanDB));
            }
            adapter.sortChannel(mList);
            pager.setCurrentItem(2);
            tabStrip.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    final static int PAGE_LIMIT = 2;
}

