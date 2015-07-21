package com.hzpd.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;

import com.hzpd.ui.App;
import com.hzpd.utils.DataCleanManager;
import com.hzpd.utils.db.AlbumListDbTask;
import com.hzpd.utils.db.NewsListDbTask;
import com.hzpd.utils.db.VideoListDbTask;
import com.hzpd.utils.db.ZhuantiDetailListDbTask;
import com.hzpd.utils.db.ZhuantiListDbTask;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

public class ClearCacheService extends IntentService {


	public ClearCacheService() {
		super("clear");
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtils.i("ClearCacheService start");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void setIntentRedelivery(boolean enabled) {
		super.setIntentRedelivery(enabled);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		try {
			DataCleanManager.deleteDir(new File(App.getInstance().getJsonFileCacheRootDir()));

			ImageLoader.getInstance().getDiskCache().clear();

			//新闻列表
			NewsListDbTask newsListDbTask = new NewsListDbTask(this);
			newsListDbTask.asyncDropTable();

			AlbumListDbTask albumListDbTask = new AlbumListDbTask(this);
			albumListDbTask.asyncDropTable();

			VideoListDbTask videoListDbTask = new VideoListDbTask(this);
			videoListDbTask.asyncDropTable();

			ZhuantiDetailListDbTask zhuantiDetailListDbTask = new ZhuantiDetailListDbTask(this);
			zhuantiDetailListDbTask.asyncDropTable();

			ZhuantiListDbTask zhuantiListDbTask = new ZhuantiListDbTask(this);
			zhuantiListDbTask.asyncDropTable();


		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static long getEnvironmentSize() {
		File localFile = Environment.getDataDirectory();
		long l1;
		if (localFile == null)
			l1 = 0L;
		while (true) {
			String str = localFile.getPath();
			StatFs localStatFs = new StatFs(str);
			long l2 = localStatFs.getBlockSize();
			l1 = localStatFs.getBlockCount() * l2;
			return l1;
		}
	}


	@Override
	public void onDestroy() {
		LogUtils.i("ClearCacheService destroy");
		super.onDestroy();
	}

}
