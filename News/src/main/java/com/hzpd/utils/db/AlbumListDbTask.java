package com.hzpd.utils.db;

import android.content.Context;
import android.os.AsyncTask;

import com.hzpd.modle.ImgListBean;
import com.hzpd.modle.db.AlbumBeanDB;
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

public class AlbumListDbTask {

	private DbUtils albumListDb;

	public AlbumListDbTask(Context context) {
		albumListDb = DBHelper.getInstance(context).getAlbumDBUitls();
	}


	public void findList(int page, int pageSize
			, I_SetList<AlbumBeanDB> callBack) {

		AlbumFindTask albumFindTask = new AlbumFindTask(page, pageSize, callBack);
		albumFindTask.executeOnExecutor(App.executorService);
	}

	public void saveList(List<ImgListBean> list, I_Result i_AlbumListDbSave) {
		AlbumSaveTask albumSaveTask = new AlbumSaveTask(list, i_AlbumListDbSave);
		albumSaveTask.executeOnExecutor(App.executorService);
	}

	public void deleteList(List<String> abList, I_Result callBack) {
		AlbumDeleteTask albumDeleteTask = new AlbumDeleteTask(abList, callBack);
		albumDeleteTask.executeOnExecutor(App.executorService);
	}

	public void asyncDeleteList(List<String> abList) {
		for (String pid : abList) {
			try {
				albumListDb.delete(AlbumBeanDB.class
						, WhereBuilder.b("pid", "=", pid));
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}

	public void clearList(I_Result callBack) {
		AlbumClearTableTask albumClearTableTask = new AlbumClearTableTask(callBack);
		albumClearTableTask.executeOnExecutor(App.executorService);
	}

	public void asyncDropTable() {
		try {
			albumListDb.deleteAll(AlbumBeanDB.class);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	class AlbumFindTask extends AsyncTask<String, String, List<AlbumBeanDB>> {
		private I_SetList<AlbumBeanDB> callBack;
		private int page;
		private int pageSize;

		public AlbumFindTask(int page, int pageSize
				, I_SetList<AlbumBeanDB> callBack) {
			this.callBack = callBack;
			this.page = page - 1;
			this.pageSize = pageSize;
		}

		@Override
		protected List<AlbumBeanDB> doInBackground(String... params) {
			List<AlbumBeanDB> list = null;
			LogUtils.i("doInBackground");
			try {
				LogUtils.i("AlbumFindTask find before");
				LogUtils.i("pageSize-->" + pageSize + "   page-->" + page);
				list = albumListDb.findAll(Selector.from(AlbumBeanDB.class)
						.orderBy("id", true)
						.limit(pageSize)
						.offset(page * pageSize));
				LogUtils.i("AlbumFindTask find after");

				if (null != list) {
					LogUtils.i("onPostExecute.findList-->" + list.size());
				}
			} catch (DbException e) {
				e.printStackTrace();
				LogUtils.i("AlbumFindTask dbException");
			}

			return list;
		}

		@Override
		protected void onPostExecute(List<AlbumBeanDB> result) {
			LogUtils.i("AlbumFindTask  onPostExecute");

			if (null != callBack) {
				callBack.setList(result);
			}
		}

	}

	class AlbumSaveTask extends AsyncTask<String, String, Boolean> {

		private List<ImgListBean> ablist;
		private I_Result callBack;


		public AlbumSaveTask(List<ImgListBean> ablist, I_Result callBack) {
			this.ablist = ablist;
			this.callBack = callBack;
			LogUtils.i("AlbumSaveTask()");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean flag = false;
			if (null == ablist) {
				LogUtils.i("ablist null");
				return false;
			}
			List<AlbumBeanDB> list = new ArrayList<AlbumBeanDB>();
			for (ImgListBean ab : ablist) {
				AlbumBeanDB abdb = new AlbumBeanDB(ab);
				list.add(abdb);
			}

			LogUtils.i("list.size()-->" + list.size());

			for (AlbumBeanDB abdb : list) {
				try {
					albumListDb.delete(AlbumBeanDB.class,
							WhereBuilder.b("pid", "=", abdb.getPid()));
					albumListDb.saveBindingIdAll(abdb.getSubphoto());
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

	class AlbumDeleteTask extends AsyncTask<String, String, Boolean> {

		private List<String> abList;
		private I_Result callBack;

		public AlbumDeleteTask(List<String> abList, I_Result callBack) {
			this.abList = abList;
			this.callBack = callBack;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (null == abList) {
				return false;
			}

			for (String pid : abList) {
				try {
					albumListDb.delete(AlbumBeanDB.class
							, WhereBuilder.b("pid", "=", pid));
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

	class AlbumClearTableTask extends AsyncTask<String, String, Boolean> {
		private I_Result callBack;

		public AlbumClearTableTask(I_Result callBack) {
			this.callBack = callBack;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				albumListDb.dropTable(AlbumBeanDB.class);
				return albumListDb.tableIsExist(AlbumBeanDB.class);
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
