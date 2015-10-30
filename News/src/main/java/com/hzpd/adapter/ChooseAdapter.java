package com.hzpd.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.ui.App;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.db.sqlite.WhereBuilder;

import org.lucasr.twowayview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class ChooseAdapter extends RecyclerView.Adapter {

    private Context context;
    View.OnClickListener onClickListener;
    private HashSet<String> readedNewsSet;
    DBHelper dbHelper;

    public ChooseAdapter(Context context, View.OnClickListener onClickListener) {
        this.context=context;
        this.onClickListener = onClickListener;
        readedNewsSet = new HashSet<String>();
        dbHelper = DBHelper.getInstance(context);
    }

    public ChooseAdapter(Context context) {
        this.context=context;
        readedNewsSet = new HashSet<String>();
        dbHelper = DBHelper.getInstance(context);
    }

    public void setReadedId(String nid) {
        readedNewsSet.add(nid);

        try {
            NewsBeanDB nbdb = new NewsBeanDB();
            nbdb.setNid(Integer.parseInt(nid));
            nbdb.setIsreaded(1);
            dbHelper.getNewsListDbUtils().update(nbdb
                    , WhereBuilder.b("nid", "=", nid)
                    , "isreaded");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String TAG = ChooseAdapter.class.getSimpleName();
    public static final int COUNT_COLUMS = 2;

    private List<NewsBean> newsList = new ArrayList<>();

    final static int TYPE_FIRST = 0x99;
    final static int TYPE_SECOND = 0x88;


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
        public TextView fromView;
        public TextView collView;
        public TextView commentView;
        public ImageView imageView;
        public View clickView;

        public SecondViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.newsitem_title);
            fromView = (TextView) itemView.findViewById(R.id.newsitem_source);
            collView = (TextView) itemView.findViewById(R.id.newsitem_collectcount);
            commentView = (TextView) itemView.findViewById(R.id.newsitem_commentcount);
            imageView = (ImageView) itemView.findViewById(R.id.newsitem_img);
            clickView = itemView.findViewById(R.id.news_item);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder value = null;
        try {
            switch (viewType) {
                case TYPE_FIRST: {
                    FirstViewHolder holder = null;
                    Context context = parent.getContext();
                    View view = LayoutInflater.from(context).inflate(R.layout.choose_item_first_layout, parent, false);
                    holder = new FirstViewHolder(view);
                    holder.clickView.setOnClickListener(onClickListener);
                    value = holder;
                }
                break;
                default: {
                    SecondViewHolder holder = null;
                    Context context = parent.getContext();
                    View view = LayoutInflater.from(context).inflate(R.layout.choose_item_second_layout, parent, false);
                    holder = new SecondViewHolder(view);
                    holder.clickView.setOnClickListener(onClickListener);
                    value = holder;
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder sHolder, int position) {
        try {
            int type = getItemViewType(position);
            NewsBean bean = newsList.get(position);
            switch (type) {
                case TYPE_FIRST: {
                    FirstViewHolder holder = (FirstViewHolder) sHolder;
                    holder.textView.setText(bean.getTitle());
                    holder.imageView.setImageResource(R.drawable.default_bg);
                    SPUtil.displayImage(bean.getImgs()[0], holder.imageView, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
                    holder.clickView.setTag(bean);
                    final StaggeredGridLayoutManager.LayoutParams lp =
                            (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                    lp.span = COUNT_COLUMS;
                    lp.setMargins(0, 0, 0, 0);
                    holder.itemView.setLayoutParams(lp);
                }
                break;
                default: {
                    final SecondViewHolder holder = (SecondViewHolder) sHolder;
                    holder.textView.setText(bean.getTitle());

                    if (readedNewsSet.contains(bean.getNid())) {
                        holder.textView.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.grey_font));
                    } else {
                        holder.textView.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.black));
                    }
                    holder.imageView.setImageResource(R.drawable.default_bg);
                    SPUtil.displayImage(bean.getImgs()[0], holder.imageView, DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Small));
                    holder.clickView.setTag(bean);
                    final StaggeredGridLayoutManager.LayoutParams lp =
                            (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                    lp.span = 1;
                    lp.setMargins(0, 0, 0, 0);
                    holder.itemView.setLayoutParams(lp);

                    holder.fromView.setVisibility(View.GONE);
                    holder.collView.setVisibility(View.GONE);
                    holder.commentView.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(bean.getCopyfrom())) {
                        holder.fromView.setText(bean.getCopyfrom());
                        holder.fromView.setVisibility(View.VISIBLE);
                    }
                    if (!TextUtils.isEmpty(bean.getFav()) && Integer.valueOf(bean.getFav()) > 0) {
                        holder.collView.setText(bean.getFav());
                        holder.collView.setVisibility(View.VISIBLE);
                    }
                    if (!TextUtils.isEmpty(bean.getComcount()) && Integer.valueOf(bean.getComcount()) > 0) {
                        holder.commentView.setText(bean.getComcount());
                        holder.commentView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return "99".equals(newsList.get(position).getType()) ? TYPE_FIRST : TYPE_SECOND;
    }

    @Override
    public int getItemCount() {
        return newsList.size();
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
     * 追加搜索历史数据
     */
    public void addNews(List<NewsBean> data) {
        if (data != null && !data.isEmpty()) {
            int start = getItemCount();
            newsList.addAll(0, data);
            notifyDataSetChanged();
        }
    }

    public String getLogTag() {
        return this.getClass().getSimpleName();
    }

}