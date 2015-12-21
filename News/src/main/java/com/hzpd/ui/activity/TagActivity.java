package com.hzpd.ui.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.TagBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.TagEvent;
import com.hzpd.ui.widget.RecyclerViewPauseOnScrollListener;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.DisplayOptionFactory;
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

import de.greenrobot.event.EventBus;

public class TagActivity extends MBaseActivity implements View.OnClickListener {

    private RecyclerView recylerlist;
    private NewsItemListViewAdapter adapter;
    private int page = 1;
    private int pageSize = 10;
    private LinearLayoutManager layoutManager;
    private int lastVisibleItem;
    private View news_nonetwork;
    private View details_tag_layout;
    private TextView details_head_tag_name;
    private TextView details_head_tag_num;
    private ImageView details_head_tag_img;
    private TextView details_tv_subscribe;

    NewsItemListViewAdapter.CallBack callBack;
    boolean addLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_layout);
        super.changeStatusBar();

        initView();

        getIntentContent();

        getNewsServer();


        layoutManager = new LinearLayoutManager(this);
        recylerlist.setLayoutManager(layoutManager);
        adapter = new NewsItemListViewAdapter(TagActivity.this, this);
        recylerlist.setAdapter(adapter);
//        adapter.showLoading = false;
//        adapter.notifyDataSetChanged();
        recylerlist.setOnScrollListener(new RecyclerView.OnScrollListener() {

//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
////                    adapter.setShowLoading(true);
//                    addLoading = true;
//                    adapter.showLoading = true;
//                    page++;
//                    Log.i("", "");
//                    getNewsServer();
//                }
//            }

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

        callBack = new NewsItemListViewAdapter.CallBack() {
            @Override
            public void loadMore() {
                Log.i("loadMore", "loadMore");
                getNewsServer();
            }
        };
        adapter.callBack = callBack;
    }

    private void initView() {
        findViewById(R.id.news_detail_bak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        news_nonetwork = findViewById(R.id.news_nonetwork);
        details_tag_layout = findViewById(R.id.details_tag_layout);
        recylerlist = (RecyclerView) findViewById(R.id.recylerlist);
        details_head_tag_name = (TextView) findViewById(R.id.details_head_tag_name);
        details_head_tag_num = (TextView) findViewById(R.id.details_head_tag_num);
        details_head_tag_img = (ImageView) findViewById(R.id.details_head_tag_img);
        details_tv_subscribe = (TextView) findViewById(R.id.details_tv_subscribe);
    }

    private void getNewsServer() {

        details_tag_layout.setVisibility(View.VISIBLE);
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("tagId", tagBean.getId() + "");
        params.addBodyParameter("page", page + "");
        params.addBodyParameter("pageSize", pageSize + "");
        SPUtil.addParams(params);
        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.tag_news_url, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
                if (null == obj) {
                    news_nonetwork.setVisibility(View.VISIBLE);
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    page++;
                    addLoading = true;
                    List<NewsBean> mlist = FjsonUtil.parseArray(obj.getString("data")
                            , NewsBean.class);
                    if (null == mlist) {
                        return;
                    }
                    if (mlist.size() >= pageSize) {
                        adapter.showLoading = true;
                    }
                    adapter.appendData(mlist, false, false);
                    LogUtils.i("listsize-->" + mlist.size());
                    if (page > 1 && adapter.showLoading) {
                        int count = adapter.getItemCount();
                        adapter.showLoading = false;
                        adapter.notifyItemRemoved(count);
                    }
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
    }

    private TagBean tagBean;

    public void getIntentContent() {
        Intent intent = getIntent();
        tagBean = (TagBean) intent.getSerializableExtra("tagbean");
        Log.i("TagActivity", "TagActivity  TagBean--->" + tagBean);
        if (tagBean.getIcon() != null) {
            details_head_tag_img.setVisibility(View.VISIBLE);
            SPUtil.displayImage(tagBean.getIcon(), details_head_tag_img
                    , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Personal_center_News));
        }
        if (tagBean == null) {
            news_nonetwork.setVisibility(View.VISIBLE);
            return;
        }
        details_head_tag_name.setText(tagBean.getName());
        if (tagBean.getNum() != null && Integer.parseInt(tagBean.getNum()) != 0) {
            details_head_tag_num.setVisibility(View.VISIBLE);
            details_head_tag_num.setText("" + tagBean.getNum() + " " + getString(R.string.follow_num));
        } else {
            details_head_tag_num.setVisibility(View.GONE);
        }
        if (SPUtil.checkTag(tagBean)) {
            details_tv_subscribe.setTextColor(getResources().getColor(R.color.white));
            Drawable nav_up = getResources().getDrawable(R.drawable.discovery_details_image_select);
            nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
            details_tv_subscribe.setCompoundDrawables(nav_up, null, null, null);
            details_tv_subscribe.setText(getString(R.string.discovery_followed));
        } else {
            details_tv_subscribe.setTextColor(getResources().getColor(R.color.white));
            Drawable nav_up = getResources().getDrawable(R.drawable.editcolum_image);
            nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
            details_tv_subscribe.setCompoundDrawables(nav_up, null, null, null);
        }
        details_tv_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details_tv_subscribe.setTextColor(getResources().getColor(R.color.white));
                Drawable nav_up = getResources().getDrawable(R.drawable.discovery_details_image_select);
                nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                details_tv_subscribe.setCompoundDrawables(nav_up, null, null, null);
                EventBus.getDefault().post(new TagEvent(tagBean));
            }
        });

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