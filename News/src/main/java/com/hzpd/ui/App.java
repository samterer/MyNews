package com.hzpd.ui;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.TextUtils;
import android.util.SparseArray;

import com.hzpd.hflt.R;
import com.hzpd.modle.Adbean;
import com.hzpd.modle.Menu_Item_Bean;
import com.hzpd.utils.CODE;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.analytics.MobclickAgent;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.ShareSDK;


public class App extends Application {

	private static App mInstance = null;

	public static boolean isStartApp = false;//app是否已经启动

	private SPUtil spu;
	public static SparseArray<Menu_Item_Bean> menuList;

	public HashMap<String, Adbean> channelADMap = null;//频道列表广告
	public HashMap<String, Adbean> newsDetailADMap = null;//新闻详情广告

	private String versionName;

	public static final boolean debug = false;

	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;
		spu = SPUtil.getInstance();

		init();

	}

	private void init() {

		channelADMap = new HashMap<String, Adbean>();
		newsDetailADMap = new HashMap<String, Adbean>();

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

		ImageLoaderConfiguration config = new ImageLoaderConfiguration
				.Builder(getApplicationContext())
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheExtraOptions(720, 1280) // max width, max height，即保存的每个缓存文件的最大长宽
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.memoryCache(new WeakMemoryCache())
				.memoryCacheSize(5 * 1024 * 1024)// 缓存大小
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheSize(50 * 1024 * 1024)
				.diskCacheFileCount(150)// 文件数量
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.threadPoolSize(3)
//				.writeDebugLogs()
				.build();
		ImageLoader.getInstance().init(config);

		LogUtils.allowD = debug;
		LogUtils.allowI = debug;
		LogUtils.allowE = debug;
		LogUtils.allowV = debug;
		LogUtils.allowW = debug;
		LogUtils.allowWtf = debug;

		JPushInterface.setDebugMode(debug);    // 设置开启日志,发布时请关闭日志
		MobclickAgent.setDebugMode(debug); // 设置开启日志,发布时请关闭日志

		/**
		 JPushInterface.init(this);

		 CustomPushNotificationBuilder builder = new
		 CustomPushNotificationBuilder(this
		 ,R.layout.jpush_notitfication_layout, R.id.jpush_icon
		 ,R.id.jpush_title,R.id.jpush_text);
		 builder.statusBarDrawable = R.drawable.app_logon16;
		 builder.layoutIconDrawable = R.drawable.logo;
		 JPushInterface.setDefaultPushNotificationBuilder(builder);
		 JPushInterface.setLatestNotificationNumber(this, 3);

		 if(null!=spu.getUser()){
		 JPushInterface.setAlias(this, spu.getUser().getUid(), new TagAliasCallback() {
		@Override public void gotResult(int arg0, String arg1, Set<String> arg2) {
		LogUtils.i("arg0-->"+arg0+" arg1-->"+arg1);
		if(arg2!=null){
		for(String s:arg2){
		LogUtils.i("arg2->"+s);
		}
		}
		}
		});
		 }
		 */

		menuList = new SparseArray<>(40);

		Menu_Item_Bean lfbean0 = new Menu_Item_Bean(CODE.MENU_NEWS, R.drawable.zy_icon_xinwenfabu, "新闻资讯");
		Menu_Item_Bean lfbean1 = new Menu_Item_Bean(CODE.MENU_ALBUM, R.drawable.zy_icon_shuzibao, "图集");
		Menu_Item_Bean lfbean2 = new Menu_Item_Bean(CODE.MENU_VIDEO_RECORDING, R.drawable.zy_icon_xinwenfabu, "视频点播");
		Menu_Item_Bean lfbean5 = new Menu_Item_Bean(CODE.MENU_SPECIAL, R.drawable.zy_icon_zhuanti, "专题报道");
		Menu_Item_Bean lfbean19 = new Menu_Item_Bean(CODE.MENU_SEARCH, R.drawable.zy_icon_xinwenfabu, "搜索");
		Menu_Item_Bean lfbean21 = new Menu_Item_Bean(CODE.MENU_ACTION, R.drawable.zy_icon_xinwenfabu, "活动");

		menuList.put(lfbean0.getId(), lfbean0);
		menuList.put(lfbean1.getId(), lfbean1);
		menuList.put(lfbean2.getId(), lfbean2);
		menuList.put(lfbean5.getId(), lfbean5);
		menuList.put(lfbean21.getId(), lfbean21);
		menuList.put(lfbean19.getId(), lfbean19);

	}

	@Override
	public void onTerminate() {
		LogUtils.i("onTerminate");
		ShareSDK.stopSDK(this);
		super.onTerminate();
	}

	public static App getInstance() {
		return mInstance;
	}


	public static final String collectiondbname = "hzpd_collectoin.db";//收藏数据库
	public static final String dbnewsjump = "newsjump.db"; //
	public static final String newsListDb = "newslist.db"; //新闻列表
	public static final String bianminListDb = "bianminlist.db"; //新闻列表
	public static final String zhuantiListDb = "zhuantilist.db"; //专题列表
	public static final String albumListDb = "albumlist.db"; //图集列表
	public static final String videoListDb = "videolist.db"; //视频列表

	public static final String mTitle = "title.dat";//频道信息
	public static List<String> mImageList;//


	public String getAllDiskCacheDir() {
		String cachePath = "";
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File externalFile = getApplicationContext().getExternalCacheDir();
			if ((null != externalFile) && (!externalFile.exists() || externalFile.mkdir())) {
				cachePath = externalFile.getPath();
			}
		}

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

	public static File getFile(String path) {

		int fp = path.lastIndexOf(File.separator);
		String sfp = path.substring(0, fp);
		LogUtils.i("spf-->" + sfp);
		File fpath = new File(sfp);

		if (!fpath.exists() && !fpath.isDirectory()) {
			fpath.mkdirs();
		}

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
				// TODO Auto-generated catch block
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
				String time = "昨天";
				return time;
			} else if ((today.getTime() - oldTime.getTime()) <= 0) { // 至少是今天
				String time = "今天";
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

}
