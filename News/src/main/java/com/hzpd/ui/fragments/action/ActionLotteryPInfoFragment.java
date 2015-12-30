package com.hzpd.ui.fragments.action;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;

import de.greenrobot.event.EventBus;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.action_lottery_pi_layout, container, false);
        lotterypi_tv_price = (TextView) view.findViewById(R.id.lotterypi_tv_price);
        lotterypi_uname = (EditText) view.findViewById(R.id.lotterypi_uname);
        lotterypi_phone = (EditText) view.findViewById(R.id.lotterypi_phone);
        lotterypi_add = (EditText) view.findViewById(R.id.lotterypi_add);
        lotterypi_submit = view.findViewById(R.id.lotterypi_submit);
        lotterypi_submit.setOnClickListener(this);
        return view;
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
            case R.id.lotterypi_submit: {
                String uname = lotterypi_uname.getText().toString();
                if (TextUtils.isEmpty(uname)) {
                    TUtils.toast(getString(R.string.toast_name_cannot_be_empty));
                    return;
                }
                String phone = lotterypi_phone.getText().toString();
                if (TextUtils.isEmpty(phone)) {
                    TUtils.toast(getString(R.string.toast_phone_cannot_be_empty));
                    return;
                }

                String addr = lotterypi_add.getText().toString();
                RequestParams params = RequestParamsUtils.getParams();
                params.addBodyParameter("username", uname);
                params.addBodyParameter("phone", phone);
                params.addBodyParameter("address", addr);
                params.addBodyParameter("device", androidid);
                params.addBodyParameter("subjectid", subjectid);
                params.addBodyParameter("number", number);

                httpUtils.send(HttpMethod.POST
                        , InterfaceJsonfile.submitPriceInfo
                        , params
                        , new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        LogUtils.i("result-->" + responseInfo.result);
                        JSONObject obj = FjsonUtil.parseObject(responseInfo.result);

                        if (null == obj) {
                            return;
                        }

                        if (200 == obj.getIntValue("code")) {

                            lotterypi_uname.setText("");
                            lotterypi_phone.setText("");
                            lotterypi_add.setText("");

                            if ("vote".equals(from)) {
                                EventBus.getDefault().post("456");
                            }
                        }
                        TUtils.toast(obj.getString("msg"));

                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        LogUtils.i("" + msg);
                    }
                });
            }
            break;
        }
    }
}
