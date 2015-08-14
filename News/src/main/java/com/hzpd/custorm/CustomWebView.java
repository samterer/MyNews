package com.hzpd.custorm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

import com.hzpd.ui.interfaces.OnScrollChangeListener;

/**
 * Created by AvatarQing on 2015/7/24.
 */
public class CustomWebView extends WebView {

	private View mTitleBar;
	private Matrix mMatrix = new Matrix();
	private Rect mClipBounds = new Rect();

	private OnScrollChangeListener mOnScrollChangeListener;

	public CustomWebView(Context context) {
		super(context);
	}

	public CustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTitleBar = getChildAt(0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();

		if(mTitleBar != null) {
			final int sy = getScrollY();
			final int sx = getScrollX();
			mClipBounds.top = sy;
			mClipBounds.left = sx;
			mClipBounds.right = mClipBounds.left + getWidth();
			mClipBounds.bottom = mClipBounds.top + getHeight();
			canvas.clipRect(mClipBounds);
			mMatrix.set(canvas.getMatrix());
			int titleBarOffs = mTitleBar.getHeight() - sy;
			if(titleBarOffs < 0) titleBarOffs = 0;
			mMatrix.postTranslate(0, titleBarOffs);
			canvas.setMatrix(mMatrix);
		}

		super.onDraw(canvas);
		canvas.restore();
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		if(child == mTitleBar) {
			mClipBounds.top = 0;
			mClipBounds.left = 0;
			mClipBounds.right = mClipBounds.left + child.getWidth();
			mClipBounds.bottom = child.getHeight();
			canvas.save();
			child.setDrawingCacheEnabled(true);
			mMatrix.set(canvas.getMatrix());
			mMatrix.postTranslate(getScrollX(), -getScrollY());
			canvas.setMatrix(mMatrix);
			canvas.clipRect(mClipBounds);
			child.draw(canvas);
			canvas.restore();

			return false;
		}

		return super.drawChild(canvas, child, drawingTime);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (mOnScrollChangeListener != null) {
			mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
		}
//		if (mTitleBar != null) {
//			mTitleBar.offsetLeftAndRight(l - mTitleBar.getLeft());
//		}
	}

	private int visibleTitleHeight() {
		if (mTitleBar == null) {
			return 0;
		} else {
			return mTitleBar.getMeasuredHeight() - getScrollY();
		}
	}

	public void setOnScrollChangeListener(OnScrollChangeListener listener) {
		mOnScrollChangeListener = listener;
	}

}