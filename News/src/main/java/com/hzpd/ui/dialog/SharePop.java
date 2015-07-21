package com.hzpd.ui.dialog;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;

import com.hzpd.hflt.R;
import com.hzpd.utils.CODE;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class SharePop extends PopupWindow {


	@ViewInject(R.id.pop_share_ib_sina)
	private ImageButton pop_share_ib_sina;
	@ViewInject(R.id.pop_share_ib_weixin)
	private ImageButton pop_share_ib_weixin;
	@ViewInject(R.id.pop_share_ib_pengyouquan)
	private ImageButton pop_share_ib_pengyouquan;

	private Handler handler;

	public SharePop(Activity a, Handler h) {
		View view = a.getLayoutInflater().inflate(R.layout.nd_popshare, null);
		setContentView(view);
		setWindowLayoutMode(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(new BitmapDrawable());
		ViewUtils.inject(this, view);
		this.handler = h;
		setOutsideTouchable(true);

	}

	@OnClick({
			R.id.pop_share_ib_sina,
			R.id.pop_share_ib_weixin,
			R.id.pop_share_ib_pengyouquan
	})
	private void onclick(View v) {
		this.dismiss();
		switch (v.getId()) {
			case R.id.pop_share_ib_sina: {
				handler.sendEmptyMessage(CODE.share_sina);
			}
			break;
			case R.id.pop_share_ib_weixin: {
				handler.sendEmptyMessage(CODE.share_weixin);
			}
			break;
			case R.id.pop_share_ib_pengyouquan: {
				handler.sendEmptyMessage(CODE.share_pengyouquan);
			}
			break;
		}
	}


}
