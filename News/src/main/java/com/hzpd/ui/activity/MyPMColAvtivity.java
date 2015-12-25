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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.CollectionAdapter;
import com.hzpd.adapter.PushmsgAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.CollectionDataBean;
import com.hzpd.modle.CollectionJsonBean;
import com.hzpd.modle.Jsonbean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.VideoItemBean;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
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
    private ListView pushmsg_lv;
    @ViewInject(R.id.pushmsg_tv_empty)
    private View pushmsg_tv_empty;

    private int Page = 1;//页数
    private static final int PageSize = 15; //每页大小

    private boolean mFlagRefresh = true;//刷新还是加载

    private PushmsgAdapter pmgadapter;
    private CollectionAdapter colladAdapter;

    private String type;
    private View coverTop;

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
            coverTop = findViewById(R.id.cover_top);
            if (App.getInstance().getThemeName().equals("0")) {
                coverTop.setVisibility(View.GONE);
            } else {
                coverTop.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.changeStatusBar();
    }


    OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent,
                                       View view, int position, long id) {
            CollectionJsonBean cb = (CollectionJsonBean) colladAdapter.getItem(position - 1);
            deletePop(view, cb, position - 1);
            return true;
        }
    };


    private void init() {
        Intent intent = getIntent();
        if (null == intent) {
            return;
        }
        type = intent.getStringExtra("type");
        pushmsg_lv.setEmptyView(pushmsg_tv_empty);

        if ("pushmsg".equals(type)) {
            stitle_tv_content.setText(R.string.prompt_my_msg);
            pmgadapter = new PushmsgAdapter(this);
            pushmsg_lv.setAdapter(pmgadapter);
        } else {
            stitle_tv_content.setText(R.string.prompt_collect);
            colladAdapter = new CollectionAdapter(this);
            pushmsg_lv.setAdapter(colladAdapter);
        }


        pushmsg_lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if ("pushmsg".equals(type)) {

                } else {
                    mycollectionItemclick(parent, view, position, id);
                }
            }
        });

        pushmsg_lv.postDelayed(new Runnable() {
            @Override
            public void run() {
                if ("pushmsg".equals(type)) {

                } else {
                    colladAdapter.clear();
                    getDbCache();
                }
            }
        }, 500);

    }

    private void mycollectionItemclick(AdapterView<?> parent, View view,
                                       int position, long id) {

        if (AvoidOnClickFastUtils.isFastDoubleClick())
            return;
        CollectionJsonBean cb = (CollectionJsonBean) colladAdapter.getItem(position);
        CollectionDataBean cdb = cb.getData();
        Intent intent = new Intent();
        intent.putExtra("from", "collection");
        boolean flag = false;//是否是预定类型
        try {
            NewsItemBeanForCollection bean = dbHelper.getCollectionDBUitls().findFirst(Selector.from(NewsItemBeanForCollection.class).where("nid", "=", cb.getData().getId()));
            if (bean != null) {
                cdb.setNid(bean.getNid());
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
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
            intent.setClass(MyPMColAvtivity.this, NewsDetailActivity.class);
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
            nb.setImgs(cdb.getImgs());
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
            if (!isResume) {
                return;
            }
            if (1 == msg.what) {
                List<CollectionJsonBean> list = (List<CollectionJsonBean>) msg.obj;
                colladAdapter.appendData(list, mFlagRefresh);
                colladAdapter.notifyDataSetChanged();
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
                    if (!isResume) {
                        return;
                    }
                    if (null != list && list.size() > 0) {
                        LogUtils.i("list.size-->" + list.toString());
                        ArrayList<CollectionJsonBean> mlist = new ArrayList<CollectionJsonBean>();
                        for (NewsItemBeanForCollection nifc : list) {
                            mlist.add(nifc.getCollectionJsonBean());
                        }
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = mlist;
                        handler.sendMessage(msg);
                        pushmsg_lv.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!isResume) {
                                    return;
                                }
                            }
                        });

                    } else {
                        pushmsg_lv.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!isResume) {
                                    return;
                                }
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
            RequestParams params = RequestParamsUtils.getParamsWithU();
            params.addBodyParameter("page", Page + "");
            params.addBodyParameter("pagesize", PageSize + "");
            params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
            SPUtil.addParams(params);
            httpUtils.send(HttpMethod.POST
                    , InterfaceJsonfile.COLLECTIONLIST//InterfaceApi.collection
                    , params
                    , new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    try {
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
        }
    }

    private void deletePop(View v, final CollectionJsonBean cb, final int position) {

        if (cb != null) {
            final PopupWindow mPopupWindow = new PopupWindow(this);
            LinearLayout pv = (LinearLayout) LayoutInflater.from(this).inflate(
                    R.layout.comment_delete_pop, null);
            View mTwo = (View) pv.findViewById(R.id.comment_delete_ll);//删除
            mTwo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupWindow.dismiss();

                    //本地数据库获取
                    try {
                        List<NewsItemBeanForCollection> nibfc = dbHelper.getCollectionDBUitls().findAll(NewsItemBeanForCollection.class);
                        if (nibfc != null && nibfc.size() > 0) {

                            Log.e("", "");

                            if ("2".equals(cb.getType())) {
                                dbHelper.getCollectionDBUitls().delete(Jsonbean.class, WhereBuilder.b("fid", "=", cb.getId()));
                            }
                            dbHelper.getCollectionDBUitls().delete(NewsItemBeanForCollection.class, WhereBuilder.b("nid", "=", cb.getData().getId
                                    ()));
                            TUtils.toast(getString(R.string.toast_delete_success));
                            colladAdapter.deleteItem(position);
                            colladAdapter.notifyDataSetChanged();
                        }

                    } catch (DbException e) {
                        e.printStackTrace();
                        TUtils.toast(getString(R.string.toast_delete_failed));
                    }

                    //网络获取
                    if (spu.getUser() != null) {
                        RequestParams params = RequestParamsUtils.getParamsWithU();
                        params.addBodyParameter("id", cb.getId());
                        SPUtil.addParams(params);
                        httpUtils.send(HttpMethod.POST
                                , InterfaceJsonfile.DELETECOLLECTION//InterfaceApi.deletecollection
                                , params
                                , new RequestCallBack<String>() {


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
                                    colladAdapter.notifyDataSetChanged();
                                } else {
                                }
                            }

                            @Override
                            public void onFailure(HttpException arg0,
                                                  String arg1) {
                                TUtils.toast(getString(R.string.toast_cannot_connect_to_server));
                            }
                        });
                    }
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