package com.hzpd.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.CommentzqzxBean;
import com.hzpd.ui.activity.LoginActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;

public class CheckcommenthotAdapter extends MyBaseAdater {

	private ArrayList<CommentzqzxBean> hotlist;
	private ArrayList<CommentzqzxBean> latestlist;

	private SPUtil spu;
	private HttpUtils httpUtils;
	private String type;
//	private String uid;

	public CheckcommenthotAdapter(Activity c, String type) {
		super(c);
		latestlist = new ArrayList<CommentzqzxBean>();
		hotlist = new ArrayList<CommentzqzxBean>();

		spu = SPUtil.getInstance();
		httpUtils = new HttpUtils();
//		if(null!=spu.getUser()){
//			uid=spu.getUser().getUid();
//		}
		this.type = type;
	}

	public void setHotCommData(ArrayList<CommentzqzxBean> mhotlist) {
		hotlist.clear();
		hotlist.addAll(mhotlist);

		notifyDataSetChanged();
	}

	public void addLatestData(ArrayList<CommentzqzxBean> mlatestlist) {
		latestlist.addAll(mlatestlist);
		notifyDataSetChanged();
	}

	public void setLatestData(ArrayList<CommentzqzxBean> mlatestlist) {
		clearLatestData();
		addLatestData(mlatestlist);
	}

	public void clearLatestData() {
		latestlist.clear();
	}

	@Override
	public int getCount() {
		return latestlist.size() + hotlist.size();
	}

	@Override
	public Object getItem(int position) {
		if (position < hotlist.size()) {
			return hotlist.get(position);
		} else {
			return latestlist.get(position - hotlist.size());
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LogUtils.i("position-->" + position);
		final ViewHolder holder;
		if (null == convertView) {
			convertView = mLayoutInflater.inflate(R.layout.checkcomment_zqzx_item, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (0 == position && hotlist.size() > 0) {
			holder.cm_item_rl.setVisibility(View.VISIBLE);
			holder.cm_item_tv_t.setText(R.string.comment_item_hot);
		} else if (hotlist.size() == position) {
			holder.cm_item_rl.setVisibility(View.VISIBLE);
			holder.cm_item_tv_t.setText(R.string.comment_item_new);
		} else {
			holder.cm_item_rl.setVisibility(View.GONE);
		}
		final CommentzqzxBean cb;
		if (position < hotlist.size()) {
			cb = hotlist.get(position);
		} else {
			cb = latestlist.get(position - hotlist.size());
		}
		holder.cm_item_tv_time.setText(cb.getDateline());
		holder.cm_item_num_pl.setText(cb.getPraise());
		holder.cm_item_tv_content.setText(cb.getContent());
		holder.cm_item_tv_user.setText(cb.getNickname());

		if (null != spu.getUser() && spu.getUser().getUid() != null && spu.getUser().getUid().equals(cb.getUid()) && "-2".equals(cb.getStatus())) {
			holder.cm_item_tv_comstate.setVisibility(View.VISIBLE);
		} else {
			holder.cm_item_tv_comstate.setVisibility(View.GONE);
		}

		holder.cm_item_ll_praise.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				priaseposition = position;
				praise(holder.cm_item_num_pl, holder.cm_item_num_img);
			}
		});
		return convertView;
	}

	private static class ViewHolder {
		@ViewInject(R.id.cm_item_rl)
		RelativeLayout cm_item_rl;//头部隐藏
		@ViewInject(R.id.cm_item_tv_t)
		TextView cm_item_tv_t;//热门 最新
		@ViewInject(R.id.cm_item_tv_user)
		TextView cm_item_tv_user;//评论人
		@ViewInject(R.id.cm_item_tv_time)//
				TextView cm_item_tv_time;//评论时间
		@ViewInject(R.id.cm_item_num_img)
		ImageView cm_item_num_img;//赞
		@ViewInject(R.id.cm_item_ll_praise)
		LinearLayout cm_item_ll_praise;

		@ViewInject(R.id.cm_item_num_pl)
		TextView cm_item_num_pl;//赞数量
		@ViewInject(R.id.cm_item_tv_content)
		TextView cm_item_tv_content;//评论内容
		@ViewInject(R.id.cm_item_tv_comstate)
		TextView cm_item_tv_comstate;

		public ViewHolder(View view) {
			ViewUtils.inject(this, view);
		}
	}

	private int priaseposition;

	private void praise(final TextView tv, final ImageView imgHand) {
		if (null == spu.getUser()) {
			TUtils.toast("请登录");
			Intent intent = new Intent(mContext, LoginActivity.class);
			mContext.startActivity(intent);
			AAnim.ActivityStartAnimation(mContext);
			return;
		}
		CommentzqzxBean cb;
		if (priaseposition < hotlist.size()) {
			cb = hotlist.get(priaseposition);
		} else {
			cb = latestlist.get(priaseposition - hotlist.size());
		}

		LogUtils.i("uid-" + spu.getUser().getUid() + "  mType-" + type + " nid-" + cb.getCid());
		RequestParams params = RequestParamsUtils.getParamsWithU();
		params.addBodyParameter("type", type);
		params.addBodyParameter("nid", cb.getCid());
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.PRISE//InterfaceApi.mPraise
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				LogUtils.i("赞failed!");
				TUtils.toast("服务器未响应");
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.i("prise-->" + arg0.result);
				JSONObject obj = JSONObject.parseObject(arg0.result);
				TUtils.toast(obj.getString("msg"));

				if (200 == obj.getInteger("code")) {
					AnimatorSet set = new AnimatorSet();
					set.playTogether(
							ObjectAnimator.ofFloat(imgHand, "scaleX", 1, 1.4f, 1),
							ObjectAnimator.ofFloat(imgHand, "scaleY", 1, 1.4f, 1),
							ObjectAnimator.ofFloat(imgHand, "rotation", 0, 45f, 0)
					);
					set.setDuration(500).start();
					set.addListener(new AnimatorListener() {
						@Override
						public void onAnimationStart(Animator animation) {
						}

						@Override
						public void onAnimationRepeat(Animator animation) {
						}

						@Override
						public void onAnimationEnd(Animator animation) {

							CommentzqzxBean cb;
							if (priaseposition < hotlist.size()) {
								cb = hotlist.get(priaseposition);
							} else {
								cb = latestlist.get(priaseposition - hotlist.size());
							}

//								String m=tv.getText().toString();
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

						@Override
						public void onAnimationCancel(Animator animation) {
						}
					});

				}
			}
		});
	}

}