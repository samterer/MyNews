package com.hzpd.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.SubjectItemColumnsBean;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class ZhuantiDetailListAdapter extends RecyclerView.Adapter {

    private Activity context;
    private LayoutInflater inflater;
    private ImageLoader mImageLoader;

    private LinkedHashMap<SubjectItemColumnsBean, List<NewsBean>> columnList = new LinkedHashMap<>();
    View.OnClickListener onClickListener;

    public ZhuantiDetailListAdapter(Activity context, View.OnClickListener onClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
        this.inflater = LayoutInflater.from(context);
    }

    public void appendData(SubjectItemColumnsBean column, List<NewsBean> list,
                           Boolean isClearOldList) {

        List<NewsBean> oldList = columnList.get(column);
        if (null == oldList) {
            oldList = new ArrayList<NewsBean>();
        } else {
            if (isClearOldList) {
                oldList.clear();
            }
        }
        oldList.addAll(list);
        columnList.put(column, oldList);

    }

    public void clearData() {
        columnList.clear();
    }


    final static int TYPE_HEAD = 0x10;
    final static int TYPE_CLOUMN_HEAD = 0x11;

    final static int TYPE_THREEPIC = 0x13;
    final static int TYPE_LEFTPIC = 0x14;
    final static int TYPE_BIGPIC = 0x15;
    final static int TYPE_LARGE = 0x16;

    public class HeadViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public HeadViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ColumnViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ColumnViewHolder(View itemView) {
            super(itemView);
        }
    }


    //三联图
    private class VHThree extends RecyclerView.ViewHolder {
        @ViewInject(R.id.newsitem_title)
        private TextView newsitem_title;
        @ViewInject(R.id.news_3_tv_time)
        private TextView tv3;
        @ViewInject(R.id.news_3_item1)
        private ImageView img0;
        @ViewInject(R.id.news_3_item2)
        private ImageView img1;
        @ViewInject(R.id.news_3_item3)
        private ImageView img2;
        @ViewInject(R.id.newsitem_foot)
        private ImageView newsitem_foot;
        @ViewInject(R.id.newsitem_comments)
        private TextView newsitem_comments;
        @ViewInject(R.id.newsitem_source)
        private TextView newsitem_source;
        @ViewInject(R.id.newsitem_collectcount)
        private TextView newsitem_collectcount;
        @ViewInject(R.id.item_type_iv)
        private ImageView item_type_iv;

        public VHThree(View v) {
            super(v);
            ViewUtils.inject(this, v);
            v.setOnClickListener(onClickListener);
        }
    }

    //左边图片，右title，评论，时间，脚标
    private class VHLeftPic extends RecyclerView.ViewHolder {
        @ViewInject(R.id.newsitem_title)
        private TextView newsitem_title;
        @ViewInject(R.id.nli_foot)
        private ImageView nli_foot;
        //		来源
        @ViewInject(R.id.newsitem_source)
        private TextView newsitem_source;
        //		收藏数
        @ViewInject(R.id.newsitem_collectcount)
        private TextView newsitem_collectcount;
        //		评论数
        @ViewInject(R.id.newsitem_commentcount)
        private TextView newsitem_commentcount;
        @ViewInject(R.id.newsitem_time)
        private TextView newsitem_time;
        @ViewInject(R.id.newsitem_img)
        private ImageView newsitem_img;
        @ViewInject(R.id.newsitem_unlike)
        private ImageView newsitem_unlike;
        @ViewInject(R.id.item_type_iv)
        private ImageView item_type_iv;
        @ViewInject(R.id.ll_tag)
        private LinearLayout ll_tag;

        public VHLeftPic(View v) {
            super(v);
            ViewUtils.inject(this, v);
            v.setOnClickListener(onClickListener);
        }
    }

    //大图
    private class VHLargePic extends RecyclerView.ViewHolder {
        @ViewInject(R.id.newsitem_title)
        private TextView newsitem_title;
        @ViewInject(R.id.nli_foot)
        private ImageView nli_foot;
        //		来源
        @ViewInject(R.id.newsitem_source)
        private TextView newsitem_source;
        //		收藏数
        @ViewInject(R.id.newsitem_collectcount)
        private TextView newsitem_collectcount;
        //		评论数
        @ViewInject(R.id.newsitem_commentcount)
        private TextView newsitem_commentcount;
        @ViewInject(R.id.newsitem_time)
        private TextView newsitem_time;
        @ViewInject(R.id.newsitem_img)
        private ImageView newsitem_img;
        @ViewInject(R.id.newsitem_unlike)
        private ImageView newsitem_unlike;
        @ViewInject(R.id.item_type_iv)
        private ImageView item_type_iv;

        public VHLargePic(View v) {
            super(v);
            ViewUtils.inject(this, v);
            v.setOnClickListener(onClickListener);
        }
    }

    private class VHBigPic extends RecyclerView.ViewHolder {

        @ViewInject(R.id.news_big_item1)
        private ImageView news_big_item1;


        public VHBigPic(View v) {
            super(v);
            ViewUtils.inject(this, v);
            v.setOnClickListener(onClickListener);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEAD:
                return new HeadViewHolder(new View(context));
            default:
                return new HeadViewHolder(new View(context));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEAD;
        }
        int columnCounts = 1;
        Set<SubjectItemColumnsBean> sets = columnList.keySet();
        for (SubjectItemColumnsBean sicb : sets) {
            if (position == columnCounts) {
                return TYPE_CLOUMN_HEAD;
            } else {
                columnCounts += 1;
                List<NewsBean> nbList = columnList.get(sicb);
                if (null != nbList && nbList.size() > 0) {
                    columnCounts += nbList.size();
                    if (position < columnCounts) {
                        NewsBean bean = nbList.get(position);
                        if ("4".equals(bean.getType())) {
                            return TYPE_THREEPIC;
                        } else if ("10".equals(bean.getType())) {
                            return TYPE_BIGPIC;
                        } else if ("99".equals(bean.getType())) {
                            return TYPE_LARGE;
                        } else {
                            return TYPE_LEFTPIC;
                        }
                    }
                }
            }
        }
        return TYPE_HEAD;
    }

    @Override
    public int getItemCount() {
        int columnCounts = 1;
        if (!columnList.isEmpty()) {
            Set<SubjectItemColumnsBean> sets = columnList.keySet();
            for (SubjectItemColumnsBean sicb : sets) {
                columnCounts += 1;
                List<NewsBean> nbList = columnList.get(sicb);
                if (null != nbList) {
                    columnCounts += nbList.size();
                }
            }
        }
        return columnCounts;
    }

}
