package com.hzpd.utils;

import com.hzpd.modle.TokenModel;
import com.lidroid.xutils.http.RequestParams;

public class RequestParamsUtils {

	public static RequestParams getParamsWithU() {
		RequestParams params = new RequestParams();
		if (null != SPUtil.getInstance().getUser()) {
			params.addBodyParameter("uid", SPUtil.getInstance().getUser().getUid());
			params.addBodyParameter("session", SPUtil.getInstance().getUser().getToken());
		}
		TokenModel tm = new TokenModel();
		params.addBodyParameter("signature", tm.getSignature());
		params.addBodyParameter("timestamp", tm.getTimestamp());
		params.addBodyParameter("nonce", tm.getNonce());

		return params;
	}

	public static RequestParams getParams() {
		RequestParams params = new RequestParams();
		TokenModel tm = new TokenModel();
		params.addBodyParameter("signature", tm.getSignature());
		params.addBodyParameter("timestamp", tm.getTimestamp());
		params.addBodyParameter("nonce", tm.getNonce());

		return params;
	}
}
