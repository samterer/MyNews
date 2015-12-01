package com.hzpd.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.ReplayBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.modle.event.UpdateNewsBeanDbEvent;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.SystemBarTintManager;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import de.greenrobot.event.EventBus;

public class ZQ_ReplyActivity extends MBaseActivity {
    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.comment;
    }

    @ViewInject(R.id.zq_reply_et_content)
    private EditText zq_reply_et_content;
    @ViewInject(R.id.zq_reply_share_iv)
    private ImageView zq_reply_share_iv;
    @ViewInject(R.id.zq_reply_share_iv1)
    private ImageView zq_reply_share_iv1;
    @ViewInject(R.id.stitle_tv_content)
    private TextView stitle_tv_content;
    @ViewInject(R.id.stitle_ll_back)
    private View stitle_ll_back;
    @ViewInject(R.id.rl_share1)
    private RelativeLayout rl_share1;
    @ViewInject(R.id.iv_reply_share)
    private ImageView iv_reply_share;

    private boolean isShare = false;

    private ReplayBean bean;

    @ViewInject(R.id.transparent_layout_id)
    private View transparent_layout_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zq_reply_layout);
        ViewUtils.inject(this);
        if (App.getInstance().getThemeName().equals("3")) {
            transparent_layout_id.setVisibility(View.VISIBLE);
        } else {
            transparent_layout_id.setVisibility(View.GONE);
        }
        changeStatus();
        stitle_tv_content.setText(R.string.comment);
        Intent intent = getIntent();
        if (null != intent) {
            bean = (ReplayBean) intent.getSerializableExtra("replay");
        }
    }
    private void changeStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.title_bar_color, typedValue, true);
        int color = typedValue.data;
        tintManager.setStatusBarTintColor(color);
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
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


    @OnClick({R.id.zq_reply_tv_cancle, R.id.zq_reply_tv_send, R.id.zq_reply_share_iv, R.id.zq_reply_share_iv1, R.id.stitle_ll_back, R.id.rl_share1})
    private void click(View view) {
        if (AvoidOnClickFastUtils.isFastDoubleClick()){
            return;
        }
        switch (view.getId()) {
            case R.id.stitle_ll_back:
                finish();
                break;
            case R.id.zq_reply_share_iv: {
            }
            break;
            case R.id.zq_reply_share_iv1: {
            }
            case R.id.rl_share1: {
                if (!isShare) {
                    iv_reply_share.setImageResource(R.drawable.iv_reply_share_select);
                    isShare = true;
                } else {
                    iv_reply_share.setImageResource(R.drawable.iv_reply_share_unselect);
                    isShare = false;
                }
            }
            break;
            case R.id.zq_reply_tv_cancle: {
                finish();
            }
            break;
            case R.id.zq_reply_tv_send: {
                String comcount = bean.getComcount();
//                if (AvoidOnClickFastUtils.isFastDoubleClick()) {
//                    return;//防止过快点击
//                }
//                try {
//                    NewsBeanDB nbfc = dbHelper.getNewsListDbUtils().findFirst(
//                            Selector.from(NewsBeanDB.class).where("nid", "=", bean.getId()));
//                    if (null != nbfc) {
//                        com.hzpd.utils.Log.e("NewsBeanDB", "NewsBeanDB--->" + nbfc.getFav() + "::::" + nbfc.getComcount());
//                        if (nbfc.getColumnid() != null || Integer.parseInt(nbfc.getComcount()) >= Integer.parseInt(comcount)) {
//                            comcount = nbfc.getComcount();
//                        }
//                    } else {
//                        com.hzpd.utils.Log.e("NewsBeanDB", "NewsBeanDB--->null");
//                    }
//
//                } catch (DbException e) {
//                    e.printStackTrace();
//                    com.hzpd.utils.Log.e("DbException", "NewsBeanDB--->" + e);
//                }
                String comment = zq_reply_et_content.getText().toString();
                if (null == comment || "".equals(comment)) {
                    TUtils.toast(getString(R.string.toast_input_cannot_be_empty));
                    return;
                }
                sendComment(comment, comcount);
            }
            break;

            default:
                break;
        }
    }

    // 发表评论
    private void sendComment(final String content, final String comcount) {

        spu.getUser();
        String station = SharePreferecesUtils.getParam(ZQ_ReplyActivity.this, StationConfig.STATION, "def").toString();
        String siteid = null;
        String PUBLISHCOMMENT_url = null;
        if (station.equals(StationConfig.DEF)) {
            siteid = InterfaceJsonfile.SITEID;
            PUBLISHCOMMENT_url = InterfaceJsonfile.PUBLISHCOMMENT;
        } else if (station.equals(StationConfig.YN)) {
            siteid = InterfaceJsonfile_YN.SITEID;
            PUBLISHCOMMENT_url = InterfaceJsonfile_YN.PUBLISHCOMMENT;
        } else if (station.equals(StationConfig.TW)) {
            siteid = InterfaceJsonfile_TW.SITEID;
            PUBLISHCOMMENT_url = InterfaceJsonfile_TW.PUBLISHCOMMENT;
        }
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("uid", spu.getUser().getUid());
//        params.addBodyParameter("uid","53");
        params.addBodyParameter("title", bean.getTitle());
        params.addBodyParameter("type", bean.getType());//"News"
        params.addBodyParameter("nid", bean.getId());
        params.addBodyParameter("content", content);
        params.addBodyParameter("json_url", bean.getJsonUrl());
        params.addBodyParameter("smallimg", bean.getImgUrl());
        params.addBodyParameter("siteid", siteid);

        httpUtils.send(HttpMethod.POST
                , PUBLISHCOMMENT_url// InterfaceApi.mSendComment
                , params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                Log.i("msg", arg1);
                TUtils.toast(getString(R.string.toast_server_no_response));
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
                    setComcountsId(bean.getId(), comcount);
                    TUtils.toast(getString(R.string.comment_ok));
                    EventBus.getDefault().post(new UpdateNewsBeanDbEvent("Update_OK"));
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", content);
                    bundle.putBoolean("isShare", isShare);
                    resultIntent.putExtras(bundle);
                    setResult(RESULT_OK, resultIntent);
                    EventUtils.sendComment(activity);
                    finish();
                } else {
                    TUtils.toast(getString(R.string.toast_fail_to_comment));
                }
            }
        });
    }

    public void setComcountsId(String nid, String comcount) {
        try {
            NewsBeanDB nbdb = new NewsBeanDB();
            nbdb.setNid(Integer.parseInt(nid));
            int counts = Integer.parseInt(comcount) + 1;
            nbdb.setComcount("" + counts);
            dbHelper.getNewsListDbUtils().update(nbdb
                    , WhereBuilder.b("nid", "=", nid)
                    , "comcount");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}