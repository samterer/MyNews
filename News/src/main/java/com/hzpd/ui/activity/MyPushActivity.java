package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.Log;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

public class MyPushActivity extends MBaseActivity implements View.OnClickListener {

    private TextView stitle_tv_content;

    private RecyclerView recylerlist;
    private NewsItemListViewAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recently_read_layout);
        super.changeStatusBar();
        stitle_tv_content = (TextView) findViewById(R.id.stitle_tv_content);
        stitle_tv_content.setText(R.string.prompt_my_msg);
        findViewById(R.id.stitle_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recylerlist = (RecyclerView) findViewById(R.id.recylerlist);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recylerlist.setLayoutManager(layoutManager);
        adapter = new NewsItemListViewAdapter(MyPushActivity.this, this);
        recylerlist.setAdapter(adapter);

        try {
            List<NewsBeanDB> list = DBHelper.getInstance(this).getPushListDbUtils().findAll(Selector
                    .from(NewsBeanDB.class));

            if (null != list) {
                Log.i("MyPush", "MyPush list--->" + list + ":::" + list.size());
                List<NewsBean> nblist = new ArrayList<>();
                for (NewsBeanDB nb : list) {
                    NewsBean newsBean = new NewsBean();
                    newsBean.setNid(nb.getNid() + "");
                    newsBean.setTitle(nb.getTitle());
                    newsBean.setSid(nb.getSid());
                    newsBean.setTid(nb.getTid());
                    newsBean.setAuthorname(nb.getAuthorname());
                    newsBean.setOutline(nb.getOutline());
                    newsBean.setType(nb.getType());
                    newsBean.setUpdate_time(nb.getUpdate_time());
                    newsBean.setJson_url(nb.getJson_url());
                    if (!TextUtils.isEmpty(nb.getImgs())) {
                        String[] nbImgs = nb.getImgs().split(",");
                        newsBean.setImgs(nbImgs);
                    }
                    newsBean.setRtype(nb.getRtype());
                    newsBean.setComcount(nb.getComcount());
                    newsBean.setSort_order(nb.getSort_order());
                    newsBean.setStatus(nb.getStatus());
                    newsBean.setComflag(nb.getComflag());
                    newsBean.setSubjectsort(nb.getSubjectsort());
                    newsBean.setColumnid(nb.getColumnid());
                    newsBean.setCopyfrom(nb.getCopyfrom());
                    newsBean.setFav(nb.getFav());
                    newsBean.setAttname(nb.getAttname());
                    newsBean.setLike(nb.getLike());
                    newsBean.setUnlike(nb.getUnlike());
                    //newsBean.setCnname(nb.getCnname);
                    //private String cnname;//频道
                    Log.i("MyPush", "MyPush  getpush--->" + nb.getNid());
                    nblist.add(newsBean);
                }
                adapter.appendData(nblist, false, false);
            } else {

            }
        } catch (DbException e) {
            e.printStackTrace();
        }


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