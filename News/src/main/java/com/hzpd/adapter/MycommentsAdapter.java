package com.hzpd.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.MycommentsBean;
import com.hzpd.modle.MycommentsitemBean;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class MycommentsAdapter extends ListBaseAdapter<MycommentsBean> {


	public MycommentsAdapter(Activity c) {
		super(c);
	}

	private static class ViewHolder {
		@ViewInject(R.id.mycoms_img_id)
		ImageView mycoms_img_id;
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

		MycommentsBean bean = list.get(position);

		holder.mycoms_content_txt.setText(bean.getTitle());

		mImageLoader.displayImage(bean.getSmallimgurl()
				, holder.mycoms_img_id
				, DisplayOptionFactory.getOption(OptionTp.Small));

		holder.mycoms_ll.removeAllViews();
		for (MycommentsitemBean itembean : bean.getComs()) {
			View vi = inflater.inflate(R.layout.mycomments_itemc_layout, null);
			holder.mycoms_ll.addView(vi);
			TextView mycomments_itemc_tv_content = (TextView) vi.findViewById(R.id.mycomments_itemc_tv_content);
			TextView mycomments_itemc_tv_prise = (TextView) vi.findViewById(R.id.mycomments_itemc_tv_prise);
			TextView mycomments_itemc_tv_sj_txt = (TextView) vi.findViewById(R.id.mycomments_itemc_tv_sj_txt);
			TextView cm_item_tv_comstate = (TextView) vi.findViewById(R.id.cm_item_tv_comstate);

			mycomments_itemc_tv_content.setText(itembean.getContent());
			mycomments_itemc_tv_prise.setText(itembean.getPraise());
			mycomments_itemc_tv_sj_txt.setText(itembean.getDateline());

			if ("-2".equals(itembean.getStatus())) {
				cm_item_tv_comstate.setVisibility(View.VISIBLE);
			} else {
				cm_item_tv_comstate.setVisibility(View.GONE);
			}

		}

		return convertView;
	}


}

