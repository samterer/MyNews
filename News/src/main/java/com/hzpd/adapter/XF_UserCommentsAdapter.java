package com.hzpd.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hzpd.custorm.CircleImageView;
import com.hzpd.custorm.UserCommPostView;
import com.hzpd.hflt.R;
import com.hzpd.modle.MyCommentBean;
import com.hzpd.modle.MycommentsBean;
import com.hzpd.modle.MycommentsitemBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.VideoItemBean;
import com.hzpd.modle.XF_CommentBean;
import com.hzpd.modle.XF_UserCommNewsBean;
import com.hzpd.modle.XF_UserCommentsBean;
import com.hzpd.ui.activity.HtmlActivity;
import com.hzpd.ui.activity.NewsAlbumActivity;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.VideoPlayerActivity;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class XF_UserCommentsAdapter extends ListBaseAdapter<XF_UserCommentsBean> {

    public XF_UserCommentsAdapter(Activity context) {
        super(context);
    }

    private boolean flag=false;

    private static class ViewHolder {
        @ViewInject(R.id.mycoms_img_id)
        ImageView mycoms_img_id;
        @ViewInject(R.id.my_newdetails)
        LinearLayout my_newdetails;
        @ViewInject(R.id.mycoms_content_txt)
        TextView mycoms_content_txt;
        @ViewInject(R.id.mycoms_ll)
        LinearLayout mycoms_ll;

        public ViewHolder(View v) {
            ViewUtils.inject(this, v);
        }
    }


    @Override
    public View getMyView(int position, View convertView, ViewGroup parent) {
        LogUtils.i("position-->" + position);
        ViewHolder holder = null;

        if (null == convertView) {
            convertView = inflater.inflate(R.layout.mycomments_item_layout, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final  XF_UserCommNewsBean myCommentBean = list.get(position).getContent();
        holder.mycoms_content_txt.setText(myCommentBean.getTitle());
//		String[] imgs = myCommentBean.getImgs();

        holder.my_newdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                if ("1".equals(myCommentBean.getType())) {
                    intent.setClass(context, NewsDetailActivity.class);
                    NewsBean nb = new NewsBean();
                    nb.setNid(myCommentBean.getNid());
                    nb.setSid("0");
                    nb.setTitle(myCommentBean.getTitle());
                    nb.setJson_url(myCommentBean.getUrl());
                    nb.setType(myCommentBean.getType());
                    nb.setRtype(myCommentBean.getType());
                    nb.setTid(myCommentBean.getTid());
                    String[] imgs = {myCommentBean.getSmallimgurl()};
                    nb.setImgs(imgs);
                    nb.setUpdate_time(myCommentBean.getUpdate_time());
                    nb.setCopyfrom(myCommentBean.getCopyfrom());
                    nb.setFav(myCommentBean.getFav());
                    nb.setComcount(myCommentBean.getComcount());
                    intent.putExtra("newbean", nb);
                    intent.putExtra("from", "mycomments");
                    flag = true;

                } else if ("2".equals(myCommentBean.getType())) {
                    intent.setClass(context, NewsAlbumActivity.class);
                    intent.putExtra("from", "collection");
                    intent.putExtra("pid", myCommentBean.getNid());
                    intent.putExtra("json_url", myCommentBean.getUrl());
                    flag = true;
                } else if ("3".equals(myCommentBean.getType())
                        || "7".equals(myCommentBean.getType())) {
                    intent.setClass(context, HtmlActivity.class);

                    NewsBean nb = new NewsBean();
                    nb.setNid(myCommentBean.getNid());
                    nb.setSid("0");
                    nb.setTitle(myCommentBean.getTitle());
                    nb.setJson_url(myCommentBean.getUrl());
                    nb.setRtype(myCommentBean.getType());
                    nb.setType("1");
                    nb.setTid(myCommentBean.getTid());
                    String[] imgs = {myCommentBean.getSmallimgurl()};
                    nb.setImgs(imgs);
                    nb.setUpdate_time(myCommentBean.getUpdate_time());
                    nb.setCopyfrom(myCommentBean.getCopyfrom());
                    intent.putExtra("newbean", nb);
                    intent.putExtra("from", "mycomments");
                    flag = true;
                } else if ("4".equals(myCommentBean.getType())) {
                    intent.setClass(context, VideoPlayerActivity.class);
                    VideoItemBean vib = new VideoItemBean();
                    vib.setVid(myCommentBean.getNid());
                    vib.setTitle(myCommentBean.getTitle());
                    vib.setTime(myCommentBean.getUpdate_time());
                    vib.setMainpic(myCommentBean.getSmallimgurl());
                    vib.setJson_url(myCommentBean.getUrl());
                    intent.putExtra("from", "collection");
                    intent.putExtra("vib", vib);
                    flag = true;
                }
                if (!flag) {
                    return;
                }
                context.startActivity(intent);
                AAnim.ActivityStartAnimation(context);
            }
        });
        if (myCommentBean.getImgs() != null&&myCommentBean.getImgs().length>0) {
            SPUtil.displayImage(myCommentBean.getImgs()[0]
                    , holder.mycoms_img_id
                    , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Personal_center_News));
        }else{
            holder.mycoms_img_id.setImageResource(R.drawable.urlicon_loadingpicture_dynamic);
//            holder.mycoms_img_id.setVisibility(View.GONE);
        }



        XF_UserCommentsBean bean = list.get(position);

        holder.mycoms_ll.removeAllViews();

        for (XF_CommentBean itembean : bean.getComs()) {
            View vi = inflater.inflate(R.layout.mycomments_itemc_layout, null);
            holder.mycoms_ll.addView(vi);
            CircleImageView my_avatar = (CircleImageView) vi.findViewById(R.id.my_avatar);
            TextView mycomment_news = (TextView) vi.findViewById(R.id.mycomment_news);
            TextView mycomments_itemc_tv_content = (TextView) vi.findViewById(R.id.mycomments_itemc_tv_content);
            TextView mycomments_itemc_tv_prise = (TextView) vi.findViewById(R.id.mycomments_itemc_tv_prise);
            TextView mycomments_itemc_tv_sj_txt = (TextView) vi.findViewById(R.id.mycomments_itemc_tv_sj_txt);
            TextView cm_item_tv_comstate = (TextView) vi.findViewById(R.id.cm_item_tv_comstate);
            mycomment_news.setText(itembean.getNickname());
            SPUtil.displayImage(itembean.getAvatar_path()
                    , my_avatar
                    , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.XF_Avatar));
//            mycomment_news.setText(itembean.getNickname());
            mycomments_itemc_tv_content.setText(itembean.getContent());
//            mycomments_itemc_tv_prise.setText(itembean.getPraise());
            mycomments_itemc_tv_sj_txt.setText(CalendarUtil.friendlyTime1(itembean.getDateline(),context));
//
//			if ("-2".equals(itembean.getStatus())) {
//				cm_item_tv_comstate.setVisibility(View.VISIBLE);
//			} else {
//				cm_item_tv_comstate.setVisibility(View.GONE);
//			}
        }


        return convertView;
    }


}