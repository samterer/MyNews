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
import com.hzpd.ui.activity.MainActivity;
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

	@OnClick(R.id.main_title_left_img)
	private void showMenu(View view) {
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).showMenu();
		}
	}

	/**
	 * 读取频道信息
	 */
	private void readTitleData() {
		// 频道信息即tab，在开屏的时候获取过了，现在取出来
		SerializeUtil<List<NewsChannelBean>> mSu = new SerializeUtil<List<NewsChannelBean>>();
		List<NewsChannelBean> mList = mSu.readyDataToFile(App.getInstance().getJsonFileCacheRootDir()
				+ File.separator + App.mTitle);

		if (null == mList) {
			mList = new ArrayList<NewsChannelBean>();
		}
		LogUtils.i("mList-->" + mList.size());

		List<BaseFragment> mFragmentsList = new ArrayList<>();
		for (int i = 0; i < mList.size(); i++) {
			NewsChannelBean ncb = mList.get(i);
			BaseFragment fragment;
			switch (ncb.getType()) {
				case NewsChannelBean.TYPE_NORMAL:
					fragment = new NewsItemFragment(ncb, i);
					break;
				case NewsChannelBean.TYPE_IMAGE_ALBUM:
					fragment = new NewsAlbumFragment();
					break;
				case NewsChannelBean.TYPE_VIDEO:
					fragment = new VideoListFragment();
					break;
				case NewsChannelBean.TYPE_SUBJECT:
					fragment = new ZhuantiFragment();
					break;
				default:
					fragment = null;
					break;
			}
			if (fragment != null) {
				mFragmentsList.add(fragment);
			}
		}

		adapter = new NewsFragmentPagerAdapter(fm);
		pager.setAdapter(adapter);
		adapter.setFragments(mFragmentsList);
		pager.setOffscreenPageLimit(adapter.getCount());
		indicator.setViewPager(pager);

		indicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				BaseFragment fragment = (BaseFragment) adapter.getItem(position);
				if (fragment instanceof NewsItemFragment) {
					NewsItemFragment frag = (NewsItemFragment) fragment;
					frag.init();
				}
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

