package com.hzpd.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;

/**
 * Created by moshuangquan on 9/7/0007.
 */
public class FontTextView extends TextView {

    public FontTextView(Context context) {
        super(context);
        SPUtil.setFont(this);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        long startTime = System.currentTimeMillis();
        Log.e("setFont","News: setFont3=="+(System.currentTimeMillis()-startTime));
        SPUtil.setFont(this);
        Log.e("setFont", "News: setFont4==" + (System.currentTimeMillis() - startTime));
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SPUtil.setFont(this);
    }

}