package com.hzpd.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.ActionItemBean;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.ViewHolder;

public class ActionListFmAdapter extends ListBaseAdapter<ActionItemBean> {

	public ActionListFmAdapter(Activity context) {
		super(context);
	}

	@Override
	public View getMyView(int position, View convertView, ViewGroup parent) {

		if (null == convertView) {
			convertView = inflater.inflate(R.layout.action_listfm_item, parent, false);
		}

		ImageView actionlist_item_iv = ViewHolder.get(convertView, R.id.actionlist_item_iv);
		TextView actionlist_item_title_tv = ViewHolder.get(convertView, R.id.actionlist_item_title_tv);
		TextView actionlist_item_time = ViewHolder.get(convertView, R.id.actionlist_item_time);

		ActionItemBean aib = list.get(position);

		actionlist_item_title_tv.setText(aib.getTitle());
		actionlist_item_time.setText(convertView.getContext().getString(
				R.string.activity_item_start_end_time, aib.getStarttime(), aib.getDeadline()));

		SPUtil.displayImage(aib.getHeadpic()
				, actionlist_item_iv
				, DisplayOptionFactory.Small.options);

		return convertView;
	}

}