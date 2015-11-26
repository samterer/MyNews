package com.hzpd.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.ZQ_FindbackpwdFragment;
import com.hzpd.ui.fragments.ZQ_ModifyPersonalInfoFragment;
import com.hzpd.ui.fragments.ZQ_PersonalInfoFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.CODE;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.SystemBarTintManager;
import com.lidroid.xutils.util.LogUtils;

public class PersonalInfoActivity extends MBaseActivity {

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.info;
    }

    private ZQ_ModifyPersonalInfoFragment modifyPersonalFm;
    private ZQ_PersonalInfoFragment personalInfoFm;
    private ZQ_FindbackpwdFragment findbackpwdFm;

    private Fragment currentFm;
    private FragmentManager fm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zq_personalinfo_layout);

        fm = getSupportFragmentManager();

        personalInfoFm = new ZQ_PersonalInfoFragment();

//		FragmentTransaction ft = fm.beginTransaction();
//		ft.replace(R.id.zq_pinfo_fm, personalInfoFm);
//		ft.commit();


        fm.beginTransaction()
                .replace(R.id.zq_pinfo_fm, personalInfoFm)
                .commit();

        currentFm = personalInfoFm;
        changeStatus();
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

    public void toModifyPinfoFm(int type) {
        String station = SharePreferecesUtils.getParam(PersonalInfoActivity.this, StationConfig.STATION, "def").toString();
        String PWDTYPE = null;
        if (station.equals(StationConfig.DEF)) {
            PWDTYPE = InterfaceJsonfile.PWDTYPE;
        } else if (station.equals(StationConfig.YN)) {
            PWDTYPE = InterfaceJsonfile_YN.PWDTYPE;
        } else if (station.equals(StationConfig.TW)) {
            PWDTYPE = InterfaceJsonfile_TW.PWDTYPE;
        }
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        modifyPersonalFm = new ZQ_ModifyPersonalInfoFragment();
        Bundle args = new Bundle();
        args.putInt(PWDTYPE, type);
        modifyPersonalFm.setArguments(args);
        ft.add(R.id.zq_pinfo_fm, modifyPersonalFm);
        ft.addToBackStack(null);
        ft.commit();
        currentFm = modifyPersonalFm;

    }

    public void toFindbackpwdFm() {
        String station = SharePreferecesUtils.getParam(PersonalInfoActivity.this, StationConfig.STATION, "def").toString();
        String PWDTYPE = null;
        if (station.equals(StationConfig.DEF)) {
            PWDTYPE = InterfaceJsonfile.PWDTYPE;
        } else if (station.equals(StationConfig.YN)) {
            PWDTYPE = InterfaceJsonfile_YN.PWDTYPE;
        } else if (station.equals(StationConfig.TW)) {
            PWDTYPE = InterfaceJsonfile_TW.PWDTYPE;
        }
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        findbackpwdFm = new ZQ_FindbackpwdFragment();
        Bundle args = new Bundle();
        args.putInt(PWDTYPE, 2);
        findbackpwdFm.setArguments(args);
        ft.add(R.id.zq_pinfo_fm, findbackpwdFm);
        ft.addToBackStack(null);
        ft.commit();
        currentFm = findbackpwdFm;
    }

    public void goBackPinfoFm() {
        fm.popBackStack();
        currentFm = personalInfoFm;
        FragmentTransaction ft = fm.beginTransaction();
        ft.show(currentFm);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if (currentFm instanceof ZQ_PersonalInfoFragment) {
            finish();
        } else if (currentFm instanceof ZQ_ModifyPersonalInfoFragment) {
            goBackPinfoFm();
            personalInfoFm.modify();
        } else if (currentFm instanceof ZQ_FindbackpwdFragment) {
            goBackPinfoFm();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.i("requestCode-->" + requestCode + "  resultCode-->" + resultCode);
        switch (requestCode) {
            case CODE.IMAGE_REQUEST_CODE: {
                if (null != data) {
                    personalInfoFm.startPhotoZoom(data.getData());
                }
            }
            break;
            case CODE.CAMERA_REQUEST_CODE: {
                personalInfoFm.startPhotoZoom();
            }
            break;
            case CODE.RESULT_REQUEST_CODE: {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data");
                        personalInfoFm.uploadPhoto(photo);
                        LogUtils.i("setphoto");
                    } else {
                        LogUtils.i("extras null");
                    }
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
