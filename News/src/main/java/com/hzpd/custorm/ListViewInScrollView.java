package com.hzpd.custorm;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListViewInScrollView extends ListView {

	public ListViewInScrollView(Context context, AttributeSet attrs,
	                            int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ListViewInScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListViewInScrollView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);

		ListAdapter adapter = getAdapter();
		if (null != adapter) {
			int dividerH = adapter.getCount() * getDividerHeight();
			if (dividerH > 0) {
				expandSpec += dividerH;
			}
		}

		super.onMeasure(widthMeasureSpec, expandSpec);
	}


}
