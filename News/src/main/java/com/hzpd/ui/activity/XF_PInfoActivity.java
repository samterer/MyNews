package com.hzpd.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.XF_PersonalInfoFragment;
import com.hzpd.utils.SystemBarTintManager;
import com.lidroid.xutils.ViewUtils;


public class XF_PInfoActivity extends MBaseActivity {

    private XF_PersonalInfoFragment personalInfoFm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xf_pinfo_layout);
        ViewUtils.inject(this);
//        changeStatus();
        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");
        personalInfoFm = new XF_PersonalInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable("uid", uid);
        personalInfoFm.setArguments(args);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.xf_pinfo_fm, personalInfoFm);
        ft.commit();
        currentFm = personalInfoFm;


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}