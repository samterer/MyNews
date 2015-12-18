package com.hzpd.ui.activity;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.CollectionAdapter;
import com.hzpd.custorm.RefreshLayout;
import com.hzpd.hflt.R;
import com.hzpd.modle.CollectionJsonBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SystemBarTintManager;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefreshMainActivity extends MBaseActivity {

    private final static String TAG = "RefreshMainActivity";
    private RefreshLayout mRefreshLayout;
    private ListView mListView;
    private View footerLayout;
    private TextView textMore;
    private ProgressBar progressBar;
    private SimpleAdapter mAdapter;
    private ArrayList<Map<String, Object>> mData = new ArrayList<>();

    private CollectionAdapter colladAdapter;
    private int Page = 1;//页数
    private static final int PageSize = 15; //每页大小

    private boolean mFlagRefresh = true;//刷新还是加载

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_main);
        changeStatus();
        mRefreshLayout = (RefreshLayout) findViewById(R.id.swipe_container);
        mListView = (ListView) findViewById(R.id.list);

        footerLayout = getLayoutInflater().inflate(R.layout.listview_footer, null);
        textMore = (TextView) footerLayout.findViewById(R.id.text_more);
        progressBar = (ProgressBar) footerLayout.findViewById(R.id.load_progress_bar);
        textMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                simulateLoadingData();
            }
        });

        //这里可以替换为自定义的footer布局
        //you can custom FooterView
        mListView.addFooterView(footerLayout);
        mRefreshLayout.setChildView(mListView);

        for (int i = 0; i < 10; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("img", R.drawable.ic_launcher);
            listItem.put("text", "Item " + i);
            mData.add(listItem);
        }
        mAdapter = new SimpleAdapter(this, mData, R.layout.list_item, new String[]{"img", "text"}, new int[]{R.id.img, R.id.text});
        mListView.setAdapter(mAdapter);

        mRefreshLayout.setColorSchemeResources(R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow);


        //使用SwipeRefreshLayout的下拉刷新监听
        //use SwipeRefreshLayout OnRefreshListener
        mRefreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                simulateFetchingData();
            }
        });


        //使用自定义的RefreshLayout加载更多监听
        //use customed RefreshLayout OnLoadListener
        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                simulateLoadingData();
            }
        });

    }
    private void changeStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.title_bar_color, typedValue, true);
        int color = typedValue.data;
        tintManager.setStatusBarTintColor(color);
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
    /**
     * 模拟一个耗时操作，获取完数据后刷新ListView
     * simulate update ListView and stop refresh after a time-consuming task
     */
    private void simulateFetchingData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> listItem = new HashMap<>();
                listItem.put("img", R.drawable.ic_launcher);
                listItem.put("text", "New Top Item " + mData.size());
                mData.add(0, listItem);
                mRefreshLayout.setRefreshing(false);
                mAdapter.notifyDataSetChanged();
                textMore.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RefreshMainActivity.this, "Refresh Finished!", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    /**
     * 模拟一个耗时操作，加载完更多底部数据后刷新ListView
     * simulate update ListView and stop load more after after a time-consuming task
     */
    private void simulateLoadingData() {
        textMore.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int size = mData.size();
                for (int i = 0; i < 3; i++) {
                    Map<String, Object> listItem = new HashMap<>();
                    listItem.put("img", R.drawable.ic_launcher);
                    listItem.put("text", "New Bottom Item " + (size + i));
                    mData.add(listItem);
                }
                mRefreshLayout.setLoading(false);
                mAdapter.notifyDataSetChanged();
                textMore.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RefreshMainActivity.this, "Load Finished!", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }



    //获取数据
    private void getCollectionInfoFromServer() {
        if (null != spu.getUser()) { //未登录
            LogUtils.i("uid-->" + spu.getUser().getUid());
            RequestParams params = RequestParamsUtils.getParamsWithU();
            params.addBodyParameter("page", Page + "");
            params.addBodyParameter("pagesize", PageSize + "");
            params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
            SPUtil.addParams(params);
            httpUtils.send(HttpRequest.HttpMethod.POST
                    , InterfaceJsonfile.COLLECTIONLIST//InterfaceApi.collection
                    , params
                    , new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    try {
                        JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
                        if (null == obj) {
                            return;
                        }
                        if (200 == obj.getIntValue("code")) {
                            List<CollectionJsonBean> mlist = FjsonUtil.parseArray(obj.getString("data")
                                    , CollectionJsonBean.class);
                            LogUtils.e("" + mlist.toString());
                            if (null == mlist) {
                                return;
                            }

                            LogUtils.i("listsize-->" + mlist.size());

                            colladAdapter.appendData(mlist, mFlagRefresh);
                            colladAdapter.notifyDataSetChanged();

                            if (mlist.size() >= PageSize) {
                            }

                        } else {
                            if (!mFlagRefresh) {
                                Page--;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mFlagRefresh = false;
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    if (!mFlagRefresh) {
                        Page--;
                    }
                    mFlagRefresh = false;
                    TUtils.toast(getString(R.string.toast_cannot_connect_to_server));
                }
            });
        } else {
        }
    }
}
