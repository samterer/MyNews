package com.hzpd.ui.fragments.action;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.OkHttpClientManager;

public class ActionLotteryPInfoFragment extends BaseFragment implements View.OnClickListener {

    private TextView lotterypi_tv_price;
    private EditText lotterypi_uname;
    private EditText lotterypi_phone;
    private EditText lotterypi_add;
    private View lotterypi_submit;

    private String number;
    private String androidid;
    private String subjectid;
    private String price;
    private String from;
    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.action_lottery_pi_layout, container, false);
        initViews(view);
        tag = OkHttpClientManager.getTag();
        return view;
    }

    private void initViews(View view) {
        lotterypi_tv_price = (TextView) view.findViewById(R.id.lotterypi_tv_price);
        lotterypi_uname = (EditText) view.findViewById(R.id.lotterypi_uname);
        lotterypi_phone = (EditText) view.findViewById(R.id.lotterypi_phone);
        lotterypi_add = (EditText) view.findViewById(R.id.lotterypi_add);
        lotterypi_submit = view.findViewById(R.id.lotterypi_submit);
        lotterypi_submit.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (null == args) {
            return;
        }
        number = args.getString("number");//, lotterydraw.getNumber());
        androidid = args.getString("androidid");//,androidId);
        subjectid = args.getString("subjectid");//,newsid);
        price = args.getString("price");
        from = args.getString("from");

        lotterypi_tv_price.setText(getString(R.string.toast_award, price));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lotterypi_submit:
            break;
        }
    }


    @Override
    public void onDestroyView() {
        OkHttpClientManager.cancel(tag);
        super.onDestroyView();
    }
}
