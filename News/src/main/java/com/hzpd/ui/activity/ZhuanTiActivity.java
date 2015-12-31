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
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import java.util.List;

public class ZhuanTiActivity extends MBaseActivity implements OnClickListener {

    private static final String tag = "ZhuanTiActivity";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_detail_layout);
        super.changeStatusBar();
        getIntentData();//获取传值

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
        Log.i(tag, "getColumns:" + nb.getNid());
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("sid", nb.getNid());
        params.addBodyParameter("page", "" + page);
        params.addBodyParameter("pagesize", "" + pageSize);
        SPUtil.addParams(params);
        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.SUBJECTCOLUMNSLIST
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(tag, "onSuccess");
                JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
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
                            Log.i(tag, "ZhuanTi   SubjectItemColumnsBean" + sicb.toString());
                            getServerList(sicb);
//                            getDbList(sicb);
                        }
                    }
                }

            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(tag, "onFailure");
            }
        });
    }

    //获取专题子分类list
    public void getServerList(final SubjectItemColumnsBean columnid) {
        Log.i(tag, "ZhuanTi  getServerList");
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("columnid", columnid.getCid());
        SPUtil.addParams(params);
        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.NEWSLIST
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(tag, "onSuccess");
                final JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null == obj) {
                    Log.i(tag, "null == obj");
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
            public void onFailure(HttpException error, String msg) {
                Log.i(tag, "onFailure");
            }
        });
    }


    public void setData(final SubjectItemColumnsBean columnid, JSONObject obj) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        } else if ("9".equals(nb.getRtype())) {
            mIntent.setClass(this, XF_NewsHtmlDetailActivity.class);
        } else {
            return;
        }

        activity.startActivityForResult(mIntent, 0);
        AAnim.ActivityStartAnimation(this);
    }


}