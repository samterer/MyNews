package com.hzpd.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;

public class ZQ_ModifyPersonalInfoFragment extends BaseFragment implements View.OnClickListener {
    private LinearLayout mi_ll_nick;//昵称布局
    private LinearLayout mi_ll_gender;//性别布局
    private TextView stitle_tv_content;
    private EditText mi_et_context;//昵称edit
    private ImageView mi_im_clean;//清除按钮
    private Button mi_bt_comfirm;//提交按钮
    private RadioButton mi_rb1;
    private RadioButton mi_rb2;
    private RadioButton mi_rb3;
    private RadioGroup mi_rg;
    private View stitle_ll_back;

    private int type;//1昵称  2性别
    private String gender = "3";
    private String nickname;

    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.modifypersonalinfo_layout, container, false);
        initViews(view);
        tag = OkHttpClientManager.getTag();
        return view;
    }

    private void initViews(View view) {
        mi_ll_nick = (LinearLayout) view.findViewById(R.id.mi_ll_nick);
        mi_ll_gender = (LinearLayout) view.findViewById(R.id.mi_ll_gender);
        stitle_tv_content = (TextView) view.findViewById(R.id.stitle_tv_content);
        mi_et_context = (EditText) view.findViewById(R.id.mi_et_context);
        mi_im_clean = (ImageView) view.findViewById(R.id.mi_im_clean);
        mi_im_clean.setOnClickListener(this);
        mi_bt_comfirm = (Button) view.findViewById(R.id.mi_bt_comfirm);
        mi_bt_comfirm.setOnClickListener(this);
        mi_rb1 = (RadioButton) view.findViewById(R.id.mi_rb1);
        mi_rb2 = (RadioButton) view.findViewById(R.id.mi_rb2);
        mi_rb3 = (RadioButton) view.findViewById(R.id.mi_rb3);
        mi_rg = (RadioGroup) view.findViewById(R.id.mi_rg);
        stitle_ll_back = view.findViewById(R.id.stitle_ll_back);
        stitle_ll_back.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        type = bundle.getInt(InterfaceJsonfile.PWDTYPE, InterfaceJsonfile.NICKNAME);
        if (InterfaceJsonfile.NICKNAME == type) {
            mi_ll_nick.setVisibility(View.VISIBLE);
            mi_ll_gender.setVisibility(View.GONE);
            stitle_tv_content.setText(R.string.prompt_change_nickname);
            mi_et_context.setText(spu.getUser().getNickname());
        } else if (InterfaceJsonfile.GENDER == type) {

            mi_ll_nick.setVisibility(View.GONE);
            mi_ll_gender.setVisibility(View.VISIBLE);
            stitle_tv_content.setText(R.string.prompt_change_sex);
            String gender = spu.getUser().getSex();

            if ("1".equals(gender)) {
                mi_rb1.setChecked(true);
            } else if ("2".equals(gender)) {
                mi_rb2.setChecked(true);
            } else {
                mi_rb3.setChecked(true);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stitle_ll_back: {
                if (1 == type) {
                    Intent intent = new Intent();
                    intent.setAction(ZY_RightFragment.ACTION_USER);
                    activity.sendBroadcast(intent);
                }
                activity.onBackPressed();
            }
            break;
            case R.id.mi_bt_comfirm:
                break;
            case R.id.mi_im_clean: {
                mi_et_context.setText("");
            }
            break;
        }
    }

    @Override
    public void onDestroyView() {
        OkHttpClientManager.cancel(tag);
        super.onDestroyView();
    }
}