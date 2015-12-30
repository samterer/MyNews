package com.hzpd.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

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

    List<BitmapDrawable> bitmaps = new ArrayList<>();

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        bitmaps.add((BitmapDrawable) getDrawable());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    public void release() {
        for (BitmapDrawable bitmapDrawable : bitmaps) {
            setImageDrawable(null);
            bitmapDrawable.getBitmap().recycle();
            bitmapDrawable.setCallback(null);
        }
        bitmaps.clear();
    }
}
