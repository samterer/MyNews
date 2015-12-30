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
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class VotePinfoFragment extends BaseFragment {

	private TextView vote_detail_tv_right;
	private TextView vote_detail_tv_bottom;
	private ListView vote_detail_lv;
	private Paint paint = new Paint();
	private Vote_detailMultiPicBean bean;
	private VoteDetailMultiPicAdapter adapter;
	private String androidId;    //
	private String newsid;        //
	private String optid;
	private String subjectid;
	private String isRadio;
	private String actionname;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.vote_detail_multipic_layout, container, false);
		vote_detail_tv_right= (TextView) view.findViewById(R.id.vote_detail_tv_right);
		vote_detail_tv_bottom= (TextView) view.findViewById(R.id.vote_detail_tv_bottom);
		vote_detail_lv= (ListView) view.findViewById(R.id.vote_detail_lv);
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

		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("optid", optid);
		params.addBodyParameter("device", androidId);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.mOptbyoptid
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
					adapter.appendData(bean.getOption().getImgurls(), true);
					adapter.notifyDataSetChanged();
				} else {

				}

			}
		});

	}

}