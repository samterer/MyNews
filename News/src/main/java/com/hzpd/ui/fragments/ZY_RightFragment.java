package com.hzpd.ui.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.facebook.CallbackManager;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.hzpd.custorm.CircleImageView;
import com.hzpd.hflt.R;
import com.hzpd.modle.ThirdLoginBean;
import com.hzpd.modle.UserBean;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.MyCommentsActivity;
import com.hzpd.ui.activity.MyPMColAvtivity;
import com.hzpd.ui.activity.SettingActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class ZY_RightFragment extends BaseFragment {
    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.leftMenu;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisible != isVisibleToUser) {
            if (isVisibleToUser) {
                AnalyticUtils.sendGaEvent(getActivity(), AnalyticUtils.CATEGORY.slidMenu, AnalyticUtils.ACTION.viewPage, null, 0L);
                AnalyticUtils.sendUmengEvent(getActivity(), AnalyticUtils.CATEGORY.slidMenu);
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    public static final String ACTION_USER = "com.hzpd.cms.user";
    public static final String ACTION_QUIT = "com.hzpd.cms.quit";
    public static final String ACTION_QUIT_LOGIN = "com.hzpd.cms.quit.login";
    @ViewInject(R.id.zy_rfrag_ll_login)
    private LinearLayout zy_rfrag_ll_login;
    @ViewInject(R.id.zy_rfrag_tv_login)
    private TextView zy_rfrag_tv_login;
    @ViewInject(R.id.zy_rfrag_iv_login)
    private CircleImageView zy_rfrag_iv_login;

    private LoginQuitBR br;
    private boolean isDay = true;

    private CallbackManager callbackManager;

    private TextView version;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.zy_rightfragment, container, false);
            ViewUtils.inject(this, view);
            version = (TextView) view.findViewById(R.id.zy_version);
        } catch (Exception e) {

        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            version.setText(getResources().getString(R.string.app_name) + "  v" + getActivity().getPackageManager().getPackageInfo(getActivity()
                    .getPackageName(), 0).versionName);
            br = new LoginQuitBR();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ZY_RightFragment.ACTION_QUIT);
            filter.addAction(ZY_RightFragment.ACTION_USER);
            filter.addAction(ZY_RightFragment.ACTION_QUIT_LOGIN);
            activity.registerReceiver(br, filter);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (null != spu.getUser()) {
            LogUtils.i("userimg-->" + spu.getUser().getAvatar_path());
            SPUtil.displayImage(spu.getUser().getAvatar_path(), zy_rfrag_iv_login,
                    DisplayOptionFactory.getOption(OptionTp.Avatar));
            zy_rfrag_tv_login.setText("" + spu.getUser().getNickname());
        }

        callbackManager = CallbackManager.Factory.create();
    }

    public void thirdlogin(ThirdLoginBean tlb) {
        String station = SharePreferecesUtils.getParam(getActivity(), StationConfig.STATION, "def").toString();
        String thirdLogin_url = null;
        if (station.equals(StationConfig.DEF)) {
            thirdLogin_url = InterfaceJsonfile.thirdLogin;
        } else if (station.equals(StationConfig.YN)) {
            thirdLogin_url = InterfaceJsonfile_YN.thirdLogin;
        } else if (station.equals(StationConfig.TW)) {
            thirdLogin_url = InterfaceJsonfile_TW.thirdLogin;
        }
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("userid", tlb.getUserid());
        params.addBodyParameter("gender", tlb.getGender());
        params.addBodyParameter("nickname", tlb.getNickname());
        params.addBodyParameter("photo", tlb.getPhoto());
        params.addBodyParameter("third", tlb.getThird());
        params.addBodyParameter("is_ucenter", "0");

        httpUtils.send(HttpRequest.HttpMethod.POST, thirdLogin_url, params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        LogUtils.i("result-->" + responseInfo.result);
                        JSONObject obj = FjsonUtil
                                .parseObject(responseInfo.result);
                        if (null == obj) {
                            return;
                        }
                        if (200 == obj.getIntValue("code")) {
                            UserBean user = FjsonUtil.parseObject(
                                    obj.getString("data"), UserBean.class);
                            spu.setUser(user);
                        } else {
                            TUtils.toast(obj.getString("msg"));
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        LogUtils.e("test login failed");
                    }
                });
    }


    ProfileTracker profileTracker = new ProfileTracker() {
        @Override
        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
            if (currentProfile != null) {
                android.util.Log.e("test", "currentProfile " + currentProfile.getId());
                ThirdLoginBean tlb = new ThirdLoginBean();
                tlb.setUserid(currentProfile.getId());
                tlb.setNickname(currentProfile.getName());
                tlb.setPhoto(currentProfile.getProfilePictureUri(200, 200).toString());
                tlb.setGender("3");
                tlb.setThird("FaceBook");
                Log.e("test", "ThirdLoginBean" + tlb.toString());
                try {
                    zy_rfrag_tv_login.setText(currentProfile.getName());
                    SPUtil.displayImage(currentProfile.getProfilePictureUri(200, 200).toString(), zy_rfrag_iv_login
                            , DisplayOptionFactory.getOption(OptionTp.Avatar));
                    thirdlogin(tlb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                android.util.Log.e("test", "oldProfile " + oldProfile);
                spu.setUser(null);

                zy_rfrag_tv_login.setText(R.string.prompt_login_now);
                zy_rfrag_iv_login.setImageResource(R.drawable.zy_pic_touxiang_new);
            }
        }
    };

    private List<String> permissions = Arrays.asList("public_profile", "user_friends");

    @OnClick({R.id.zy_rfrag_ll_login, R.id.zy_rfrag_ll_comm, R.id.zy_rfrag_ll_collect, R.id.zy_rfrag_ll_push,
            R.id.zy_rfrag_ll_setting, })
    private void rightClick(View v) {
        if (AvoidOnClickFastUtils.isFastDoubleClick())
            return;
        boolean flag = false;
        Intent mIntent = new Intent();
        switch (v.getId()) {
//            case R.id.zy_rfrag_ll_login: {
//                if (spu.getUser() != null) {
//
//                } else {
//                    flag = false;
//                }
//            }
//            break;
            case R.id.zy_rfrag_ll_comm: {
                mIntent.setClass(activity, MyCommentsActivity.class);
                flag = true;
            }
            break;
            case R.id.zy_rfrag_ll_collect: {
                Log.i("", "shoucang--");
                mIntent.setClass(activity, MyPMColAvtivity.class);
                mIntent.putExtra("type", "collection");
                flag = true;
            }
            break;
            case R.id.zy_rfrag_ll_push: {
                Log.i("", "shoucang--push");
                mIntent.setClass(activity, MyPMColAvtivity.class);
                mIntent.putExtra("type", "pushmsg");
                flag = true;
            }
            break;
            case R.id.zy_rfrag_ll_setting: {
                mIntent.setClass(activity, SettingActivity.class);
                flag = true;
            }
            break;
            case R.id.zy_rfrag_ll_login: {
                final LoginManager loginManager = LoginManager.getInstance();
                if (null == spu.getUser()) {
                    loginManager.setDefaultAudience(DefaultAudience.FRIENDS);
                    loginManager.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
                    loginManager.logInWithReadPermissions(this, permissions);
                } else {
                    String logout = getResources().getString(
                            R.string.com_facebook_loginview_log_out_action);
                    String cancel = getResources().getString(
                            R.string.com_facebook_loginview_cancel_action);
                    String message;
                    Profile profile = Profile.getCurrentProfile();
                    if (profile != null && profile.getName() != null) {
                        message = String.format(
                                getResources().getString(
                                        R.string.com_facebook_loginview_logged_in_as),
                                profile.getName());
                    } else {
                        message = getResources().getString(
                                R.string.com_facebook_loginview_logged_in_using_facebook);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(message)
                            .setCancelable(true)
                            .setPositiveButton(logout, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    loginManager.logOut();
                                }
                            })
                            .setNegativeButton(cancel, null);
                    builder.create().show();
                }
            }
            break;
            default:
                break;
        }
        if (flag) {
            startActivity(mIntent);
            AAnim.ActivityStartAnimation(activity);
        }
    }

    public class LoginQuitBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ZY_RightFragment.ACTION_USER)) {
                setLogin();
            } else if (action.equals(ZY_RightFragment.ACTION_QUIT)) {
                setQuit();
                LogUtils.i("setquit");
            } else if (action.equals(ZY_RightFragment.ACTION_QUIT_LOGIN)) {
                setQuit();
                LogUtils.i("setquitlogin");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        zy_rfrag_ll_login.performClick();
                        LogUtils.i("r_login_layout.callOnClick()");
                    }
                }, 500);
            }
        }
    }

    private void setLogin() {
        LogUtils.i("imgUrl-->" + spu.getUser().getAvatar_path() + "  name-->" + spu.getUser().getNickname());
        SPUtil.displayImage(spu.getUser().getAvatar_path(), zy_rfrag_iv_login,
                DisplayOptionFactory.getOption(OptionTp.Avatar));
        zy_rfrag_tv_login.setText(spu.getUser().getNickname());
        // -----
        JPushInterface.setAlias(activity, spu.getUser().getUid(), new TagAliasCallback() {
            @Override
            public void gotResult(int arg0, String arg1, Set<String> arg2) {
                LogUtils.i("arg0-->" + arg0 + " arg1-->" + arg1);
                if (arg2 != null) {
                    for (String s : arg2) {
                        LogUtils.i("arg2->" + s);
                    }
                }
            }
        });
    }

    private void setQuit() {
        zy_rfrag_iv_login.setImageResource(R.drawable.zy_pic_touxiang);
        zy_rfrag_tv_login.setText(R.string.login);

        JPushInterface.setAlias(activity, "", new TagAliasCallback() {
            @Override
            public void gotResult(int arg0, String arg1, Set<String> arg2) {
                LogUtils.i("arg0-->" + arg0 + " arg1-->" + arg1);
                if (arg2 != null) {
                    for (String s : arg2) {
                        LogUtils.i("arg2->" + s);
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        try {
            if (profileTracker != null) {
                profileTracker.stopTracking();
                profileTracker = null;
            }
            activity.unregisterReceiver(br);
        } catch (Exception e) {

        }
        super.onDestroy();
    }

}