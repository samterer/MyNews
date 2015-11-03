package com.hzpd.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.CollectionAdapter;
import com.hzpd.adapter.PushmsgAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.CollectionDataBean;
import com.hzpd.modle.CollectionJsonBean;
import com.hzpd.modle.Jsonbean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.PushmsgBean;
import com.hzpd.modle.VideoItemBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;
import java.util.List;

/**
 * @author color
 *         推送和收藏页面
 */

public class MyPMColAvtivity extends MBaseActivity {
    @Override
    public String getAnalyticPageName() {
        if ("pushmsg".equals(type)) {
            return "我的推送页";
        } else {
            return "我的收藏页";
        }
    }

    @ViewInject(R.id.stitle_tv_content)
    private TextView stitle_tv_content;

    @ViewInject(R.id.pushmsg_lv)
    private PullToRefreshListView pushmsg_lv;
    @ViewInject(R.id.pushmsg_tv_empty)
    private View pushmsg_tv_empty;

    private int Page = 1;//页数
    private static final int PageSize = 15; //每页大小

    private boolean mFlagRefresh = true;//刷新还是加载

    private PushmsgAdapter pmgadapter;
    private CollectionAdapter colladAdapter;

    private String type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (null == intent) {
            return;
        }
        try {
            type = intent.getStringExtra("type");
            super.onCreate(savedInstanceState);
            setContentView(R.layout.mypushmsg_layout);
            ViewUtils.inject(this);
            findViewById(R.id.mycomments_title).setVisibility(View.GONE);
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        Intent intent = getIntent();
        if (null == intent) {
            return;
        }
        type = intent.getStringExtra("type");
        LogUtils.i("type-->" + type);

        pushmsg_lv.setEmptyView(pushmsg_tv_empty);
        pushmsg_lv.setMode(Mode.PULL_FROM_START);

        if ("pushmsg".equals(type)) {
            stitle_tv_content.setText(R.string.prompt_my_msg);

            pmgadapter = new PushmsgAdapter(this);
            pushmsg_lv.setAdapter(pmgadapter);
        } else {

            stitle_tv_content.setText(R.string.prompt_collect);

            colladAdapter = new CollectionAdapter(this);
            pushmsg_lv.setAdapter(colladAdapter);
            pushmsg_lv.getRefreshableView().setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent,
                                               View view, int position, long id) {
                    LogUtils.i("position-->" + position + " id-->" + id);
                    CollectionJsonBean cb = (CollectionJsonBean) colladAdapter.getItem(position - 1);
                    deletePop(view, cb, position - 1);
                    return true;
                }
            });
        }

        pushmsg_lv.setOnRefreshListener(new OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                LogUtils.i("下拉刷新");
                //下拉刷新

                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_pull_label));
                Page = 1;
                mFlagRefresh = true;
                if ("pushmsg".equals(type)) {
                    getPushmsgInfoFromServer();
                } else {
                    colladAdapter.clear();
                    getDbCache();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载
                LogUtils.i("上拉加载");

                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.pull_to_refresh_from_bottom_pull_label));
                Page++;
                mFlagRefresh = false;
                if ("pushmsg".equals(type)) {
                    getPushmsgInfoFromServer();
                } else {
                    getDbCache();
                }
            }
        });

        pushmsg_lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if ("pushmsg".equals(type)) {
                    pushmsgItemclick(parent, view, position, id);
                } else {
                    mycollectionItemclick(parent, view, position, id);
                }
            }
        });

        pushmsg_lv.postDelayed(new Runnable() {
            @Override
            public void run() {
                pushmsg_lv.setRefreshing(true);
            }
        }, 500);

    }

    private void getPushmsgInfoFromServer() {
        if (null == spu.getUser()) {
            pushmsg_lv.onRefreshComplete();
            return;
        }
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("Page", Page + "");
        params.addBodyParameter("PageSize", PageSize + "");

        httpUtils.send(HttpMethod.POST
                , ""
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                pushmsg_lv.onRefreshComplete();

                LogUtils.i("tsbl--list-->" + responseInfo.result);
                JSONObject obj = null;
                try {
                    obj = JSONObject.parseObject(responseInfo.result);
                } catch (Exception e) {
                    return;
                }

                try {
                    if (200 == obj.getIntValue("code")) {
                        JSONArray array = obj.getJSONArray("data");
                        LogUtils.i("array-->" + array.toJSONString());
                        ArrayList<PushmsgBean> list = (ArrayList<PushmsgBean>) JSONArray.parseArray(array.toJSONString(), PushmsgBean.class);
                        LogUtils.i("listsize-->" + list.size());

                        pmgadapter.appendData(list, mFlagRefresh);

                        if (list.size() < PageSize) {
                            pushmsg_lv.setMode(Mode.PULL_FROM_START);
                        } else {
                            pushmsg_lv.setMode(Mode.BOTH);
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
                Log.i("push", msg);
                pushmsg_lv.onRefreshComplete();
                if (!mFlagRefresh) {
                    Page--;
                }
                mFlagRefresh = false;
            }
        });
    }

    private void pushmsgItemclick(AdapterView<?> parent, View view,
                                  int position, long id) {
        PushmsgBean pb = (PushmsgBean) pmgadapter.getItem(position - 1);
        Intent intent = new Intent();
        boolean flag = false;//是否是预定类型

        if ("1".equals(pb.getAtype())) {//
            intent.setClass(MyPMColAvtivity.this, NewsDetailActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putString("nid", pb.getArticleid());//
            mBundle.putString("type", "1");
            mBundle.putString("commentCount", pb.getComcount());
            intent.putExtras(mBundle);
            flag = true;
        }

        if (!flag) {
            return;
        }

        startActivity(intent);
        AAnim.ActivityStartAnimation(activity);
    }

    private void mycollectionItemclick(AdapterView<?> parent, View view,
                                       int position, long id) {
        CollectionJsonBean cb = (CollectionJsonBean) colladAdapter.getItem(position - 1);
        CollectionDataBean cdb = cb.getData();
        Intent intent = new Intent();
        intent.putExtra("from", "collection");
        boolean flag = false;//是否是预定类型
        //跳转脚标）1新闻  2图集  3视频 4html5
        LogUtils.i("type-->" + cb.getType());
        if ("1".equals(cb.getType())) {//
            intent.setClass(MyPMColAvtivity.this, NewsDetailActivity.class);

            NewsBean nb = new NewsBean();
            nb.setNid(cdb.getNid());
            nb.setSid("0");
            nb.setTitle(cdb.getTitle());
            nb.setJson_url(cdb.getJson_url());
            nb.setType(cb.getType());
            nb.setTid(cdb.getTid());
//            nb.setNid(cdb.getNid());
            nb.setUpdate_time(cdb.getTime());
            nb.setImgs(cdb.getImgs());
            nb.setFav(cdb.getFav());
            nb.setCopyfrom(cdb.getCopyfrom());
            nb.setComcount(cdb.getComcount());
            Log.e("test", "test--->" + cdb.getJson_url());
            Log.e("test", "test--->" + cdb.getNid());
            Log.e("test", "test--->" + nb.toString());
            intent.putExtra("newbean", nb);

            flag = true;
        } else if ("2".equals(cb.getType())) {
            intent.setClass(MyPMColAvtivity.this, NewsAlbumActivity.class);
            intent.putExtra("pid", cb.getId());
            intent.putExtra("json_url", cb.getData().getJson_url());
            flag = true;
        } else if ("3".equals(cb.getType())) {
            VideoItemBean vib = new VideoItemBean(cb);
            intent.setClass(MyPMColAvtivity.this, VideoPlayerActivity.class);
            intent.putExtra("vib", vib);
            flag = true;
        } else if ("4".equals(cb.getType())) {
            intent.setClass(MyPMColAvtivity.this, HtmlActivity.class);

            NewsBean nb = new NewsBean();
            nb.setCopyfrom(cdb.getCopyfrom());
            nb.setFav(cdb.getFav());
            nb.setComcount(cdb.getComcount());
            nb.setNid(cdb.getNid());
            nb.setSid("0");
            nb.setComflag("0");
            nb.setTitle(cb.getData().getTitle());
            nb.setJson_url(cb.getData().getJson_url());
            nb.setType(cb.getType());
            nb.setTid(cb.getData().getTid());
            nb.setUpdate_time(cb.getData().getTime());
//            String imgs[] = new String[3];
//            imgs = cb.getData().getImgs();
            nb.setImgs(cdb.getImgs());
            Log.e("test", "test--->" + nb.toString());
            intent.putExtra("newbean", nb);

            flag = true;
        }

        if (flag) {
            startActivity(intent);
            AAnim.ActivityStartAnimation(activity);
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pushmsg_lv.onRefreshComplete();

            if (1 == msg.what) {
                List<CollectionJsonBean> list = (List<CollectionJsonBean>) msg.obj;
                colladAdapter.appendData(list, mFlagRefresh);
                colladAdapter.notifyDataSetChanged();
                if (list.size() >= PageSize) {
                    pushmsg_lv.setMode(Mode.BOTH);
                } else {
                    pushmsg_lv.setMode(Mode.PULL_FROM_START);
                }
            }
            mFlagRefresh = false;
        }
    };


    private void getDbCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<NewsItemBeanForCollection> list = dbHelper.getCollectionDBUitls().findAll(Selector
                            .from(NewsItemBeanForCollection.class)
                            .where("id", "!=", null)
                            .orderBy("id", true)
                            .limit(PageSize)
                            .offset((Page - 1) * PageSize));

                    if (null != list && list.size() > 0) {
                        LogUtils.i("list.size-->" + list.size());
                        LogUtils.i("list.size-->" + list.toString());
                        ArrayList<CollectionJsonBean> mlist = new ArrayList<CollectionJsonBean>();
                        for (NewsItemBeanForCollection nifc : list) {
                            LogUtils.i("list.size-->" + nifc);
                            mlist.add(nifc.getCollectionJsonBean());
                        }
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = mlist;
                        handler.sendMessage(msg);
                        pushmsg_lv.post(new Runnable() {
                            @Override
                            public void run() {
                                pushmsg_lv.onRefreshComplete();
                                pushmsg_lv.setMode(Mode.BOTH);
                            }
                        });

                    } else {
                        pushmsg_lv.post(new Runnable() {
                            @Override
                            public void run() {
                                getCollectionInfoFromServer();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(500);
                }

            }
        }).start();
    }

    //获取数据
    private void getCollectionInfoFromServer() {
        if (null != spu.getUser()) { //登录
            LogUtils.i("uid-->" + spu.getUser().getUid());
            String station = SharePreferecesUtils.getParam(MyPMColAvtivity.this, StationConfig.STATION, "def").toString();
            String siteid = null;
            String COLLECTIONLIST_url = null;
            if (station.equals(StationConfig.DEF)) {
                siteid = InterfaceJsonfile.SITEID;
                COLLECTIONLIST_url = InterfaceJsonfile.COLLECTIONLIST;
            } else if (station.equals(StationConfig.YN)) {
                siteid = InterfaceJsonfile_YN.SITEID;
                COLLECTIONLIST_url = InterfaceJsonfile_YN.COLLECTIONLIST;
            } else if (station.equals(StationConfig.TW)) {
                siteid = InterfaceJsonfile_TW.SITEID;
                COLLECTIONLIST_url = InterfaceJsonfile_TW.COLLECTIONLIST;
            }
            RequestParams params = RequestParamsUtils.getParamsWithU();
            params.addBodyParameter("page", Page + "");
            params.addBodyParameter("pagesize", PageSize + "");
            params.addBodyParameter("siteid", siteid);

            httpUtils.send(HttpMethod.POST
                    , COLLECTIONLIST_url//InterfaceApi.collection
                    , params
                    , new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    try {
                        pushmsg_lv.onRefreshComplete();
                        pushmsg_lv.setMode(Mode.PULL_FROM_START);
                        LogUtils.i("collection--list-->" + responseInfo.result);
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
                                pushmsg_lv.setMode(Mode.BOTH);
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
                    pushmsg_lv.onRefreshComplete();
                    pushmsg_lv.setMode(Mode.PULL_FROM_START);
                    if (!mFlagRefresh) {
                        Page--;
                    }
                    mFlagRefresh = false;
                    TUtils.toast(getString(R.string.toast_cannot_connect_to_server));
                }
            });
        } else {
            pushmsg_lv.onRefreshComplete();
            pushmsg_lv.setMode(Mode.PULL_FROM_START);
        }
    }

    private void deletePop(View v, final CollectionJsonBean cb, final int position) {
        try {
            final NewsItemBeanForCollection nbfc = dbHelper.getCollectionDBUitls().findFirst(
                    Selector.from(NewsItemBeanForCollection.class).where("colldataid", "=", cb.getData().getId()));

            if (null != nbfc) {
                com.hzpd.utils.Log.e("NewsBeanDB", "NewsBeanDB--->" + nbfc.getTid() + "::::" + nbfc.getTitle());
            } else {
                com.hzpd.utils.Log.e("NewsBeanDB", "NewsBeanDB--->null");
            }


            final PopupWindow mPopupWindow = new PopupWindow(this);
            LinearLayout pv = (LinearLayout) LayoutInflater.from(this).inflate(
                    R.layout.comment_delete_pop, null);
            ImageView mTwo = (ImageView) pv.findViewById(R.id.comment_delete_img);//删除
            mTwo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupWindow.dismiss();
                    if (null != nbfc) {
                        try {
                            if ("2".equals(cb.getType())) {
                                dbHelper.getCollectionDBUitls().delete(Jsonbean.class, WhereBuilder.b("fid", "=", cb.getId()));
                            }
                            dbHelper.getCollectionDBUitls().delete(NewsItemBeanForCollection.class, WhereBuilder.b("colldataid", "=", cb.getData().getId
                                    ()));
                            TUtils.toast(getString(R.string.toast_delete_success));
                            colladAdapter.deleteItem(position);
                        } catch (DbException e) {
                            e.printStackTrace();
                            TUtils.toast(getString(R.string.toast_delete_failed));
                        }
                        return;
                    }
                    String station = SharePreferecesUtils.getParam(MyPMColAvtivity.this, StationConfig.STATION, "def").toString();
                    String DELETECOLLECTION_url = null;
                    if (station.equals(StationConfig.DEF)) {
                        DELETECOLLECTION_url = InterfaceJsonfile.DELETECOLLECTION;
                    } else if (station.equals(StationConfig.YN)) {
                        DELETECOLLECTION_url = InterfaceJsonfile_YN.DELETECOLLECTION;
                    } else if (station.equals(StationConfig.TW)) {
                        DELETECOLLECTION_url = InterfaceJsonfile_TW.DELETECOLLECTION;
                    }
                    RequestParams pa = RequestParamsUtils.getParamsWithU();
                    pa.addBodyParameter("id", cb.getId());

                    httpUtils.send(HttpMethod.POST
                            , DELETECOLLECTION_url//InterfaceApi.deletecollection
                            , pa
                            , new RequestCallBack<String>() {
                        @Override
                        public void onFailure(HttpException arg0,
                                              String arg1) {
                            TUtils.toast(getString(R.string.toast_cannot_connect_to_server));
                        }

                        @Override
                        public void onSuccess(ResponseInfo<String> arg0) {
                            LogUtils.i("delete reply-->" + arg0.result);
                            JSONObject obj = null;
                            try {
                                obj = JSONObject.parseObject(arg0.result);
                            } catch (Exception e) {
                                return;
                            }
                            if (200 == obj.getIntValue("code")) {
                                TUtils.toast(getString(R.string.toast_delete_success));
                                colladAdapter.deleteItem(position);
                            } else {
                            }
                        }
                    });
                }
            });
            mPopupWindow.setContentView(pv);
            mPopupWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
            mPopupWindow.setBackgroundDrawable(dw);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setFocusable(true);

            mPopupWindow.showAsDropDown(v,
                    v.getWidth() / 2 - 30,
                    -v.getHeight());

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.stitle_ll_back)
    private void goback(View v) {
        finish();
    }


    @Override
    protected void onDestroy() {
        handler.removeCallbacks(null);
        super.onDestroy();
    }
}