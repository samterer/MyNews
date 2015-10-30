package com.hzpd.custorm;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Scroller;

import com.avatarqing.loadmore.lib.LoadMoreContainerBase;
import com.hzpd.ui.interfaces.OnScrollChangeListener;
import com.hzpd.utils.Log;

/**
 * Created by AvatarQing on 2015/7/23.
 */
public class CustomScrollView extends ViewGroup {

    private boolean isDebug = true;

    private CustomWebView mWebView;
    private ListView mListView;
    private ViewGroup mListViewContainer;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private boolean mHasMoved = false;
    private float mLastY;

    private float mMaxVelocity;
    private float mMinVelocity;

    private MotionEvent mDownEvent;
    private MotionEvent mLastMoveEvent;

    public CustomScrollView(Context context) {
        super(context);
        init();
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
        mMinVelocity = vc.getScaledMinimumFlingVelocity();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() == 2) {
            mWebView = (CustomWebView) getChildAt(0);
            mListViewContainer = (ViewGroup) getChildAt(1);
            mListView = (ListView) mListViewContainer.getChildAt(0);
            mWebView.setOnScrollChangeListener(mOnScrollChangeListener);
        } else {
            throw new IllegalStateException("There must be two child");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);

        int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        mWebView.measure(widthSpec, heightSpec);
        widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        heightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED);
        mListViewContainer.measure(widthSpec, heightSpec);

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = mWebView.getMeasuredWidth();
        int height = mWebView.getMeasuredHeight();

        int wvTop = mWebView.getTop();
        int wvBottom = wvTop + height;
        mWebView.layout(0, wvTop, width, wvBottom);

        int lvcTop = height + wvTop;
        int lvcBottom = lvcTop + mListViewContainer.getMeasuredHeight();

        if (mListViewContainer.getVisibility() == VISIBLE) {
            mListViewContainer.layout(0, lvcTop, width, lvcBottom);
            _ContainerViewWidth = mListViewContainer.getMeasuredWidth();
            _ContainerViewHeight = mListViewContainer.getMeasuredHeight();
            mostTop = -_ContainerViewHeight;
        }

        _mWidth = getMeasuredWidth();
        _mHeight = getMeasuredHeight();
        _WebViewWidth = mWebView.getMeasuredWidth();
        _WebViewHeight = mWebView.getMeasuredHeight();
    }

    int _mWidth;
    int _mHeight;
    int _WebViewWidth;
    int _WebViewHeight;
    int _ContainerViewWidth;
    int _ContainerViewHeight;
    int mostTop;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mListViewContainer.getVisibility() != VISIBLE) {
            return super.dispatchTouchEvent(ev);
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getActionMasked();
        final int actionIndex = ev.getActionIndex();

        if (actionIndex > 0) {
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownEvent = ev;
                mLastY = ev.getY();
                mHasMoved = false;
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                break;
            case MotionEvent.ACTION_MOVE: {
                mLastMoveEvent = ev;
                float yDiff = ev.getY() - mLastY;
                mLastY = ev.getY();
                if (yDiff == 0) {
                    break;
                }
                mHasMoved = true;
                boolean isScrollFromUpToDown = yDiff > 0;
                boolean isScrollFromDownToUp = !isScrollFromUpToDown;
                if (isScrollFromDownToUp && isWebViewScrolledAtEnd()) {
                    sendCancelEvent();
                    offsetContent(yDiff);
                    return true;
                }
                if (isScrollFromUpToDown && isListViewVisible() && mWebView.getTop() < 0) {
                    sendCancelEvent();
                    offsetContent(yDiff);
                    if (mWebView.getTop() == 0) {
                        sendDownEvent();
                    }
                    return true;
                }

                break;
            }
            case MotionEvent.ACTION_UP: {
                boolean intercept = false;
                if (mHasMoved && isWebViewScrolledAtEnd()) {
                    intercept = true;
                    sendCancelEvent();
                    // fling

                    mVelocityTracker.computeCurrentVelocity(1000);
                    int velocityY = (int) mVelocityTracker.getYVelocity();
                    velocityY = clampMag(velocityY, (int) mMinVelocity, (int) mMaxVelocity);
                    mScroller.fling(0, mWebView.getTop(),
                            0, velocityY,
                            0, 0,
                            -_ContainerViewHeight, 0);
                    invalidate();
                }
                mHasMoved = false;
                cancelVelocityTrack();
                if (intercept) {
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
                mHasMoved = false;
                cancelVelocityTrack();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int offset = mScroller.getCurrY() - mWebView.getTop();
            offsetContent(offset);
        }
    }

    private boolean isWebViewScrolledAtEnd() {
        return mWebView.getHeight() + mWebView.getScrollY() + 2 >= ((int) (mWebView.getContentHeight() * mWebView.getScale()));
    }

    private boolean isListViewVisible() {
        return mListView != null && mListView.getVisibility() == VISIBLE && mListView.getChildCount() > 0 && mListView.getFirstVisiblePosition() >= 0;
    }


    private boolean isWebViewFullVisible() {
        return mWebView.getTop() == 0;
    }

    private boolean isListViewFullVisible() {
        return mWebView.getTop() == mostTop;
    }

    private boolean isSwitchingPage() {
        return mWebView.getTop() > -mWebView.getHeight() && mWebView.getTop() < 0;
    }

    private void sendCancelEvent() {
        sendEvent(mDownEvent, MotionEvent.ACTION_CANCEL);
    }

    private void sendDownEvent() {
        sendEvent(mLastMoveEvent, MotionEvent.ACTION_DOWN);
    }

    private void sendEvent(final MotionEvent ev, int action) {
        MotionEvent e = MotionEvent.obtain(
                ev.getDownTime(),
                ev.getEventTime(),
                action,
                ev.getX(), ev.getY(),
                ev.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void cancelVelocityTrack() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void updateUI() {
        Log.e("CustomScrollView", "updateUI");
        postDelayed(new Runnable() {
            @Override
            public void run() {
                offsetContent(-2);
            }
        }, 100);
    }

    private void offsetContent(float offset) {

//        Log.e("CustomScrollView", "offsetContent " + offset);
        if (mListViewContainer.getVisibility() != mListViewContainer.VISIBLE) {
            return;
        }
        // 限制滑动范围
        int finalTopOfWebView = (int) (mWebView.getTop() + offset);
        if (finalTopOfWebView < mostTop) {
            // 下限
            finalTopOfWebView = mostTop;
        } else if (finalTopOfWebView > 0) {
            // 上限
            finalTopOfWebView = 0;
        }
        // 偏移量
        int fixedOffset = finalTopOfWebView - mWebView.getTop();
        // 移动整个内容
        mWebView.offsetTopAndBottom(fixedOffset);
        mListViewContainer.offsetTopAndBottom(fixedOffset);
        if (finalTopOfWebView == mostTop && mListViewContainer instanceof LoadMoreContainerBase) {
            ((LoadMoreContainerBase) mListViewContainer).onReachBottom();
        }
        invalidate();
    }

    private OnScrollChangeListener mOnScrollChangeListener = new OnScrollChangeListener() {
        @Override
        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (mListViewContainer.getVisibility() == VISIBLE && v == mWebView) {
                if (!mHasMoved && isWebViewScrolledAtEnd()) {
                    // 让ListView滚动一点出来
                    if (!mScroller.isFinished()) {
                        mScroller.forceFinished(true);
                    }
                    int startY = mWebView.getTop();
                    int dy = -10;
                    int duration = 400;
                    mScroller.startScroll(0, startY, 0, dy, duration);
                    invalidate();
                }
            }
        }
    };


    private int clampMag(int value, int absMin, int absMax) {
        final int absValue = Math.abs(value);
        if (absValue < absMin) return 0;
        if (absValue > absMax) return value > 0 ? absMax : -absMax;
        return value;
    }

    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.MarginLayoutParams(-1, -1);
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.MarginLayoutParams(getContext(), attrs);
    }

    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new ViewGroup.MarginLayoutParams(lp.width, lp.height);
    }

    public String getLogTag() {
        return getClass().getSimpleName();
    }

    private void log(String msg) {
        if (isDebug) {
            Log.d(getLogTag(), msg);
        }
    }

}