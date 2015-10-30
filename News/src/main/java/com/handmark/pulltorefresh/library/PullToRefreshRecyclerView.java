package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;


/**
 * Support RecyclerView
 *
 * @author Dean.Ding
 */
public class PullToRefreshRecyclerView extends PullToRefreshBase<RecyclerView> {

    public PullToRefreshRecyclerView(Context context) {
        super(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode,
                                     AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected RecyclerView createRefreshableView(Context context,
                                                 AttributeSet attrs) {
        RecyclerView recyclerView = new RecyclerView(context, attrs);
        recyclerView.setId(android.R.id.list);
        return recyclerView;
    }

    @Override
    protected boolean isReadyForPullStart() {
        boolean result = false;

        if (mRefreshableView.getChildCount() <= 0) {
            result = true;
        }
        int firstVisiblePosition = mRefreshableView
                .getChildPosition(mRefreshableView.getChildAt(0));
        if (firstVisiblePosition == 0) {
            result = mRefreshableView.getChildAt(0).getTop() >= 0;
        } else {
            result = false;
        }
        return result;
    }

    @Override
    protected boolean isReadyForPullEnd() {
        int lastVisiblePosition = mRefreshableView
                .getChildPosition(mRefreshableView.getChildAt(mRefreshableView
                        .getChildCount() - 1));

        RecyclerView.Adapter<?> adpater = mRefreshableView.getAdapter();
        if (adpater != null
                && lastVisiblePosition >= adpater.getItemCount() - 1) {
            View lastChild = mRefreshableView.getChildAt(mRefreshableView
                    .getChildCount() - 1);
            if (lastChild != null) {
                return lastChild.getBottom() <= mRefreshableView.getBottom();
            }
        }
        return false;
    }

}
