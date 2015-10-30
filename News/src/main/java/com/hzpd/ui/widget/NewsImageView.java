package com.hzpd.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.hzpd.utils.SPUtil;

/**
 * 保持宽高比的图片
 */
public class NewsImageView extends ImageView {
    public NewsImageView(Context context) {
        super(context);
    }

    public NewsImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private float whScale = 1.0f;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public boolean show = false;

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        show = false;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        show = true;
        clearAnimation();
        startAnimation(SPUtil.imageAnimation);
    }
}
