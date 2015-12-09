package com.hzpd.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hzpd.custorm.CircleImageView;
import com.hzpd.hflt.R;
import com.hzpd.modle.DiscoveryItemBean;
import com.hzpd.modle.MyCommentBean;
import com.hzpd.modle.MycommentsBean;
import com.hzpd.modle.MycommentsitemBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.TagBean;
import com.hzpd.modle.VideoItemBean;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.HtmlActivity;
import com.hzpd.ui.activity.NewsAlbumActivity;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.VideoPlayerActivity;
import com.hzpd.ui.widget.FontTextView;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class DiscoveryItemAdapter extends ListBaseAdapter<DiscoveryItemBean> {

    private int size;

    private boolean flag = false;

    public DiscoveryItemAdapter(Activity c) {
        super(c);
    }

    public DiscoveryItemAdapter(Activity c, int size) {
        super(c);
        this.size = size;
    }

    private static class ViewHolder {
        @ViewInject(R.id.mycoms_img_id)
        ImageView mycoms_img_id;
        @ViewInject(R.id.discovery_iv_tag)
        ImageView discovery_iv_tag;
        @ViewInject(R.id.my_newdetails)
        LinearLayout my_newdetails;
        @ViewInject(R.id.mycoms_content_txt)
        TextView mycoms_content_txt;
        @ViewInject(R.id.news_ll)
        LinearLayout news_ll;
        @ViewInject(R.id.tv_subscribe)
        TextView tv_subscribe;
        public ViewHolder(View v) {
            ViewUtils.inject(this, v);
        }
    }


    @Override
    public View getMyView(int position, View convertView, ViewGroup parent) {
        LogUtils.i("position-->" + position);
        final ViewHolder holder ;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.discovery_item_layout, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final TagBean tagBean = list.get(position).getTag();
        holder.mycoms_content_txt.setText(tagBean.getName());
        String tagIcon = tagBean.getIcon();
        if (tagIcon != null) {
            SPUtil.displayImage(tagIcon
                    , holder.discovery_iv_tag
                    , DisplayOptionFactory.getOption(OptionTp.Personal_center_News));
        }
        holder.tv_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"订阅",Toast.LENGTH_SHORT).show();
                holder.tv_subscribe.setBackgroundResource(R.drawable.corners_bg);
                holder.tv_subscribe.setTextColor(context.getResources().getColor(R.color.details_tv_check_color));
                Drawable nav_up=context.getResources().getDrawable(R.drawable.discovery_image_select);
                nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                holder.tv_subscribe.setCompoundDrawables(nav_up, null, null, null);
            }
        });

        DiscoveryItemBean bean = list.get(position);
        holder.news_ll.removeAllViews();
        for (final NewsBean itembean : bean.getNews()) {
            View vi = inflater.inflate(R.layout.news_list_item_layout, null);
            holder.news_ll.addView(vi);
            ImageView newsitem_img = (ImageView) vi.findViewById(R.id.newsitem_img);
            TextView newsitem_title = (TextView) vi.findViewById(R.id.newsitem_title);
            TextView newsitem_time = (TextView) vi.findViewById(R.id.newsitem_time);
            LinearLayout ll_tag = (LinearLayout) vi.findViewById(R.id.ll_tag);
//            newsitem_time.setText("312321");
//            newsitem_time.setText(itembean.getUpdate_time());
            if (CalendarUtil.friendlyTime(itembean.getUpdate_time(), context) == null) {
                newsitem_time.setText(" ");
            } else {
                newsitem_time.setText(CalendarUtil.friendlyTime(itembean.getUpdate_time(), context));
            }

            newsitem_title.setText(itembean.getTitle());
            if (null != itembean.getImgs()
                    && itembean.getImgs().length > 0) {
                newsitem_title.setPadding(App.px_15dp, 0, 0, 0);
                ll_tag.setPadding(App.px_15dp, 0, 0, 0);
                SPUtil.displayImage(itembean.getImgs()[0], newsitem_img,
                        DisplayOptionFactory.getOption(OptionTp.Small));
            } else {
                newsitem_img.setVisibility(View.GONE);
                newsitem_title.setPadding(0, 0, 0, App.px_15dp);
                ll_tag.setPadding(0, 0, 0, 0);
            }
            TextView newsitem_collectcount = (TextView) vi.findViewById(R.id.newsitem_collectcount);
            String fav = itembean.getFav();
            if (!TextUtils.isEmpty(fav)) {
                int fav_counts = Integer.parseInt(fav);
                if (fav_counts > 0) {
                    newsitem_collectcount.setVisibility(View.VISIBLE);
                    newsitem_collectcount.setText(fav_counts + "");
                } else {
                    newsitem_collectcount.setVisibility(View.GONE);
                }
            } else {
                newsitem_collectcount.setVisibility(View.GONE);
            }
            TextView newsitem_source = (TextView) vi.findViewById(R.id.newsitem_source);
            String from = itembean.getCopyfrom();
            if (!TextUtils.isEmpty(from)) {
                newsitem_source.setVisibility(View.VISIBLE);
                newsitem_source.setText(from);
            } else {
                newsitem_source.setVisibility(View.GONE);
            }
            TextView newsitem_commentcount = (TextView) vi.findViewById(R.id.newsitem_commentcount);
            String comcount = itembean.getComcount();
            if (!TextUtils.isEmpty(comcount)) {
                int counts = Integer.parseInt(comcount);
                if (counts > 0) {
                    newsitem_commentcount.setVisibility(View.VISIBLE);
                    itembean.setComcount(counts + "");
                    newsitem_commentcount.setText(counts + "");
                } else {
                    newsitem_commentcount.setVisibility(View.GONE);
                }
            } else {
                newsitem_commentcount.setVisibility(View.GONE);
            }



            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "" + itembean.getNid(), Toast.LENGTH_SHORT).show();
                    Intent mIntent = new Intent(context,NewsDetailActivity.class);
                    mIntent.putExtra("newbean", itembean);
                    mIntent.putExtra("from", "newsitem");
                    context.startActivity(mIntent);
                }
            });
//            TextView mycomment_news = (TextView) vi.findViewById(R.id.mycomment_news);
//            TextView mycomments_itemc_tv_content = (TextView) vi.findViewById(R.id.mycomments_itemc_tv_content);
//            TextView mycomments_itemc_tv_prise = (TextView) vi.findViewById(R.id.mycomments_itemc_tv_prise);
//            TextView mycomments_itemc_tv_sj_txt = (TextView) vi.findViewById(R.id.mycomments_itemc_tv_sj_txt);
//            TextView cm_item_tv_comstate = (TextView) vi.findViewById(R.id.cm_item_tv_comstate);
        }


        return convertView;
    }


}