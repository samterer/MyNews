package com.hzpd.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.hzpd.utils.SPUtil;

public class BritannicboldTextView extends TextView {

    public BritannicboldTextView(Context context) {
        super(context);
        init();
    }

    public BritannicboldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BritannicboldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTypeface(SPUtil.typeFaceTitle);
    }
}
