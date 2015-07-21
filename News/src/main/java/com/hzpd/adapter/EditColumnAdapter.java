package com.hzpd.adapter;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.NewsChannelBean;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditColumnAdapter extends MyBaseAdater {

	private ArrayList<NewsChannelBean> selectedlist;
	private ArrayList<NewsChannelBean> unselectedlist;

	public EditColumnAdapter(Activity c) {
		super(c);
		selectedlist = new ArrayList<NewsChannelBean>();
		unselectedlist = new ArrayList<NewsChannelBean>();
	}

	public void setData(List<NewsChannelBean> mselectedlist
			, List<NewsChannelBean> all) {

		for (NewsChannelBean ncb : mselectedlist) {
			for (int i = 0; i < all.size(); i++) {
				NewsChannelBean ncball = all.get(i);
				if (ncb.getTid().equals(ncball.getTid())) {
					all.remove(i);
				}
			}
		}
		selectedlist.clear();
		unselectedlist.clear();
		selectedlist.addAll(mselectedlist);
		unselectedlist.addAll(all);
		Collections.sort(selectedlist);
		Collections.sort(unselectedlist);
		notifyDataSetChanged();

	}

	public ArrayList<NewsChannelBean> getSelectedList() {
		return selectedlist;
	}

	@Override
	public int getCount() {
		if (unselectedlist.size() < 1) {
			return selectedlist.size();
		} else {
			return selectedlist.size() + unselectedlist.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (position < selectedlist.size()) {
			return selectedlist.get(position);
		} else {
			return unselectedlist.get(position - selectedlist.size());
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (null == convertView) {
			convertView = mLayoutInflater.inflate(R.layout.editcolumn_iteml, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		NewsChannelBean ncb = null;
		if (position < selectedlist.size()) {
			ncb = selectedlist.get(position);
			holder.ib.setImageResource(R.drawable.zqzx_ec_minus);
		} else {
			ncb = unselectedlist.get(position - selectedlist.size());
			holder.ib.setImageResource(R.drawable.zqzx_ec_add);
		}
		holder.tv1.setVisibility(View.GONE);
		if (position == selectedlist.size()) {
			holder.tv1.setVisibility(View.VISIBLE);
		}

		holder.tv.setText(ncb.getCnname());
		holder.ib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (position < selectedlist.size()) {
					NewsChannelBean ncbs = selectedlist.get(position);
					unselectedlist.add(ncbs);
					Collections.sort(unselectedlist);
					selectedlist.remove(position);
					notifyDataSetChanged();
				} else {
					NewsChannelBean ncbs = unselectedlist.get(position - selectedlist.size());
					unselectedlist.remove(position - selectedlist.size());
					selectedlist.add(ncbs);
					Collections.sort(selectedlist);
					notifyDataSetChanged();
				}
			}
		});

		return convertView;
	}

	private static class ViewHolder {
		@ViewInject(R.id.editcolumn_title_tv)
		TextView tv;
		@ViewInject(R.id.editcolumn_title_tv1)
		TextView tv1;
		@ViewInject(R.id.editcolumn_title_ib)
		ImageView ib;

		public ViewHolder(View view) {
			ViewUtils.inject(this, view);
		}
	}
}
