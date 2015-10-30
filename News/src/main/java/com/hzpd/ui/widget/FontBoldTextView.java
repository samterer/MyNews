package com.hzpd.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.hzpd.utils.SPUtil;

/**
 * Created by moshuangquan on 9/7/0007.
 */
public class FontBoldTextView extends TextView {

    public FontBoldTextView(Context context) {
        super(context);
        SPUtil.setFont(this);
    }

    public FontBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SPUtil.setFontBold(this);
    }

    public FontBoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SPUtil.setFontBold(this);
    }

}
