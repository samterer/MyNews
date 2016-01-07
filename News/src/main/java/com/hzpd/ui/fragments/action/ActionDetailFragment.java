package com.hzpd.ui.fragments.action;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.color.tools.mytools.LogUtils;
import com.hzpd.hflt.R;
import com.hzpd.hflt.wxapi.FacebookSharedUtil;
import com.hzpd.modle.ActionDetailBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.CODE;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.Request;

import java.util.Map;

public class ActionDetailFragment extends BaseFragment implements View.OnClickListener {

    private TextView actiondetail_title_tv;
    private TextView actiondetail_time_tv;
    private WebView actiondetail_content_wv;
    private ImageView actiondetail_content_iv;
    private TextView actiondetail_tv_register;
    private TextView actiondetail_tv_vote;
    private TextView actiondetail_tv_leto;
    private TextView actiondetail_tv_share;

    private String id;
    private ActionDetailBean adb;
    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.actiondetail_fm_layout, container, false);
        initViews(view);
        tag= OkHttpClientManager.getTag();
        return view;
    }

    private void initViews(View view) {
        actiondetail_title_tv= (TextView) view.findViewById(R.id.actiondetail_title_tv);
        actiondetail_time_tv= (TextView) view.findViewById(R.id.actiondetail_time_tv);
        actiondetail_content_wv= (WebView) view.findViewById(R.id.actiondetail_content_wv);
        actiondetail_content_iv= (ImageView) view.findViewById(R.id.actiondetail_content_iv);
        actiondetail_tv_register= (TextView) view.findViewById(R.id.actiondetail_tv_register);
        actiondetail_tv_register.setOnClickListener(this);
        actiondetail_tv_vote= (TextView) view.findViewById(R.id.actiondetail_tv_vote);
        actiondetail_tv_vote.setOnClickListener(this);
        actiondetail_tv_leto= (TextView) view.findViewById(R.id.actiondetail_tv_leto);
        actiondetail_tv_leto.setOnClickListener(this);
        actiondetail_tv_share= (TextView) view.findViewById(R.id.actiondetail_tv_share);
        actiondetail_tv_share.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (null == args) {
            return;
        }
        id = args.getString("id");
        getInfoFromSever();
    }


    private void getInfoFromSever() {
        Map<String,String> params = RequestParamsUtils.getMaps();
        params.put("id", id);
        OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.actionDetail, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                LogUtils.i("action list result-->" + response.toString());
                JSONObject obj = FjsonUtil.parseObject(response.toString());
                if (null == obj) {
                    return;
                }

                if (200 == obj.getIntValue("code")) {

                    actiondetail_tv_share.setClickable(true);
                    actiondetail_tv_share.setBackgroundColor(getResources().getColor(R.color.toolbar_bg));

                    adb = JSONObject.parseObject(obj.getString("data"), ActionDetailBean.class);
                    actiondetail_title_tv.setText(adb.getTitle());
                    actiondetail_time_tv.setText(getString(R.string.prompt_activity_period, adb.getStarttime(), adb.getDeadline()));

                    String data = adb.getContent();

                    if (!TextUtils.isEmpty(data)) {
                        switch (spu.getTextSize()) {
                            case CODE.textSize_small:
                                data = data.replaceAll("<p>",
                                        "<p style=\"color:#444;font-size:16px;text-indent: 0.8em;line-height: 1.45em;margin-bottom: 0.5em;" +
                                                "letter-spacing:0.05em\">");
                                break;
                            case CODE.textSize_normal:
                                data = data.replaceAll("<p>",
                                        "<p style=\"color:#444;font-size:20px;text-indent: 0.8em;line-height: 1.45em;margin-bottom: 0.5em;" +
                                                "letter-spacing:0.05em\">");
                                break;
                            case CODE.textSize_big:
                                data = data.replaceAll("<p>",
                                        "<p style=\"color:#444;font-size:28px;text-indent: 0.8em;line-height: 1.45em;margin-bottom: 0.5em;" +
                                                "letter-spacing:0.05em\">");
                                break;
                        }
                        actiondetail_content_wv.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
                    }


                    mImageLoader.displayImage(adb.getHeadpic(), actiondetail_content_iv
                            , DisplayOptionFactory.getOption(OptionTp.Small), new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            float rat = loadedImage.getHeight() * 1.0f / loadedImage.getWidth();
                            LayoutParams params = actiondetail_content_iv.getLayoutParams();
                            params.height =
                                    (int) ((MyCommonUtil.getDisplayMetric(activity.getResources())
                                            .widthPixels - MyCommonUtil.dp2px(activity.getResources(), 20)) * rat);
                            actiondetail_content_iv.setLayoutParams(params);
                            actiondetail_content_iv.setImageBitmap(loadedImage);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                        }
                    });

                    if ("1".equals(adb.getVoteable())) {
                        actiondetail_tv_vote.setBackgroundColor(getResources().getColor(R.color.toolbar_bg));
                        actiondetail_tv_vote.setClickable(true);
                    }
                    if ("1".equals(adb.getRollable())) {
                        actiondetail_tv_leto.setBackgroundColor(getResources().getColor(R.color.toolbar_bg));
                        actiondetail_tv_leto.setClickable(true);
                    }
                    if ("1".equals(adb.getRegable())) {
                        actiondetail_tv_register.setBackgroundColor(getResources().getColor(R.color.toolbar_bg));
                        actiondetail_tv_register.setClickable(true);
                    }

                } else {
                    TUtils.toast(obj.getString("msg"));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                Log.i("test","action list failed");
            }
        }, params);
    }

    @Override
    public void onClick(View v) {
        if (null == adb) {
            return;
        }
        switch (v.getId()) {
            case R.id.actiondetail_tv_register: {
                if (!"1".equals(adb.getRegable())) {
                    TUtils.toast(getString(R.string.toast_activity_not_started));
                    return;
                }
                ((ActionDetailActivity) activity).toRegister(id);
            }
            break;
            case R.id.actiondetail_tv_vote: {
                if (!"1".equals(adb.getVoteable())) {
                    TUtils.toast(getString(R.string.toast_activity_not_started));
                    return;
                }
                ((ActionDetailActivity) activity).toVote(adb.getSubjectid());
            }
            break;
            case R.id.actiondetail_tv_leto: {
                if (!"1".equals(adb.getRollable())) {
                    TUtils.toast(getString(R.string.toast_activity_not_started));
                    return;
                }
                if (null == spu.getUser()) {
                    return;
                }
                ((ActionDetailActivity) activity).toLottery(adb.getSubjectid());
            }
            break;
            case R.id.actiondetail_tv_share: {
                if (null == adb) {
                    return;
                }
                FacebookSharedUtil.showShares(adb.getTitle()
                        , adb.getUrl(), adb.getHeadpic(), activity);

            }
            break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        OkHttpClientManager.cancel(tag);
        super.onDestroyView();
    }
}