

package android.support.v4.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hzpd.utils.AvoidOnClickFastUtils;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    return false;
                case MotionEvent.ACTION_DOWN:
                    if (AvoidOnClickFastUtils.isFastDoubleClick(this)) {
                        return false;
                    }
            }
            return super.onTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (isRefreshing()) {
            return false;
        }
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }
}