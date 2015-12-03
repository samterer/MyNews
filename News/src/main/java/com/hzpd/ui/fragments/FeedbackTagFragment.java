package com.hzpd.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.ui.App;
import com.hzpd.ui.widget.ChoiceRateView;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.common.lib.analytics.AnalyticBaseDialogFragment;

/**
 * 用户反馈，带标签
 */
public class FeedbackTagFragment extends AnalyticBaseDialogFragment implements View.OnClickListener {

    public static final String TAG = FeedbackTagFragment.class.getSimpleName();
    public static final int COUNT_COLUMN = 50;
    private static boolean sending = false;
    private static boolean success = false;

    public FeedbackTagFragment() {
        success = false;
    }

    public static boolean shown = false;

    @Override
    public void show(FragmentManager manager, String tag) {
        sending = false;
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
        dialog.setCanceledOnTouchOutside(true);
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

        DisplayMetrics screenMetrics = new DisplayMetrics();
        dialogWindow.getWindowManager().getDefaultDisplay()
                .getMetrics(screenMetrics);
        // 竖屏状态下以宽度为基准
        lp.width = (int) (screenMetrics.widthPixels * 6f / 7f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
    }

    View dialogView;

    private void createContentView(Dialog dialog) {
        try {
            dialogView = LayoutInflater.from(getActivity()).inflate(
                    R.layout.feedback_tag_layout,
                    (ViewGroup) dialog.getWindow().getDecorView(), false);
            configDialogSize(dialog);
            // 设置内容
            dialogView.findViewById(R.id.feedback_send).setOnClickListener(this);
            dialogView.findViewById(R.id.dialog_btn_close).setOnClickListener(this);
            rateView = (ChoiceRateView) dialogView.findViewById(R.id.feedback_rate);
            editText = (EditText) dialogView.findViewById(R.id.feedback_comment);
            editText.setOnClickListener(this);
            dialog.setContentView(dialogView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ChoiceRateView rateView;
    private EditText editText;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.dialog_btn_close:
                    dismiss();
                    break;
                case R.id.feedback_send:
                    sendFeedback(getActivity().getApplicationContext());
                    break;
                case R.id.feedback_comment:
                    editText.setHint("");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFeedback(Context context) {
        try {
            if (sending) {
                return;
            }
            success = false;
            String feedback = "" + editText.getText();
            int rate = rateView.getScore();
            if (TextUtils.isEmpty(feedback) || feedback.length() < 5) {
                TUtils.toast(getString(R.string.toast_feedback_cannot_be_short));//太短
                return;
            }
            dismiss();
            //TODO 发送反馈
            submit(feedback, rate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submit(String content, int rate) {
        sending = true;
        String feedback_url = InterfaceJsonfile.feedback;
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", "1");
        params.addBodyParameter("rate", "" + rate);
        params.addBodyParameter("content", content);

        HttpUtils httpUtils = SPUtil.getHttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.POST
                , feedback_url
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                try {
                    String json = responseInfo.result;
                    JSONObject obj = FjsonUtil
                            .parseObject(json);
                    if (null != obj) {
                        if (200 == obj.getIntValue("code")) {
                            TUtils.toast(App.getInstance().getString(R.string.feed_ok));
                        } else {
                            TUtils.toast(App.getInstance().getString(R.string.feed_fail));
                        }
                    } else {
                        TUtils.toast(App.getInstance().getString(R.string.toast_server_error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sending = false;
                success = true;
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                sending = false;
                TUtils.toast(getString(R.string.toast_server_error));
            }
        });

    }

    @Override
    public String getAnalyticPageName() {
        return null;
    }
}
