package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.ZhuantiDetailListAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.SubjectItemColumnsBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.squareup.okhttp.Request;

import java.util.List;
import java.util.Map;

public class ZhuanTiActivity extends MBaseActivity implements OnClickListener {

    private View stitle_ll_back;
    private TextView stitle_tv_content;

    private ZhuantiDetailListAdapter adapter;
    //    private ZhuantiDetailListAdapter2 adapter;
    private String from;
    private int page = 1;
    private int pageSize = 10;//
    private NewsBean nb = null;//专题id
    private List<SubjectItemColumnsBean> columnList;
    RecyclerView mRecyclerView;
    private ImageView zhuanti_header_iv;
    private TextView zhuanti_tv_title;
    private ListView zhuanti_item_listview;
    private Object tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_detail_layout);
        super.changeStatusBar();
        getIntentData();//获取传值
        tag = OkHttpClientManager.getTag();
        stitle_ll_back = findViewById(R.id.stitle_ll_back);
        stitle_ll_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        stitle_tv_content = (TextView) findViewById(R.id.stitle_tv_content);
        stitle_tv_content.setText(getString(R.string.prompt_subject));

        mRecyclerView = (RecyclerView) findViewById(R.id.recylerlist);
        zhuanti_item_listview = (ListView) findViewById(R.id.zhuanti_item_listview);


        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new ZhuantiDetailListAdapter(activity, this, nb);
        mRecyclerView.setAdapter(adapter);
        getColumns();

    }

    private void getIntentData() {
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

    }

    //专题栏目列表
    public void getColumns() {
        Log.i("test", "getColumns:" + nb.getNid());
        Map<String, String> params = RequestParamsUtils.getMaps();
        params.put("sid", nb.getNid());
        params.put("page", "" + page);
        params.put("pagesize", "" + pageSize);
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.SUBJECTCOLUMNSLIST

                , new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                Log.i("test", "onSuccess");
                JSONObject obj = FjsonUtil
                        .parseObject(response.toString());
                if (null == obj) {
                    return;
                }
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
                            Log.i("test", "ZhuanTi   SubjectItemColumnsBean" + sicb.toString());
                            getServerList(sicb);
//                            getDbList(sicb);
                        }
                    }
                }

            }

            @Override
            public void onFailure(Request request, Exception e) {
                Log.i("test", "onFailure");
            }
        }, params);
    }

    //获取专题子分类list
    public void getServerList(final SubjectItemColumnsBean columnid) {
        Log.i("test", "ZhuanTi  getServerList");
        Map<String, String> params = RequestParamsUtils.getMaps();
        params.put("siteid", InterfaceJsonfile.SITEID);
        params.put("columnid", columnid.getCid());
        SPUtil.addParams(params);
        OkHttpClientManager.postAsyn(tag
                , InterfaceJsonfile.NEWSLIST

                , new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                Log.i("test", "onSuccess");
                final JSONObject obj = FjsonUtil.parseObject(response.toString());
                if (null == obj) {
                    Log.i("test", "null == obj");
                    return;
                }
                JSONObject cache = obj.getJSONObject("cachetime");
                //数据处理
                if (200 == obj.getIntValue("code")) {
                    List<NewsBean> list = FjsonUtil.parseArray(obj.getString("data"), NewsBean.class);
                    adapter.appendData(columnid, list, false);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                Log.i("test", "onFailure");
            }
        }, params);
    }


    public void setData(final SubjectItemColumnsBean columnid, JSONObject obj) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OkHttpClientManager.cancel(tag);
    }

    //点击操作
    @Override
    public void onClick(View view) {
        if (AvoidOnClickFastUtils.isFastDoubleClick(view)) {
            return;
        }
        TextView title = (TextView) view.findViewById(R.id.newsitem_title);
        if (null != title) {
            title.setTextColor(getResources().getColor(R.color.grey_font));
        }

        NewsBean nb = (NewsBean) view.getTag();
        Intent mIntent = new Intent();
        mIntent.putExtra("newbean", nb);
        mIntent.putExtra("from", "newsitem");
        adapter.setReadedId(nb.getNid());
        ////////////////////////////
        //1新闻  2图集  3直播 4专题  5关联新闻 6视频
//TODO 视频新闻
        if ("1".equals(nb.getRtype())) {
            mIntent.setClass(this, NewsDetailActivity.class);
        } else if ("2".equals(nb.getRtype())) {
            mIntent.setClass(this, NewsAlbumActivity.class);
        } else if ("3".equals(nb.getRtype())) {
            mIntent.setClass(this, NewsDetailActivity.class);//直播界面
        } else if ("4".equals(nb.getRtype())) {
            mIntent.setClass(this, ZhuanTiActivity.class);
        } else if ("5".equals(nb.getRtype())) {
            mIntent.setClass(this, NewsDetailActivity.class);
        } else if ("6".equals(nb.getRtype())) {
            mIntent.setClass(this, VideoPlayerActivity.class);
        } else if ("7".equals(nb.getRtype())) {
            mIntent.setClass(this, NewsDetailActivity.class);
        } else {
            return;
        }

        activity.startActivityForResult(mIntent, 0);
        AAnim.ActivityStartAnimation(this);
    }


}