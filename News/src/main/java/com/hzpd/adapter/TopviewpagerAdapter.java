package com.hzpd.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

import com.hzpd.custorm.RecyclingPagerAdapter;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsPageListBean;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.ZhuanTiActivity;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.SPUtil;

import java.util.ArrayList;
import java.util.List;

public class TopviewpagerAdapter extends RecyclingPagerAdapter {

    private List<NewsPageListBean> list;
    private String tid;
    private Activity context;

    public TopviewpagerAdapter(Activity mcontext) {
        list = new ArrayList<NewsPageListBean>();
        this.context = mcontext;

    }

    public NewsPageListBean getBean(int position) {
        if (list.size() == 0) {
            return null;
        }
        return list.get(position % list.size());
    }

    public void setData(List<NewsPageListBean> mViewPagelist) {
        clearData();
        addData(mViewPagelist);
    }

    public int getListSize() {
        return list.size();
    }

    public void addData(List<NewsPageListBean> mViewPagelist) {
        list.addAll(mViewPagelist);
        if (list.size() > 1) {
//            NewsPageListBean npbfirst = mViewPagelist.get(0);
//            NewsPageListBean npbend = mViewPagelist.get(mViewPagelist.size() - 1);
//            list.add(npbfirst);
//            list.add(0, npbend);
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        list.clear();
    }

    public void setTid(String mtid) {
        this.tid = mtid;
    }

    @Override
    public int getCount() {
//		return Integer.MAX_VALUE;
        return list.size();
    }


    public int getPosition(int position) {
        if (list.size() == 0) {
            return 0;
        }
        return position % list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        ImageView iv = new ImageView(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT
                , LayoutParams.MATCH_PARENT);
        iv.setLayoutParams(params);
        iv.setScaleType(ScaleType.CENTER_CROP);

        final NewsPageListBean nplb = list.get(getPosition(position));
        SPUtil.displayImage(nplb.getImgurl()
                , iv, DisplayOptionFactory.getOption(OptionTp.Small));
        iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in = new Intent();
                NewsBean nb = new NewsBean();
                nb.setNid(nplb.getNewid());
                nb.setTitle(nplb.getTitle());
                nb.setJson_url(nplb.getJson_url());
                nb.setUpdate_time(nplb.getUpdate_time());
                nb.setSid(nplb.getSid());
                nb.setTid(nplb.getTid());
                nb.setComcount("-1");
                String s[] = new String[3];
                s[0] = nplb.getImgurl();
                nb.setImgs(s);
                nb.setType("1");

                in.putExtra("newbean", nb);
                in.putExtra("tid", tid);
                in.putExtra("from", "news");
                if ("0".equals(nplb.getSid())) {
                    in.setClass(context, NewsDetailActivity.class);
                } else {
                    in.setClass(context, ZhuanTiActivity.class);
                }

                context.startActivity(in);
                AAnim.ActivityStartAnimation(context);
            }
        });

        return iv;
    }

}