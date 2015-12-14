package com.hzpd.custorm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.hzpd.ui.interfaces.OnScrollChangeListener;

/**
 * Created by AvatarQing on 2015/7/24.
 */
public class CustomWebView extends WebView {

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

    float lastX = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (Build.VERSION.SDK_INT < 16) { //4.1
            invalidate();
        }
        //int height = (int) (1.0 * getContentHeight() * getScale());
        //heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        //Log.e("test", "getContentHeight() " + height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                break;
        }
        // 禁止平移
        event = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), lastX, event.getY(), event.getMetaState());
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangeListener != null) {
            mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        mOnScrollChangeListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        invalidate();
    }

    @Override
    public String getOriginalUrl() {
        return super.getOriginalUrl();
    }

    @Override
    public String getUrl() {
        return super.getUrl();
    }
}