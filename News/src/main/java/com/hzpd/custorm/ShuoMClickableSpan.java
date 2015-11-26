package com.hzpd.custorm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.hzpd.hflt.R;
import com.hzpd.ui.activity.ZQ_FeedBackActivity;

public class ShuoMClickableSpan extends ClickableSpan {

    String string;
    Context context;

    public ShuoMClickableSpan(String str, Context context) {
        super();
        this.string = str;
        this.context = context;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
//		ds.setColor(Color.BLUE);
        ds.setColor(context.getResources().getColor(R.color.details_contact_color));
    }

    @Override
    public void onClick(View widget) {
        Intent intent
                = new Intent();
        intent.setClass(context, ZQ_FeedBackActivity.class);
        context.startActivity(intent);

    }

}
