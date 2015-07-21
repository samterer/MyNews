package com.hzpd.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.SearchBean;

import java.util.ArrayList;
import java.util.List;

public class SearchListViewAdapter extends BaseAdapter {

	private Context mContext;

	private List<SearchBean> mList = new ArrayList<SearchBean>();

	public SearchListViewAdapter(Context c) {
		this.mContext = c;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	public void setData(List<SearchBean> list) {
		mList = list;
		notifyDataSetChanged();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {

		view = LayoutInflater.from(mContext).inflate(
				R.layout.search_item_layout, arg2, false);
		TextView content =
				(TextView) view.findViewById(R.id.search_other_content_id);
		TextView time = (TextView) view.findViewById(R.id.search_other_time_id);
		content.setText(mList.get(position).getTitle());

		time.setText(mList.get(position).getTime());

		return view;
	}

}
