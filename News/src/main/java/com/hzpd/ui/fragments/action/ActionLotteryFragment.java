package com.hzpd.ui.fragments.action;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hzpd.custorm.LotteryView;
import com.hzpd.hflt.R;
import com.hzpd.modle.LotteryDrawBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.TUtils;

public class ActionLotteryFragment extends BaseFragment implements View.OnClickListener {


    private TextView lotteryv_tv;
    private LotteryView lotteryv;
    private TextView lotteryv_tv_number;
    private TextView lotteryv_tv_price;
    private Button lottery_bt;
    private String androidId;    //
    private String subjectid;        //
    private LotteryDrawBean lotterydraw;
    private String from;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (100 == msg.what) {
                if ("1".equals(lotterydraw.getStatus())) {
                    TUtils.toast(getString(R.string.toast_take_award, lotterydraw.getPrize()));
                    lotteryv_tv_price.setVisibility(View.VISIBLE);
                    lotteryv.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//							lotteryv.setVisibility(View.GONE);
                            lottery_bt.setVisibility(View.VISIBLE);
                        }
                    }, 1000);
                } else {
                    TUtils.toast(getString(R.string.toast_no_award));
                }
            }
        }
    };

    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.action_lottery_layout, container, false);
        initViews(view);
        tag = OkHttpClientManager.getTag();
        return view;
    }

    private void initViews(View view) {
        lotteryv_tv = (TextView) view.findViewById(R.id.lotteryv_tv);
        lotteryv = (LotteryView) view.findViewById(R.id.lotteryv);
        lotteryv_tv_number = (TextView) view.findViewById(R.id.lotteryv_tv_number);
        lotteryv_tv_price = (TextView) view.findViewById(R.id.lotteryv_tv_price);
        lottery_bt = (Button) view.findViewById(R.id.lottery_bt);
        lottery_bt.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (null == args) {
            return;
        }
        subjectid = args.getString("subjectid");
        from = args.getString("from");
        androidId = MyCommonUtil.getMyUUID(activity);

        lotteryv.setHandler(handler);

        getInfo();
    }

    private void getInfo() {}

    @Override
    public void onDestroyView() {
        OkHttpClientManager.cancel(tag);
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lottery_bt: {
                Bundle args = new Bundle();
                args.putString("number", lotterydraw.getNumber());
                args.putString("androidid", androidId);
                args.putString("subjectid", subjectid);
                args.putString("price", lotterydraw.getPrize());
                args.putString("from", from);
                ((ActionDetailActivity) activity).toLotteryPinfo(args);
            }
            break;
        }
    }
}