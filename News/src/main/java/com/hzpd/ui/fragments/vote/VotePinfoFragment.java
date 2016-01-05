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
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.squareup.okhttp.Request;

import java.util.Map;

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
	private Object tag;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.vote_detail_multipic_layout, container, false);
		vote_detail_tv_right= (TextView) view.findViewById(R.id.vote_detail_tv_right);
		vote_detail_tv_bottom= (TextView) view.findViewById(R.id.vote_detail_tv_bottom);
		vote_detail_lv= (ListView) view.findViewById(R.id.vote_detail_lv);
		tag= OkHttpClientManager.getTag();
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
		getInfoFromServer();

	}


	private void getInfoFromServer() {

		Map<String,String> params = RequestParamsUtils.getMaps();
		params.put("optid", optid);
		params.put("device", androidId);

		OkHttpClientManager.postAsyn(tag
				, InterfaceJsonfile.mOptbyoptid
				, new OkHttpClientManager.ResultCallback() {
			@Override
			public void onSuccess(Object response) {
				Log.e("rest","getInfoFromServer:" + response.toString());

				JSONObject obj = FjsonUtil.parseObject(response.toString());
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

			@Override
			public void onFailure(Request request, Exception e) {
			}
		}, params);

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		OkHttpClientManager.cancel(tag);
	}
}