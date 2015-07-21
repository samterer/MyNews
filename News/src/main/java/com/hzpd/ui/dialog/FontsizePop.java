package com.hzpd.ui.dialog;

import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.utils.CODE;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class FontsizePop extends PopupWindow {

	@ViewInject(R.id.nd_tv_big)
	private TextView nd_tv_big;
	@ViewInject(R.id.nd_tv_mid)
	private TextView nd_tv_mid;
	@ViewInject(R.id.nd_tv_small)
	private TextView nd_tv_small;

	private Handler handler;
	private View view;

	public FontsizePop(View view, Handler h) {
		super(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new BitmapDrawable());
		ViewUtils.inject(this, view);
		this.handler = h;
		setOutsideTouchable(true);

	}

	@OnClick({
			R.id.nd_tv_big,
			R.id.nd_tv_mid,
			R.id.nd_tv_small
	})
	private void onclick(View v) {
		this.dismiss();
		switch (v.getId()) {
			case R.id.nd_tv_big: {
				handler.sendEmptyMessage(CODE.font_big);
			}
			break;
			case R.id.nd_tv_mid: {
				handler.sendEmptyMessage(CODE.font_mid);
			}
			break;
			case R.id.nd_tv_small: {
				handler.sendEmptyMessage(CODE.font_small);
			}
			break;

		}


	}


}
