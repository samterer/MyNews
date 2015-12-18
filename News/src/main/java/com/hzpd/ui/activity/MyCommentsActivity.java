package com.hzpd.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.hzpd.adapter.MycommentsAdapter;
import com.hzpd.custorm.CircleImageView;
import com.hzpd.hflt.R;
import com.hzpd.modle.MycommentsBean;
import com.hzpd.modle.XF_UserInfoBean;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
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

public class MyCommentsActivity extends MBaseActivity {

    private CircleImageView xf_pinfo_iv_avatar;//头像
    private TextView xf_pinfo_tv_nickname;//昵称
    private ImageView xf_pinfo_iv_gender;//性别
    private TextView xf_pinfo_tv_level_alias;//级别
    private TextView xf_pinfo_tv_level;//级别
    private TextView xf_pinfo_tv_score;//分数
    private NumberProgressBar xf_pinfo_npb;//进度条
    private TextView xf_pinfo_tv_regtime;//注册时间
    private TextView xf_pinfo_tv_levelup;//升级提示

    private View  app_progress_bar;

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.myComment;
    }


    @ViewInject(R.id.title)
    private View title;
    @ViewInject(R.id.stitle_tv_content)
    private TextView stitle_tv_content;
    @ViewInject(R.id.pushmsg_tv_empty)
    private View pushmsg_tv_empty;
    @ViewInject(R.id.mycomments_title)
    private LinearLayout mycomments_title;
    @ViewInject(R.id.mycoms_text)
    private TextView mycoms_text;
    private ListView listView;

    private int Page = 1;
    private static final int PageSize = 1500;
    private boolean mFlagRefresh;
    private MycommentsAdapter adapter;
    @ViewInject(R.id.transparent_layout_id)
    private View transparent_layout_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mycomment_layout);
        app_progress_bar=findViewById(R.id.app_progress_bar);
        ViewUtils.inject(this);
        if (App.getInstance().getThemeName().equals("3")) {
            transparent_layout_id.setVisibility(View.VISIBLE);
        } else {
            transparent_layout_id.setVisibility(View.GONE);
        }
        super.changeStatusBar();
        stitle_tv_content.setText(R.string.comment_mine);
        listView = (ListView) findViewById(R.id.list_view);
        LayoutInflater infla = LayoutInflater.from(activity);
        View headView = infla.inflate(R.layout.my_comment_headview, null);
        headView.findViewById(R.id.xf_pinfo_iv_back).setVisibility(View.GONE);
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
        listView.addHeaderView(headView);

        listView.setEmptyView(pushmsg_tv_empty);
        adapter = new MycommentsAdapter(this);
        listView.setAdapter(adapter);

        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Page = 1;
                getInfoFromServer();
            }
        }, 600);



        getUserInfoFromServer();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private XF_UserInfoBean userInfoBean;

    private void getUserInfoFromServer() {
        if (spu.getUser() == null) {
            return;
        }

        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", "" + spu.getUser().getUid());
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        httpUtils.send(HttpMethod.POST
                , InterfaceJsonfile.XF_USERINFO
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String json = responseInfo.result;
                android.util.Log.i("getUserInfoFromServer", json.toString());

                JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj) {
                    if (200 == obj.getIntValue("code")) {
                        userInfoBean = FjsonUtil.parseObject(obj.getString("data")
                                , XF_UserInfoBean.class);
                        setUserInfo();
                    } else {
                    }
                } else {
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

        android.util.Log.e("test", userInfoBean.getAvatar_path());
        SPUtil.displayImage(userInfoBean.getAvatar_path()
                , xf_pinfo_iv_avatar
                , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Avatar));

        xf_pinfo_tv_nickname.setText(userInfoBean.getNickname());
        xf_pinfo_tv_level_alias.setText("" + userInfoBean.getAlias());
        xf_pinfo_tv_regtime.setText(getString(R.string.sgin_time) + userInfoBean.getRegtime());
    }

    private void getInfoFromServer() {
        if (null == spu.getUser()) {
//            TUtils.toast(getString(R.string.toast_please_login));
            return;
        }
        String uid = spu.getUser().getUid();

        LogUtils.e("uid" + uid);

        String siteid = null;
        String myComm_url = null;
        siteid = InterfaceJsonfile.SITEID;
        myComm_url = InterfaceJsonfile.XF_MYCOMMENTS;
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("uid", uid);
        params.addBodyParameter("siteid", siteid);
        params.addBodyParameter("Page", Page + "");
        params.addBodyParameter("PageSize", PageSize + "");
        params.addBodyParameter("session", "");
        SPUtil.addParams(params);
        httpUtils.send(HttpMethod.POST
                , myComm_url
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                app_progress_bar.setVisibility(View.GONE);
                LogUtils.i("data-->" + responseInfo.result);
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);

                if (null == obj) {
                    return;
                }

                Log.e("test", "test" + obj.toString());

                if (200 == obj.getIntValue("code")) {


                    List<MycommentsBean> mlist = FjsonUtil.parseArray(obj.getString("data"), MycommentsBean.class);
                    Log.e("test", mlist.toString());
                    LogUtils.e("getContent" + mlist.get(0).getContent().toString());

                    if (null == mlist) {
                        return;
                    }
                    Log.e("MyCommentsActivity", "mlist--->" + mlist.toString());
                    adapter.appendData(mlist, mFlagRefresh);
                    adapter.notifyDataSetChanged();

                    String counts = obj.getString("count");
                    LogUtils.e("count" + counts);
                    if (counts != null) {
//						mycomments_title.setVisibility(View.VISIBLE);
                        mycoms_text.setText(counts + " comments");
                    }

                } else {
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                if (!mFlagRefresh) {
                    app_progress_bar.setVisibility(View.GONE);
                    Page--;
                }
            }
        });
    }

    @OnClick(R.id.stitle_ll_back)
    private void goback(View v) {
        finish();
    }

}
