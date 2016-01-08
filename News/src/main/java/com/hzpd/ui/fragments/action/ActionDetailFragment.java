package com.hzpd.ui.fragments.action;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.hflt.wxapi.FacebookSharedUtil;
import com.hzpd.modle.ActionDetailBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.TUtils;

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


    private void getInfoFromSever() {}

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