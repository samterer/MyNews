package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.TagBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.ui.widget.RecyclerViewPauseOnScrollListener;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class TagActivity extends MBaseActivity implements View.OnClickListener {

    private TextView stitle_tv_content;
    private RecyclerView recylerlist;
    private NewsItemListViewAdapter adapter;
    private int page = 1;
    private int pageSize = 10;
    boolean addLoading = false;
    private LinearLayoutManager layoutManager;
    private int lastVisibleItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_layout);
        super.changeStatusBar();
        findViewById(R.id.news_detail_bak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getIntentContent();

        getNewsServer();

        recylerlist = (RecyclerView) findViewById(R.id.recylerlist);
        layoutManager =new LinearLayoutManager(this);
        recylerlist.setLayoutManager(layoutManager);
        adapter = new NewsItemListViewAdapter(TagActivity.this, this);
        recylerlist.setAdapter(adapter);
        recylerlist.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
//                    adapter.setShowLoading(true);
                    addLoading=true;
                    adapter.showLoading=true;
                    page++;
                    Log.i("","");
                    getNewsServer();
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (addLoading && !adapter.showLoading) {
                    addLoading = false;
                    int count = adapter.getItemCount();
                    adapter.showLoading = true;
                    adapter.notifyItemInserted(count);
                }
            }
        });
    }

    private void getNewsServer() {
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("tagId", tagBean.getId() + "");
        params.addBodyParameter("page", page + "");
        params.addBodyParameter("pageSize", pageSize + "");
        SPUtil.addParams(params);
        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.tag_news_url, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                adapter.showLoading=false;
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
                if (null == obj) {
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    List<NewsBean> mlist = FjsonUtil.parseArray(obj.getString("data")
                            , NewsBean.class);
                    if (null == mlist) {
                        return;
                    }
                    adapter.appendData(mlist, false, false);
                    if (mlist.size() >= pageSize) {
                        adapter.showLoading = true;
                    }
                    LogUtils.i("listsize-->" + mlist.size());
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                adapter.showLoading=false;

            }
        });
    }

    private TagBean tagBean;

    public void getIntentContent() {
        Intent intent = getIntent();
        tagBean = (TagBean) intent.getSerializableExtra("tagbean");
        Log.i("TagActivity", "TagActivity  TagBean--->" + tagBean);
    }

    @Override
    public void onClick(View view) {
        if (AvoidOnClickFastUtils.isFastDoubleClick()) {
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
        if ("1".equals(nb.getRtype())) {
            mIntent.setClass(this, NewsDetailActivity.class);
        } else if ("2".equals(nb.getRtype())) {
            mIntent.setClass(this, NewsAlbumActivity.class);
        } else if ("3".equals(nb.getRtype())) {
            mIntent.setClass(this, HtmlActivity.class);//直播界面
        } else if ("4".equals(nb.getRtype())) {
            mIntent.setClass(this, ZhuanTiActivity.class);
        } else if ("5".equals(nb.getRtype())) {
            mIntent.setClass(this, NewsDetailActivity.class);
        } else if ("6".equals(nb.getRtype())) {
//					mIntent.setClass(getActivity(),VideoPlayerActivity.class);
            mIntent.setClass(this, NewsDetailActivity.class);
        } else if ("7".equals(nb.getRtype())) {
            mIntent.setClass(this, HtmlActivity.class);
        } else {
            return;
        }

        activity.startActivityForResult(mIntent, 0);
        AAnim.ActivityStartAnimation(this);
    }


}