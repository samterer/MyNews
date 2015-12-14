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

import java.util.ArrayList;
import java.util.List;

public class PushListDbTask {
    private DbUtils pushListDb;

    public PushListDbTask(Context context) {
        pushListDb = DBHelper.getInstance(context).getPushListDbUtils();
    }

    public void findList(String tid
            , int page, int pageSize
            , I_SetList<NewsBeanDB> callBack) {

        NewsFindTask newsFindTask = new NewsFindTask(tid, page, pageSize, callBack);
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
                pushListDb.delete(NewsBeanDB.class
                        , WhereBuilder.b("nid", "=", nid));
            } catch (Exception e) {
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
            pushListDb.deleteAll(NewsBeanDB.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class NewsFindTask extends AsyncTask<String, String, List<NewsBeanDB>> {
        private int page;
        private int pageSize;
        private String tid;
        private I_SetList<NewsBeanDB> callBack;
        private String sid;

        public NewsFindTask(String tid
                , int page, int pageSize
                , I_SetList<NewsBeanDB> callBack) {
            this(tid, page, pageSize, "0", callBack);
        }

        public NewsFindTask(String tid
                , int page, int pageSize, String sid
                , I_SetList<NewsBeanDB> callBack) {
            super();
            this.page = page - 1;
            this.pageSize = pageSize;
            this.callBack = callBack;
            this.tid = tid;
            this.sid = sid;
        }

        @Override
        protected List<NewsBeanDB> doInBackground(String... params) {
            List<NewsBeanDB> list = null;
            try {
                Selector selector = Selector.from(NewsBeanDB.class)
                        .where("tid", "=", tid);
                selector.orderBy("nid", true)
                        .limit(pageSize)
                        .offset(page * pageSize);
                list = pushListDb.findAll(selector);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<NewsBeanDB> result) {
            if (null != callBack) {
                callBack.setList(result);
            }
        }

    }
    class NewsSaveTask extends AsyncTask<String, String, Boolean> {
        private I_Result callBack;
        private List<NewsBean> nbList;

        public NewsSaveTask(List<NewsBean> nbList, I_Result callBack) {
            super();
            this.nbList = nbList;
            this.callBack = callBack;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            boolean flag = false;
            if (null == nbList) {
                return flag;
            }
            try {
                List<NewsBeanDB> list = new ArrayList<NewsBeanDB>();
                for (NewsBean nb : nbList) {
                    NewsBeanDB nbdb = new NewsBeanDB(nb);
                    list.add(nbdb);
                }

                for (NewsBeanDB nbdb : list) {
                    try {
                        Log.i("PushListDbTask", "PushListDbTask--->OK");
                        pushListDb.delete(NewsBeanDB.class
                                , WhereBuilder.b("nid", "=", nbdb.getNid()));
                        pushListDb.save(nbdb);
                        flag = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
//    class NewsSaveTask extends AsyncTask<String, String, Boolean> {
//        private I_Result callBack;
//        private NewsBean nbList;
//
//        public NewsSaveTask(NewsBean nbList, I_Result callBack) {
//            super();
//            this.nbList = nbList;
//            this.callBack = callBack;
//        }
//
//
//        @Override
//        protected Boolean doInBackground(String... params) {
//            boolean flag = false;
//            if (null == nbList) {
//                return flag;
//            }
//            Log.i("PushListDbTask", "PushListDbTask"+nbList.toString());
//
//            try {
//                Log.i("PushListDbTask", "PushListDbTask--->OK");
//                pushListDb.save(nbList);
//                flag = true;
//                Log.i("PushListDbTask", "PushListDbTask--->"+flag);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            return flag;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            if (null != callBack) {
//                callBack.setResult(result);
//            }
//        }
//
//
//    }

    class NewsDeleteTask extends AsyncTask<String, String, Boolean> {
        private I_Result callBack;
        private List<String> nbList;

        public NewsDeleteTask(List<String> nbList, I_Result callBack) {
            super();
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
                    pushListDb.delete(NewsBeanDB.class
                            , WhereBuilder.b("nid", "=", nid));
                } catch (Exception e) {
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
            super();
            this.nid = nid;
            this.callBack = callBack;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean isReaded = false;
            try {
                NewsBeanDB newsdb = pushListDb.findFirst(Selector
                        .from(NewsBeanDB.class)
                        .where("nid", "=", nid)
                        .and("isreaded", "=", 1));
                if (null != newsdb) {
                    isReaded = true;
                }
            } catch (Exception e) {
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
            super();
            this.callBack = callBack;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                pushListDb.dropTable(NewsBeanDB.class);
                return pushListDb.tableIsExist(NewsBeanDB.class);
            } catch (Exception e) {
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
