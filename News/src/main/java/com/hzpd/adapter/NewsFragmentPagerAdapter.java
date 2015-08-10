package com.hzpd.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hzpd.modle.NewsChannelBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.ui.fragments.NewsAlbumFragment;
import com.hzpd.ui.fragments.NewsItemFragment;
import com.hzpd.ui.fragments.VideoListFragment;
import com.hzpd.ui.fragments.ZhuantiFragment;
import com.hzpd.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class NewsFragmentPagerAdapter extends FragmentVPAdapter<BaseFragment> {
	private int selectedPosition = 0;

	public NewsFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	public void setSelectedPosition(int mselectedPosition) {
		this.selectedPosition = mselectedPosition;
	}

	public void sortChannel(List<NewsChannelBean> saveTitleList) {
		FragmentTransaction ft = fm.beginTransaction();

		for (Fragment entry : fragments) {
			ft.remove(entry);
		}
		ft.commit();
		ft = null;
		fm.executePendingTransactions();
		List<BaseFragment> list = new ArrayList<>();

		for (int i = 0; i < saveTitleList.size(); i++) {
			NewsChannelBean ncb = saveTitleList.get(i);

			BaseFragment fragment;
			switch (ncb.getType()) {
				case NewsChannelBean.TYPE_NORMAL:
					fragment = new NewsItemFragment(ncb, i);
					if (i == selectedPosition) {
						((NewsItemFragment) fragment).setIsNeedRefresh();
					}
					Log.d(getLogTag(), "NewsItemFragment()->" + ncb.getCnname());
					break;
				case NewsChannelBean.TYPE_IMAGE_ALBUM:
					fragment = new NewsAlbumFragment();
					Log.d(getLogTag(), "NewsAlbumFragment");
					break;
				case NewsChannelBean.TYPE_VIDEO:
					fragment = new VideoListFragment();
					Log.d(getLogTag(), "VideoListFragment");
					break;
				case NewsChannelBean.TYPE_SUBJECT:
					fragment = new ZhuantiFragment();
					Log.d(getLogTag(), "ZhuantiFragment");
					break;
				default:
					fragment = null;
					break;
			}
			if (fragment != null) {
				list.add(fragment);
			}
		}

		setFragments(list);
	}


	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public CharSequence getPageTitle(int position) {

		BaseFragment frag = fragments.get(position);

		String ti = frag.getTitle();
		return ti;
	}

}