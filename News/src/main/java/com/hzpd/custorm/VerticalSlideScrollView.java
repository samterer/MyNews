/**
 *
 */

package com.hzpd.custorm;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * @author zte 2013-1-23
 */
public class VerticalSlideScrollView extends ScrollView {

	private View inner;// 孩子View
	private float y;// 点击时y坐标
	private Rect normal = new Rect();// 矩形(这里只是个形式，只是用于判断是否需要动画.)

	private boolean isCount = false;// 是否开始计算
	private boolean isStart = false;// 是否开始拖动布局，而不是交给ScrollView自己处理move事件


	public VerticalSlideScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalSlideScrollView(Context context) {
		super(context);
	}

	/**
	 * 根据 XML 生成视图工作完成.该函数在生成视图的最后调用，在所有子视图添加完之后. 即使子类覆盖了 onFinishInflate
	 * 方法，也应该调用父类的方法，使该方法得以执行.
	 */
	@Override
	protected void onFinishInflate() {
		if (getChildCount() > 0) {
			inner = getChildAt(0);
		}
	}

	public int getTempScrollY() {
		return this.mTempScrollY;
	}

	/**
	 * 监听touch
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {


		if (inner != null) {
			if (commOnTouchEvent(ev)) {
				return true;
			}
		}

		return super.onTouchEvent(ev);
	}

	@Override
	public void addView(View child) {
		super.addView(child);

		if (getChildCount() > 0) {
			inner = getChildAt(0);
		}
	}

	/**
	 * 触摸事件
	 *
	 * @param ev
	 */
	public boolean commOnTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isStart = false;
				break;
			case MotionEvent.ACTION_UP:

				// 手指松开.
				if (isNeedAnimation()) {
					animation();
					isCount = false;
				}
				isStart = false;

				break;
			/***
			 * 排除出第一次移动计算，因为第一次无法得知y坐标， 在MotionEvent.ACTION_DOWN中获取不到，
			 * 因为此时是MyScrollView的touch事件传递到到了LIstView的孩子item上面.所以从第二次计算开始.
			 * 然而我们也要进行初始化，就是第一次移动的时候让滑动距离归0. 之后记录准确了就正常执行.
			 */

			case MotionEvent.ACTION_MOVE:

				float preY = y;// 按下时的y坐标
				float nowY = ev.getY();// 时时y坐标

				int deltaY = (int) (preY - nowY);// 滑动Y距离

				if (!isCount) {
					deltaY = 0; // 在这里要归0.
				}
				y = nowY;
				// 当滚动到最上或者最下时就不会再滚动，这时移动布局
				if (isNeedMove(deltaY)) {
					// 初始化头部矩形
					if (normal.isEmpty()) {
						// 保存正常的布局位置
						normal.set(inner.getLeft(), inner.getTop(),
								inner.getRight(), inner.getBottom());
					}

					// 移动布局
					inner.layout(inner.getLeft(), inner.getTop() - deltaY / 2,
							inner.getRight(), inner.getBottom() - deltaY / 2);

					isStart = true;
				}
				isCount = true;
				if (isStart) {
					return true;
				}
				break;

			default:
				break;
		}
		return false;
	}

	/**
	 * 回缩动画
	 */
	public void animation() {
		// 开启移动动画
		TranslateAnimation ta =
				new TranslateAnimation(0, 0, inner.getTop(), normal.top);
		ta.setDuration(150);
		inner.startAnimation(ta);
		// 设置回到正常的布局位置
		inner.layout(normal.left, normal.top, normal.right, normal.bottom);

//		LogUtils.e("回归：" + normal.left + "," + normal.top + "," + normal.right
//				+ "," + normal.bottom);

		normal.setEmpty();

	}

	// 是否需要开启动画
	public boolean isNeedAnimation() {
		return !normal.isEmpty();
	}

	private int mTempScrollY;

	/**
	 * 是否需要移动布局 inner.getMeasuredHeight():获取的是控件的总高度
	 * getHeight()：获取的是屏幕的高度
	 *
	 * @param deltaY
	 * @return
	 */
	public boolean isNeedMove(int deltaY) {

		int scrollY = getScrollY();
		int offset = inner.getMeasuredHeight() - getHeight();
		mTempScrollY = scrollY;
		if ((scrollY == 0 && deltaY < 0) || (scrollY == offset && deltaY > 0)) {
			isStart = true;
		}
		// else{
		// isStart=false;
		// }
		// 0是顶部，后面那个是底部
		if ((scrollY == 0 && isStart) || (scrollY == offset && isStart)) {
			return true;
		}
		return false;
	}

}
