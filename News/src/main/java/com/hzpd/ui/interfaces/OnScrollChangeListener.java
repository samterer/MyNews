package com.hzpd.ui.interfaces;

import android.view.View;

/**
 * Created by AvatarQing on 2015/7/24.
 */
public interface OnScrollChangeListener {
	/**
	 * Called when the scroll position of a view changes.
	 *
	 * @param v          The view whose scroll position has changed.
	 * @param scrollX    Current horizontal scroll origin.
	 * @param scrollY    Current vertical scroll origin.
	 * @param oldScrollX Previous horizontal scroll origin.
	 * @param oldScrollY Previous vertical scroll origin.
	 */
	void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
}