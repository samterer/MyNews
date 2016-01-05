package com.hzpd.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.hzpd.utils.SPUtil;

/**
 * 释放图片内存
 */
public class FixedImageView extends ImageView {

    public FixedImageView(Context context) {
        super(context);
    }

    public FixedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    BitmapDrawable bitmapDrawable;

    @Override
    public void setImageBitmap(Bitmap bm) {
        release();
        if (!bm.isRecycled()) {
            super.setImageBitmap(bm);
            bitmapDrawable = (BitmapDrawable) getDrawable();
        }
    }

    @Override
    public void setImageResource(int resId) {
        release();
        bitmapDrawable = SPUtil.getBitmapDrawable(getResources(), resId);
        super.setImageDrawable(bitmapDrawable);
        bitmapDrawable = (BitmapDrawable) getDrawable();
    }

    @Override
    protected void onDetachedFromWindow() {
        release();
        super.onDetachedFromWindow();
    }

    public void release() {
        if (bitmapDrawable != null) {
            super.setImageDrawable(null);
            bitmapDrawable.getBitmap().recycle();
            bitmapDrawable.setCallback(null);
            bitmapDrawable = null;
        }
    }

}
