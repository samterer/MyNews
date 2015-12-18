package com.hzpd.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.adapter.XF_UserCommentsAdapter;
import com.hzpd.custorm.CircleImageView;
import com.hzpd.hflt.R;
import com.hzpd.modle.XF_UserCommentsBean;
import com.hzpd.modle.XF_UserInfoBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
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

import java.util.List;


public class XF_PersonalInfoFragment extends BaseFragment {

    private CircleImageView xf_pinfo_iv_avatar;//头像
    private TextView xf_pinfo_tv_nickname;//昵称
    private ImageView xf_pinfo_iv_gender;//性别
    private TextView xf_pinfo_tv_level_alias;//级别
    private TextView xf_pinfo_tv_level;//级别
    private TextView xf_pinfo_tv_score;//分数
    private NumberProgressBar xf_pinfo_npb;//进度条
    private TextView xf_pinfo_tv_regtime;//注册时间
    private TextView xf_pinfo_tv_levelup;//升级提示

    @ViewInject(R.id.xf_pinfo_lv)
    private PullToRefreshListView xf_pinfo_lv;//回复列表

    private XF_UserCommentsAdapter adapter;
    private String uid;
    private XF_UserInfoBean userInfoBean;

    private int page = 1;
    private static final int pagesize = 15;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xf_personalinfofm_layout, container, false);
        ViewUtils.inject(this, view);

        LayoutInflater infla = LayoutInflater.from(activity);
        View headView = infla.inflate(R.layout.xf_personal_headview, null);
        headView.findViewById(R.id.xf_pinfo_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });

        xf_pinfo_iv_avatar = (CircleImageView) headView.findViewById(R.id.xf_pinfo_iv_avatar);
        xf_pinfo_tv_nickname = (TextView) headView.findViewById(R.id.xf_pinfo_tv_nickname);
        xf_pinfo_iv_gender = (ImageView) headView.findViewById(R.id.xf_pinfo_iv_gender);
        xf_pinfo_tv_level_alias = (TextView) headView.findViewById(R.id.xf_pinfo_tv_level_alias);
        xf_pinfo_tv_level = (TextView) headView.findViewById(R.id.xf_pinfo_tv_level);
        xf_pinfo_tv_score = (TextView) headView.findViewById(R.id.xf_pinfo_tv_score);
        xf_pinfo_npb = (NumberProgressBar) headView.findViewById(R.id.xf_pinfo_npb);
        xf_pinfo_tv_regtime = (TextView) headView.findViewById(R.id.xf_pinfo_tv_regtime);
        xf_pinfo_tv_levelup = (TextView) headView.findViewById(R.id.xf_pinfo_tv_levelup);
        ListView lv = xf_pinfo_lv.getRefreshableView();
        lv.addHeaderView(headView);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    protected void initData() {
        Bundle bundle = getArguments();
        uid = bundle.getString("uid");
        if (TextUtils.isEmpty(uid)) {
            return;
        }
        adapter = new XF_UserCommentsAdapter(activity);
        xf_pinfo_lv.setAdapter(adapter);
        xf_pinfo_lv.setMode(Mode.PULL_FROM_END);
        xf_pinfo_lv.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                page++;
                getCommentsFromServer();
            }
        });

        getUserInfoFromServer();
        getCommentsFromServer();

    }

    private void getUserInfoFromServer() {
        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", uid);
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.XF_USERINFO
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!isAdded()) {
                    return;
                }
                String json = responseInfo.result;
                Log.i("getUserInfoFromServer", json.toString());

                JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj) {
                    if (200 == obj.getIntValue("code")) {
                        userInfoBean = FjsonUtil.parseObject(obj.getString("data")
                                , XF_UserInfoBean.class);
                        setUserInfo();
                    } else {
//                        TUtils.toast(obj.getString("msg"));
                    }
                } else {
//                    TUtils.toast("服务器错误");
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

    private void setUserInfo() {
        if (null == userInfoBean) {
            return;
        }
        int progress = 0;
        int maxProgress = 0;
        String sprogress = userInfoBean.getExp();
        String slastProgress = userInfoBean.getLastexp();
        if (!TextUtils.isEmpty(sprogress)
                && TextUtils.isDigitsOnly(sprogress)
                && TextUtils.isDigitsOnly(slastProgress)) {
            try {
                progress = Integer.parseInt(sprogress);
                int lastProgress = Integer.parseInt(slastProgress);
                if (-1 == lastProgress) {
                    maxProgress = progress;
                } else {
                    maxProgress = lastProgress + progress;
                }

            } catch (Exception e) {
            }
        }

        xf_pinfo_npb.setMax(maxProgress);
        xf_pinfo_npb.setProgress(progress);

        Log.e("test", userInfoBean.getAvatar_path());
        SPUtil.displayImage(userInfoBean.getAvatar_path()
                , xf_pinfo_iv_avatar
                , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Avatar));

        xf_pinfo_tv_nickname.setText(userInfoBean.getNickname());
        xf_pinfo_tv_level_alias.setText("" + userInfoBean.getAlias());
        xf_pinfo_tv_regtime.setText(getString(R.string.sgin_time) + userInfoBean.getRegtime());
//        xf_pinfo_tv_level.setText(userInfoBean.getLevel());
//        xf_pinfo_tv_score.setText(userInfoBean.getExp() + "分");
//        if ("-1".equals(slastProgress)) {
//            xf_pinfo_tv_levelup.setText("满级");
//        } else {
//            xf_pinfo_tv_levelup.setText("距升级还需" + userInfoBean.getLastexp() + "积分");
//        }

        if ("2".equals(userInfoBean.getSex())) {
            xf_pinfo_iv_gender.setImageResource(R.drawable.xf_icon_female);
        } else {
            xf_pinfo_iv_gender.setImageResource(R.drawable.xf_icon_male);
        }

    }

    private void getCommentsFromServer() {
        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", uid);
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        params.addBodyParameter("page", page + "");
        params.addBodyParameter("pagesize", pagesize + "");
        SPUtil.addParams(params);
        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.XF_MYCOMMENTS
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String json = responseInfo.result;
                LogUtils.i("getCommentsFromServer-->" + json);

                xf_pinfo_lv.onRefreshComplete();
                JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj) {
                    if (200 == obj.getIntValue("code")) {
                        List<XF_UserCommentsBean> list = FjsonUtil.parseArray(obj.getString("data")
                                , XF_UserCommentsBean.class);
                        LogUtils.e("" + list.toString());
                        adapter.appendData(list, false);
                        adapter.notifyDataSetChanged();
                        if (null == list || list.size() < pagesize) {
                            xf_pinfo_lv.setMode(Mode.DISABLED);
                        }
                    } else {
                        TUtils.toast(obj.getString("msg"));
                    }
                } else {
                    page--;
                    TUtils.toast("服务器错误");
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                xf_pinfo_lv.onRefreshComplete();
                page--;
            }
        });
    }

    @OnClick(R.id.xf_pinfo_iv_back)
    private void goBack(View view) {
        activity.onBackPressed();
    }

}