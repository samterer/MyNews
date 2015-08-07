package com.hzpd.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Looper;

import com.hzpd.hflt.R;

import java.lang.Thread.UncaughtExceptionHandler;

public class CrashHandler implements UncaughtExceptionHandler {
	public static final String TAG = "CrashHandler";
	private static CrashHandler INSTANCE = new CrashHandler();
	private Context mContext;
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				new AlertDialog.Builder(mContext).setTitle(R.string.title_prompt).setCancelable(false)
						.setMessage(R.string.prompt_reopen_app)
						.setNeutralButton(android.R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								System.exit(0);
							}
						})
						.create().show();
				Looper.loop();
			}
		}.start();
	}

}