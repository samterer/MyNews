package com.hzpd.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hzpd.adapter.ZY_LFAdapter;
import com.hzpd.hflt.R;
import com.hzpd.ui.interfaces.I_ChangeFm;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnItemClick;


public class ZY_LeftFragment extends BaseFragment {
	@ViewInject(R.id.zy_lv)
	private ListView listView;
	private ZY_LFAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.zy_leftfragment, container, false);
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new ZY_LFAdapter(getActivity());
		listView.setAdapter(adapter);
	}

	@OnItemClick(R.id.zy_lv)
	private void onItemclick(AdapterView<?> parent, View view,
	                         int position, long id) {
		adapter.setSelection(position);
		((I_ChangeFm) getActivity()).changeFm((int) id);
	}

}