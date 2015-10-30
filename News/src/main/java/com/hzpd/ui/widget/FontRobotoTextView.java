package com.hzpd.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.hzpd.utils.SPUtil;

/**
 * Created by moshuangquan on 9/7/0007.
 */
public class FontRobotoTextView extends TextView {

    public FontRobotoTextView(Context context) {
        super(context);
        SPUtil.setFontRoboto(this);
    }

    public FontRobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SPUtil.setFontRoboto(this);
    }

    public FontRobotoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SPUtil.setFontRoboto(this);
    }

}
