package com.hzpd.custorm;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TopPicViewPager extends ViewPager {
	private float mX;
	private float mY;


	public TopPicViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TopPicViewPager(Context context) {
		super(context);

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mX = ev.getX();
				mY = ev.getY();
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			break;
			case MotionEvent.ACTION_MOVE: {
				if (Math.abs(ev.getX() - mX) > Math.abs(ev.getY() - mY)) {
					if (ev.getX() - mX > 10) {//left-->right
						int c = getCurrentItem();
						if (0 == c) {
							getParent().requestDisallowInterceptTouchEvent(false);
						} else {
							getParent().requestDisallowInterceptTouchEvent(true);
						}
					} else if (ev.getX() - mX < -10) {
						int c = getCurrentItem();
						int total = getAdapter().getCount();
						if (total == c + 1) {
							getParent().requestDisallowInterceptTouchEvent(false);
						} else {
							getParent().requestDisallowInterceptTouchEvent(true);
						}
					} else {
						getParent().requestDisallowInterceptTouchEvent(true);
					}

				} else {
					getParent().requestDisallowInterceptTouchEvent(false);
				}
			}
			break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL: {
				getParent().requestDisallowInterceptTouchEvent(false);
			}
			break;
		}
		if (this.getChildCount() < 1) {
			return false;
		}
		return super.dispatchTouchEvent(ev);
	}

}
