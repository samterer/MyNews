package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.ZhuantiDetailListAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.SubjectItemColumnsBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.hzpd.utils.db.ZhuantiDetailListDbTask;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ZhuanTiActivity extends MBaseActivity implements OnClickListener {

    private ZhuantiDetailListAdapter adapter;

    private String from;//newsitem subject

    private boolean mFlagRefresh;
    private int page = 1;
    private static final int pageSize = 1500;//

    private NewsBean nb = null;//专题id

    private ZhuantiDetailListDbTask newsListDbTask; //zhuanti列表数据库

    private List<SubjectItemColumnsBean> columnList;

    SwipeRefreshLayout mSwipeRefreshWidget;

    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.subject_detail_layout);
            init();
            page = 1;
            mFlagRefresh = true;
            adapter.clearData();
            getColumns();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.changeStatusBar();
    }

    private void init() {
        newsListDbTask = new ZhuantiDetailListDbTask(activity);

        mSwipeRefreshWidget = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        mRecyclerView = (RecyclerView) findViewById(R.id.recylerlist);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //解决RecyclerView和SwipeRefreshLayout共用存在的bug
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshWidget.setEnabled(topRowVerticalPosition >= 0);
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new ZhuantiDetailListAdapter(activity, null);
        mRecyclerView.setAdapter(adapter);


        Intent intent = getIntent();
        from = intent.getStringExtra("from");
        if ("newsitem".equals(from)) {
            nb = (NewsBean) intent.getSerializableExtra("newbean");
        } else if ("subject".equals(from)) {
            nb = (NewsBean) intent.getSerializableExtra("nb");

        }
        if (null == nb) {
            return;
        }

        String imgs[] = nb.getImgs();
        String img = "";
        if (null != imgs && imgs.length > 0) {
            img = imgs[0];
        }

        page = 1;
        mFlagRefresh = true;
        adapter.clearData();
        getColumns();
    }

    @OnClick(R.id.stitle_ll_back)
    private void goback(View v) {
        finish();
    }

    //专题栏目列表
    public void getColumns() {
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("sid", nb.getNid());
        params.addBodyParameter("page", "" + page);
        params.addBodyParameter("pagesize", "" + pageSize);

        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.SUBJECTCOLUMNSLIST
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String json = responseInfo.result;

                mSwipeRefreshWidget.setRefreshing(false);
                JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj) {

                    if (200 == obj.getIntValue("code")) {
                        JSONArray array = obj.getJSONArray("data");

                        columnList = FjsonUtil.parseArray(array.toJSONString(), SubjectItemColumnsBean.class);

                        if (null != columnList && columnList.size() > 0) {

                            spu.setSubjectColumnList(array);
                        } else {
                            JSONArray oldarray = spu.getSubjectColumnList();
                            columnList = FjsonUtil.parseArray(oldarray.toJSONString(), SubjectItemColumnsBean.class);
                        }

                        if (null != columnList && columnList.size() > 0) {
                            for (SubjectItemColumnsBean sicb : columnList) {

                                getDbList(sicb);
                            }
                        }

                    } else {
                        TUtils.toast(obj.getString("msg"));
                    }
                } else {
                    TUtils.toast(getString(R.string.toast_cannot_connect_network));
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                mSwipeRefreshWidget.setRefreshing(false);
            }
        });
    }

    //专题子分类列表
    public void getDbList(final SubjectItemColumnsBean columnid) {

        newsListDbTask.findList(columnid.getCid(), page, pageSize, new I_SetList<NewsBeanDB>() {
            @Override
            public void setList(List<NewsBeanDB> list) {

                String nids = "";
                if (null != list) {
                    StringBuilder sb = new StringBuilder();
                    List<NewsBean> nbList = new ArrayList<NewsBean>();
                    for (NewsBeanDB nbdb : list) {
                        sb.append(nbdb.getNid() + ",");
                        nbList.add(nbdb.getNewsBean());
                    }

                    adapter.appendData(columnid, nbList, mFlagRefresh);
                    adapter.notifyDataSetChanged();
                    if (sb.length() > 1) {
                        nids = sb.substring(0, sb.length() - 1);
                    }
                }
                getServerList(columnid);
            }
        });
    }

    //获取新闻list
    public void getServerList(String nids) {

    }


    //获取专题子分类list
    public void getServerList(final SubjectItemColumnsBean columnid) {

        String station = SharePreferecesUtils.getParam(ZhuanTiActivity.this, StationConfig.STATION, "def").toString();
        String siteid = null;
        String NEWSLIST_url = null;
        if (station.equals(StationConfig.DEF)) {
            siteid = InterfaceJsonfile.SITEID;
            NEWSLIST_url = InterfaceJsonfile.NEWSLIST;
        } else if (station.equals(StationConfig.YN)) {
            siteid = InterfaceJsonfile_YN.SITEID;
            NEWSLIST_url = InterfaceJsonfile_YN.NEWSLIST;
        } else if (station.equals(StationConfig.TW)) {
            siteid = InterfaceJsonfile_TW.SITEID;
            NEWSLIST_url = InterfaceJsonfile_TW.NEWSLIST;
        }
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", siteid);
        params.addBodyParameter("columnid", columnid.getCid());
        params.addBodyParameter("Page", "" + page);
        params.addBodyParameter("PageSize", "" + pageSize);
        params.addBodyParameter("update_time", spu.getCacheUpdatetime());

        httpUtils.send(HttpMethod.POST
                , NEWSLIST_url
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!isResume) {
                    return;
                }

                mSwipeRefreshWidget.setRefreshing(false);

                final JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj) {
                    //缓存更新

                    JSONObject cache = obj.getJSONObject("cachetime");
                    setData(columnid, obj);
                } else {
                    TUtils.toast(getString(R.string.toast_cannot_connect_network));
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                if (!isResume) {
                    return;
                }
                mSwipeRefreshWidget.setRefreshing(false);
            }
        });
    }


    public void setData(final SubjectItemColumnsBean columnid, JSONObject obj) {
        if (!isResume) {
            return;
        }
        //数据处理
        switch (obj.getIntValue("code")) {
            case 200: {
                List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);

                newsListDbTask.saveList(list, new I_Result() {
                    @Override
                    public void setResult(Boolean flag) {
                        if (!flag) {
                            return;
                        }
                        newsListDbTask.findList(columnid.getCid(), page, pageSize
                                , new I_SetList<NewsBeanDB>() {
                            @Override
                            public void setList(List<NewsBeanDB> list) {

                                List<NewsBean> nbList = new ArrayList<NewsBean>();
                                if (null != list) {

                                    for (NewsBeanDB nbdb : list) {
                                        nbList.add(nbdb.getNewsBean());
                                    }

                                    adapter.appendData(columnid, nbList, mFlagRefresh);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                });
            }
            break;
            default: {
                TUtils.toast(getString(R.string.toast_cannot_connect_network));
            }
            break;
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }
}