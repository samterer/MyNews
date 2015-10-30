package com.hzpd.ui.fragments.vote;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.VoteDetailMultiPicAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.vote.Vote_detailMultiPicBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class VotePinfoFragment extends BaseFragment {

	@ViewInject(R.id.vote_detail_tv_right)
	private TextView vote_detail_tv_right;
	@ViewInject(R.id.vote_detail_tv_bottom)
	private TextView vote_detail_tv_bottom;
	@ViewInject(R.id.vote_detail_lv)
	private ListView vote_detail_lv;


	private int screenWidth = 0;
	private int count = 0;
	private float textTotalWidth = 0.0f;
	private float textWidth = 0.0f;
	private Paint paint = new Paint();

	//	private JSONObject object;//
	private Vote_detailMultiPicBean bean;
	private VoteDetailMultiPicAdapter adapter;
	private String text = "";

	private String androidId;    //
	private String newsid;        //
	private String optid;
	private String subjectid;
	private String isRadio;

	private boolean isVoted;
	private String actionname;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.vote_detail_multipic_layout, container, false);
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
		newsid = args.getString("newsid");
		optid = args.getString("optionid");
		subjectid = args.getString("subjectid");
		isRadio = args.getString("isRadio");
		actionname = args.getString("actionname");

		androidId = MyCommonUtil.getMyUUID(activity);


		adapter = new VoteDetailMultiPicAdapter(activity);
		vote_detail_lv.setAdapter(adapter);

//		if("1".equals(isRadio)){
//			vote_detail_bt_submit.setVisibility(View.GONE);
//		}

		getInfoFromServer();

	}


	/**
	 * @OnClick(R.id.vote_detail_bt_submit) private void vote(View v){
	 * <p>
	 * if(!isVoted){
	 * DebugUntil.createInstance().toastStr("已投票！");
	 * return;
	 * }
	 * <p>
	 * RequestParams params=new RequestParams();
	 * params.addBodyParameter("uid",uid);
	 * params.addBodyParameter("device",androidId);
	 * params.addBodyParameter("subjectid",subjectid);
	 * params.addBodyParameter("optionid",optid);
	 * <p>
	 * //		vote_detail_bt_submit.setClickable(false);
	 * httpUtils.send(HttpMethod.POST
	 * , InterfaceApi.mSetvote
	 * , params
	 * , new RequestCallBack<String>() {
	 * @Override public void onFailure(HttpException arg0, String arg1) {
	 * //					vote_detail_bt_submit.setClickable(true);
	 * }
	 * @Override public void onSuccess(ResponseInfo<String> arg0) {
	 * //					vote_detail_bt_submit.setClickable(true);
	 * LogUtils.i("submit-->"+arg0.result);
	 * JSONObject obj=JSONObject.parseObject(arg0.result);
	 * if(200==obj.getIntValue("code")){
	 * DebugUntil.createInstance().toastStr(obj.getString("data"));
	 * isVoted=true;
	 * //						vote_detail_bt_submit.setClickable(false);
	 * }
	 * }
	 * });
	 * }
	 */


	private void getInfoFromServer() {

		String station= SharePreferecesUtils.getParam(getActivity(), StationConfig.STATION, "def").toString();
		String mOptbyoptid_url =null;
		if (station.equals(StationConfig.DEF)){
			mOptbyoptid_url =InterfaceJsonfile.mOptbyoptid;
		}else if (station.equals(StationConfig.YN)){
			mOptbyoptid_url = InterfaceJsonfile_YN.mOptbyoptid;
		}else if (station.equals(StationConfig.TW)){
			mOptbyoptid_url = InterfaceJsonfile_TW.mOptbyoptid;
		}

		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("optid", optid);
		params.addBodyParameter("device", androidId);

		httpUtils.send(HttpMethod.POST
				, mOptbyoptid_url
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				LogUtils.i("");
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.e("getInfoFromServer:" + arg0.result);

				JSONObject obj = FjsonUtil.parseObject(arg0.result);
				if (null == obj) {
					return;
				}
				if (200 == obj.getIntValue("code")) {
					bean = JSONObject.parseObject(obj.getString("data"), Vote_detailMultiPicBean.class);
					vote_detail_tv_right.setText(getString(R.string.prompt_name, bean.getOption().getName()));
					vote_detail_tv_bottom.setText(getString(R.string.prompt_brief_intro, bean.getOption().getDescription()));
//						mImageLoader.displayImage(object.getJSONObject("option").getString("imgurl"), vote_detail_imge,diOp2);
//						if("1".equals(object.getJSONObject("option").getString("status"))){
//							vote_detail_bt_submit.setClickable(false);
//						}
					adapter.appendData(bean.getOption().getImgurls(), true);
					adapter.notifyDataSetChanged();
				} else {

				}

			}
		});

	}

	//	@OnClick(R.id.votedmulti_ll_share)
	private void shareInfo(View v) {
		if (null == bean) {
			return;
		}

	}

}