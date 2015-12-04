package com.hzpd.utils.db;

import android.content.Context;
import android.os.AsyncTask;

import com.hzpd.modle.NewsBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.ui.App;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.interfaces.I_SetList;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.Log;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

public class ZhuantiDetailListDbTask {
	private DbUtils zhuantiListDb;

	public ZhuantiDetailListDbTask(Context context) {
		zhuantiListDb = DBHelper.getInstance(context).getNewsListDbUtils();
	}

	public void findList(String columnid, int page, int pageSize
			, I_SetList<NewsBeanDB> callBack) {
		Log.e("test","NewsFindTask");
		NewsFindTask newsFindTask = new NewsFindTask(page, pageSize, columnid, callBack);
		newsFindTask.executeOnExecutor(App.executorService);
	}

	public void saveList(List<NewsBean> nbList, I_Result callBack) {
		NewsSaveTask newsSaveTask = new NewsSaveTask(nbList, callBack);
		newsSaveTask.executeOnExecutor(App.executorService);
	}

	public void deleteList(List<String> nbList, I_Result callBack) {
		NewsDeleteTask newsDeleteTask = new NewsDeleteTask(nbList, callBack);
		newsDeleteTask.executeOnExecutor(App.executorService);
	}

	public void asyncDeleteList(List<String> nbList) {
		for (String nid : nbList) {
			try {
				zhuantiListDb.delete(NewsBeanDB.class
						, WhereBuilder.b("nid", "=", nid));
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}


	public void isRead(String nid, I_Result callBack) {
		NewsIsReadedTask newsIsReadTask = new NewsIsReadedTask(nid, callBack);
		newsIsReadTask.executeOnExecutor(App.executorService);
	}

	public void dropTable(I_Result callBack) {
		NewsClearTableTask newsDropTable = new NewsClearTableTask(callBack);
		newsDropTable.executeOnExecutor(App.executorService);
	}

	public void asyncDropTable() {
		try {
			zhuantiListDb.deleteAll(NewsBeanDB.class);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	class NewsFindTask extends AsyncTask<String, String, List<NewsBeanDB>> {
		private int page;
		private int pageSize;
		private I_SetList<NewsBeanDB> callBack;
		private String columnid;

		public NewsFindTask(int page, int pageSize
				, I_SetList<NewsBeanDB> callBack) {
			this(page, pageSize, "0", callBack);
		}

		public NewsFindTask(int page, int pageSize, String columnid
				, I_SetList<NewsBeanDB> callBack) {
			this.page = page - 1;
			this.pageSize = pageSize;
			this.callBack = callBack;
			this.columnid = columnid;
		}

		@Override
		protected List<NewsBeanDB> doInBackground(String... params) {
			List<NewsBeanDB> list = null;
			Log.e("test","NewsFindTask doInBackground");
			try {
				Selector selector = Selector.from(NewsBeanDB.class);

				selector.where("columnid", "=", columnid);


				selector.orderBy("subjectsort", true)
						.limit(pageSize)
						.offset(page * pageSize);
				list = zhuantiListDb.findAll(selector);
			} catch (DbException e) {
				e.printStackTrace();
			}

			return list;
		}

		@Override
		protected void onPostExecute(List<NewsBeanDB> result) {
			Log.e("test","NewsFindTask onPostExecute");
			if (null != callBack) {
				callBack.setList(result);
			}
		}

	}

	class NewsSaveTask extends AsyncTask<String, String, Boolean> {
		private I_Result callBack;
		private List<NewsBean> nbList;

		public NewsSaveTask(List<NewsBean> nbList, I_Result callBack) {
			this.nbList = nbList;
			this.callBack = callBack;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean flag = false;
			if (null == nbList) {
				return flag;
			}
			List<NewsBeanDB> list = new ArrayList<NewsBeanDB>();
			for (NewsBean nb : nbList) {
				NewsBeanDB nbdb = new NewsBeanDB(nb);
				list.add(nbdb);
			}

			for (NewsBeanDB nbdb : list) {
				try {
					zhuantiListDb.delete(NewsBeanDB.class
							, WhereBuilder.b("nid", "=", nbdb.getNid()));
					zhuantiListDb.save(nbdb);
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

	class NewsDeleteTask extends AsyncTask<String, String, Boolean> {
		private I_Result callBack;
		private List<String> nbList;

		public NewsDeleteTask(List<String> nbList, I_Result callBack) {
			this.nbList = nbList;
			this.callBack = callBack;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (null == nbList) {
				return false;
			}

			for (String nid : nbList) {
				try {
					zhuantiListDb.delete(NewsBeanDB.class
							, WhereBuilder.b("nid", "=", nid));
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

	class NewsIsReadedTask extends AsyncTask<String, String, Boolean> {
		private String nid;
		private I_Result callBack;

		public NewsIsReadedTask(String nid, I_Result callBack) {
			this.nid = nid;
			this.callBack = callBack;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean isReaded = false;

			try {
				NewsBeanDB newsdb = zhuantiListDb.findFirst(Selector
						.from(NewsBeanDB.class)
						.where("nid", "=", nid)
						.and("isreaded", "=", 1));
				if (null != newsdb) {
					isReaded = true;
				}
			} catch (DbException e) {
				e.printStackTrace();
			}

			return isReaded;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (null != callBack) {
				callBack.setResult(result);
			}
		}
	}

	class NewsClearTableTask extends AsyncTask<String, String, Boolean> {

		private I_Result callBack;

		public NewsClearTableTask(I_Result callBack) {
			this.callBack = callBack;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				zhuantiListDb.dropTable(NewsBeanDB.class);
				return zhuantiListDb.tableIsExist(NewsBeanDB.class);
			} catch (DbException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			callBack.setResult(result);
		}
	}

}
