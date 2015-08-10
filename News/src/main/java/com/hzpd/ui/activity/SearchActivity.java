package com.hzpd.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.ui.fragments.MySearchFragment;

/**
 * Created by liuqing on 2015/8/10.
 */
public class SearchActivity extends SBaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		initTitle();
		initFragment();
	}

	private void initTitle() {
		findViewById(R.id.stitle_ll_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		TextView tvTitle = (TextView) findViewById(R.id.stitle_tv_content);
		tvTitle.setText(R.string.prompt_search);
	}

	private void initFragment() {
		BaseFragment fragment = new MySearchFragment();
		fm.beginTransaction()
				.replace(R.id.search_content, fragment)
				.commitAllowingStateLoss();
	}
}
