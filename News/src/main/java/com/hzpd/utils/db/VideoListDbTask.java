package com.hzpd.utils.db;

import android.content.Context;
import android.os.AsyncTask;

import com.hzpd.modle.VideoItemBean;
import com.hzpd.modle.db.VideoItemBeanDb;
import com.hzpd.ui.App;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.utils.DBHelper;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class VideoListDbTask {

	private DbUtils videoListDb;

	public VideoListDbTask(Context context) {
		videoListDb = DBHelper.getInstance(context).getVideoDBUitls();
	}


	public void findList(int page, int pageSize
			, I_SetList<VideoItemBeanDb> callBack) {

		VideoFindTask albumFindTask = new VideoFindTask(page, pageSize, callBack);
		albumFindTask.executeOnExecutor(App.executorService);
	}

	public void saveList(List<VideoItemBean> list, I_Result i_VideoListDbSave) {
		VideoSaveTask albumSaveTask = new VideoSaveTask(list, i_VideoListDbSave);
		albumSaveTask.executeOnExecutor(App.executorService);
	}

	public void deleteList(List<String> vbList, I_Result callBack) {
		VideoDeleteTask albumDeleteTask = new VideoDeleteTask(vbList, callBack);
		albumDeleteTask.executeOnExecutor(App.executorService);
	}

	public void asyncDeleteList(List<String> vbList) {

		for (String vid : vbList) {
			try {
				videoListDb.delete(VideoItemBeanDb.class
						, WhereBuilder.b("vid", "=", vid));
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}


	public void asyncDropTable() {
		try {
			videoListDb.deleteAll(VideoItemBeanDb.class);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	public void clearList(I_Result callBack) {
		VideoClearTableTask videoClearTableTask = new VideoClearTableTask(callBack);
		videoClearTableTask.executeOnExecutor(App.executorService);
	}

	class VideoFindTask extends AsyncTask<String, String, List<VideoItemBeanDb>> {
		private I_SetList<VideoItemBeanDb> callBack;
		private int page;
		private int pageSize;

		public VideoFindTask(int page, int pageSize
				, I_SetList<VideoItemBeanDb> callBack) {
			this.callBack = callBack;
			this.page = page - 1;
			this.pageSize = pageSize;
		}

		@Override
		protected List<VideoItemBeanDb> doInBackground(String... params) {
			List<VideoItemBeanDb> list = null;
			LogUtils.i("doInBackground");
			try {
				LogUtils.i("videoFindTask find before");
				list = videoListDb.findAll(Selector.from(VideoItemBeanDb.class)
						.orderBy("id", true)
						.limit(pageSize)
						.offset(page * pageSize));
				LogUtils.i("videoFindTask find after");
			} catch (DbException e) {
				e.printStackTrace();
				LogUtils.i("videoFindTask dbException");
			}

			return list;
		}

		@Override
		protected void onPostExecute(List<VideoItemBeanDb> result) {
			LogUtils.i("videoFindTask  onPostExecute");
			if (null != callBack) {
				callBack.setList(result);
			}
		}

	}

	class VideoSaveTask extends AsyncTask<String, String, Boolean> {

		private List<VideoItemBean> vblist;
		private I_Result callBack;


		public VideoSaveTask(List<VideoItemBean> vblist, I_Result callBack) {
			this.vblist = vblist;
			this.callBack = callBack;
			LogUtils.i("VideoSaveTask()");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean flag = false;
			if (null == vblist) {
				LogUtils.i("vblist null");
				return false;
			}
			List<VideoItemBeanDb> list = new ArrayList<VideoItemBeanDb>();
			for (VideoItemBean vb : vblist) {
				VideoItemBeanDb vbdb = new VideoItemBeanDb(vb);
				list.add(vbdb);
			}

			LogUtils.i("list.size()-->" + list.size());

			for (VideoItemBeanDb vbdb : list) {
				try {

					videoListDb.delete(VideoItemBeanDb.class
							, WhereBuilder.b("vid", "=", vbdb.getVid()));
					videoListDb.save(vbdb);
					flag = true;
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
			return flag;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (null != callBack) {
				callBack.setResult(result);
			}
		}

	}

	class VideoDeleteTask extends AsyncTask<String, String, Boolean> {

		private List<String> vbList;
		private I_Result callBack;

		public VideoDeleteTask(List<String> vbList, I_Result callBack) {
			this.vbList = vbList;
			this.callBack = callBack;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (null == vbList) {
				return false;
			}

			for (String vid : vbList) {
				try {
					videoListDb.delete(VideoItemBeanDb.class
							, WhereBuilder.b("vid", "=", vid));
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (null != callBack) {
				callBack.setResult(result);
			}
		}

	}

	class VideoClearTableTask extends AsyncTask<String, String, Boolean> {
		private I_Result callBack;

		public VideoClearTableTask(I_Result callBack) {
			this.callBack = callBack;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				videoListDb.dropTable(VideoItemBeanDb.class);
				return videoListDb.tableIsExist(VideoItemBeanDb.class);
			} catch (DbException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (null != callBack) {
				callBack.setResult(result);
			}
		}

	}
}
