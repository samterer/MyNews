package com.hzpd.adapter;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.alibaba.fastjson.JSONArray;
import com.hzpd.custorm.CheckComment_ReplyItem;

public class CheckCommentAdapter extends BaseAdapter {

	private JSONArray array;
	private CheckComment_ReplyItem replay;
	private String mType;

	public CheckCommentAdapter(Activity context, Handler handler, String mType) {
		replay = new CheckComment_ReplyItem(context, handler, mType);
		array = new JSONArray();
		this.mType = mType;
	}

	public void setItems(JSONArray arrays) {
		if (arrays != null) {
			this.array = arrays;
		}
		notifyDataSetChanged();
	}

	public void addItems(JSONArray arrays) {
		if (arrays != null) {
			this.array.add(arrays);
		}
		notifyDataSetChanged();
	}

	//
	@Override
	public int getCount() {
		return array.size();
	}

	@Override
	public Object getItem(int position) {
		return array.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		convertView = replay.getView(array.getJSONObject(position), 1);
		return convertView;
	}


}
