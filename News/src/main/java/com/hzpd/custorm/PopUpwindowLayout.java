package com.hzpd.custorm;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.utils.DisplayUtil;

import java.util.List;

/**
 * [PopUpWindow中的标题容器]
 * 
 * @author mashidong
 * @version V3.0
 * */
public class PopUpwindowLayout extends LinearLayout {

	Context mContext;
	private LayoutInflater mInflater;
	private OnClickCallback mCallback;

	public PopUpwindowLayout(Context context) {
		this(context, null);
	}

	public PopUpwindowLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PopUpwindowLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOrientation(HORIZONTAL);
		setBackgroundResource(R.drawable.bmap_bg);
	}

	/**
	 * 设置标题内容(默认带向下箭)?
	 * 
	 * @param context
	 * @param titles
	 *            标题文字内容
	 * */
	public void initViews(Context context, List<String> titles) {
		initViews(context, titles, true);
	}

	/**
	 * 设置标题内容
	 * 
	 * @param context
	 * @param titles
	 *            标题文字内容
	 * @param hasDraw
	 *            是否带右侧向下箭�?
	 * */
	public void initViews(Context context, List<String> titles, boolean hasDraw) {
		this.mContext = context;
		setLayoutContent(mContext, titles, hasDraw);
	}

	/** 设置条目点击监听 */
	public void setClickListener(OnClickCallback callback) {
		this.mCallback = callback;
	}

	/**
	 * 设置内容
	 * 
	 * @param context
	 * @param titles
	 * @param hasDraw
	 */
	private void setLayoutContent(Context context, final List<String> titles, boolean hasDraw) {
		removeAllViews();
		if (titles != null && titles.size() > 0) {
			// 不带箭头
			if (!hasDraw) {
				for (int i = 0; i < titles.size(); i++) {
					final int index = i;
					final TextView textView = new TextView(context);
					// 文本
					textView.setText(titles.get(i));
					// 颜色
					textView.setTextColor(getContext().getResources().getColor(R.color.white));
					float titleSize = getContext().getResources().getDimension(R.dimen.text_size_16);
					// 字体
					textView.setTextSize(DisplayUtil.px2sp(context, titleSize));
					textView.setGravity(Gravity.CENTER);
					textView.setPadding(20, 0, 20, 0);
					LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
					params.gravity = Gravity.CENTER;
					textView.setLayoutParams(params);
					addView(textView);
					if (i < titles.size() - 1) {
						LayoutParams layoutParams = new LayoutParams(1, 48);
						layoutParams.gravity = Gravity.CENTER_VERTICAL;
						View view = new View(context);
						view.setLayoutParams(layoutParams);
						view.setBackgroundResource(R.color.white);
						addView(view);
					}
					textView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (mCallback != null) {
								mCallback.onItemClick(PopUpwindowLayout.this, titles.size(), index);
							}
						}
					});
				}
			}
		} else {
			throw new RuntimeException("title counts<0");
		}
	}

	/** 点击事件接口 */
	public interface OnClickCallback {
		/**
		 * 点击子视图时调用
		 * 
		 * @param parentView
		 *            当前操作的View视图
		 * @param size
		 *            当前视图中子视图数量
		 * @param index
		 *            当前点击子视图索�?
		 */
		public void onItemClick(LinearLayout parentView, int size, int index);
	}

}
