package com.hzpd.custorm;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MagazineViewpager extends ViewPager {
	private boolean enabled = false;

	public MagazineViewpager(Context context) {
		super(context);
	}

	public MagazineViewpager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setScanScroll(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (enabled) {
			return super.onTouchEvent(event);
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (enabled) {
			return super.onInterceptTouchEvent(event);
		}
		return false;
	}
}
