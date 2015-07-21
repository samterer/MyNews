package com.hzpd.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.hzpd.custorm.UserCommPostView;
import com.hzpd.modle.XF_UserCommentsBean;

public class XF_UserCommentsAdapter extends ListBaseAdapter<XF_UserCommentsBean> {

	public XF_UserCommentsAdapter(Activity context) {
		super(context);
	}

	@Override
	public View getMyView(int position, View convertView, ViewGroup parent) {
		if (null == convertView) {
			convertView = new UserCommPostView(context);
		}
		((UserCommPostView) convertView).setPost(list.get(position));
		return convertView;
	}

}
