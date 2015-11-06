package com.hzpd.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.CollectionDataBean;
import com.hzpd.modle.CollectionJsonBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.ui.App;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.util.LogUtils;

public class CollectionAdapter extends ListBaseAdapter<CollectionJsonBean> {
    private SPUtil spu;

    public static enum Itemtype {
        THREEPIC(0), LEFTPIC(1), BIGPIC(2);
        private int type;

        private Itemtype(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    public CollectionAdapter(Activity activity) {
        super(activity);
        spu = SPUtil.getInstance();
    }

    public void deleteItem(int position) {
        if (list != null&&list.size()>0) {
            this.list.remove(position);
//            notifyDataSetChanged();
        }
    }


    @Override
    public int getViewTypeCount() {
        return Itemtype.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        CollectionJsonBean cb = list.get(position);
        if (cb != null) {
            if ("4".equals(cb.getType())) {
                return Itemtype.THREEPIC.getType();
            } else if ("10".equals(cb.getType())) {
                return Itemtype.BIGPIC.getType();
            } else {
                return Itemtype.LEFTPIC.getType();
            }
        }
        return -1;
    }


    @Override
    public View getMyView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        ViewHolder2 holder2 = null;
        int type = getItemViewType(position);

        CollectionJsonBean cb = list.get(position);
        CollectionDataBean cdb = cb.getData();
        if (cb!=null)
        Log.e("CollectionJsonBean","CollectionJsonBean--->"+cb.toString());
        else
        Log.e("CollectionJsonBean","CollectionJsonBean--->null");


        if (convertView == null) {
            switch (type) {
                case 0: {
                    holder2 = new ViewHolder2();
                    convertView = inflater.inflate(R.layout.collection_list_item_layout, parent, false);
                    convertView.setTag(holder2);
                }
                break;
                case 1: {
                    holder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.news_list_item_layout, parent, false);
                    holder.newsitem_img1 = (ImageView) convertView.findViewById(R.id.newsitem_img);
                    holder.newsitem_title = (TextView) convertView.findViewById(R.id.newsitem_title);
                    holder.newsitem_time = (TextView) convertView.findViewById(R.id.newsitem_time);
                    holder.newsitem_source = (TextView) convertView.findViewById(R.id.newsitem_source);
                    holder.newsitem_collectcount = (TextView) convertView.findViewById(R.id.newsitem_collectcount);
                    holder.newsitem_commentcount = (TextView) convertView.findViewById(R.id.newsitem_commentcount);
                    holder.nli_foot = (ImageView) convertView.findViewById(R.id.nli_foot);
                    holder.ll_tag= (LinearLayout) convertView.findViewById(R.id.ll_tag);
                    convertView.setTag(holder);
                }
                break;

            }
        } else {
            switch (type) {
                case 0: {
                    holder2 = (ViewHolder2) convertView.getTag();
                }
                break;
                case 1: {
                    holder = (ViewHolder) convertView.getTag();
                }
                break;
            }

        }


        switch (type) {
            case 0: {

            }
            break;
            case 1: {

                if (cdb.getTitle() != null) {
                    holder.newsitem_title.setText(cdb.getTitle());
                }
                if (cdb.getImgs() != null && cdb.getImgs().length > 0) {
                    Log.e("getImgs", "getImgs" + cdb.getImgs().length+"::::"+ cdb.getTitle());
                    String s[] = cdb.getImgs();
                    holder.newsitem_title.setPadding(App.px_15dp, 0, 0, 0);
                    holder.ll_tag.setPadding(App.px_15dp, 0, 0, 0);
                    SPUtil.displayImage(s[0], holder.newsitem_img1, DisplayOptionFactory.getOption(OptionTp.Small));
                } else {
                    Log.e("cdb.getImgs()","cdb.getImgs()--->"+cdb.getTitle());
                    holder.newsitem_img1.setVisibility(View.GONE);
                    holder.newsitem_title.setPadding(0, 0, 0, App.px_15dp);
                    holder.ll_tag.setPadding(0, 0, 0, 0);
                }



                String copyfrom = cdb.getCopyfrom();
                if (!TextUtils.isEmpty(copyfrom)) {
                    holder.newsitem_source.setVisibility(View.VISIBLE);
                    holder.newsitem_source.setText(copyfrom);
                } else {
                    holder.newsitem_source.setVisibility(View.GONE);
                }
                String fav = cdb.getFav();
                if (fav != null) {
                    int favcounts = Integer.parseInt(fav);
                    if (favcounts > 0) {
                        holder.newsitem_collectcount.setVisibility(View.VISIBLE);
                        holder.newsitem_collectcount.setText("" + favcounts);
                    } else {
                        holder.newsitem_collectcount.setVisibility(View.GONE);
                    }

                }

                String comment = cdb.getComcount();
                if (comment != null) {
                    int commentcounts = Integer.parseInt(comment);
                    if (commentcounts > 0) {
                        holder.newsitem_commentcount.setVisibility(View.VISIBLE);
                        holder.newsitem_commentcount.setText("" + Integer.parseInt(comment));
                    } else {
                        holder.newsitem_commentcount.setVisibility(View.GONE);
                    }
                }


                String sj = CalendarUtil.friendlyTime1(cb.getDatetime(), context);
                if (!TextUtils.isEmpty(sj)) {
                    holder.newsitem_time.setVisibility(View.VISIBLE);
                    holder.newsitem_time.setText(sj);
                }

                if ("2".equals(cb.getType())) {
                    LogUtils.i("img");
                    holder.nli_foot.setVisibility(View.VISIBLE);
                    holder.nli_foot.setImageResource(R.drawable.zq_subscript_album);
                } else if ("3".equals(cb.getType())) {
                    LogUtils.i("img");
                    holder.nli_foot.setVisibility(View.VISIBLE);
                    holder.nli_foot.setImageResource(R.drawable.zq_subscript_video);
                } else if ("4".equals(cb.getType())) {
                    LogUtils.i("img");
                    holder.nli_foot.setVisibility(View.VISIBLE);
                    holder.nli_foot.setImageResource(R.drawable.zq_subscript_html);
                } else {
                    holder.nli_foot.setVisibility(View.GONE);
                }
            }
            break;

        }


        return convertView;
    }

    private static class ViewHolder {
        ImageView line;
        ImageView newsitem_img1;
        TextView newsitem_title;
        TextView newsitem_time;
        TextView newsitem_source;
        TextView newsitem_collectcount;
        TextView newsitem_commentcount;
        ImageView nli_foot;
        LinearLayout ll_tag;
    }

    private static class ViewHolder2 {

    }
}