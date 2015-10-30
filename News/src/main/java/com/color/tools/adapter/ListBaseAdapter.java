
package com.color.tools.adapter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ListBaseAdapter<T> extends BaseAdapter {
	protected Activity context;
	protected LayoutInflater inflater;
	protected List<T> myList = null;
	
	
	public ListBaseAdapter(Activity context) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		myList = new ArrayList<T>();
	}


	public List<T> getAdapterData() {
		return myList;
	}

	// 封装加数据的方法--1条 多条
	public void appendData(T t, boolean isClearOld) {
		if (t == null)
			return;
		if (isClearOld)
			myList.clear();
		myList.add(t);
	}

	public void appendData(List<T> data, boolean isClearOld) {
		if (data == null)
			return;
		if (isClearOld)
			myList.clear();
		myList.addAll(data);
	}

	// 封装加数据的方法--1条 多条 在顶部添加
	public void appendDataTop(T t, boolean isClearOld) {
		if (t == null)
			return;
		if (isClearOld)
			myList.clear();
		myList.add(0, t);
	}

	public void appendDataTop(List<T> data, boolean isClearOld) {
		if (data == null)
			return;
		if (isClearOld)
			myList.clear();
		myList.addAll(0, data);
	}

	public void update() {
		this.notifyDataSetChanged();
	}

	public void clear() {
		myList.clear();
	}

	public int getCount() {
		if (myList == null)
			return 0;
		return myList.size();
	}

	public T getItem(int position) {
		if (myList == null)
			return null;
		if (position > myList.size() - 1)
			return null;
		return myList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		return getMyView(position, convertView, parent);
	}

	public abstract View getMyView(int position, View convertView,
			ViewGroup parent);

}
