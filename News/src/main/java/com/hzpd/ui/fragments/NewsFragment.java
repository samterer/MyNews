package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hzpd.adapter.NewsFragmentPagerAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.event.ChannelSortedList;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.MyEditColumnActivity;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.SerializeUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.viewpagerindicator.TabPageIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class NewsFragment extends BaseFragment {

	@ViewInject(R.id.news_pager)
	private ViewPager pager;
	@ViewInject(R.id.news_indicator)
	private TabPageIndicator indicator;

	private NewsFragmentPagerAdapter adapter;

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
		readTitleData();
	}

	@OnClick(R.id.news_button)
	private void editChannel(View v) {
		Intent in = new Intent();
		in.setClass(getActivity(), MyEditColumnActivity.class);
		startActivity(in);
		AAnim.ActivityStartAnimation(getActivity());
	}

	private void readTitleData() {
		SerializeUtil<List<NewsChannelBean>> mSu = new SerializeUtil<List<NewsChannelBean>>();
		List<NewsChannelBean> mList = mSu.readyDataToFile(App.getInstance().getJsonFileCacheRootDir()
				+ File.separator + App.mTitle);

		if (null == mList) {
			mList = new ArrayList<NewsChannelBean>();
		}
		LogUtils.i("mList-->" + mList.size());

		List<NewsItemFragment> mFragmentsList = new ArrayList<NewsItemFragment>();
		for (int i = 0; i < mList.size(); i++) {
			NewsChannelBean ncb = mList.get(i);
			NewsItemFragment f1 = new NewsItemFragment(ncb, i);
			mFragmentsList.add(f1);
		}

		adapter = new NewsFragmentPagerAdapter(fm);
		pager.setAdapter(adapter);
		adapter.setFragments(mFragmentsList);
		pager.setOffscreenPageLimit(adapter.getCount());
		indicator.setViewPager(pager);

		indicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				NewsItemFragment frag = (NewsItemFragment) adapter.getItem(position);
				frag.init();
				adapter.setSelectedPosition(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

	}

	public void onEventMainThread(ChannelSortedList csl) {
		adapter.sortChannel(csl.getSaveTitleList());
		pager.setOffscreenPageLimit(adapter.getCount());
		indicator.notifyDataSetChanged();
	}


}

