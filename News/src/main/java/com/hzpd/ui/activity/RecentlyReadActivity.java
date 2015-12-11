package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hzpd.adapter.NewsItemListViewAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.Log;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

public class RecentlyReadActivity extends MBaseActivity implements View.OnClickListener {

    private TextView stitle_tv_content;

    private RecyclerView recylerlist;
    private NewsItemListViewAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recently_read_layout);
        super.changeStatusBar();
        stitle_tv_content = (TextView) findViewById(R.id.stitle_tv_content);
        stitle_tv_content.setText("最近阅读");
        findViewById(R.id.stitle_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recylerlist = (RecyclerView) findViewById(R.id.recylerlist);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recylerlist.setLayoutManager(layoutManager);
        adapter = new NewsItemListViewAdapter(RecentlyReadActivity.this, this);
//        recylerlist = search_listview_id.getRefreshableView();
        recylerlist.setAdapter(adapter);

        try {
            List<NewsBeanDB> list = DBHelper.getInstance(this).getNewsListDbUtils().findAll(Selector
                    .from(NewsBeanDB.class)
                    .where("isreaded", "=", "1")
                    .orderBy("id", true));
//			DBHelper dbHelper;
//			NewsBeanDB nbfc = DBHelper.getInstance(this).getNewsListDbUtils().findFirst(
//					Selector.from(NewsBeanDB.class).where("isreaded", "=", "1"));

            if (null != list) {
                Log.i("isreaded", "isreaded" + list + ":::" + list.size());
//                adapter.appendData(list,false,false);

//                nb.setType(nbobj.getString("type"));
//                nb.setTid(nbobj.getString("tid"));

                List<NewsBean> nblist = new ArrayList<>();
                int count = 0;
                for (NewsBeanDB nb : list) {
                    NewsBean newsBean = new NewsBean();
                    newsBean.setTid(nb.getTid());
                    newsBean.setType(nb.getType());
                    newsBean.setNid(nb.getNid() + "");
                    newsBean.setTitle(nb.getTitle());
                    newsBean.setJson_url(nb.getJson_url());
                    newsBean.setAttname(nb.getAttname());
                    newsBean.setLike(nb.getLike());
                    newsBean.setUnlike(nb.getUnlike());
                    Log.i("isreaded", "isreaded  getIsreaded--->" + nb.getIsreaded());
                    count++;
                    nblist.add(newsBean);
                }
                Log.i("isreaded", "isreaded  count--->" + count);
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