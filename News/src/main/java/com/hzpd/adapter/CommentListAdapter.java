package com.hzpd.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.CommentzqzxBean;
import com.hzpd.ui.activity.LoginActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuqing on 2015/8/13.
 */
public class CommentListAdapter extends BaseAdapter {

	private List<CommentzqzxBean> mDataList = new ArrayList<>();
	private SPUtil spu;
	private HttpUtils httpUtils;

	public CommentListAdapter() {
		spu = SPUtil.getInstance();
		httpUtils = new HttpUtils();
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public CommentzqzxBean getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
			holder = new ViewHolder();
			holder.avatar = (ImageView) convertView.findViewById(R.id.comment_user_avatar);
			holder.userName = (TextView) convertView.findViewById(R.id.comment_user_name);
			holder.content = (TextView) convertView.findViewById(R.id.comment_content);
			holder.time = (TextView) convertView.findViewById(R.id.comment_time);
			holder.digNum = (TextView) convertView.findViewById(R.id.comment_dig_num);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CommentzqzxBean item = getItem(position);

		// 显示头像
		ImageLoader.getInstance().displayImage(item.getAvatar_path(), holder.avatar);

		// 用户名
		holder.userName.setText(item.getNickname());

		// 评论内容
		holder.content.setText(item.getContent());

		// 评论时间
		holder.time.setText(item.getDateline());

		// 点赞数
		holder.digNum.setText(item.getPraise());
		holder.digNum.setTag(item);
		holder.digNum.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					TextView digNum = (TextView) v;
					CommentzqzxBean item = (CommentzqzxBean) v.getTag();
					praise(digNum, item);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		return convertView;
	}

	public void appendData(List<CommentzqzxBean> data) {
		if (data != null && !data.isEmpty()) {
			mDataList.addAll(data);
			notifyDataSetChanged();
		}
	}

	private void praise(final TextView tv, final CommentzqzxBean cb) {
		final Context context = tv.getContext();
		if (null == spu.getUser()) {
			TUtils.toast(context.getString(R.string.toast_please_login));
			Intent intent = new Intent(context, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			if (context instanceof Activity) {
				AAnim.ActivityStartAnimation((Activity) context);
			}
			return;
		}

		Log.i(getLogTag(), "uid-" + spu.getUser().getUid() + "  mType-News" + " nid-" + cb.getCid());
		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("type", "News");
		params.addBodyParameter("cid", cb.getCid());
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

		httpUtils.send(HttpRequest.HttpMethod.POST
				, InterfaceJsonfile.PRISE//InterfaceApi.mPraise
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Log.e(getLogTag(), "赞failed!");
				TUtils.toast(context.getString(R.string.toast_server_no_response));
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				Log.d(getLogTag(), "赞-->" + arg0.result);
				JSONObject obj = JSONObject.parseObject(arg0.result);
				TUtils.toast(obj.getString("msg"));

				if (200 == obj.getInteger("code")) {
					LogUtils.i("m---->" + cb.getPraise());
					if (TextUtils.isDigitsOnly(cb.getPraise())) {
						int i = Integer.parseInt(cb.getPraise());
						i++;
						LogUtils.i("i---->" + i);
						tv.setText(i + "");
						cb.setPraise(i + "");
						notifyDataSetChanged();
					}
				}
			}
		});
	}

	public String getLogTag() {
		return getClass().getSimpleName();
	}

	private static class ViewHolder {
		public ImageView avatar;
		public TextView userName;
		public TextView content;
		public TextView time;
		public TextView digNum;
	}
}
