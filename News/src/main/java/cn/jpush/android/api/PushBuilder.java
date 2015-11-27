package cn.jpush.android.api;

import android.content.Context;
import android.widget.RemoteViews;

import com.hzpd.hflt.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PushBuilder extends BasicPushNotificationBuilder {

    public int layout;
    public int layoutIconId;
    public int layoutTitleId;
    public int layoutContentId;
    public int layoutTime;
    public int layoutIconDrawable;

    //    private static final String[] z;
    public PushBuilder(Context context) {
        super(context);
    }

    public PushBuilder(Context var1, int var2, int var4) {
        super(var1);
        this.layout = var2;
        this.layoutTitleId = var4;
    }

    public PushBuilder(Context var1, int var2, int var3, int var4, int var5,int layoutTime) {
        super(var1);
        this.layout = var2;
        this.layoutIconId = var3;
        this.layoutTitleId = var4;
        this.layoutContentId = var5;
        this.layoutTime=layoutTime;
    }

    @Override
    RemoteViews b(String s) {
        RemoteViews var2;
//        var2 = new RemoteViews(this.a.getPackageName(), R.layout.customer_notitfication_layout);
        (var2 = new RemoteViews(this.a.getPackageName(), this.layout)).setTextViewText(this.layoutTitleId, this.b);
        var2.setImageViewResource(this.layoutIconId, this.layoutIconDrawable);
        SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String date = sDateFormat.format(curDate);
//        CalendarUtil.friendlyTime1(date,);
        var2.setTextViewText(this.layoutContentId, s);
        var2.setTextViewText(R.id.time, ""+date);
//        var2.setTextViewText(this.layoutTime, s);
        return var2;
    }

    @Override
    void a(String[] var1) {
        super.a(var1);
        this.layout = Integer.parseInt(var1[5]);
        this.layoutIconId = Integer.parseInt(var1[6]);
        this.layoutTitleId = Integer.parseInt(var1[7]);
        this.layoutContentId = Integer.parseInt(var1[8]);
        this.layoutIconDrawable = Integer.parseInt(var1[9]);
    }


//    static {
//        String[] var10000 = new String[2];
//        String[] var10001 = var10000;
//        byte var10002 = 0;
//        String var10003 = "pFI51~le\u001e\u0001L";
//        byte var10004 = -1;
//
//        while(true) {
//            char[] var5;
//            label36: {
//                char[] var2 = var10003.toCharArray();
//                int var10006 = var2.length;
//                int var0 = 0;
//                var5 = var2;
//                int var6 = var10006;
//                char[] var8;
//                int var10007;
//                if(var10006 <= 1) {
//                    var8 = var2;
//                    var10007 = var0;
//                } else {
//                    var5 = var2;
//                    var6 = var10006;
//                    if(var10006 <= var0) {
//                        break label36;
//                    }
//
//                    var8 = var2;
//                    var10007 = var0;
//                }
//
//                while(true) {
//                    char var10008 = var8[var10007];
//                    byte var10009;
//                    switch(var0 % 5) {
//                        case 0:
//                            var10009 = 19;
//                            break;
//                        case 1:
//                            var10009 = 51;
//                            break;
//                        case 2:
//                            var10009 = 58;
//                            break;
//                        case 3:
//                            var10009 = 65;
//                            break;
//                        default:
//                            var10009 = 94;
//                    }
//
//                    var8[var10007] = (char)(var10008 ^ var10009);
//                    ++var0;
//                    if(var6 == 0) {
//                        var10007 = var6;
//                        var8 = var5;
//                    } else {
//                        if(var6 <= var0) {
//                            break;
//                        }
//
//                        var8 = var5;
//                        var10007 = var0;
//                    }
//                }
//            }
//
//            String var4 = (new String(var5)).intern();
//            switch(var10004) {
//                case 0:
//                    var10001[var10002] = var4;
//                    z = var10000;
//                    return;
//                default:
//                    var10001[var10002] = var4;
//                    var10001 = var10000;
//                    var10002 = 1;
//                    var10003 = "Lle\u001e\u0001";
//                    var10004 = 0;
//            }
//        }
//    }

}

