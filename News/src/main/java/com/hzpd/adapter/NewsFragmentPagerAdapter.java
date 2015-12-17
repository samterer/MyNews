package com.hzpd.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.hzpd.modle.NewsChannelBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.ui.fragments.ChooseFragment;
import com.hzpd.ui.fragments.NewsAlbumFragment;
import com.hzpd.ui.fragments.NewsItemFragment;
import com.hzpd.ui.fragments.VideoListFragment;
import com.hzpd.ui.fragments.ZhuantiFragment;

import java.util.ArrayList;
import java.util.List;

public class NewsFragmentPagerAdapter extends FragmentPagerAdapter {
    List<NewsChannelBean> saveTitleList = new ArrayList<>();
    int selectedPosition = 0;
    BaseFragment selectedFragment;

    public NewsFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    public void sortChannel(List<NewsChannelBean> saveTitleList) {
        if (saveTitleList != null) {
            this.saveTitleList.clear();
            this.saveTitleList.addAll(saveTitleList);
        }
        notifyDataSetChanged();
    }

    @Override
    public BaseFragment getItem(int position) {
        return getChannelFragment(saveTitleList.get(position));
    }

    @Override
    public long getItemId(int position) {
        NewsChannelBean bean = saveTitleList.get(position);
        return ("tid:" + bean.getTid() + "#" + "tagid:" + bean.getId()).hashCode();
    }

    @Override
    public int getCount() {
        return saveTitleList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        NewsChannelBean bean = saveTitleList.get(position);
        String ti = bean.getCnname();
        return ti;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    boolean changed = false;

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        changed = true;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (changed) {
            changed = false;
            ((BaseFragment) object).loadData();
        }
    }

    BaseFragment getChannelFragment(NewsChannelBean ncb) {
        BaseFragment fragment = null;
        switch (ncb.getType()) {
            case NewsChannelBean.TYPE_RECOMMEND:
                fragment = new ChooseFragment(ncb);
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
                fragment = new NewsItemFragment(ncb);
                break;
        }
        return fragment;
    }
}