package com.hzpd.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.SubjectItemColumnsBean;
import com.hzpd.ui.App;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class ZhuantiDetailListAdapter2 extends BaseAdapter {

    private Activity context;
    private LayoutInflater inflater;
    private ImageLoader mImageLoader;

    private float fontSize = 0;// 字体大小

    private LinkedHashMap<SubjectItemColumnsBean, List<NewsBean>> columnList;

    public ZhuantiDetailListAdapter2(Activity context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        mImageLoader = ImageLoader.getInstance();
        columnList = new LinkedHashMap<SubjectItemColumnsBean, List<NewsBean>>();

        fontSize = SPUtil.getInstance().getTextSize();
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

    @Override
    public int getCount() {
        if (0 == columnList.size()) {
            return 0;
        } else {
            int columnCounts = 0;
            Set<SubjectItemColumnsBean> sets = columnList.keySet();
            for (SubjectItemColumnsBean sicb : sets) {
                columnCounts += 1;
                List<NewsBean> nbList = columnList.get(sicb);
                if (null != nbList) {
                    columnCounts += nbList.size();
                }
            }
            return columnCounts;
        }

    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        int columnCounts = 0;
        Set<SubjectItemColumnsBean> sets = columnList.keySet();
        for (SubjectItemColumnsBean sicb : sets) {
            if (position == columnCounts) {
                return 0;
            } else {
                columnCounts += 1;
                List<NewsBean> nbList = columnList.get(sicb);
                if (null != nbList && nbList.size() > 0) {
                    columnCounts += nbList.size();
                    if (position < columnCounts) {
                        return 1;
                    }
                }
            }
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        NewsBean nb = null;
        int columnCounts = 0;
        Set<SubjectItemColumnsBean> sets = columnList.keySet();
        for (SubjectItemColumnsBean sicb : sets) {
            if (position == columnCounts) {
                return sicb.getCname();
            } else {
                columnCounts += 1;
                List<NewsBean> nbList = columnList.get(sicb);
                if (null != nbList && nbList.size() > 0) {
                    if (position >= columnCounts
                            && position < columnCounts + nbList.size()) {
                        nb = nbList.get((position - columnCounts)
                                % nbList.size());
                        break;
                    }
                    columnCounts += nbList.size();
                }
            }
        }

        return nb;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        if (null == convertView) {
            if (0 == type) {
                convertView = inflater.inflate(R.layout.zhuanti_column_item,
                        parent, false);
            } else {
                convertView = inflater.inflate(R.layout.news_list_item_layout,
                        parent, false);
            }
        }

        if (0 == type) {
            TextView zhuanti_tv_column = ViewHolder.get(convertView,
                    R.id.zhuanti_tv_column);
            String title = (String) getItem(position);
            zhuanti_tv_column.setText("" + title);
        } else {
            NewsBean nb = (NewsBean) getItem(position);
            TextView title = ViewHolder
                    .get(convertView, R.id.newsitem_title);
//            title.setTextSize(fontSize);
            title.setText(nb.getTitle());

            TextView sj = ViewHolder.get(convertView, R.id.newsitem_time);
            sj.setText(nb.getUpdate_time());
            if (CalendarUtil.friendlyTime(nb.getUpdate_time(), context) == null) {
                sj.setText("");
            } else {
                sj.setText(CalendarUtil.friendlyTime(nb.getUpdate_time(), context));
            }
            LinearLayout ll_tag = ViewHolder.get(convertView, R.id.ll_tag);
            ImageView img = ViewHolder.get(convertView, R.id.newsitem_img);
            String s[] = nb.getImgs();
            String simg = "";
            if (null != s && s.length > 0) {
                simg = s[0];
            }
            SPUtil.displayImage(simg, img,
                    DisplayOptionFactory.getOption(OptionTp.Small));
            title.setPadding(App.px_15dp, 0, 0, 0);
            ll_tag.setPadding(App.px_15dp, 0, 0, 0);

            TextView newsitem_source = ViewHolder.get(convertView, R.id.newsitem_source);
            Log.i("nb.getCopyfrom()","nb.getCopyfrom()"+nb.getCopyfrom());
            if (!TextUtils.isEmpty(nb.getCopyfrom())) {
                newsitem_source.setText(nb.getCopyfrom());
            } else {
                newsitem_source.setVisibility(View.GONE);
            }
            TextView newsitem_collectcount = ViewHolder.get(convertView, R.id.newsitem_collectcount);
            if (!TextUtils.isEmpty(nb.getFav())) {
                int fav_counts = Integer.parseInt(nb.getFav());
                if (fav_counts > 0) {
                    newsitem_collectcount.setText(nb.getFav());
                } else {
                    newsitem_collectcount.setVisibility(View.GONE);
                }
            } else {
                newsitem_collectcount.setVisibility(View.GONE);
            }

            TextView newsitem_commentcount = ViewHolder.get(convertView, R.id.newsitem_commentcount);
            if (!TextUtils.isEmpty(nb.getComcount())) {
                int counts = Integer.parseInt(nb.getComcount());
                if (counts > 0) {
                    newsitem_commentcount.setVisibility(View.VISIBLE);
                    nb.setComcount(counts + "");
                    newsitem_commentcount.setText(counts + "");
                } else {
                    newsitem_commentcount.setVisibility(View.GONE);
                }
            } else {
                newsitem_commentcount.setVisibility(View.GONE);
            }

        }

        return convertView;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

}
