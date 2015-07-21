package com.hzpd.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hzpd.modle.NewsChannelBean;
import com.hzpd.ui.fragments.NewsItemFragment;

import java.util.ArrayList;
import java.util.List;

public class NewsFragmentPagerAdapter extends FragmentVPAdapter<NewsItemFragment> {
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
		List<NewsItemFragment> list = new ArrayList<NewsItemFragment>();

		for (int i = 0; i < saveTitleList.size(); i++) {
			NewsChannelBean ncb = saveTitleList.get(i);
			NewsItemFragment nif = new NewsItemFragment(ncb, i);

			if (i == selectedPosition) {
				nif.setIsNeedRefresh();
			}
			list.add(nif);
		}

		setFragments(list);
	}


	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public CharSequence getPageTitle(int position) {

		NewsItemFragment frag = fragments.get(position);

		String ti = frag.getTitle();
		return ti;
	}

}