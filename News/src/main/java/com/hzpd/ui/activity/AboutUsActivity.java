package com.hzpd.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.utils.SystemBarTintManager;

public class AboutUsActivity extends MBaseActivity {

	private TextView stitle_tv_content;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutus_layout);
		stitle_tv_content = (TextView) findViewById(R.id.stitle_tv_content);
		stitle_tv_content.setText(R.string.title_about_us);
		super.changeStatusBar();
	}

	@TargetApi(19)
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}


}