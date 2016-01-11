package com.hzpd.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;
import com.hzpd.hflt.BuildConfig;
import com.hzpd.hflt.R;
import com.hzpd.modle.TagBean;
import com.hzpd.modle.UserBean;
import com.hzpd.modle.db.NewsChannelBeanDB;
import com.hzpd.ui.App;
import com.news.update.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author color
 *         程序配置
 */
public class SPUtil {
    public static Random random = new Random(System.currentTimeMillis());

    public static List<NewsChannelBeanDB> dbs;

    public static boolean checkTag(TagBean tagBean) {
        if (dbs == null) {
            return false;
        }
        try {
            for (NewsChannelBeanDB beanDB : dbs) {
                if (beanDB.getDefault_show().equals("1") && tagBean.getId().equals(beanDB.getTagid())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static NewsChannelBeanDB getTag(TagBean tagBean) {
        if (dbs == null) {
            return null;
        }
        for (NewsChannelBeanDB beanDB : dbs) {
            if (tagBean.getId().equals(beanDB.getTagid())) {
                return beanDB;
            }
        }
        return null;
    }

    public static void updateChannel() {
        try {
            if (dbs != null) dbs.clear();
            dbs = DBHelper.getInstance(App.getInstance()).getChannel().loadAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCountry() {
        if (current != null) {
            return current;
        }
        String temp = SPUtil.getGlobal("CountryCode", "id");
        current = temp;
        return current;
    }

    private static String current = null;

    public static void setCountry(String country) {
        if (!TextUtils.isEmpty(country)) {
            if (SPUtil.getCountry().equals(country)) {
                return;
            }
            country = country.toLowerCase();
            SPUtil.setGlobal("CountryCode", country);
            getInstance().current = null;
            App.getInstance().newTime = null;
            App.getInstance().oldTime = null;
            App.getInstance().newTimeMap.clear();
            App.getInstance().updateDao();
            SharePreferecesUtils.init();
            getInstance().msp = ACache.get(App.getInstance().getApplicationContext(), getCountry());
            DBHelper.setInstance(App.getInstance());
        }
    }

    public static String getCountryName() {
        return SPUtil.getGlobal("CountryName", App.getInstance().getString(R.string.default_county));
    }

    public static void setCountryName(String country) {
        if (!TextUtils.isEmpty(country)) {
            country = country.toLowerCase();
            SPUtil.setGlobal("CountryName", country);

            SharePreferecesUtils.init();
            getInstance().msp = ACache.get(App.getInstance().getApplicationContext(), getCountry());
            DBHelper.setInstance(App.getInstance());
        }
    }


    public static View getRandomAdView(Context context, NativeAd nativeAd) {
        View view = null;

        if (nativeAd != null) {
            int value = random.nextInt(10);
            NativeAdView.Type type;
            switch (value) {
                case 1:
                case 2:
                case 3:
                    type = NativeAdView.Type.HEIGHT_100;
                    break;
                case 7:
                case 8:
                case 9:
                    type = NativeAdView.Type.HEIGHT_300;
                    break;
                default:
                    type = NativeAdView.Type.HEIGHT_120;
                    break;
            }
            view = NativeAdView.render(context, nativeAd, type);
        }
        return view;
    }

    public static void clearAds(HashMap<String, NativeAd> ads) {
        if (ads != null) {
            for (NativeAd nativeAd : ads.values()) {
                if (nativeAd != null) {
                    nativeAd.unregisterView();
                    nativeAd.setAdListener(null);
                    nativeAd.destroy();
                }
            }
            ads.clear();
        }
    }

    public static LinearLayout.LayoutParams NORMAL;
    public static LinearLayout.LayoutParams LARGE;


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

    public static void addParams(Map<String, String> params) {
        Context context = App.getInstance();
        params.put(COUNTRY, getCountry());
        params.put(UUID, Utils.getDeviceUUID(context));
        params.put(ANDROID_ID, Utils.getAndroidId(context));
        params.put(IMEI, Utils.getIMEI(context));
        params.put(LAUGUAGE, Utils.getLanguage(context));
        params.put(VERSION_CODE, "" + Utils.getVersionCode(context));
        params.put(PACKAGE_NAME_SELF, context.getPackageName());
        params.put(IS_ROM, "" + Utils.isRomVersion(context));
    }

    public static Intent getIntent(Context context) {
        final String appPackageName = context.getPackageName();
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)); // 点击该通知后要跳转的Activity
        if (Utils.isAppInstall(context, Utils.GOOGLE_PLAY_PACKAGE_NAME)) {
            notificationIntent.setPackage(Utils.GOOGLE_PLAY_PACKAGE_NAME);
        }
        return notificationIntent;
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

    static int count = 0;

    public static void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener loadingListener,
                                    ImageLoadingProgressListener progressListener) {
        if (BuildConfig.DEBUG) {
//            return;
        }
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
        }
    };

    private static String FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String format(Calendar calendar) {
        return "" + DateFormat.format(FORMAT, calendar);
    }

    /**
     * 缓存
     */
    private ACache msp;
    private static SPUtil mSPutil;

    private UserBean user;

    private SPUtil() {
        current = null;
        updateChannel();
        msp = ACache.get(App.getInstance().getApplicationContext(), getCountry());
        AssetManager assetManager = App.getInstance()
                .getApplicationContext().getAssets();
        typeFaceTitle = Typeface.createFromAsset(assetManager, "fonts/britannic-bold.ttf");
        typeFace = Typeface.createFromAsset(assetManager, "fonts/KievitPro-Regular.otf");
        typeFaceBold = Typeface.createFromAsset(assetManager, "fonts/KievitPro-Medium.otf");
        typeFaceRoboto = Typeface.createFromAsset(assetManager, "fonts/Roboto-Regular.ttf");
        titleTypeFace = Typeface.createFromAsset(assetManager, "fonts/BRLNSR.TTF");
        imageAnimation = AnimationUtils.loadAnimation(App.getInstance().getApplicationContext(), R.anim.image_anim);
        DisplayMetrics metrics = App.getInstance().getResources().getDisplayMetrics();
        int normal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics);
        int larger = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, metrics);
        NORMAL = new LinearLayout.LayoutParams(normal, normal);
        LARGE = new LinearLayout.LayoutParams(larger, larger);
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

    public static BitmapDrawable getBitmapDrawable(Resources resources, int rid) {
        long start = System.currentTimeMillis();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, rid);
        Log.e("test", "News: " + (System.currentTimeMillis() - start));
        if (bitmap != null && !bitmap.isRecycled()) {
            return new BitmapDrawable(resources, bitmap);
        } else {
            return null;
        }
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


    public static void setAtt(ImageView item_type_iv, String attname) {
        if (!TextUtils.isEmpty(attname)) {
            if (attname.equals("a")) {
                item_type_iv.setVisibility(View.VISIBLE);
                item_type_iv.setImageResource(R.drawable.zq_subscript_hot);
            } else if (attname.equals("b")) {
                item_type_iv.setVisibility(View.VISIBLE);
                item_type_iv.setImageResource(R.drawable.zq_subscript_rekom);
            } else if (attname.equals("c")) {
                item_type_iv.setVisibility(View.VISIBLE);
                item_type_iv.setImageResource(R.drawable.zq_subscript_kolom);
            } else if (attname.equals("f")) {
                item_type_iv.setVisibility(View.VISIBLE);
                item_type_iv.setImageResource(R.drawable.zq_subscript_fokus);
            } else if (attname.equals("h")) {
                item_type_iv.setVisibility(View.VISIBLE);
                item_type_iv.setImageResource(R.drawable.zq_subscript_xtend);
            } else if (attname.equals("j")) {
                item_type_iv.setVisibility(View.VISIBLE);
                item_type_iv.setImageResource(R.drawable.zq_subscript_issue);
            } else if (attname.equals("p")) {
                item_type_iv.setVisibility(View.VISIBLE);
                item_type_iv.setImageResource(R.drawable.zq_subscript_album);
            } else if (attname.equals("s")) {
                item_type_iv.setVisibility(View.VISIBLE);
                item_type_iv.setImageResource(R.drawable.zq_subscript_video);
            } else {
                item_type_iv.setVisibility(View.GONE);
            }
        } else {
            item_type_iv.setVisibility(View.GONE);
        }
    }

    public static void setRtype(String rtype, ImageView nli_foot) {
        //1新闻  2图集  3直播 4专题  5关联新闻 6视频 7引用
        if (rtype != null) {
            switch (Integer.valueOf(rtype)) {
                case 1:
                    break;
                case 2:
                    nli_foot.setImageResource(R.drawable.zq_subscript_album);
                    nli_foot.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    nli_foot.setImageResource(R.drawable.zq_subscript_live);
                    nli_foot.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    nli_foot.setImageResource(R.drawable.zq_subscript_fokus);
                    nli_foot.setVisibility(View.VISIBLE);
                    break;
                case 6:
                    nli_foot.setImageResource(R.drawable.zq_subscript_video);
                    nli_foot.setVisibility(View.VISIBLE);
                    break;
                case 7:
                    nli_foot.setImageResource(R.drawable.zq_subscript_html);
                    nli_foot.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }


    public static void saveFile(File target, String str) {
        String filePath = target.getAbsolutePath();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(str.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定文件
     *
     * @param fileName
     */
    public static void deleteFiles(String fileName) {
        App.getInstance().getFilesDir();
        File file = new File(fileName);
        if (file.exists())
            file.delete();

    }


    //保存全局配置
    public static void setGlobal(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(App.getInstance())
                .edit().putString(key, value).commit();
    }

    public static void setGlobal(String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(App.getInstance())
                .edit().putLong(key, value).commit();
    }

    //获取全局配置
    public static String getGlobal(String key, String value) {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getString(key, value);
    }

    public static long getGlobal(String key, long value) {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getLong(key, value);
    }

    /**
     */
    public static final String IS_ROM = "is_rom";
    /**
     * 用户设备唯一标识，如00000000-54b3-e7c7-0000-000046bffd97
     */
    public static final String UUID = "uuid";
    /**
     * 语言代码，如en、zh
     */
    public static final String LAUGUAGE = "language";
    /**
     * 国家代码，如CN
     */
    public static final String COUNTRY = "country";
    /**
     * 屏幕分辨率，如720*1080
     */
    public static final String SCREEN_RESOLUTION = "screen_type";
    /**
     * 设备制造商名称，如Xiaomi
     */
    public static final String MANUFACTURE = "manufacture";
    /**
     * 设备型号，如MI 1S
     */
    public static final String MODEL = "model";
    /**
     * 系统版本号，如21（代表Android5.0）
     */
    public static final String OS_VERSION = "android_version";
    /**
     * 手机SIM卡运营商
     */
    public static final String SIM_OPERATOR = "operator";
    /**
     * 手机卡IMSI
     */
    public static final String IMSI = "imsi";
    /**
     * 手机卡IMEI
     */
    public static final String IMEI = "imei";
    /**
     * Android ID
     */
    public static final String ANDROID_ID = "android_id";
    /**
     * 本机是否安装了GooglePlay
     */
    public static final String HAS_GOOGLE_MARKET = "has_google_market";
    /**
     * 当前应用自身包名
     */
    public static final String PACKAGE_NAME_SELF = "packageNameSelf";
    /**
     * 版本号
     */
    public static final String VERSION_CODE = "ver_code";
}
