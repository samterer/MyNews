package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hzpd.adapter.NewsFragmentPagerAdapter1;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.event.ChannelSortedList;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.MyEditColumnActivity;
import com.hzpd.ui.widget.PagerSlidingTabStrip;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.SerializeUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class NewsFragment extends BaseFragment {

    @ViewInject(R.id.news_pager)
    private ViewPager pager;

    @ViewInject(R.id.news_button)
    private Button news_button;
    @ViewInject(R.id.psts_tabs_app)
    private PagerSlidingTabStrip tabStrip;

    private NewsFragmentPagerAdapter1 adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment_main, container, false);
        ViewUtils.inject(this, view);
        return view;
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

    @OnClick(R.id.news_button)
    private void editChannel(View v) {
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
            return;
        }
        Intent in = new Intent();
        in.setClass(getActivity(), MyEditColumnActivity.class);
        startActivity(in);
        AAnim.ActivityStartAnimation(getActivity());
//		news_button.setEnabled(false);
    }


    /**
     * 读取频道信息
     */
    private void readTitleData() {
        // 频道信息即tab，在开屏的时候获取过了，现在取出来
        SerializeUtil<List<NewsChannelBean>> mSu = new SerializeUtil<List<NewsChannelBean>>();
        List<NewsChannelBean> mList = mSu.readyDataToFile(App.getInstance().getAllDiskCacheDir()
                + File.separator + App.mTitle);

        if (null == mList) {
            mList = new ArrayList<NewsChannelBean>();
        }
        LogUtils.i("mList-->" + mList.size());

        adapter = new NewsFragmentPagerAdapter1(fm);
        pager.setAdapter(adapter);
        adapter.sortChannel(mList);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pager.setCurrentItem(position);
                adapter.setSelectedPosition(position);
                BaseFragment fragment = (BaseFragment) adapter.getItem(position);
                if (fragment instanceof NewsItemFragment1) {
                    NewsItemFragment1 frag = (NewsItemFragment1) fragment;
                    frag.init();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setOffscreenPageLimit(adapter.getCount());
        tabStrip.setViewPager(pager);
        tabStrip.setOnTabClickListener(new PagerSlidingTabStrip.TabClickListener() {

                                           public void onTabClicked(int position) {
                                               pager.setCurrentItem(position);
                                               adapter.setSelectedPosition(position);
                                           }
                                       }

        );
    }

    public void onEventMainThread(ChannelSortedList csl) {
        adapter.sortChannel(csl.getSaveTitleList());
        pager.setOffscreenPageLimit(adapter.getCount());
        tabStrip.notifyDataSetChanged();
    }

}

