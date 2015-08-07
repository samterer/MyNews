package com.hzpd.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.SubjectNumber;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.ViewHolder;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;

import java.util.HashMap;
import java.util.List;

public class ZhuantiListAdapter extends ListBaseAdapter<NewsBean> {
	private float fontSize = 0;//字体大小

	private HashMap<String, String> couts;

	public ZhuantiListAdapter(Activity c) {
		super(c);
		fontSize = SPUtil.getInstance().getTextSize();
		couts = new HashMap<String, String>();
	}

	@Override
	public void appendData(List<NewsBean> data, boolean isClearOld) {
		super.appendData(data, isClearOld);

		StringBuilder sb = new StringBuilder();
		for (NewsBean bean : data) {
			sb.append(bean.getNid() + ",");
		}
		getCounts(sb.substring(0, sb.length() - 1));
	}

	@Override
	public void appendDataTop(List<NewsBean> data, boolean isClearOld) {
		super.appendDataTop(data, isClearOld);
		StringBuilder sb = new StringBuilder();
		for (NewsBean bean : data) {
			sb.append(bean.getNid() + ",");
		}
		getCounts(sb.substring(0, sb.length() - 1));
	}

	public void setFontSize(float mfontSize) {
		this.fontSize = mfontSize;
		notifyDataSetChanged();
	}

	@Override
	public View getMyView(int position, View convertView, ViewGroup parent) {
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.special_lv_item, parent, false);
		}

		Context context = convertView.getContext();
		LogUtils.i("position" + position);

		TextView special_lvitem_tv_title = ViewHolder.get(convertView, R.id.special_lvitem_tv_title);
		TextView special_lvitem_tv_con = ViewHolder.get(convertView, R.id.special_lvitem_tv_con);
		ImageView special_lvitem_iv = ViewHolder.get(convertView, R.id.special_lvitem_iv);

		NewsBean klb = list.get(position);
		special_lvitem_tv_title.setTextSize(fontSize);
		special_lvitem_tv_con.setTextSize(fontSize - 4);

		special_lvitem_tv_title.setText(klb.getTitle());

		String number = couts.get(klb.getNid());
		if (!TextUtils.isEmpty(number)) {
			special_lvitem_tv_con.setVisibility(View.VISIBLE);
			special_lvitem_tv_con.setText(context.getString(R.string.prompt_reports, number));
		} else {
			special_lvitem_tv_con.setVisibility(View.GONE);
		}
		String imgs[] = klb.getImgs();
		String img = "";
		if (null != imgs && imgs.length > 0) {
			img = imgs[0];
		}
		mImageLoader.displayImage(img, special_lvitem_iv, DisplayOptionFactory.getOption(OptionTp.retRound));


		return convertView;
	}

	private void getCounts(String nid) {
		RequestParams params = new RequestParams();
		params.addBodyParameter("nids", nid);

		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.subjectNum
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String json = responseInfo.result;
				LogUtils.i("getCounts-->" + json);

				JSONObject obj = FjsonUtil
						.parseObject(responseInfo.result);
				if (null == obj) {
					return;
				}

				if (200 == obj.getIntValue("code")) {
					List<SubjectNumber> list = FjsonUtil.parseArray(obj.getString("data"), SubjectNumber.class);
					for (SubjectNumber num : list) {
						couts.put(num.getNid(), num.getNum());
					}
					notifyDataSetChanged();
				}

			}

			@Override
			public void onFailure(HttpException error, String msg) {

			}
		});
	}

}