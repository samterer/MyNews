package com.hzpd.custorm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.XF_CommentBean;
import com.hzpd.modle.XF_UserCommNewsBean;
import com.hzpd.modle.XF_UserCommentsBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 用来显示Post的自定义控件
 *
 * @author Aige
 * @since 2014/11/14
 */
public class  UserCommPostView extends LinearLayout {
	private TextView tvType; // 显示类型标签
	private TextView tvUserName; // 用户名
	private TextView tvLocation; // 地理位置
	private TextView tvDate; // 日期
	private TextView tvPraise; // 赞数据
	private ImageView view_post_praise_iv;// 赞图标
	private TextView tvContent; // 最后一条评论内容的TextView
	private CircleImageView civNick;// 用户圆形头像显示控件
	private TextView xf_post_tv;//文章标题
	private FloorView floorView; // 盖楼控件

	private ImageLoader imgLoader;

	public UserCommPostView(Context context) {
		this(context, null);
	}

	public UserCommPostView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public UserCommPostView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// 初始化控件
		initWidget(context);
	}

	/**
	 * 初始化控件
	 *
	 * @param context 上下文环境引用
	 */
	private void initWidget(Context context) {
		// 设置布局
		LayoutInflater.from(context).inflate(R.layout.xf_usercomm_post, this);

		// 获取控件
		tvType = (TextView) findViewById(R.id.view_post_type_tv);
		tvUserName = (TextView) findViewById(R.id.view_post_username_tv);
		tvLocation = (TextView) findViewById(R.id.view_post_location_tv);
		tvDate = (TextView) findViewById(R.id.view_post_date_tv);
		view_post_praise_iv = (ImageView) findViewById(R.id.view_post_praise_iv);
		tvPraise = (TextView) findViewById(R.id.view_post_praise_tv);
		tvContent = (TextView) findViewById(R.id.view_post_content_tv);
		civNick = (CircleImageView) findViewById(R.id.view_post_nick_civ);
		xf_post_tv = (TextView) findViewById(R.id.xf_post_tv);
		floorView = (FloorView) findViewById(R.id.view_post_floor_fv);
		imgLoader = ImageLoader.getInstance();
	}

	/**
	 * 为PostView设置数据
	 *
	 * @param post 数据源
	 */
	public void setPost(XF_UserCommentsBean post) {

		// 获取该条帖子下的评论列表
		List<XF_CommentBean> comments = post.getComs();
		/*
		 * 判断评论长度 1.如果只有一条评论那么则显示该评论即可并隐藏盖楼布局 2.否则我们进行盖楼显示
		 */
		if (comments.size() == 1) {
			floorView.setVisibility(GONE);
			XF_CommentBean comment = comments.get(0);
			// 设置控件显示数据
			initUserDate(post);
		} else {
			// 盖楼前我们要把最后一条评论数据提出来显示在Post最外层
			int index = comments.size() - 1;
			XF_CommentBean comment = comments.get(index);

			// 设置控件显示数据
			initUserDate(post);

			floorView.setComments(comments);
		}
	}

	/**
	 * 设置与用户相关的控件数据显示
	 *
	 * @param Usercomment 评论对象
	 */
	private void initUserDate(XF_UserCommentsBean Usercomment) {

		XF_UserCommNewsBean bean = Usercomment.getContent();
		List<XF_CommentBean> list = Usercomment.getComs();
		final XF_CommentBean myComm = list.get(list.size() - 1);

		tvContent.setText(myComm.getContent());
		tvDate.setText(myComm.getDateline());
		tvUserName.setText(myComm.getNickname());
		tvPraise.setText(myComm.getPraise());
		tvLocation.setText(myComm.getUlevel());

		xf_post_tv.setText(getContext().getString(R.string.prompt_comment_belong_post, bean.getTitle()));


		imgLoader.displayImage(myComm.getAvatar_path(), civNick,
				DisplayOptionFactory.getOption(OptionTp.Small));

		tvContent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				replyPop(v, myComm);
			}
		});
		view_post_praise_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				priase(myComm);
			}
		});
		civNick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				EventBus.getDefault().post(myComm.getUid());
			}
		});
	}

	private void replyPop(View v, final XF_CommentBean bean) {

		final PopupWindow mPopupWindow = new PopupWindow(getContext());
		LinearLayout pv = (LinearLayout) LayoutInflater.from(getContext())
				.inflate(R.layout.comment_delete_pop, null);
		ImageView mTwo = (ImageView) pv.findViewById(R.id.comment_delete_img);// 回复
		mTwo.setImageResource(R.drawable.bt_huifu_unselected);

		mTwo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
				EventBus.getDefault().post(bean);
			}
		});

		mPopupWindow.setContentView(pv);
		mPopupWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
		mPopupWindow.setBackgroundDrawable(dw);

		// mPopupWindow.showAsDropDown(v,
		// v.getWidth()/2-30,
		// -v.getHeight());

		int[] location = new int[2];

		v.getLocationOnScreen(location);
		mPopupWindow.setAnimationStyle(R.style.AnimationPopup);
		mPopupWindow.showAtLocation(v, Gravity.CENTER_HORIZONTAL | Gravity.TOP,
				0, location[1] - (int) MyCommonUtil.dp2px(getResources(), 40f));
		LogUtils.i("y:" + location[1] + "  windowH:" + mPopupWindow.getHeight());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);

		mPopupWindow.update();

	}

	private void priase(final XF_CommentBean comment) {

		if (null == SPUtil.getInstance().getUser()) {
			TUtils.toast("请登录");
			return;
		}
		TUtils.toast("👍");

		RequestParams params = new RequestParams();
		params.addBodyParameter("uid", SPUtil.getInstance().getUser().getUid());
		params.addBodyParameter("type", "News");
		params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
		params.addBodyParameter("cid", comment.getCid());
		HttpUtils httpUtils = SPUtil.getHttpUtils();
		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.XF_PRAISECOM, params,
				new RequestCallBack<String>() {
					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						LogUtils.i("praiseArtical-result-->"
								+ responseInfo.result);
						JSONObject obj = FjsonUtil
								.parseObject(responseInfo.result);
						if (null == obj) {
							return;
						}
						if (200 == obj.getIntValue("code")) {
							JSONObject object = obj.getJSONObject("data");
							tvPraise.setText(object.getString("exp"));
							comment.setPraise(object.getString("exp"));
						} else {
							LogUtils.i(obj.getString("msg"));
						}
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						LogUtils.e("praiseArtical-failed->" + msg);
					}
				});

	}

}