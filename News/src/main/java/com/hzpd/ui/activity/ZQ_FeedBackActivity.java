package com.hzpd.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class ZQ_FeedBackActivity extends MBaseActivity {
	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;

	@ViewInject(R.id.zq_feedback_et_content)
	private EditText zq_feedback_et_content;
	@ViewInject(R.id.zq_feedback_et_email)
	private EditText zq_feedback_et_email;

	private long start;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zq_feedback_layout);
		ViewUtils.inject(this);

		stitle_tv_content.setText("意见反馈");

	}

	@OnClick(R.id.zq_feedback_btn_submit)
	private void submit(View view) {
		if (start > 0) {
			if (System.currentTimeMillis() - start < 2000) {
				return;
			}
		}

		String content = zq_feedback_et_content.getText().toString();
		String email = zq_feedback_et_email.getText().toString();

		submit(content, email);
		start = System.currentTimeMillis();
	}


	private void submit(String content, String email) {
		if (TextUtils.isEmpty(content)) {
			TUtils.toast("反馈内容不能为空");
			return;
		}
		if (TextUtils.isEmpty(email)) {
			TUtils.toast("Email不能为空");
			return;
		}

		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("Email", email);
		params.addBodyParameter("content", content);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.feedback
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String json = responseInfo.result;
				LogUtils.i("loginSubmit-->" + json);

				JSONObject obj = FjsonUtil
						.parseObject(responseInfo.result);
				if (null != obj) {
					if (200 == obj.getIntValue("code")) {
						TUtils.toast(obj.getString("msg"));
						zq_feedback_et_content.setText("");
						zq_feedback_et_email.setText("");
					} else {
						TUtils.toast(obj.getString("msg"));
					}
				} else {
					TUtils.toast("服务器错误");
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});

	}


	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		finish();
	}
}
