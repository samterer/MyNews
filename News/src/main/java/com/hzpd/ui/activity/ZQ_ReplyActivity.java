package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.ReplayBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.EventUtils;
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

public class ZQ_ReplyActivity extends MBaseActivity {

	@ViewInject(R.id.zq_reply_et_content)
	private EditText zq_reply_et_content;

	private ReplayBean bean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zq_reply_layout);
		ViewUtils.inject(this);

		Intent intent = getIntent();
		if (null != intent) {
			bean = (ReplayBean) intent.getSerializableExtra("replay");
		}

	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("replay", bean);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (null == bean) {
			bean = (ReplayBean) savedInstanceState.getSerializable("replay");
		}
		super.onRestoreInstanceState(savedInstanceState);
	}


	@OnClick({R.id.zq_reply_tv_cancle, R.id.zq_reply_tv_send})
	private void click(View view) {
		switch (view.getId()) {
			case R.id.zq_reply_tv_cancle: {
				finish();
			}
			break;
			case R.id.zq_reply_tv_send: {

				if (null == spu.getUser()) {
					TUtils.toast("请登录");
					Intent intent = new Intent(this, LoginActivity.class);
					startActivity(intent);
					AAnim.ActivityStartAnimation(this);
					return;
				}

				String comment = zq_reply_et_content.getText().toString();
				if (null == comment || "".equals(comment)) {
					TUtils.toast("输入的内容不能为空");
					return;
				}
				sendComment(comment);
			}
			break;

			default:
				break;
		}
	}

	// 发表评论
	private void sendComment(String content) {

		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("title", bean.getTitle());
		params.addBodyParameter("type", bean.getType());//"News"
		params.addBodyParameter("nid", bean.getId());
		params.addBodyParameter("content", content);
		params.addBodyParameter("json_url", bean.getJsonUrl());
		params.addBodyParameter("smallimg", bean.getImgUrl());
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.PUBLISHCOMMENT// InterfaceApi.mSendComment
				, params, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				LogUtils.i("arg1-->" + arg1);
				Log.i("msg", arg1);
				TUtils.toast("服务器未响应！");
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.i("news-comment-->" + arg0.result);
				JSONObject obj = null;
				try {
					obj = JSONObject.parseObject(arg0.result);
				} catch (Exception e) {
					return;
				}

				if (200 == obj.getIntValue("code")) {
					TUtils.toast(obj.getString("msg"));
					EventUtils.sendComment(activity);
					finish();
				} else {
					TUtils.toast("评论失败！");
				}
			}
		});
	}

}
