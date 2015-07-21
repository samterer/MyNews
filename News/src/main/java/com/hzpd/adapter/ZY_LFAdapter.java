package com.hzpd.adapter;


import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.Menu_Item_Bean;
import com.hzpd.ui.App;
import com.hzpd.utils.CODE;
import com.hzpd.utils.ViewHolder;

public class ZY_LFAdapter extends ListBaseAdapter<Menu_Item_Bean> {

	private int currentPosition = 0;

	public ZY_LFAdapter(Activity activity) {
		super(activity);

		////-------列表顺序------
		list.add(App.menuList.get(CODE.MENU_NEWS));
		list.add(App.menuList.get(CODE.MENU_ALBUM));
		list.add(App.menuList.get(CODE.MENU_VIDEO_RECORDING));
		list.add(App.menuList.get(CODE.MENU_SPECIAL));
		list.add(App.menuList.get(CODE.MENU_ACTION));
		list.add(App.menuList.get(CODE.MENU_SEARCH));

	}

	public void setSelection(int position) {
		currentPosition = position;
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		return list.get(position).getId();
	}

	@Override
	public View getMyView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.zy_lfitem, parent, false);
		}

		if (position == currentPosition) {
			convertView.setBackgroundColor(Color.parseColor("#20ffffff"));
		} else {
			convertView.setBackgroundColor(Color.TRANSPARENT);
		}

		TextView textView = (TextView) ViewHolder.get(convertView, R.id.zy_tv);
		ImageView imageView = (ImageView) ViewHolder.get(convertView, R.id.zy_iv);

		Menu_Item_Bean bean = list.get(position);
		textView.setText(bean.getName());
		imageView.setImageResource(bean.getIcon());

		return convertView;

	}


}
