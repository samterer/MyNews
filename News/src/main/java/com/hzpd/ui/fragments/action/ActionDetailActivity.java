package com.hzpd.ui.fragments.action;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.ui.activity.MBaseActivity;
import com.hzpd.ui.fragments.vote.VoteDetailFragment;
import com.hzpd.ui.fragments.vote.VotePinfoFragment;
import com.hzpd.utils.CODE;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class ActionDetailActivity extends MBaseActivity implements View.OnClickListener {

    private TextView title;
    private View stitle_ll_back;
    private ActionDetailFragment detailFm;
    private ActionRegisterFragment registerFm;
    private ActionLotteryFragment lotteryFm;
    private ActionLotteryPInfoFragment lotteryPinfoFm;
    private VoteDetailFragment voteFm;
    private VotePinfoFragment voteDetailFm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_fm_std_layout);
        title= (TextView) findViewById(R.id.stitle_tv_content);
        stitle_ll_back=findViewById(R.id.stitle_ll_back);
        stitle_ll_back.setOnClickListener(this);
        EventBus.getDefault().register(this);
        title.setText(R.string.title_activity_detail);
        Intent intent = getIntent();
        if (null == intent) {
            return;
        }
        String id = intent.getStringExtra("id");

        Bundle args = new Bundle();
        detailFm = new ActionDetailFragment();
        args.putString("id", id);
        detailFm.setArguments(args);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.std_fm, detailFm);
        ft.commit();
        currentFm = detailFm;
    }

    @Override
    public void onBackPressed() {

        if (currentFm instanceof ActionDetailFragment) {
            super.onBackPressed();
        } else if (currentFm instanceof VoteDetailFragment) {
            fm.popBackStack();
            title.setText(R.string.title_activity_detail);
            currentFm = detailFm;
        } else if (currentFm instanceof ActionRegisterFragment) {
            fm.popBackStack();
            title.setText(R.string.title_activity_detail);
            currentFm = detailFm;
        } else if (currentFm instanceof ActionLotteryFragment) {
            fm.popBackStack();
            title.setText(R.string.title_activity_detail);
            currentFm = detailFm;
        } else if (currentFm instanceof ActionLotteryPInfoFragment) {
            fm.popBackStack();
            title.setText(R.string.title_activity_award);
            currentFm = lotteryFm;
        } else if (currentFm instanceof VotePinfoFragment) {
            fm.popBackStack();
            title.setText(R.string.title_activity_vote);
            currentFm = voteFm;
        }
    }

    public void onEventMainThread(String from) {
        if ("vote".equals(from)) {
            onBackPressed();
        }
    }

    //报名
    public void toRegister(String id) {

        registerFm = new ActionRegisterFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        registerFm.setArguments(args);

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.add(R.id.std_fm, registerFm);
        ft.addToBackStack(null);
        ft.commit();
        currentFm = registerFm;
        title.setText(R.string.title_activity_register);
    }

    //投票
    public void toVote(String id) {

        voteFm = new VoteDetailFragment();
        Bundle args = new Bundle();
        args.putString("subjectid", id);
        voteFm.setArguments(args);

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.add(R.id.std_fm, voteFm);
        ft.addToBackStack(null);
        ft.commit();
        currentFm = voteFm;
        title.setText(R.string.title_activity_vote);
    }

    //
    public void toVoteDetail(Bundle args) {

        voteDetailFm = new VotePinfoFragment();
        voteDetailFm.setArguments(args);
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.add(R.id.std_fm, voteDetailFm);
        ft.addToBackStack(null);
        ft.commit();
        currentFm = voteDetailFm;
        title.setText(R.string.title_activity_vote_detail);
    }

    //抽奖
    public void toLottery(String id) {

        lotteryFm = new ActionLotteryFragment();
        Bundle args = new Bundle();
        args.putString("subjectid", id);
        lotteryFm.setArguments(args);

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.add(R.id.std_fm, lotteryFm);
        ft.addToBackStack(null);
        ft.commit();
        currentFm = lotteryFm;
        title.setText("活动抽奖");
    }

    //个人信息
    public void toLotteryPinfo(Bundle args) {

        lotteryPinfoFm = new ActionLotteryPInfoFragment();
        lotteryPinfoFm.setArguments(args);

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.add(R.id.std_fm, lotteryPinfoFm);
        ft.addToBackStack(null);
        ft.commit();
        currentFm = lotteryPinfoFm;
        title.setText(R.string.title_activity_personal_award);
    }

    // 获取,传回的数据;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE.REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                EventBus.getDefault().post(mSelectPath);
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.stitle_ll_back:
                onBackPressed();
                break;
        }
    }
}