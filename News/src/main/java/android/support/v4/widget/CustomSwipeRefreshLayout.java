

package android.support.v4.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (isRefreshing()) {
            return false;
        }
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }
}