package cn.jpush.android.api;

import android.content.Context;
import android.widget.RemoteViews;

import com.hzpd.hflt.R;
import android.util.Log;

public class PushBuilder extends BasicPushNotificationBuilder {


    public PushBuilder(Context context) {
        super(context);
    }

    @Override
    RemoteViews b(String s) {
        Log.e("PushBuilder","执行B");
        RemoteViews var2;
        var2 = new RemoteViews(this.a.getPackageName(), R.layout.customer_notitfication_layout);
        (var2 = new RemoteViews(this.a.getPackageName(), this.layout)).setTextViewText(this.layoutTitleId, this.b);
        return var2;
    }

    @Override
    void a(String[] strings) {
        super.a(strings);
    }
}

