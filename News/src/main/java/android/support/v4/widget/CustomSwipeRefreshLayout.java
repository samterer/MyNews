

package android.support.v4.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.Log;

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

    boolean isRefresh = false;

    @Override
    public void setRefreshing(boolean refreshing) {
        Log.e("test", "News: " + refreshing);
        isRefresh = refreshing;
        super.setRefreshing(refreshing);
        if(!isRefresh){
            super.setRefreshing(false);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (isRefresh || isRefreshing()) {
            return false;
        }
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }
}