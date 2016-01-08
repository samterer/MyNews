package com.hzpd.ui.fragments.vote;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.vote.VoteBaseInfo;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.Log;
import com.hzpd.utils.MyCommonUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class VoteDetailFragment extends BaseFragment implements View.OnClickListener {


    private TabPageIndicator indicator;//
    private ScrollView vote_body_scroll;//
    private ViewPager pager; //
    private ImageView vote_titlepic; // 投票 头部图片
    private TextView vote_info_tv; // 详细规则
    private Button vote_btn_vote; // 提交
    public VoteBaseInfo voteBaseinfo; // 投票基本信息
    private String androidId; //
    private String subjectid; //
    private MyAdapter adapter;
    private VoteResultDialog dialog;
    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.votedetail_fm_layout, container, false);
        indicator = (TabPageIndicator) view.findViewById(R.id.vote_indicator);
        vote_body_scroll = (ScrollView) view.findViewById(R.id.vote_body_scroll);
        pager = (ViewPager) view.findViewById(R.id.vote_pager);
        vote_titlepic = (ImageView) view.findViewById(R.id.vote_titlepic);
        vote_info_tv = (TextView) view.findViewById(R.id.vote_info_tv);
        vote_btn_vote = (Button) view.findViewById(R.id.vote_btn_vote);
        vote_btn_vote.setOnClickListener(this);
        tag = OkHttpClientManager.getTag();
        return view;
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 111) {
                int h = (Integer) msg.obj;

                Log.i("test","hhh555-->" + h);
                LayoutParams p = pager.getLayoutParams();
                p.height = (int) MyCommonUtil.dp2px(getResources(), 220);
                if (h == 0) {
                    p.height = (int) MyCommonUtil.dp2px(getResources(), 220);
                } else {
                    p.height = (int) MyCommonUtil.dp2px(getResources(), h * 220);
                }

                pager.setLayoutParams(p);
                adapter.notifyDataSetChanged();
                indicator.notifyDataSetChanged();

            } else if (msg.what == 112) {
                String opt = (String) msg.obj;
                adapter.clear(opt);
            }
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        Bundle args = getArguments();
        if (null == args) {
            return;
        }
        subjectid = args.getString("subjectid");
        Log.i("test","subjectid-->" + subjectid);

        androidId = MyCommonUtil.getMyUUID(activity);

        adapter = new MyAdapter(this.getChildFragmentManager());
        pager.setAdapter(adapter);

        indicator.setViewPager(pager);

        indicator.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                int h = adapter.getHeight(arg0);

                Log.i("test","maxHetght-->" + h);
                LayoutParams p = pager.getLayoutParams();
                p.height = (int) MyCommonUtil.dp2px(getResources(), 220);
                if (h == 0) {
                    p.height = (int) MyCommonUtil.dp2px(getResources(), 220);
                } else {
                    p.height = (int) MyCommonUtil.dp2px(getResources(), h * 220);
                }
                pager.setLayoutParams(p);

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        getVoteBaseInfo();// 投票信息

    }

    public void onEventMainThread(String s) {

    }

    //
    private void setBaseInfo(VoteBaseInfo vbi) {
        if (vbi == null)
            return;
        mImageLoader.displayImage(vbi.getImgurl(), vote_titlepic
                , DisplayOptionFactory.getOption(OptionTp.Small), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                float rat = loadedImage.getHeight() * 1.0f / loadedImage.getWidth();
                LayoutParams params = vote_titlepic.getLayoutParams();
                params.height =
                        (int) ((MyCommonUtil.getDisplayMetric(activity.getResources())
                                .widthPixels - MyCommonUtil.dp2px(activity.getResources(), 20)) * rat);
                vote_titlepic.setLayoutParams(params);
                vote_titlepic.setImageBitmap(loadedImage);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
        vote_info_tv.setText(vbi.getDescription());
        // vote_info_tv.setText(Html.fromHtml(Html.toHtml(Span)));
        if ("1".equals(voteBaseinfo.getLottery()) && "1".equals(voteBaseinfo.getSubstat())) {
            vote_btn_vote.setText(R.string.prompt_start_lottery);
        }

    }

    // 获取投票基本信息
    private void getVoteBaseInfo() {}

    // 提交投票
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vote_btn_vote:
            break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OkHttpClientManager.cancel(tag);
    }

    class MyAdapter extends FragmentPagerAdapter {
        private String optionId;
        private List<String> optionIdList;
        private List<VoteGroupFragment> list;

        public MyAdapter(FragmentManager fm) {
            super(fm);
            optionIdList = new ArrayList<String>();
            list = new ArrayList<VoteGroupFragment>();
        }

        public void setSubjectId(String subjectid) {
            getVoteMassage(subjectid);
        }

        public void setOptionId(String optionId) {
            this.optionId = optionId;
        }

        @Override
        public Fragment getItem(int position) {
            VoteGroupFragment f = list.get(position);
            f.setSelectedRadio(optionId);
            Log.i("test","--->" + optionId + "   position-->" + position);
            return f;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return list.get(position).getTitle();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        private int getHeight(int position) {
            return list.get(position).getHeight();
        }

        public void clear(String optionId) {
            for (VoteGroupFragment vg : list) {
                vg.clear(optionId);
            }
            setOptionId(optionId);
        }

        public void clearAll() {
            for (VoteGroupFragment vg : list) {
                vg.clearAll();
            }

        }

        public void setVoted(String optionId) {
            for (VoteGroupFragment vg : list) {
                Log.i("test","setVoted ");
                vg.clear(optionId);
                // 移除click事件
                vg.setVoted();
            }
            setOptionId(optionId);
        }

        public void setMultiVoted() {
            Log.i("test","setMultiVoted");
            for (VoteGroupFragment vg : list) {
                // 移除click事件
                vg.setVoted();
            }
        }

        public List<String> getAllMultiVoted() {
            for (VoteGroupFragment vg : list) {
                optionIdList.addAll(vg.getMultiVoted());
            }
            return optionIdList;
        }

        public String getOpt() {
            return optionId;
        }

        public List<String> getOptionList() {
            return optionIdList;
        }

        // 获取投票选项的所有可用类型
        private void getVoteMassage(String subjectid) {}

    }

    public void onEventMainThread(int requestCode, int resultCode, Intent data) {
        if (1990 == resultCode) {
            Log.i("test","detail voted");
            String optid = data.getStringExtra("optid");
            adapter.setVoted(optid);
            voteBaseinfo.setSubstat("1");
        }
    }

    private void alertDialog(String text, String butext, IVoteresultClick click) {
        dialog = new VoteResultDialog(activity, R.style.Theme_CustomDialog_Activity, text, butext, click);
        dialog.show();
    }

    @Override
    public void onDestroy() {
        if (null != dialog) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}