package com.hzpd.hflt.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;


public class FacebookSharedUtil {

    public static void showShares(String title, String link, String imagePath
            , final Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, link); //TODO 生成短网址
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showImgShares(String title, String imagePath, String nid, final Context context) {
        ShareDialog shareDialog = new ShareDialog((Activity) context);
        String station = SharePreferecesUtils.getParam(context, StationConfig.STATION, "def").toString();
        String ROOT_url = null;
        if (station.equals(StationConfig.DEF)) {
            ROOT_url = InterfaceJsonfile.ROOT;
        } else if (station.equals(StationConfig.YN)) {
            ROOT_url = InterfaceJsonfile_YN.ROOT;
        } else if (station.equals(StationConfig.TW)) {
            ROOT_url = InterfaceJsonfile_TW.ROOT;
        }
        String link = ROOT_url + "index.php?s=/Public/photoview/id/" + nid;
        ShareLinkContent shareContent = new ShareLinkContent.Builder()
                .setContentTitle(title)
                .setImageUrl(!TextUtils.isEmpty(imagePath) ? Uri.parse(imagePath) : null)
                .setContentUrl(!TextUtils.isEmpty(link) ? Uri.parse(link) : null)
                .build();
        shareDialog.show((Activity) context, shareContent);

    }

    // 检查是否安装facebook
    public static boolean installFacebook(Context context) {
        boolean flag = false;
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
            if (intent != null) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

}