package com.hzpd.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.db.NewsBeanDBDao;
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

    LayoutInflater inflate;
    private int fontSize;
    public CallBack callBack = null;

    private Context context;
    View.OnClickListener onClickListener;
    private HashSet<String> readedNewsSet;
    DBHelper dbHelper;

    public ChooseAdapter(Context context, View.OnClickListener onClickListener) {
        this.context = context;
        inflate = LayoutInflater.from(context);
        this.onClickListener = onClickListener;
        readedNewsSet = new HashSet<String>();
        dbHelper = DBHelper.getInstance();
        this.fontSize = SPUtil.getInstance().getTextSize();
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
    public static final int COUNT_COLUMS = 2;

    private List<NewsBean> newsList = new ArrayList<>();

    final static int TYPE_FIRST = 0x99;
    final static int TYPE_SECOND = 0x88;
    final static int TYPE_LOADING = 0xDD;
    public boolean showLoading = false;

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


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder value = null;
        try {
            long start = System.currentTimeMillis();
            switch (viewType) {
                case TYPE_FIRST: {
                    FirstViewHolder holder = null;
                    View view = inflate.inflate(R.layout.choose_item_first_layout, parent, false);
                    int time = (int) (System.currentTimeMillis() - start);
                    if (time > 16) {
                        Log.e("test", "News:inflate FirstViewHolder " + time + "   => ");
                    }
                    holder = new FirstViewHolder(view);
                    holder.clickView.setOnClickListener(onClickListener);
                    value = holder;
                }
                break;
                case TYPE_LOADING: {
                    View view = inflate.inflate(
                            R.layout.list_load_more_layout, parent, false);
                    value = new LoadingHolder(view);
                }
                break;
                default: {
                    SecondViewHolder holder = null;
                    View view = inflate.inflate(R.layout.choose_item_second_layout, parent, false);
                    int time = (int) (System.currentTimeMillis() - start);
                    if (time > 16) {
                        Log.e("test", "News:inflate SecondViewHolder " + time + "   => ");
                    }
                    holder = new SecondViewHolder(view);
                    holder.clickView.setOnClickListener(onClickListener);
                    value = holder;
                }
                break;
            }
            int time = (int) (System.currentTimeMillis() - start);
            if (time > 16) {
                Log.e("test", "News: " + time + "  =>  " + value.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder sHolder, int position) {
        long start = System.currentTimeMillis();
        try {
            int type = getItemViewType(position);
            NewsBean bean = null;
            if (type != TYPE_LOADING) {
                bean = newsList.get(position);
            }
            switch (type) {
                case TYPE_LOADING: {
                    Log.e("test", " TYPE_LOADING ");
                    if (showLoading && callBack != null) {
                        callBack.loadMore();
                    }
                    final StaggeredGridLayoutManager.LayoutParams lp =
                            (StaggeredGridLayoutManager.LayoutParams) sHolder.itemView.getLayoutParams();
                    lp.span = COUNT_COLUMS;
                    lp.setMargins(0, 0, 0, 0);
                    sHolder.itemView.setLayoutParams(lp);
                }
                break;
                case TYPE_FIRST: {
                    FirstViewHolder holder = (FirstViewHolder) sHolder;
                    holder.textView.setTextSize(fontSize);
                    holder.textView.setText(bean.getTitle());
                    holder.imageView.setImageResource(R.drawable.default_bg);
                    SPUtil.displayImage(bean.getImgs()[0], holder.imageView, DisplayOptionFactory.Small.options);
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
                    holder.textView.setTextSize(fontSize);
                    holder.textView.setText(bean.getTitle());
                    holder.timeView.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), context));

                    if (readedNewsSet.contains(bean.getNid())) {
                        TypedValue typedValue = new TypedValue();
                        context.getTheme().resolveAttribute(R.attr.item_title_read, typedValue, true);
                        int color = typedValue.data;
//                        holder.textView.setTextColor(App.getInstance()
//                                .getResources().getColor(R.color.grey_font));
                        holder.textView.setTextColor(color);
                    }
                    holder.imageView.setImageResource(R.drawable.default_bg);
                    SPUtil.displayImage(bean.getImgs()[0], holder.imageView, DisplayOptionFactory.Small.options);
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
        int time = (int) (System.currentTimeMillis() - start);
        if (time > 16) {
            Log.e("test", "News: " + time + "  =>  " + sHolder.getClass().getName());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoading && position == getItemCount() - 1) {
            return TYPE_LOADING;
        }
        return "99".equals(newsList.get(position).getType()) ? TYPE_FIRST : TYPE_SECOND;
    }

    @Override
    public int getItemCount() {
        int count = newsList.size();
        if (showLoading) {
            ++count;
        }
        return count;
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

    public String getLogTag() {
        return this.getClass().getSimpleName();
    }

}