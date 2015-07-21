package com.hzpd.custorm;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;


public class CustomProgressDialog extends Dialog {

	private static CustomProgressDialog customProgressDialog = null;

	public CustomProgressDialog(Context context) {
		super(context);
	}

	public CustomProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	public static CustomProgressDialog createDialog(Context context, boolean isCancle) {
		customProgressDialog = new CustomProgressDialog(context, R.style.progressDialog);
//		customProgressDialog.setContentView(R.layout.load_progress);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.load_progress, null);
		customProgressDialog.setContentView(view);

		customProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		if (isCancle) {
			customProgressDialog.setCanceledOnTouchOutside(false);
		}
		return customProgressDialog;
	}

	public void onWindowFocusChanged(boolean hasFocus) {

		if (customProgressDialog == null) {
			return;
		}

		ImageView imageView = (ImageView) customProgressDialog.findViewById(R.id.loadingImageView);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
		animationDrawable.start();
	}

	public CustomProgressDialog setTitile(String strTitle) {
		return customProgressDialog;
	}

	public CustomProgressDialog setMessage(String strMessage) {
		TextView tvMsg = (TextView) customProgressDialog.findViewById(R.id.id_tv_loadingmsg);

		if (tvMsg != null) {
			tvMsg.setText(strMessage);
		}

		return customProgressDialog;
	}
}
