package com.hzpd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.XF_PersonalInfoFragment;
import com.lidroid.xutils.ViewUtils;


public class XF_PInfoActivity extends MBaseActivity {

    private XF_PersonalInfoFragment personalInfoFm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xf_pinfo_layout);
        ViewUtils.inject(this);

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