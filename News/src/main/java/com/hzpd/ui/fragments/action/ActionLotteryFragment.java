package com.hzpd.ui.fragments.action;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.custorm.LotteryView;
import com.hzpd.hflt.R;
import com.hzpd.modle.LotteryDrawBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
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

public class ActionLotteryFragment extends BaseFragment {


	@ViewInject(R.id.lotteryv_tv)
	private TextView lotteryv_tv;

	@ViewInject(R.id.lotteryv)
	private LotteryView lotteryv;
	@ViewInject(R.id.lotteryv_tv_number)
	private TextView lotteryv_tv_number;
	@ViewInject(R.id.lotteryv_tv_price)
	private TextView lotteryv_tv_price;
	@ViewInject(R.id.lottery_bt)
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.action_lottery_layout, container, false);
		ViewUtils.inject(this, view);
		return view;
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

	private void getInfo() {

		String station= SharePreferecesUtils.getParam(getActivity(), StationConfig.STATION, "def").toString();
		String drawPrice_url =null;
		if (station.equals(StationConfig.DEF)){
			drawPrice_url =InterfaceJsonfile.drawPrice;
		}else if (station.equals(StationConfig.YN)){
			drawPrice_url = InterfaceJsonfile_YN.drawPrice;
		}else if (station.equals(StationConfig.TW)){
			drawPrice_url = InterfaceJsonfile_TW.drawPrice;
		}
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("device", androidId);
		params.addBodyParameter("subjectid", subjectid);

		httpUtils.send(HttpMethod.POST
				, drawPrice_url
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
					lotterydraw = FjsonUtil.parseObject(obj.getString("data")
							, LotteryDrawBean.class);
					lotteryv_tv.setText(lotterydraw.getInfo() + "");

					lotteryv_tv_number.setText(getString(R.string.toast_left_vote_award_today,
							lotterydraw.getTod_lastvotes(), lotterydraw.getLastdraw()));
					lotteryv_tv_price.setText(getString(R.string.toast_award, lotterydraw.getPrize()));


					if ("2".equals(lotterydraw.getStatus())) {
						lottery_bt.setVisibility(View.VISIBLE);
						lotteryv_tv_price.setVisibility(View.VISIBLE);
					} else {
						try {
							int last = Integer.parseInt(lotterydraw.getLastdraw());
							if (last > 0) {
								lotteryv.setVisibility(View.VISIBLE);
								lotteryv.setText(lotterydraw.getLevel());
							} else {
								TUtils.toast(getString(R.string.toast_lottery_running_out));
							}
						} catch (Exception e) {

						}
					}
				} else {
					TUtils.toast(obj.getString("msg"));
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				TUtils.toast(getString(R.string.toast_server_no_response));
			}
		});
	}

	@OnClick(R.id.lottery_bt)
	private void gotoAccess(View view) {
		Bundle args = new Bundle();
		args.putString("number", lotterydraw.getNumber());
		args.putString("androidid", androidId);
		args.putString("subjectid", subjectid);
		args.putString("price", lotterydraw.getPrize());
		args.putString("from", from);
		((ActionDetailActivity) activity).toLotteryPinfo(args);
	}

}