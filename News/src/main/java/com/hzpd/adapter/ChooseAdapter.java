package com.hzpd.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.db.NewsBeanDBDao;
import com.hzpd.ui.App;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;

import org.lucasr.twowayview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class ChooseAdapter extends RecyclerView.Adapter {

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        notifyDataSetChanged();
    }

    public interface CallBack {
        void loadMore();
    }

    LayoutInflater inflater;
    private int fontSize;
    public CallBack callBack = null;

    private Context context;
    View.OnClickListener onClickListener;
    private HashSet<String> readedNewsSet;
    DBHelper dbHelper;

    public ChooseAdapter(Context context, View.OnClickListener onClickListener) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.onClickListener = onClickListener;
        readedNewsSet = new HashSet<String>();
        dbHelper = DBHelper.getInstance();
        this.fontSize = SPUtil.getInstance().getTextSize();
    }

    /**
     * 重新加载数据
     */
    public void setData(List<NewsBean> data) {
        if (data != null) {
            newsList.clear();
            newsList.addAll(data);
            notifyDataSetChanged();
        }
    }


    /**
     * 底部追加数据
     */
    public void addBottom(List<NewsBean> data) {
        if (data != null && !data.isEmpty()) {
            newsList.addAll(data);
            notifyDataSetChanged();
        }
    }

    /**
     * 顶部追加数据
     */
    public void addTop(List<NewsBean> data) {
        if (data != null && !data.isEmpty()) {
            newsList.addAll(0, data);
            notifyDataSetChanged();
        }
    }

    public void setReadedId(String nid) {
        readedNewsSet.add(nid);
        try {
            NewsBeanDB nbdb = dbHelper.getNewsList().queryBuilder().where(NewsBeanDBDao.Properties.Nid.eq(nid)).build().unique();
            nbdb.setIsreaded("1");
            dbHelper.getNewsList().update(nbdb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String TAG = ChooseAdapter.class.getSimpleName();

    private List<NewsBean> newsList = new ArrayList<>();

    final static int TYPE_FIRST = 0x99;
    final static int TYPE_SECOND = 0x88;
    final static int TYPE_LOADING = 0xDD;
    public boolean showLoading = false;
    final static int TYPE_FLASH = 9;
    final static int TYPE_THREEPIC = 0;
    final static int TYPE_LEFTPIC = 1;
    final static int TYPE_BIGPIC = 2;
    final static int TYPE_LARGE = 3;
    final static int TYPE_TEXT = 4; // 纯文本
    final static int TYPE_JOKE = 5; // JOKE 段子
    final static int TYPE_AD = 0xad;

    @Override
    public int getItemViewType(int position) {

        if (showLoading && position == getItemCount() - 1) {
            return TYPE_LOADING;
        }
        Log.i("newsList", "newsList" + newsList.size());
        NewsBean bean = newsList.get(position);
        if ("11".equals(bean.getType())) {
            return TYPE_JOKE;
        } else if (bean.getImgs() == null || bean.getImgs().length == 0) {
            return TYPE_TEXT;
        } else if ("4".equals(bean.getType())) {
            return TYPE_THREEPIC;
        } else if ("10".equals(bean.getType())) {
            return TYPE_BIGPIC;
        } else if ("99".equals(bean.getType())) {
            return TYPE_LARGE;
        } else {
            return TYPE_LEFTPIC;
        }
    }

    @Override
    public int getItemCount() {
        int count = newsList.size();
        if (showLoading) {
            ++count;
        }
        return count;
    }


    public class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }

    protected class FirstViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public View clickView;

        public FirstViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.newsitem_title);
            imageView = (ImageView) itemView.findViewById(R.id.newsitem_img);
            clickView = itemView.findViewById(R.id.news_item);
        }

    }

    protected class SecondViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public TextView timeView;
        public TextView fromView;
        public TextView collView;
        public TextView commentView;
        public ImageView imageView;
        public View clickView;

        public SecondViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.newsitem_title);
            timeView = (TextView) itemView.findViewById(R.id.newsitem_time);
            fromView = (TextView) itemView.findViewById(R.id.newsitem_source);
            collView = (TextView) itemView.findViewById(R.id.newsitem_collectcount);
            commentView = (TextView) itemView.findViewById(R.id.newsitem_commentcount);
            imageView = (ImageView) itemView.findViewById(R.id.newsitem_img);
            clickView = itemView.findViewById(R.id.news_item);
        }
    }

    //JOKE 段子
    private class JokeHolder extends RecyclerView.ViewHolder {
        private TextView newsitem_title;
        private TextView joke_good_tv;
        private TextView joke_bad_tv;
        private TextView joke_share_tv;
        private TextView joke_comment_counts_tv;
        private ImageView joke_good_img;
        private ImageView joke_bad_img;
        private View joke_good_layout;
        private View joke_bad_layout;
        private View joke_comment_counts_layout;


        public JokeHolder(View v) {
            super(v);
            newsitem_title = (TextView) v.findViewById(R.id.newsitem_title);
            joke_good_tv = (TextView) v.findViewById(R.id.joke_good_tv);
            joke_bad_tv = (TextView) v.findViewById(R.id.joke_bad_tv);
            joke_share_tv = (TextView) v.findViewById(R.id.joke_share_tv);
            joke_comment_counts_tv = (TextView) v.findViewById(R.id.joke_comment_counts_tv);
            joke_good_img = (ImageView) v.findViewById(R.id.joke_good_img);
            joke_bad_img = (ImageView) v.findViewById(R.id.joke_bad_img);
            joke_good_layout = v.findViewById(R.id.joke_good_layout);
            joke_bad_layout = v.findViewById(R.id.joke_bad_layout);
            joke_comment_counts_layout = v.findViewById(R.id.joke_comment_counts_layout);
            v.setOnClickListener(onClickListener);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View convertView;
        try {
            switch (viewType) {
                case TYPE_TEXT://纯文本
                    convertView = inflater.inflate(
                            R.layout.news_list_text_layout, parent, false);
                    viewHolder = new TextViewHolder(convertView);
                    break;

                case TYPE_FIRST: {
                    FirstViewHolder holder = null;
                    View view = inflater.inflate(R.layout.choose_item_first_layout, parent, false);
                    holder = new FirstViewHolder(view);
                    holder.clickView.setOnClickListener(onClickListener);
                    viewHolder = holder;
                }
                break;
                case TYPE_LOADING: {
                    View view = inflater.inflate(
                            R.layout.list_load_more_layout, parent, false);
                    viewHolder = new LoadingHolder(view);
                }
                break;
                default: {
                    SecondViewHolder holder = null;
                    View view = inflater.inflate(R.layout.choose_item_second_layout, parent, false);
                    holder = new SecondViewHolder(view);
                    holder.clickView.setOnClickListener(onClickListener);
                    viewHolder = holder;
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.item_title, typedValue, true);
        int color = typedValue.data;
        long start = System.currentTimeMillis();
        try {
            int type = getItemViewType(position);
            NewsBean bean = null;
            if (type != TYPE_LOADING) {
                bean = newsList.get(position);
            }
            switch (type) {
                case TYPE_TEXT: {
                    bindText(holder, position, color);
                }
                break;
                case TYPE_LOADING: {
                    Log.e("test", " TYPE_LOADING ");
                    if (showLoading && callBack != null) {
                        callBack.loadMore();
                    }
                }
                break;
                case TYPE_FIRST: {
                    FirstViewHolder fholder = (FirstViewHolder) holder;
                    fholder.textView.setTextSize(fontSize);
                    fholder.textView.setText(bean.getTitle());
                    fholder.imageView.setImageResource(R.drawable.default_bg);
                    SPUtil.displayImage(bean.getImgs()[0], fholder.imageView, DisplayOptionFactory.Small.options);
                    fholder.clickView.setTag(bean);
                }
                break;
                default: {
                    final SecondViewHolder sholder = (SecondViewHolder) holder;
                    sholder.textView.setTextSize(fontSize);
                    sholder.textView.setText(bean.getTitle());
                    sholder.timeView.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), context));

                    if (readedNewsSet.contains(bean.getNid())) {
                        sholder.textView.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.grey_font));
                    }
                    sholder.imageView.setImageResource(R.drawable.default_bg);
                    SPUtil.displayImage(bean.getImgs()[0], sholder.imageView, DisplayOptionFactory.Small.options);
                    sholder.clickView.setTag(bean);

                    sholder.fromView.setVisibility(View.GONE);
                    sholder.collView.setVisibility(View.GONE);
                    sholder.commentView.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(bean.getCopyfrom())) {
                        sholder.fromView.setText(bean.getCopyfrom());
                        sholder.fromView.setVisibility(View.VISIBLE);
                    }
                    if (!TextUtils.isEmpty(bean.getFav()) && Integer.valueOf(bean.getFav()) > 0) {
                        sholder.collView.setText(bean.getFav());
                        sholder.collView.setVisibility(View.VISIBLE);
                    }
                    if (!TextUtils.isEmpty(bean.getComcount()) && Integer.valueOf(bean.getComcount()) > 0) {
                        sholder.commentView.setText(bean.getComcount());
                        sholder.commentView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int time = (int) (System.currentTimeMillis() - start);
        if (time > 16) {
            Log.e("test", "News: " + time + "  =>  " + holder.getClass().getName());
        }
    }

    //纯文本，评论，时间，脚标
    private class TextViewHolder extends RecyclerView.ViewHolder {
        private TextView newsitem_title;
        private ImageView nli_foot;
        private TextView newsitem_source;//来源
        private TextView newsitem_collectcount;//收藏数
        private TextView newsitem_commentcount;//评论数
        private TextView newsitem_time;
        private ImageView newsitem_unlike;
        private ImageView item_type_iv;
        private LinearLayout ll_tag;

        public TextViewHolder(View v) {
            super(v);
            newsitem_title = (TextView) v.findViewById(R.id.newsitem_title);
            nli_foot = (ImageView) v.findViewById(R.id.nli_foot);
            newsitem_source = (TextView) v.findViewById(R.id.newsitem_source);
            newsitem_collectcount = (TextView) v.findViewById(R.id.newsitem_collectcount);
            newsitem_commentcount = (TextView) v.findViewById(R.id.newsitem_commentcount);
            newsitem_time = (TextView) v.findViewById(R.id.newsitem_time);
            newsitem_unlike = (ImageView) v.findViewById(R.id.newsitem_unlike);
            item_type_iv = (ImageView) v.findViewById(R.id.item_type_iv);
            ll_tag = (LinearLayout) v.findViewById(R.id.ll_tag);
            v.setOnClickListener(onClickListener);
        }
    }

    private void bindText(RecyclerView.ViewHolder holder, int position, int color) {
        NewsBean bean;
        bean = newsList.get(position);
        holder.itemView.setTag(bean);
        TextViewHolder textViewHolder = (TextViewHolder) holder;
        textViewHolder.newsitem_title.setTextSize(fontSize);
        textViewHolder.newsitem_title.setText(bean.getTitle());
        if (readedNewsSet.contains(bean.getNid())) {
            textViewHolder.newsitem_title.setTextColor(App.getInstance()
                    .getResources().getColor(R.color.grey_font));
        } else {
            textViewHolder.newsitem_title.setTextColor(color);
        }
        SPUtil.setAtt(textViewHolder.item_type_iv, bean.getAttname());

        String fav = bean.getFav();
        if (!TextUtils.isEmpty(fav)) {

            int fav_counts = Integer.parseInt(fav);
            if (fav_counts > 0) {
                textViewHolder.newsitem_collectcount.setVisibility(View.VISIBLE);
                textViewHolder.newsitem_collectcount.setText(fav_counts + "");
            } else {
                textViewHolder.newsitem_collectcount.setVisibility(View.GONE);
            }
        } else {
            textViewHolder.newsitem_collectcount.setVisibility(View.GONE);
        }

        String from = bean.getCopyfrom();
        if (!TextUtils.isEmpty(from)) {
            textViewHolder.newsitem_source.setVisibility(View.VISIBLE);
            textViewHolder.newsitem_source.setText(from);
        } else {
            textViewHolder.newsitem_source.setVisibility(View.GONE);
        }

        String comcount = bean.getComcount();
        if (!TextUtils.isEmpty(comcount)) {
            int counts = Integer.parseInt(comcount);
            if (counts > 0) {
                textViewHolder.newsitem_commentcount.setVisibility(View.VISIBLE);
                bean.setComcount(counts + "");
                textViewHolder.newsitem_commentcount.setText(counts + "");
            } else {
                textViewHolder.newsitem_commentcount.setVisibility(View.GONE);
            }
        } else {
            textViewHolder.newsitem_commentcount.setVisibility(View.GONE);
        }


        if (CalendarUtil.friendlyTime(bean.getUpdate_time(), context) == null) {
            textViewHolder.newsitem_time.setText("");
        } else {
            textViewHolder.newsitem_time.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), context));
        }
        textViewHolder.nli_foot.setVisibility(View.GONE);

        textViewHolder.newsitem_unlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "不喜欢", Toast.LENGTH_SHORT).show();
            }
        });

        if (bean.getSid() != null && !"0".equals(bean.getSid())) {
            textViewHolder.nli_foot.setImageResource(R.drawable.zq_subscript_issue);
            textViewHolder.nli_foot.setVisibility(View.VISIBLE);
        }
        SPUtil.setRtype(bean.getRtype(), textViewHolder.nli_foot);
    }

    public String getLogTag() {
        return this.getClass().getSimpleName();
    }

}