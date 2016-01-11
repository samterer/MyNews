package com.hzpd.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.color.tools.mytools.LogUtils;
import com.hzpd.hflt.R;
import com.hzpd.modle.CollectionDataBean;
import com.hzpd.modle.CollectionJsonBean;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;

public class CollectionNewAdapter extends ListBaseAdapter<CollectionJsonBean> {
    private SPUtil spu;

    public CollectionNewAdapter(Activity activity) {
        super(activity);
        spu = SPUtil.getInstance();
    }

    public void deleteItem(int position) {
        this.list.remove(position);
        notifyDataSetChanged();
    }


    @Override
    public View getMyView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.lehuo_list_item_layout, parent, false);
            holder.line = (ImageView) convertView.findViewById(R.id.line);
            holder.lehuo_img_id = (ImageView) convertView.findViewById(R.id.lehuo_img_id);
            holder.lehuo_content_txt = (TextView) convertView.findViewById(R.id.lehuo_content_txt);
            holder.lehuo_sj_txt = (TextView) convertView.findViewById(R.id.lehuo_sj_txt);
            holder.lehuo_content_source = (TextView) convertView.findViewById(R.id.lehuo_content_source);
            holder.lehuo_content_collectcount = (TextView) convertView.findViewById(R.id.lehuo_content_collectcount);
            holder.lehuo_commentcount = (TextView) convertView.findViewById(R.id.lehuo_commentcount);
            holder.lehuo_type = (ImageView) convertView.findViewById(R.id.lehuo_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            holder.line.setVisibility(View.GONE);
        }
        CollectionJsonBean cb = list.get(position);
        CollectionDataBean cdb = cb.getData();
        Log.e("CollectionJsonBean", "CollectionJsonBean" + cb.toString());


        if (cdb.getImgs() != null && cdb.getImgs().length > 0) {
            Log.e("getImgs", "getImgs" + cdb.getImgs());
            String s[] = cdb.getImgs();
            SPUtil.displayImage(s[0], holder.lehuo_img_id, DisplayOptionFactory.Small.options);
        }

        if (cdb.getTitle() != null) {
            holder.lehuo_content_txt.setText(cdb.getTitle());
        }

        String copyfrom = cdb.getCopyfrom();
        if (copyfrom != null) {
            holder.lehuo_content_source.setVisibility(View.VISIBLE);
            holder.lehuo_content_source.setText(copyfrom);
        } else {
            holder.lehuo_content_source.setVisibility(View.GONE);
        }
        String fav = cdb.getFav();
        if (TextUtils.isEmpty(fav)) {
            holder.lehuo_content_collectcount.setVisibility(View.GONE);
        } else {
            int favcounts = Integer.parseInt(fav);
            if (favcounts > 0) {
                holder.lehuo_content_collectcount.setVisibility(View.VISIBLE);
                holder.lehuo_content_collectcount.setText("" + favcounts);
            }
        }

        String comment = cdb.getComcount();
        if (TextUtils.isEmpty(comment)) {
            holder.lehuo_commentcount.setVisibility(View.GONE);
        } else {
            int commentcounts = Integer.parseInt(comment);
            if (commentcounts > 0) {
                holder.lehuo_commentcount.setVisibility(View.VISIBLE);
                holder.lehuo_commentcount.setText("" + Integer.parseInt(comment));
            }
        }

        String sj = CalendarUtil.friendlyTime1(cb.getDatetime(), context);
        if (!TextUtils.isEmpty(sj)) {
            holder.lehuo_sj_txt.setVisibility(View.VISIBLE);//暂时先不显示
            holder.lehuo_sj_txt.setText(sj);
        }

        if ("2".equals(cb.getType())) {
            LogUtils.i("img");
            holder.lehuo_type.setVisibility(View.VISIBLE);
            holder.lehuo_type.setImageResource(R.drawable.zq_subscript_album);
        } else if ("3".equals(cb.getType())) {
            LogUtils.i("img");
            holder.lehuo_type.setVisibility(View.VISIBLE);
            holder.lehuo_type.setImageResource(R.drawable.zq_subscript_video);
        } else if ("4".equals(cb.getType())) {
            LogUtils.i("img");
            holder.lehuo_type.setVisibility(View.VISIBLE);
            holder.lehuo_type.setImageResource(R.drawable.zq_subscript_html);
        } else {
            holder.lehuo_type.setVisibility(View.GONE);
        }
        return convertView;
    }

    private static class ViewHolder {
        ImageView line;
        ImageView lehuo_img_id;
        TextView lehuo_content_txt;
        TextView lehuo_sj_txt;
        TextView lehuo_content_source;
        TextView lehuo_content_collectcount;
        TextView lehuo_commentcount;
        ImageView lehuo_type;
    }
}