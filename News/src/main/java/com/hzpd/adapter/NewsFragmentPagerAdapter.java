package com.hzpd.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hzpd.modle.NewsChannelBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.ui.fragments.ChooseFragment;
import com.hzpd.ui.fragments.NewsAlbumFragment;
import com.hzpd.ui.fragments.NewsItemFragment;
import com.hzpd.ui.fragments.VideoListFragment;
import com.hzpd.ui.fragments.ZhuantiFragment;
import com.hzpd.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewsFragmentPagerAdapter extends FragmentVPAdapter<BaseFragment> {
    private int selectedPosition = 0;
    private HashMap<Integer, BaseFragment> fragmentMap = new HashMap<>(30);

    public NewsFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setSelectedPosition(int mselectedPosition) {
        this.selectedPosition = mselectedPosition;
    }

    public void sortChannel(List<NewsChannelBean> saveTitleList) {
        List<BaseFragment> list = new ArrayList<>();
        long start = System.currentTimeMillis();
        Log.e("test", start);
        for (int i = 0; i < saveTitleList.size(); i++) {
            NewsChannelBean ncb = saveTitleList.get(i);
            BaseFragment fragment = getChannelFragment(ncb);
            fragment.mPosition = i;
            if (fragment != null) {
                list.add(fragment);
            }
        }
        Log.e("test", "time" + (System.currentTimeMillis() - start));
        setFragments(list);
    }

    @Override
    public void setFragments(List<BaseFragment> fragments) {
        // 覆盖父方法，缓存fragment
        if (fm.isDestroyed()) {
            return;
        }
        try {
            super.setFragments(fragments);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (true) {
            //TODO
            return;
        }
        if (this.fragments != null) {
            FragmentTransaction ft = fm.beginTransaction();
            for (Fragment f : this.fragments) {
                if (!fragments.contains(f)) {
                    ft.remove(f);
                }
            }
            ft.commit();
            ft = null;
            fm.executePendingTransactions();
        }
        this.fragments = fragments;
        notifyDataSetChanged();
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

    BaseFragment getChannelFragment(NewsChannelBean ncb) {
        BaseFragment fragment = null;
        if (fragmentMap.containsKey(ncb.getTid())) {
            return fragmentMap.get(Integer.valueOf(ncb.getTid()));
        }
        switch (ncb.getType()) {
            case NewsChannelBean.TYPE_RECOMMEND:
                fragment = new ChooseFragment(ncb);
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
                fragment = new NewsItemFragment(ncb);
                Log.d(getLogTag(), "NewsItemFragment()->" + ncb.getCnname());
                break;
        }
        fragmentMap.put(Integer.valueOf(ncb.getTid()), fragment);
        return fragment;
    }
}