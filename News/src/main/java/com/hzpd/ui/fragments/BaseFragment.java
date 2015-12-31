package com.hzpd.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.hzpd.ui.App;
import com.hzpd.utils.ACache;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.HttpHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.common.lib.analytics.AnalyticCallback;
import org.common.lib.analytics.FragmentLifecycleAction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BaseFragment extends Fragment implements AnalyticCallback {

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible != isVisibleToUser) {
            isVisible = isVisibleToUser;
            if (TextUtils.isEmpty(getAnalyticPageName())) {
                return;
            }
            try {
                if (isVisibleToUser) {
                    AnalyticUtils.sendGaScreenViewHit(getActivity(), getAnalyticPageName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isVisible = false;
    public int mPosition = -2; //ViewPager Position

    private FragmentLifecycleAction action = new FragmentLifecycleAction(this);

    protected ImageLoader mImageLoader;
    protected HttpUtils httpUtils;
    protected SPUtil spu;//
    protected ACache aCache;
    protected FragmentManager fm;
    protected DBHelper dbHelper;
    protected Activity activity;
    protected String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        action.onCreate(this);
        fm = getChildFragmentManager();
        httpUtils = SPUtil.getHttpUtils();
        spu = SPUtil.getInstance();
        mImageLoader = ImageLoader.getInstance();
    }

    List<HttpHandler> handlerList = new ArrayList<>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        dbHelper = DBHelper.getInstance(getActivity().getApplicationContext());
    }

    public void onResume() {
        super.onResume();
        action.onResume(this);
    }

    public void onPause() {
        super.onPause();
        action.onPause(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public String getLogTag() {
        return getClass().getSimpleName();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public void onStart() {
        super.onStart();
        action.onStart(this);
    }


    @Override
    public void onStop() {
        for (HttpHandler httpHandler : handlerList) {
            if (httpHandler.getState() == HttpHandler.State.LOADING || httpHandler.getState() == HttpHandler.State.STARTED) {
                httpHandler.setRequestCallBack(null);
                httpHandler.cancel();
            }
        }
        handlerList.clear();
        super.onStop();
        action.onStop(this);
    }

    //LOAD DATA
    public void loadData() {

    }

    @Override
    public String getAnalyticPageName() {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getInstance().getRefWatcher().watch(this);
    }
}
