package com.hzpd.ui.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.hzpd.hflt.R;

public class AboutUsActivity extends MBaseActivity {

	private TextView stitle_tv_content;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutus_layout);
		stitle_tv_content = (TextView) findViewById(R.id.stitle_tv_content);
		stitle_tv_content.setText(R.string.title_about_us);
	}

}