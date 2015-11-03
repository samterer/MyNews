package com.hzpd.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.UserBean;
import com.hzpd.ui.App;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.util.Calendar;

/**
 * @author color
 *         程序配置
 */
public class SPUtil {

    public static Typeface typeFace;
    public static Typeface typeFaceTitle;
    public static Typeface typeFaceBold;
    public static Typeface titleTypeFace;
    public static Typeface typeFaceRoboto;
    public static Animation imageAnimation;

    public static void setFont(TextView textview) {
        textview.setTypeface(typeFace);
    }

    public static void setFontBold(TextView textview) {
        textview.setTypeface(typeFaceBold);
    }

    public static void setFontRoboto(TextView textview) {
        textview.setTypeface(typeFaceRoboto);
    }

    public static void setTitleFont(TextView textview) {
        textview.setTypeface(titleTypeFace);
    }

    public static boolean isImageUri(String uri) {
        if (!TextUtils.isEmpty(uri)) {
            String str = uri.toLowerCase();
            if (str.contains(".jpg")
                    || str.contains(".jpeg")
                    || str.contains(".png")
                    || str.contains(".gif")
                    || str.contains(".bmp")
                    || str.contains("facebook.com")
                    ) {
                return true;
            }
        }
        return false;
    }

    // 注入js函数监听
    public static void addImageClickListner(WebView mWebView) {
        // 这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，在还是执行的时候调用本地接口传递url过去
        mWebView.loadUrl("javascript:(function(){"
                + "var objs = document.getElementsByTagName(\"img\"); "
                + "for(var i=0;i<objs.length;i++)  " + "{"
                + "    objs[i].onclick=function()  " + "{"
                + "        window.imagelistner.openImage(this.src);  "
                + "}"
                + "}"
                + "})()");
    }

    public static void displayImage(String uri, ImageView imageView) {
        SPUtil.displayImage(uri, imageView, null, null, null);
    }

    public static void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        SPUtil.displayImage(uri, imageView, options,
                null, null);
    }

    public static void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener loadingListener) {
        SPUtil.displayImage(uri, imageView, options, loadingListener, null);

    }

    public static void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener loadingListener,
                                    ImageLoadingProgressListener progressListener) {
        if (isImageUri(uri)) {
            try {
                uri = uri.replaceAll("&amp;", "&");
            } catch (Exception e) {
            }
            ImageLoader.getInstance().displayImage(uri, imageView, options, mLoadingListener, progressListener);
        }
    }

    static ImageLoadingListener mLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String imageUri, View view) {
            Log.w("ImageLoadingListener", imageUri);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            Log.e("ImageLoadingListener", imageUri);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            Log.e("ImageLoadingListener", imageUri);
        }
    };

    private static String FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String format(Calendar calendar) {
        return "" + DateFormat.format(FORMAT, calendar);
    }

    /**
     * 配置名称 *
     */
    private static final String SETTINGS = "HZPD";
    /**
     * 缓存
     */
    private ACache msp;
    private static SPUtil mSPutil;

    private UserBean user;

    private SPUtil() {
        AssetManager assetManager = App.getInstance()
                .getApplicationContext().getAssets();
        msp = ACache.get(App.getInstance()
                .getApplicationContext(), SETTINGS);
        typeFaceTitle = Typeface.createFromAsset(assetManager, "fonts/britannic-bold.ttf");
        typeFace = Typeface.createFromAsset(assetManager, "fonts/KievitPro-Regular.otf");
        typeFaceBold = Typeface.createFromAsset(assetManager, "fonts/KievitPro-Medium.otf");
        typeFaceRoboto = Typeface.createFromAsset(assetManager, "fonts/Roboto-Regular.ttf");
        titleTypeFace = Typeface.createFromAsset(assetManager, "fonts/BRLNSR.TTF");
        imageAnimation = AnimationUtils.loadAnimation(App.getInstance().getApplicationContext(), R.anim.image_anim);
    }

    public static synchronized SPUtil getInstance() {
        if (mSPutil == null) {
            mSPutil = new SPUtil();
        }
        return mSPutil;
    }

    public int getVersionCode() {
        String svc = msp.getAsString("versioncode");
        int vc = 0;
        if (!TextUtils.isEmpty(svc)) {
            try {
                if (TextUtils.isDigitsOnly(svc)) {
                    vc = Integer.parseInt(svc);
                }
            } catch (Exception e) {
            }
        }
        setVersionCode(vc);
        return vc;
    }

    private void setVersionCode(int vc) {
        if (vc > 0) {
            msp.put("versioncode", vc + "");
        }
    }

    public boolean getOffTuiSong() {
        boolean start = msp.getAsBoolean("off_ts", true);
        return start;
    }

    public void setOffTuiSong(boolean flag) {
        msp.put("off_ts", flag);
    }

    public boolean getIsTodayFistStartApp() {
        String oldDay = msp.getAsString("addate");
        String newDay = CalendarUtil.getToday("yyyy-MM-dd");
        LogUtils.e("newDay-->" + newDay);

        setDate(newDay);

        if (null != oldDay) {
            if (oldDay.equals(newDay)) {
                return false;
            }
        }
        return true;
    }

    private void setDate(String d) {
        msp.put("addate", d);
    }

    public void setTextSize(int size) {
        msp.put("textsize", size + "");
    }

    public int getTextSize() {
        int textSize = CODE.textSize_small;
        String ts = msp.getAsString("textsize");
        if (!TextUtils.isEmpty(ts)) {
            try {
                if (TextUtils.isDigitsOnly(ts)) {
                    textSize = Integer.parseInt(ts);
                }
            } catch (Exception e) {
            }
        }
        return textSize;
    }

    public void setTextSizeNews(int size) {
        msp.put("textsizenews", size + "");
    }

    public int getTextSizeNews() {
        int textSize = CODE.textSize_small;
        String ts = msp.getAsString("textsizenews");
        if (!TextUtils.isEmpty(ts)) {
            try {
                if (TextUtils.isDigitsOnly(ts)) {
                    textSize = Integer.parseInt(ts);
                }
            } catch (Exception e) {
            }
        }
        return textSize;
    }


    public void setUser(UserBean user) {
        this.user = user;
        String dsuser = FjsonUtil.toJsonString(user);
        if (TextUtils.isEmpty(dsuser)) {
            return;
        }
        String suser = null;
        try {
            suser = CipherUtils.encrypt(dsuser, CipherUtils.getDESKey("2015key2015".getBytes()), "DES");
        } catch (Exception e) {
            e.printStackTrace();
        }
        msp.put("user", suser);
    }

    public UserBean getUser() {
        if (null != user) {
            return user;
        }
        String dsuser = msp.getAsString("user");
        String suser = null;
        if (!TextUtils.isEmpty(dsuser)) {
            try {
                suser = CipherUtils.decrypt(dsuser, CipherUtils.getDESKey("2015key2015".getBytes()), "DES");
            } catch (Exception e) {
                e.printStackTrace();
            }
            user = FjsonUtil.parseObject(suser, UserBean.class);
        }
        return user;
    }

    public void setWelImg(String img) {
        msp.put("wel_img", img);
    }

    public String getWelImg() {
        String img = msp.getAsString("wel_img");
        return img;
    }

    // all ad
    public JSONObject getWelcome() {
        return msp.getAsJSONObject("welcomeString");
    }

    // all ad
    public void setWelcome(JSONObject obj) {
        msp.put("welcomeString", obj);
    }

    public String getHistory() {
        return msp.getAsString("histort");
    }

    public void setHistory(String obj) {
        msp.put("histort", obj);
    }

    public void setForceUpdateTime(String updateTime) {
        msp.put("updateTime", updateTime);
    }

    public String getForceUpdateTime() {
        String updateTime = msp.getAsString("updateTime");
        return updateTime;
    }

    public String getCacheUpdatetime() {
        String updateTime = msp.getAsString("cacheupdateTime");
        return updateTime;
    }

    public void setCacheUpdatetime(String cacheupdateTime) {
        msp.put("cacheupdateTime", cacheupdateTime);
    }

    public void setSubjectColumnList(JSONArray array) {
        msp.put("subjectcolumnlist", array);
    }

    public JSONArray getSubjectColumnList() {
        JSONArray array = msp.getAsJSONArray("subjectcolumnlist");
        return array;
    }

    public String getForumTitle() {
        return msp.getAsString("forumTitle");
    }

    public void setForumTitle(String forumTitle) {
        msp.put("forumTitle", forumTitle);
    }

}
