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
import com.hzpd.modle.event.ChangeChannelEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.MyEditColumnActivity;
import com.hzpd.ui.activity.SearchActivity;
import com.hzpd.ui.widget.PagerSlidingTabStrip;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.SerializeUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
        main_top_search=view.findViewById(R.id.main_top_search);
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
        SerializeUtil<List<NewsChannelBean>> mSu = new SerializeUtil<List<NewsChannelBean>>();
        mList = mSu.readyDataToFile(App.getInstance().getAllDiskCacheDir()
                + File.separator + App.mTitle);

        if (null == mList) {
            mList = new ArrayList<NewsChannelBean>();
            ll_main.setVisibility(View.GONE);
            main_no_news.setVisibility(View.VISIBLE);
            app_progress_bar.setVisibility(View.GONE);
            main_no_news.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(getActivity(), "频道刷新", Toast.LENGTH_SHORT).show();
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
        LogUtils.i("mList-->" + mList.size());
        adapter = new NewsFragmentPagerAdapter(fm);
        pager.setAdapter(adapter);
        adapter.sortChannel(mList);
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
                            // 读取频道信息的本地缓存
                            SerializeUtil<List<NewsChannelBean>> serializeUtil = new SerializeUtil<List<NewsChannelBean>>();
                            List<NewsChannelBean> cacheChannels = serializeUtil
                                    .readyDataToFile(channelCacheFile.getAbsolutePath());
                            // 如果没有缓存
                            if (null == cacheChannels || cacheChannels.size() < 1) {
                                if (newestChannels != null && newestChannels.size() > 0) {
                                    for (int i = 0; i < newestChannels.size(); i++) {
                                        String default_show = newestChannels.get(i).getDefault_show();
                                        if (default_show.equals("0")) {
                                            newestChannels.remove(i);
                                        }
                                    }
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
        boolean changed = false;
        if (event.csl != null) {
            changed = true;
            pager.setOffscreenPageLimit(PAGE_LIMIT);
            adapter.sortChannel(event.csl.getSaveTitleList());
            tabStrip.notifyDataSetChanged();
        }
        if (event.position != -1) {
            pager.setCurrentItem(event.position);
        }
    }

    final static int PAGE_LIMIT = 2;
}

