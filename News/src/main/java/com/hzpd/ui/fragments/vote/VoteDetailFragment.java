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

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.vote.VoteBaseInfo;
import com.hzpd.modle.vote.VoteTitleBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.ui.fragments.action.ActionDetailActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
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

        return view;
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 111) {
                int h = (Integer) msg.obj;

                LogUtils.i("hhh555-->" + h);
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
        LogUtils.i("subjectid-->" + subjectid);

        androidId = MyCommonUtil.getMyUUID(activity);

        adapter = new MyAdapter(this.getChildFragmentManager());
        pager.setAdapter(adapter);

        indicator.setViewPager(pager);

        indicator.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                int h = adapter.getHeight(arg0);

                LogUtils.i("maxHetght-->" + h);
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
    private void getVoteBaseInfo() {


        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("subjectid", subjectid);
        params.addBodyParameter("device", androidId);

        httpUtils.send(HttpMethod.POST, InterfaceJsonfile.mVoteinfo, params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                LogUtils.i("获取投票基本信息 failed!");
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                LogUtils.i("获取投票基本信息 -->" + arg0.result);
                JSONObject obj = FjsonUtil.parseObject(arg0.result);

                if (null == obj) {
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    voteBaseinfo = JSONObject.parseObject(obj.getJSONObject("data").toJSONString(), VoteBaseInfo.class);
                    setBaseInfo(voteBaseinfo);
                    String subjectId = voteBaseinfo.getSubjectid();
                    LogUtils.i("subjectId-->" + subjectId);
                    adapter.setSubjectId(subjectId);
                } else {
                    TUtils.toast(obj.getString("msg"));
                }
            }
        });
    }

    // 提交投票
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vote_btn_vote: {
                if (null == voteBaseinfo) {
                    return;
                }


                if ("1".equals(voteBaseinfo.getSubstat())) {
                    TUtils.toast(getString(R.string.toast_has_voted));
                    return;
                }
                if ("0".equals(voteBaseinfo.getType())) {
                    if (adapter.getOpt() == null || "".equals(adapter.getOpt())) {
                        TUtils.toast(getString(R.string.toast_please_select));
                        return;
                    }
                } else {
                    if (adapter.getAllMultiVoted().size() == 0) {
                        TUtils.toast(getString(R.string.toast_please_select));
                        return;
                    }
                }
                RequestParams params = RequestParamsUtils.getParams();
                params.addBodyParameter("device", androidId);
                params.addBodyParameter("subjectid", voteBaseinfo.getSubjectid());
                params.addBodyParameter("type", voteBaseinfo.getType());

                if ("0".equals(voteBaseinfo.getType())) {
                    String opt = adapter.getOpt();
                    params.addBodyParameter("optionid", opt);
                    LogUtils.i("opt-->" + opt);
                } else {
                    if (adapter.getOptionList().size() > 31) {
                        TUtils.toast(getString(R.string.toast_max_selected_person));
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (String s : adapter.getOptionList()) {
                        sb.append(s + ",");
                    }
                    LogUtils.i("voteString---" + sb.toString());
                    params.addBodyParameter("optionid", sb.substring(0, sb.length() - 1));
                }

                vote_btn_vote.setClickable(false);
                httpUtils.send(HttpMethod.POST, InterfaceJsonfile.mSetvote, params, new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        vote_btn_vote.setClickable(true);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        vote_btn_vote.setClickable(true);
                        LogUtils.i("submit-->" + arg0.result);
                        JSONObject obj = JSONObject.parseObject(arg0.result);
                        if (200 == obj.getIntValue("code")) {
                            TUtils.toast(obj.getString("msg"));
                            // 对话框
                            // if("0".equals(voteBaseinfo.getType())){
                            // if("1".equals(voteBaseinfo.getSubstat())){
                            // adapter.setVoted(adapter.getOpt());
                            // }
                            // }else{
                            // if("1".equals(voteBaseinfo.getSubstat())){
                            // adapter.setMultiVoted();
                            // }
                            // }
                            adapter.clearAll();
                            if ("1".equals(voteBaseinfo.getLottery())) {
                                if (null != spu.getUser()) {
                                    alertDialog(getString(R.string.prompt_join_lottery), getString(R.string.prompt_start_lottery), new IVoteresultClick() {
                                        @Override
                                        public void onclick() {
                                            Bundle args = new Bundle();
                                            args.putString("subjectid", subjectid);
                                            args.putString("from", "vote");
                                            ((ActionDetailActivity) activity).toLotteryPinfo(args);
                                        }
                                    });
                                } else {
                                }
                            }
                        } else {
                            TUtils.toast(obj.getString("msg"));
                        }
                    }
                });
            }
            break;
        }
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
            LogUtils.i("--->" + optionId + "   position-->" + position);
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
                LogUtils.i("setVoted ");
                vg.clear(optionId);
                // 移除click事件
                vg.setVoted();
            }
            setOptionId(optionId);
        }

        public void setMultiVoted() {
            LogUtils.i("setMultiVoted");
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
        private void getVoteMassage(String subjectid) {

            RequestParams params = new RequestParams();
            params.addBodyParameter("subjectid", subjectid);

            httpUtils.send(HttpMethod.POST, InterfaceJsonfile.mVotetys, params, new RequestCallBack<String>() {
                @Override
                public void onFailure(HttpException arg0, String arg1) {
                    LogUtils.i("获取投票选项的所有可用类型  failed!");
                }

                @Override
                public void onSuccess(ResponseInfo<String> arg0) {
                    LogUtils.i("获取投票选项的所有可用类型  -->" + arg0.result);
                    JSONObject obj = FjsonUtil.parseObject(arg0.result);
                    if (null == obj) {
                        return;
                    }
                    if (200 == obj.getIntValue("code")) {
                        List<VoteTitleBean> vtbList = FjsonUtil.parseArray(obj.getJSONArray("data").toJSONString(),
                                VoteTitleBean.class);
                        if (vtbList != null) {
                            LogUtils.i("vl.size--> " + vtbList.size());

                            for (int i = 0; i < vtbList.size(); i++) {
                                VoteGroupFragment f = new VoteGroupFragment(vtbList.get(i), androidId,
                                        voteBaseinfo.getSubjectid(), voteBaseinfo.getSubstat(), voteBaseinfo.getType(),
                                        activity, handler, i, voteBaseinfo.getSubjectid());
                                list.add(f);
                            }
                            notifyDataSetChanged();
                            pager.setOffscreenPageLimit(list.size());
                        }
                    } else {
                        TUtils.toast(obj.getString("msg"));
                    }
                }
            });

        }

    }

    public void onEventMainThread(int requestCode, int resultCode, Intent data) {
        if (1990 == resultCode) {
            LogUtils.i("detail voted");
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