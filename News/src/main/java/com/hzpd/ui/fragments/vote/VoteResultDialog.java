package com.hzpd.ui.fragments.vote;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class VoteResultDialog extends Dialog {
	@ViewInject(R.id.vote_dialog_name)
	private TextView vote_dialog_name;
	@ViewInject(R.id.vote_close)
	private LinearLayout vote_close;
	@ViewInject(R.id.vote_dia_tv1)
	private TextView vote_dia_tv1;
	@ViewInject(R.id.vote_dia_bu2)
	private Button vote_dia_bu2;


	private String content;
	private String butext;
	private IVoteresultClick click;

	public VoteResultDialog(Context context, int theme
			, String content, String buText
			, IVoteresultClick click) {
		super(context, theme);
		this.content = content;
		this.butext = buText;
		this.click = click;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = LayoutInflater.from(getContext())
				.inflate(R.layout.voteresultdialog_layout, null);
		setContentView(view);
		ViewUtils.inject(this, view);
		vote_dia_bu2.setText(butext);
		vote_dia_tv1.setText(content);

	}


	@OnClick(R.id.vote_dia_bu2)
	private void click(View v) {
		this.dismiss();
		if (null != click) {
			click.onclick();
		}
	}

	@OnClick(R.id.vote_close)
	private void close(View v) {
		dismiss();
	}
}


