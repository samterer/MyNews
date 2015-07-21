package com.hzpd.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hzpd.custorm.smoothimg.SpaceImageDetailActivity;
import com.hzpd.hflt.R;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.ViewHolder;
import com.lidroid.xutils.util.LogUtils;

import de.greenrobot.event.EventBus;

public class ZY_Tsbl_blAdapter extends ListBaseAdapter<String> {

	private int maxSize = 9;

	public ZY_Tsbl_blAdapter(Activity context) {
		super(context);
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	@Override
	public int getCount() {
		if (list.size() < maxSize) {
			return list.size() + 1;
		}
		return super.getCount();
	}

	@Override
	public View getMyView(int position, View convertView, ViewGroup parent) {
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.zy_tsbl_bl_item, parent, false);
		}

		ImageView iv = ViewHolder.get(convertView, R.id.zy_tsbl_bl_item_iv);

		if (position < list.size() || maxSize == list.size()) {
			final String imgUri = "file://" + list.get(position);

			mImageLoader.displayImage(imgUri, iv, DisplayOptionFactory.getOption(OptionTp.Small));

			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, SpaceImageDetailActivity.class);
					LogUtils.i("urlPath-->" + imgUri);
					intent.putExtra("type", "url");
					intent.putExtra("images", imgUri);
					int[] location = new int[2];
					v.getLocationOnScreen(location);
					intent.putExtra("locationX", location[0]);
					intent.putExtra("locationY", location[1]);
					intent.putExtra("width", v.getWidth());
					intent.putExtra("height", v.getHeight());
					EventBus.getDefault().post(intent);
				}
			});
		} else {

			mImageLoader.displayImage("drawable://" + R.drawable.img_icon_addpic_unfocused, iv, DisplayOptionFactory.getOption(OptionTp.Small));
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					EventBus.getDefault().post(Integer.valueOf(R.id.zy_tsbl_bl_item_iv));
				}
			});
		}

		return convertView;
	}

}
