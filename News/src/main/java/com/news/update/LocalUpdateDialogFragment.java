package com.news.update;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hzpd.hflt.R;
import com.hzpd.ui.widget.MyProgressBar;
import com.hzpd.utils.SPUtil;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * 本地更新,提示对话框
 */
public class LocalUpdateDialogFragment extends DialogFragment implements View.OnClickListener {


    public static final String DIALOG_CONTENT = "dialogContent";
    public static final String FORCING = "forcing";
    public static final String AUTO_DOWNLOAD = "autoDownload";
    public static final String TAG = LocalUpdateDialogFragment.class
            .getSimpleName();

    boolean forcing = false; // 是否强制更新

    public LocalUpdateDialogFragment() {

    }

    public static boolean shown = false;

    @Override
    public void show(FragmentManager manager, String tag) {
        shown = true;
        super.show(manager, tag);
    }

    @Override
    public void onResume() {
        super.onResume();
        shown = true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        shown = false;
        super.onDismiss(dialog);
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        createContentView(dialog);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void configDialogSize(Dialog dialog) {
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);

        int orientation = getResources().getConfiguration().orientation;
        DisplayMetrics screenMetrics = new DisplayMetrics();
        dialogWindow.getWindowManager().getDefaultDisplay()
                .getMetrics(screenMetrics);
        float proportion = 5f / 6f;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏状态下以高度为基准
            lp.height = (int) (screenMetrics.heightPixels * proportion);
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        } else {
            // 竖屏状态下以宽度为基准
            lp.width = (int) (screenMetrics.widthPixels * proportion);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        dialogWindow.setAttributes(lp);

    }

    View dialogView;

    private void createContentView(Dialog dialog) {
        forcing = getArguments().getBoolean(FORCING);
        dialogView = LayoutInflater.from(getActivity()).inflate(
                R.layout.local_dialog_app_update,
                (ViewGroup) dialog.getWindow().getDecorView(), false);
        configDialogSize(dialog);
        //TODO 设置内容
        String content = getArguments().getString(DIALOG_CONTENT);
        ((TextView) dialogView.findViewById(R.id.dialog_app_update_content_text)).setText("" + content);
        dialogView.findViewById(R.id.dialog_app_update_btn_close).setOnClickListener(this);
        dialogView.findViewById(R.id.dialog_app_update_btn).setOnClickListener(this);
        dialogView.findViewById(R.id.dialog_app_cancel_btn).setOnClickListener(this);
        dialogView.findViewById(R.id.dialog_app_install).setOnClickListener(this);

        checkStatus(dialogView);
        dialog.setContentView(dialogView);
    }

    private MyProgressBar progressBar;
    private TextView textView;

    private void checkStatus(View dialogView) {
        try {
            dialogView.findViewById(R.id.progress_container).setVisibility(View.GONE);
            if (!UpdateUtils.isRomVersion(getActivity())) {
                dialogView.findViewById(R.id.local_auto_download).setVisibility(View.GONE);
                if (forcing) {
                    dialogView.findViewById(R.id.layout__app_cancel).setVisibility(View.GONE);
                }
                return;
            }

            SharedPreferences prefs = getActivity().getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
            if (getArguments().getBoolean(AUTO_DOWNLOAD)) {
                dialogView.findViewById(R.id.local_update).setVisibility(View.GONE);
                dialogView.findViewById(R.id.local_auto_download).setVisibility(View.VISIBLE);
            }
            if (forcing) {
                dialogView.findViewById(R.id.layout__app_cancel).setVisibility(View.GONE);

                if (DownloadService.progress < 100 && prefs.getBoolean(UpdateUtils.KEY.IS_DOWNLOADING, false) || prefs.getBoolean(UpdateUtils.KEY
                        .IS_WIFI_DOWNLOADING, false)) {
                    dialogView.findViewById(R.id.local_update).setVisibility(View.GONE);
                    dialogView.findViewById(R.id.local_auto_download).setVisibility(View.GONE);
                    if (getArguments().getBoolean(AUTO_DOWNLOAD)) {

                        dialogView.findViewById(R.id.local_update).setVisibility(View.GONE);
                        dialogView.findViewById(R.id.local_auto_download).setVisibility(View.GONE);

                        dialogView.findViewById(R.id.progress_container).setVisibility(View.VISIBLE);
                        progressBar = (MyProgressBar) dialogView.findViewById(R.id.progress_indicator);
                        textView = (TextView) dialogView.findViewById(R.id.progress_text);
                        textView.setText(DownloadService.progress + "%");
                        progressBar.setProgress(DownloadService.progress);
                        try {
                            EventBus.getDefault().register(this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEventMainThread(ProgressUpdateEvent event) {
        try {
            textView.setText(event.progress + "%");
            progressBar.setProgress(event.progress);
            if (event.progress >= 100) {
                checkStatus(dialogView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (forcing) {
            getActivity().finish();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.dialog_app_update_btn_close:
                    if (!forcing) {
                        dismiss();
                    }
                    break;
                case R.id.dialog_app_update_btn:
                    try {
                        getActivity().getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putLong(UpdateUtils.KEY.UPDATE_LATER_TIME, 0L)
                                .apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (UpdateUtils.isRomVersion(getActivity())) {
                            Intent intent = new Intent(getActivity(), DownloadService.class);
                            intent.putExtra(DownloadService.TAG, UpdateUtils.KEY.IS_DOWNLOADING);
                            getActivity().startService(intent);
                            Toast.makeText(getActivity(), R.string.local_update_ticker, Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = SPUtil.getIntent(getActivity());
                            startActivity(intent);
                            NotificationManager nm = (NotificationManager) getActivity()
                                    .getSystemService(Context.NOTIFICATION_SERVICE);
                            nm.cancel(UpdateUtils.REQUEST_CODE);
                        }
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    }
                    dismiss();
                    break;
                case R.id.dialog_app_install:
                    try {
                        getActivity().getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putLong(UpdateUtils.KEY.UPDATE_LATER_TIME, 0L)
                                .apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    File root = Environment.getExternalStorageDirectory();
                    if (root != null) {
                        File fold = new File(root, UpdateUtils.PATH_SAVE);
                        File target = new File(fold, UpdateUtils.getFileName(getActivity()));
                        if (target.exists()) {
                            UpdateUtils.installApk(getActivity(), target, getActivity().getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME,
                                    Context.MODE_PRIVATE).getBoolean(UpdateUtils.KEY.KEY_AUTO_INSTALL, false));
                        }
                    }
                    dismiss();
                    break;
                case R.id.dialog_app_cancel_btn:

                    if (!forcing) {
                        dismiss();
                    }
                    try {
                        getActivity().getSharedPreferences(UpdateUtils.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putLong(UpdateUtils.KEY.UPDATE_LATER_TIME, System.currentTimeMillis())
                                .apply();
                        NotificationManager nm = (NotificationManager) getActivity()
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.cancel(UpdateUtils.REQUEST_CODE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
