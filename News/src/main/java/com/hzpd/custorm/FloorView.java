package com.hzpd.custorm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.XF_CommentBean;
import com.hzpd.utils.MyCommonUtil;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 用来显示PostView中盖楼的自定义控件
 *
 * @author Aige
 * @since 2014/11/14
 */
public class FloorView extends LinearLayout {
	private Context context;//上下文环境引用

	private Drawable drawable;//背景Drawable

	public FloorView(Context context) {
		this(context, null);
	}

	public FloorView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FloorView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;

		//获取背景Drawable的资源文件
		drawable = context.getResources().getDrawable(R.drawable.xf_view_post_comment_bg);
	}

	/**
	 * 设置Comment数据
	 *
	 * @param comments Comment数据列表
	 */
	public void setComments(List<XF_CommentBean> comments) {
		//清除子View
		removeAllViews();

		//获取评论数
		int count = comments.size();
	    /*
        如果评论条数小于9条则直接显示，否则我们只显示评论的头两条和最后一条（这里的最后一条是相对于PostView中已经显示的一条评论来说的）
         */
		if (count < 9) {
			initViewWithAll(comments);
		} else {
			initViewWithHide(comments);
		}
	}

	/**
	 * 初始化所有的View
	 *
	 * @param comments 评论数据列表
	 */
	private void initViewWithAll(List<XF_CommentBean> comments) {
		removeAllViews();
		for (int i = 0; i < comments.size() - 1; i++) {
			final XF_CommentBean bean = comments.get(i);
			View commentView = getView(bean, i, comments.size() - 1, false);
			TextView tvContent = (TextView) commentView.findViewById(R.id.view_post_comment_content_tv);
			tvContent.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					replyPop(v, bean);
				}
			});

			addView(commentView);
		}
		this.setVisibility(View.VISIBLE);
	}

	/**
	 * 初始化带有隐藏楼层的View
	 *
	 * @param comments 评论数据列表
	 */
	private void initViewWithHide(final List<XF_CommentBean> comments) {
		View commentView = null;

		//初始化一楼
		commentView = getView(comments.get(0), 0, comments.size() - 1, false);
		addView(commentView);

		//初始化二楼
		commentView = getView(comments.get(1), 1, comments.size() - 1, false);
		addView(commentView);

		//初始化隐藏楼层标识
		commentView = getView(null, 2, comments.size() - 1, true);
		commentView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				initViewWithAll(comments);
			}
		});
		addView(commentView);

		//初始化倒数第二楼
		commentView = getView(comments.get(comments.size() - 2), 3, comments.size() - 1, false);
		addView(commentView);
	}

	/**
	 * 获取单个评论子视图
	 *
	 * @param comment 评论对象
	 * @param index   第几个评论
	 * @param count   总共有几个评论
	 * @param isHide  是否是隐藏显示
	 * @return 一个评论子视图
	 */
	private View getView(XF_CommentBean comment, int index, int count, boolean isHide) {
		//获取根布局
		View commentView = LayoutInflater.from(context).inflate(R.layout.xf_view_post_comment, null);

		//获取控件
		TextView tvUserName = (TextView) commentView.findViewById(R.id.view_post_comment_username_tv);
		TextView tvContent = (TextView) commentView.findViewById(R.id.view_post_comment_content_tv);
		TextView tvNum = (TextView) commentView.findViewById(R.id.view_post_comment_num_tv);
		TextView tvHide = (TextView) commentView.findViewById(R.id.view_post_comment_hide_tv);

        /*
        判断是否是隐藏楼层
         */
		if (isHide) {
            /*
            是则显示“点击显示隐藏楼层”控件而隐藏其他的不相干控件
             */
			tvUserName.setVisibility(GONE);
			tvContent.setVisibility(GONE);
			tvNum.setVisibility(GONE);
			tvHide.setVisibility(VISIBLE);
		} else {
            /*
            否则隐藏“点击显示隐藏楼层”控件而显示其他的不相干控件
             */
			tvUserName.setVisibility(VISIBLE);
			tvContent.setVisibility(VISIBLE);
			tvNum.setVisibility(VISIBLE);
			tvHide.setVisibility(GONE);


			//设置显示数据
			tvUserName.setText(comment.getNickname());
			tvContent.setText(comment.getContent());
			tvNum.setText((index + 1) + "楼");
		}

		//设置布局参数
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		//计算margin指数，这个指数的意义在于将第一个的margin值设置为最大的，然后依次递减体现层叠效果
		int marginIndex = count - index;
		int margin = marginIndex * 3;

		params.setMargins(margin, margin, margin, 0);
		commentView.setLayoutParams(params);

		return commentView;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
        /*
        在FloorView绘制子控件前先绘制层叠的背景图片
         */
		for (int i = getChildCount() - 1; i >= 0; i--) {
			View view = getChildAt(i);
			drawable.setBounds(view.getLeft(), view.getLeft(), view.getRight(), view.getBottom());
			drawable.draw(canvas);
		}
		super.dispatchDraw(canvas);
	}


	private void replyPop(View v, final XF_CommentBean bean) {

		final PopupWindow mPopupWindow = new PopupWindow(context);
		LinearLayout pv = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.comment_delete_pop, null);
		ImageView mTwo = (ImageView) pv.findViewById(R.id.comment_delete_img);//回复
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
		ColorDrawable dw = new ColorDrawable(-00000);
		mPopupWindow.setBackgroundDrawable(dw);
		int[] location = new int[2];


		v.getLocationOnScreen(location);
		mPopupWindow.setAnimationStyle(R.style.AnimationPopup);
		mPopupWindow.showAtLocation(v, Gravity.CENTER_HORIZONTAL
				| Gravity.TOP, 0, location[1] - (int) MyCommonUtil.dp2px(getResources(), 45));
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);

		mPopupWindow.update();
	}
}
