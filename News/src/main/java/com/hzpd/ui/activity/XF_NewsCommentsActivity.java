package com.hzpd.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.ReplayBean;
import com.hzpd.ui.dialog.CommentFragment;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class XF_NewsCommentsActivity extends MBaseActivity {

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;

	private CommentFragment commentFm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xf_newscomment_layout);
		ViewUtils.inject(this);
		stitle_tv_content.setText("评论");
		Bundle bundle = getIntent().getExtras();
		ReplayBean bean = (ReplayBean) bundle.getSerializable("reply");
		Bundle args = new Bundle();
		args.putSerializable("reply", bean);
		commentFm = new CommentFragment();
		commentFm.setArguments(args);

		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.xf_newscomment_fm, commentFm);
		ft.commit();

	}


	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		finish();
	}


}
