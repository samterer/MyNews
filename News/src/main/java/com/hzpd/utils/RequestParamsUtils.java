package com.hzpd.utils;

import com.hzpd.modle.TokenModel;
import com.lidroid.xutils.http.RequestParams;

import java.util.HashMap;
import java.util.Map;

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

    public static Map<String, String> getMapWithU() {
        Map<String, String> map = new HashMap<>();
        if (null != SPUtil.getInstance().getUser()) {
            map.put("uid", SPUtil.getInstance().getUser().getUid());
            map.put("session", SPUtil.getInstance().getUser().getToken());
        }
        TokenModel tm = new TokenModel();
        map.put("signature", tm.getSignature());
        map.put("timestamp", tm.getTimestamp());
        map.put("nonce", tm.getNonce());

        return map;
    }

    public static RequestParams getParams() {
        RequestParams params = new RequestParams();
        TokenModel tm = new TokenModel();
        params.addBodyParameter("signature", tm.getSignature());
        params.addBodyParameter("timestamp", tm.getTimestamp());
        params.addBodyParameter("nonce", tm.getNonce());

        return params;
    }

    public static Map<String, String> getMaps() {
        Map<String, String> map = new HashMap<String, String>();
        TokenModel tm = new TokenModel();
        map.put("signature", tm.getSignature());
        map.put("timestamp", tm.getTimestamp());
        map.put("nonce", tm.getNonce());
        return map;
    }
}
