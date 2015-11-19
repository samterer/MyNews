package com.hzpd.ui;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.hzpd.hflt.BuildConfig;
import com.hzpd.hflt.R;
import com.hzpd.modle.Adbean;
import com.hzpd.modle.Menu_Item_Bean;
import com.hzpd.modle.db.UserLog;
import com.hzpd.utils.CODE;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;


public class App extends Application {

    public static int px_5dp;
    public static int px_10dp;
    public static int px_15dp;
    public static int px_150dp;
    private static App mInstance = null;

    public static boolean isStartApp = false;//app是否已经启动
    public List<UserLog> userLogs = new ArrayList<>();

    private SPUtil spu;
    public static SparseArray<Menu_Item_Bean> menuList;

    public Adbean welcomeAdbean; // 欢迎页广告
    public HashMap<String, Adbean> channelADMap = null;//频道列表广告
    public HashMap<String, Adbean> newsDetailADMap = null;//新闻详情广告

    public HashMap<String, String> newTimeMap = new HashMap<>(); // 缓存时间戳

    public String newTime; // 最新时间
    public String oldTime; // 最早时间

    private String versionName;

    public static final boolean debug = BuildConfig.DEBUG;
    public static InputMethodManager inputMethodManager;//输入法管理
    public static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public String themeName;

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        SharePreferecesUtils.setParam(this, "THEME", "0");
        this.themeName = themeName;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        themeName = SharePreferecesUtils.getParam(this, "THEME", "0").toString();
//        newTimeMap.clear();
        refWatcher = LeakCanary.install(this);
        newTimeMap.clear();
        long start = System.currentTimeMillis();
        FacebookSdk.sdkInitialize(getApplicationContext());
        mInstance = this;
        spu = SPUtil.getInstance();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        px_5dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        px_10dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        px_15dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        px_150dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
        String str = SharePreferecesUtils.getParam(this, "STATION", "def").toString();

        LogUtils.i("启动日志" + str);

        com.hzpd.utils.Log.e("App", "App 1here " + (System.currentTimeMillis() - start));
        init();
        com.hzpd.utils.Log.e("App", "App 2here " + (System.currentTimeMillis() - start));
    }

    private RefWatcher refWatcher;

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }

    final static int IMAGE_LOAD_SIZE = 1024 * 1024 * 5;

    private void init() {
        initAds();

        // 获取应用当前版本号
        PackageManager localPackageManager = this.getPackageManager();
        try {
            PackageInfo localPackageInfo =
                    localPackageManager.getPackageInfo(
                            this.getPackageName(), 0);

            if (localPackageInfo != null)
                versionName = localPackageInfo.versionName;
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
            localNameNotFoundException.printStackTrace();
        }
        // 初始化UniversalImageLoader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getApplicationContext())
                .memoryCacheExtraOptions(720, 1280) // max width, max height，即保存的每个缓存文件的最大长宽
                .defaultDisplayImageOptions(DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Big))
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .memoryCacheSize(IMAGE_LOAD_SIZE)// 缓存大小
                .memoryCache(new LruMemoryCache(IMAGE_LOAD_SIZE))
                .threadPoolSize(5)
                .build();
        ImageLoader.getInstance().init(config);
        L.writeDebugLogs(false);
        L.writeLogs(false);

        // xUtils日志控制
        LogUtils.allowD = debug;
        LogUtils.allowI = debug;
        LogUtils.allowE = debug;
        LogUtils.allowV = debug;
        LogUtils.allowW = debug;
        LogUtils.allowWtf = debug;

        try {
            // 极光推送
            JPushInterface.setDebugMode(debug);    // 设置开启日志,发布时请关闭日志
            JPushInterface.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }


        menuList = new SparseArray<>(40);

        Menu_Item_Bean lfbean0 = new Menu_Item_Bean(CODE.MENU_NEWS, R.drawable.zy_icon_xinwenfabu, getString(R.string.menu_news));
        Menu_Item_Bean lfbean1 = new Menu_Item_Bean(CODE.MENU_ALBUM, R.drawable.zy_icon_shuzibao, getString(R.string.menu_album));
        Menu_Item_Bean lfbean2 = new Menu_Item_Bean(CODE.MENU_VIDEO_RECORDING, R.drawable.zy_icon_xinwenfabu, getString(R.string.menu_video));
        Menu_Item_Bean lfbean5 = new Menu_Item_Bean(CODE.MENU_SPECIAL, R.drawable.zy_icon_zhuanti, getString(R.string.menu_subject));
        Menu_Item_Bean lfbean19 = new Menu_Item_Bean(CODE.MENU_SEARCH, R.drawable.zy_icon_xinwenfabu, getString(R.string.menu_search));
        Menu_Item_Bean lfbean21 = new Menu_Item_Bean(CODE.MENU_ACTION, R.drawable.zy_icon_xinwenfabu, getString(R.string.menu_activity));

        menuList.put(lfbean0.getId(), lfbean0);
        menuList.put(lfbean1.getId(), lfbean1);
        menuList.put(lfbean2.getId(), lfbean2);
        menuList.put(lfbean5.getId(), lfbean5);
        menuList.put(lfbean19.getId(), lfbean19);
        menuList.put(lfbean21.getId(), lfbean21);


    }

    public static Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
            try {
                com.hzpd.utils.Log.e("Alert", " UncaughtException !!! " + paramThrowable.toString());
                paramThrowable.printStackTrace();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void initAds() {
        // 初始化广告容器
        try {
            channelADMap = new HashMap<String, Adbean>();
            newsDetailADMap = new HashMap<String, Adbean>();
            JSONObject jsonObject = SPUtil.getInstance().getWelcome();
            List<Adbean> adbeans = JSONArray.parseArray(jsonObject.getJSONArray("data").toJSONString(), Adbean.class);
            if (adbeans != null && !adbeans.isEmpty()) {
                for (Adbean adbean : adbeans) {
                    switch (adbean.getType()) {
                        case 1:
                            welcomeAdbean = adbean;
                            break;
                        case 2:
                            if (!adbean.getTid().isEmpty()) {
                                for (String tid : adbean.getTid()) {
                                    channelADMap.put(tid, adbean);
                                }
                            }
                            break;
                        case 3:
                            break;
                        case 4:
                            break;
                        case 5:
                            break;
                    }

                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onTerminate() {
        LogUtils.i("onTerminate");
        super.onTerminate();
    }

    public static App getInstance() {
        return mInstance;
    }


    public static final String collectiondbname = "hzpd_collectoin.db";//收藏数据库
    public static final int collectiondbversion = 2;//收藏数据库版本号
    public static final String dbnewsjump = "newsjump.db"; //
    public static final String newsListDb = "newslist.db"; //新闻列表
    public static final String bianminListDb = "bianminlist.db"; //新闻列表
    public static final String zhuantiListDb = "zhuantilist.db"; //专题列表
    public static final String albumListDb = "albumlist.db"; //图集列表
    public static final String videoListDb = "videolist.db"; //视频列表
    public static final String userLogDb = "userlog.db"; // 用户日志

    /**
     * 频道信息
     */
    public static final String mTitle = "title.dat";//频道信息
    public static List<String> mImageList;//


    /**
     * 获取缓存存放路径
     *
     * @return 缓存存放路径
     */
    public String getAllDiskCacheDir() {
        String cachePath = "";

        // 先获取SD卡缓存目录
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File externalFile = getApplicationContext().getExternalCacheDir();
            if ((null != externalFile) && (!externalFile.exists() || externalFile.mkdir())) {
                cachePath = externalFile.getPath();
            }
        }

        // 若无SD卡，在获取内部缓存目录
        if (TextUtils.isEmpty(cachePath)) {
            File dir = getApplicationContext().getCacheDir();
            if (!dir.exists()) {
                dir.mkdir();
            }
            cachePath = dir.getPath();
        }

        return cachePath;
    }

    public String getJsonFileCacheRootDir() {
        String cachePath = getAllDiskCacheDir() + File.separator + "jsonfile";
        File file = new File(cachePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.toString();
    }

    /**
     * 获取指定路径的文件对象，确保该文件所在目录被创建，该路径创建一个空文件
     */
    public static File getFile(String path) {
        // 创建目录
        int fp = path.lastIndexOf(File.separator);
        String sfp = path.substring(0, fp);
        LogUtils.i("spf-->" + sfp);
        File fpath = new File(sfp);
        if (!fpath.exists() && !fpath.isDirectory()) {
            fpath.mkdirs();
        }

        // 创建空文件
        File f = new File(path);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return f;
    }

    public static File getFileDir(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    /**
     * 获取文本文件的全部字符内容
     *
     * @param file 文本文件
     * @return 文本文件的全部字符内容
     */
    public static String getFileContext(File file) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        BufferedReader bufReader = new BufferedReader(fileReader);
        StringBuilder sb = new StringBuilder();
        try {
            String data;
            while ((data = bufReader.readLine()) != null) {
                sb.append(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                bufReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String getDateTimeByMillisecond(long l) {
        Date date = new Date(l);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
        String time = format.format(date);
        return time;
    }

    public static String getDateTimeByMillisecond(String str) {
        Date date = new Date(Long.valueOf(str));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
        String time = format.format(date);
        return time;
    }

    public static String isYeaterday(String str, Date newTime) {
        if (newTime == null) {
            newTime = new Date();
        }
        Date oldTime = null;
        try {
            oldTime = new Date(Long.valueOf(str));
        } catch (Exception e) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
            try {
                long millionSeconds = sdf.parse(str).getTime();
                oldTime = new Date(millionSeconds);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }// 毫秒
        }

        // 将下面的 理解成 yyyy-MM-dd 00：00：00 更好理解点
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm");
        // SimpleDateFormat formatMonth = new SimpleDateFormat("MM-dd hh:mm");
        String todayStr = format.format(newTime);
        Date today;
        try {
            today = format.parse(todayStr);
            // 昨天 86400000=24*60*60*1000 一天
            if ((today.getTime() - oldTime.getTime()) > 0
                    && (today.getTime() - oldTime.getTime()) <= 86400000) {
                // String time = "昨天" + formatTime.format(oldTime);
                String time = mInstance.getString(R.string.prompt_yesterday);
                return time;
            } else if ((today.getTime() - oldTime.getTime()) <= 0) { // 至少是今天
                String time = mInstance.getString(R.string.prompt_today);
                return time;
            } else { // 至少是前天
                String time = format.format(oldTime);
                return time;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String timeToMillSecound(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        long millionSeconds = 0;

        try {
            millionSeconds = sdf.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }// 毫秒
        return millionSeconds + "";
    }

    public static List<String> getHtmlImgSrc(String htmlStr) {
        List<String> list = new ArrayList<String>();
        Matcher m = Pattern.compile("src=\"?(.*?)(\"|>|\\s+)").matcher(htmlStr);
        while (m.find()) {
            System.out.println(m.group(1));
            list.add(m.group(1));
        }
        return list;
    }

    public static long getTimeStamp(String time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        Date date = null;
        if (null == time || "".equals(time)) {
            return 0;
        }
        try {
            date = sdf.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        if (date != null) {
            long t = date.getTime();
            return t;
        }
        return 0;
    }

    public static String getTimeString(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        String stime = sdf.format(new Date(time));
        return stime;
    }

    public static int compareTimeString(String time1, String time2) {
        long ltime1 = getTimeStamp(time1, "yyyy-MM-dd HH:mm:ss");
        long ltime2 = getTimeStamp(time2, "yyyy-MM-dd HH:mm:ss");

        if (ltime1 > ltime2) {
            return 1;
        } else if (ltime1 == ltime2) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * 截取字符长度
     *
     * @param str
     * @param ruleInt
     * @return
     */

    public static String GetStringLeng(String str, int ruleInt) {
        if (str.length() > ruleInt) {
            return str.substring(0, ruleInt);
        } else {
            return str;
        }
    }

    public static String MatherString(String data) {
        // 匹配类似velocity规则的字符串
        if (null == data || "".equals(data)) {
            return "";
        }
        // 生成匹配模式的正则表达式
        String patternString = "(http://).+?(\\.(jpg|gif|png|jpeg))";
        // String patternString = "([^=])((http://).+?(\\.(jpg|gif|png|jpeg)))";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(data);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<img src=\"" + matcher.group(0)
                    + "\" />");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVerCode() {
        int verCode = -1;
        try {
            verCode = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            LogUtils.e(e.getMessage());
        }
        return verCode;
    }

    public String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            String device_id = tm.getDeviceId();

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }

            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public interface Callback {
        void onSuccess(Profile currentProfile);
    }

    Callback callback = null;

    public void setProfileTracker(final Callback callback) {
        this.callback = callback;
        if (callback == null) {
            profileTracker = null;
            if (profileTracker != null) {
                profileTracker.stopTracking();
                profileTracker = null;
            }
        } else if (profileTracker == null) {
            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    Log.e("test", " detail onCurrentProfileChanged " + currentProfile);
                    //callback.onSuccess(currentProfile);
                }
            };
        }
    }

    ProfileTracker profileTracker = null;

}