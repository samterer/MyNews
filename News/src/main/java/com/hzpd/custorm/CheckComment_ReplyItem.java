package com.hzpd.custorm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.ui.activity.LoginActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class CheckComment_ReplyItem {
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private Activity context;

	private HttpUtils httpUtils;

	private RequestParams params;
	private Handler handler;
	private String mType;
	private AnimatorSet set;

	public CheckComment_ReplyItem(Activity context, Handler handler, String mType) {
		this.context = context;
		this.handler = handler;
		this.mType = mType;
		LogUtils.i("mtype-->>>" + mType);

		inflater = LayoutInflater.from(context);

		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
				.imageScaleType(ImageScaleType.EXACTLY)
				.showImageOnFail(R.drawable.icon_comment_touxiang)
				.showImageForEmptyUri(R.drawable.icon_comment_touxiang)
				.showImageOnLoading(R.drawable.icon_comment_touxiang)
				.cacheInMemory(true).cacheOnDisk(true)
				.bitmapConfig(Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300))
				.build();

		httpUtils = new HttpUtils();
		set = new AnimatorSet();


	}

	public View getView(JSONObject object, int reply_level) {//reply_level 1 2 3
		View root = null;
		final ImageView check_comment_touxiang_img;//头像
		final TextView check_comment_name;        //姓名
		final TextView check_comment_name2;        //姓名2
		final TextView check_comment_content;    //回复内容
		final TextView check_comment_num_pl;    //赞数量
		final LinearLayout layout;                //多级回复
		final LinearLayout layout_pop;            //pop
		final ImageView check_comment_num_img;    //赞
		final TextView check_comment_num_one;//赞+1
		final TextView check_comment_time;//评论时间
		switch (reply_level) {
			case 1: {
				root = inflater.inflate(R.layout.check_comment_item_layout, null);
				check_comment_touxiang_img = (ImageView) root.findViewById(R.id.check_comment_touxiang_img);
				check_comment_name = (TextView) root.findViewById(R.id.check_comment_name);
				check_comment_content = (TextView) root.findViewById(R.id.check_comment_content);
				check_comment_num_pl = (TextView) root.findViewById(R.id.check_comment_num_pl);
				check_comment_num_img = (ImageView) root.findViewById(R.id.check_comment_num_img);
				check_comment_num_one = (TextView) root.findViewById(R.id.check_comment_num_one);
				check_comment_time = (TextView) root.findViewById(R.id.check_comment_time);

				layout = (LinearLayout) root.findViewById(R.id.check_comment_reply_linearLayout);
				layout_pop = (LinearLayout) root.findViewById(R.id.check_comment_ll_pop);

				imageLoader.displayImage(object.getString("avatar_path"), check_comment_touxiang_img, options);
				check_comment_name.setText(object.getString("nickname"));
				check_comment_content.setText(object.getString("content"));
				check_comment_num_pl.setText(object.getString("praise"));
				check_comment_time.setText(object.getString("dateline"));

				final String nid = object.getString("cid");

				LogUtils.i("nid-->" + nid);
				check_comment_num_img.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.i("赞");
						Praise(nid, check_comment_num_pl, check_comment_num_one);

					}
				});
				root.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						initPop(layout_pop, nid);
					}
				});
				root.setOnLongClickListener(new OnLongClickListener() {
					public boolean onLongClick(View v) {
						deletePop(layout_pop, nid);
						return false;
					}
				});

				JSONArray array = object.getJSONArray("childs");
				LogUtils.i("array1-->" + array.toJSONString());
				for (int i = 0; i < array.size(); i++) {
					View view = getView(array.getJSONObject(i), 2);
					layout.addView(view);
				}
			}
			break;
			case 2: {
				root = inflater.inflate(R.layout.check_comment_item_layout1, null);
				check_comment_touxiang_img = (ImageView) root.findViewById(R.id.check_comment1_touxiang_img);
				check_comment_name = (TextView) root.findViewById(R.id.check_comment1_name);
				check_comment_content = (TextView) root.findViewById(R.id.check_comment1_content);
				layout = (LinearLayout) root.findViewById(R.id.check_comment_reply2_linearLayout);
				layout_pop = (LinearLayout) root.findViewById(R.id.check_comment1_ll_pop);
				check_comment_time = (TextView) root.findViewById(R.id.check_comment1_time);

				imageLoader.displayImage(object.getString("avatar_path"), check_comment_touxiang_img, options);
				check_comment_name.setText(object.getString("nickname"));
				check_comment_content.setText(object.getString("content"));
//			check_comment_time.setText(object.getString("dateline"));

				final String nid = object.getString("rid");
				root.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						replyPop(layout_pop, nid);
					}
				});
				root.setOnLongClickListener(new OnLongClickListener() {
					public boolean onLongClick(View v) {
						deletePop(layout_pop, nid);
						return false;
					}
				});

				JSONArray array = object.getJSONArray("childs");
				LogUtils.i("array2-->" + array.toJSONString());
				for (int i = 0; i < array.size(); i++) {
					View view = getView(array.getJSONObject(i), 3);
					layout.addView(view);
				}
			}
			break;
			case 3: {
				root = inflater.inflate(R.layout.check_comment_item_layout2, null);
				check_comment_touxiang_img = (ImageView) root.findViewById(R.id.check_comment2_touxiang_iv);
				check_comment_name = (TextView) root.findViewById(R.id.check_comment2_name1);
				check_comment_name2 = (TextView) root.findViewById(R.id.check_comment2_name2);
				check_comment_content = (TextView) root.findViewById(R.id.check_comment2_content);
				layout_pop = (LinearLayout) root.findViewById(R.id.check_comment2_ll_pop);
				check_comment_time = (TextView) root.findViewById(R.id.check_comment2_time);

				imageLoader.displayImage(object.getString("avatar_path"), check_comment_touxiang_img, options);
				check_comment_name.setText(object.getString("nickname"));
				check_comment_name2.setText(object.getString("tonickname"));
				check_comment_content.setText(object.getString("content"));
//			check_comment_time.setText(object.getString("dateline"));

				final String nid = object.getString("rid");
				root.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						replyPop(layout_pop, nid);
					}
				});
				root.setOnLongClickListener(new OnLongClickListener() {
					public boolean onLongClick(View v) {
						deletePop(layout_pop, nid);
						return false;
					}
				});

			}
			break;
		}
		return root;
	}


	//回复  分享  收藏  对一级回复适用
	private void initPop(View v, final String nid) {
		final PopupWindow mPopupWindow = new PopupWindow(context);
		ImageView mTwo;
		ImageView mThree;
		ImageView mFour;

		LinearLayout pv = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.check_comment_popuwindow_layout, null);

		mTwo = (ImageView) pv.findViewById(R.id.check_two);//回复
		mThree = (ImageView) pv.findViewById(R.id.check_three);//分享
		mFour = (ImageView) pv.findViewById(R.id.check_four);//收藏

		mTwo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {//回复
				if (null == SPUtil.getInstance().getUser()) {
					TUtils.toast("请登录");
					return;
				}
				params = new RequestParams();
				params.addBodyParameter("type", "2");
				params.addBodyParameter("nid", nid);
				params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

				Message msg = handler.obtainMessage();
				msg.what = 100;
				msg.obj = params;
				handler.sendMessage(msg);
				mPopupWindow.dismiss();
			}
		});
		mThree.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {//分享
				if (null == SPUtil.getInstance().getUser()) {
					TUtils.toast("请登录");
					return;
				}
			}
		});
		mFour.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {//收藏
				if (null == SPUtil.getInstance().getUser()) {
					TUtils.toast("请登录");
					return;
				}
			}
		});

		mPopupWindow.setContentView(pv);
		mPopupWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		ColorDrawable dw = new ColorDrawable(-00000);
		mPopupWindow.setBackgroundDrawable(dw);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);

		int[] location = new int[2];
		v.getLocationOnScreen(location);
		LogUtils.i("x-" + location[0] + "  y-" + location[1]);
		mPopupWindow.showAsDropDown(v,
				160,
				-130);

		mPopupWindow.update();
		LogUtils.i(mPopupWindow.getWidth() + "   " + mPopupWindow.getHeight());
		LogUtils.i(pv.getWidth() + "   " + pv.getHeight());

	}

	//删除  长按……
	private void deletePop(View v, final String nid) {
		if (null == SPUtil.getInstance().getUser()) {
			TUtils.toast("请登录");
			return;
		}
		if (!SPUtil.getInstance().getUser().getUid().equals(nid)) return;
		PopupWindow mPopupWindow = null;
		LinearLayout pv = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.comment_delete_pop, null);
		ImageView mTwo = (ImageView) pv.findViewById(R.id.comment_delete_img);//删除
		mTwo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RequestParams pa = new RequestParams();
				pa.addBodyParameter("uid", SPUtil.getInstance().getUser().getUid());
				pa.addBodyParameter("rid", nid);
				pa.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

				httpUtils.send(HttpMethod.POST
						, InterfaceJsonfile.DELETEREPLY//InterfaceApi.mDeletereply
						, pa
						, new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0,
					                      String arg1) {
					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						LogUtils.i("delete reply-->" + arg0.result);
						JSONObject obj = JSONObject.parseObject(arg0.result);
						TUtils.toast(obj.getString("msg"));
					}
				});
			}
		});
		mPopupWindow = new PopupWindow(context);
		mPopupWindow.setContentView(pv);
		mPopupWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		ColorDrawable dw = new ColorDrawable(-00000);
		mPopupWindow.setBackgroundDrawable(dw);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);

		mPopupWindow.showAsDropDown(v,
				200,
				-130);
	}

	//回复  短按对二级三级回复适用
	private void replyPop(View v, final String nid) {
		final PopupWindow mPopupWindow = new PopupWindow(context);
		LinearLayout pv = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.comment_reply_pop, null);
		ImageView mTwo = (ImageView) pv.findViewById(R.id.comment_reply_img);//回复
		mTwo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null == SPUtil.getInstance().getUser()) {
					TUtils.toast("请登录");
					return;
				}

				params = new RequestParams();
				params.addBodyParameter("type", "3");
				params.addBodyParameter("nid", nid);
				LogUtils.i("nid-->" + nid);

				Message msg = handler.obtainMessage();
				msg.what = 100;
				msg.obj = params;
				handler.sendMessage(msg);
				mPopupWindow.dismiss();
			}
		});
		mPopupWindow.setContentView(pv);
		mPopupWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		ColorDrawable dw = new ColorDrawable(-00000);
		mPopupWindow.setBackgroundDrawable(dw);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);

		mPopupWindow.showAsDropDown(v, 200, -130);
	}

	//赞
	private void Praise(String nid, final TextView tv, final TextView tv2) {
		if (null == SPUtil.getInstance().getUser()) {
			TUtils.toast("请登录！");
			Intent intent = new Intent(context, LoginActivity.class);
			context.startActivity(intent);
			AAnim.ActivityStartAnimation(context);
			return;
		}
		LogUtils.i("uid-" + SPUtil.getInstance().getUser().getUid() + "  mType-" + mType + " nid-" + nid);
		RequestParams params = new RequestParams();
		params.addBodyParameter("uid", SPUtil.getInstance().getUser().getUid());
		params.addBodyParameter("type", mType);
		params.addBodyParameter("nid", nid);
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
				LogUtils.i("pricse-->" + arg0.result);
				JSONObject obj = JSONObject.parseObject(arg0.result);
				TUtils.toast(obj.getString("msg"));

				if (200 == obj.getInteger("code")) {

					tv2.setVisibility(View.VISIBLE);
					set.playTogether(
							ObjectAnimator.ofFloat(tv2, "translationY", 0, -50),
							ObjectAnimator.ofFloat(tv2, "alpha", 1, 0)
					);
					set.setDuration(1000).start();

					new Handler().postDelayed(new Runnable() {
						public void run() {
							String m = tv.getText().toString();
							LogUtils.i("m---->" + m);

							tv2.setVisibility(View.GONE);
							int i = Integer.parseInt(m);
							i++;
							LogUtils.i("i---->" + i);
							tv.setText(i + "");
						}
					}, 1000);


				}
			}
		});
	}


}
