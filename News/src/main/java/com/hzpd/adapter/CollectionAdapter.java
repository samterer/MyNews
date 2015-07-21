package com.hzpd.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.CollectionDataBean;
import com.hzpd.modle.CollectionJsonBean;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.util.LogUtils;

public class CollectionAdapter extends ListBaseAdapter<CollectionJsonBean> {
	private SPUtil spu;

	public CollectionAdapter(Activity activity) {
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
			holder.lehuo_img_id = (ImageView) convertView.findViewById(R.id.lehuo_img_id);
			holder.lehuo_content_txt = (TextView) convertView.findViewById(R.id.lehuo_content_txt);
			holder.lehuo_sj_txt = (TextView) convertView.findViewById(R.id.lehuo_sj_txt);
			holder.lehuo_type = (ImageView) convertView.findViewById(R.id.lehuo_type);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		CollectionJsonBean cb = list.get(position);
		CollectionDataBean cdb = cb.getData();
		mImageLoader.displayImage(cdb.getThumb(), holder.lehuo_img_id, DisplayOptionFactory.getOption(OptionTp.Small));
		holder.lehuo_content_txt.setText(cdb.getTitle());

		String sj = CalendarUtil.friendlyTime(cb.getDatetime());
		if (!TextUtils.isEmpty(sj)) {
//			holder.lehuo_sj_txt.setVisibility(View.VISIBLE);//暂时先不显示
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
		ImageView lehuo_img_id;
		TextView lehuo_content_txt;
		TextView lehuo_sj_txt;
		ImageView lehuo_type;
	}


}
