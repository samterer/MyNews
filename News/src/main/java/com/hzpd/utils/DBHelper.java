package com.hzpd.utils;

import android.app.Activity;
import android.content.Context;

import com.hzpd.modle.db.AlbumBeanDBDao;
import com.hzpd.modle.db.DaoMaster;
import com.hzpd.modle.db.DaoSession;
import com.hzpd.modle.db.JsonbeanDao;
import com.hzpd.modle.db.NewsBeanDBDao;
import com.hzpd.modle.db.NewsChannelBeanDBDao;
import com.hzpd.modle.db.NewsItemBeanForCollectionDao;
import com.hzpd.modle.db.NewsJumpBeanDao;
import com.hzpd.modle.db.PushBeanDBDao;
import com.hzpd.modle.db.UserLogDao;
import com.hzpd.modle.db.VideoItemBeanDbDao;
import com.hzpd.modle.db.ZhuantiBeanDBDao;
import com.hzpd.ui.App;

public class DBHelper {
    private static DBHelper instance;
    private final NewsJumpBeanDao newsJumpBeanDao;
    private final JsonbeanDao jsonbeanDao;
    private final AlbumBeanDBDao albumDBUitls;
    private final VideoItemBeanDbDao videoDBUitls;
    private final NewsBeanDBDao newsList;
    private final ZhuantiBeanDBDao zhuantiList;
    private final UserLogDao log;
    private final PushBeanDBDao pushList;
    private final NewsChannelBeanDBDao channel;
    private final NewsItemBeanForCollectionDao collectionDBUitls;

    private DBHelper(Context mContext) {
        Log.e("DBHelper", "DBHelper collectionDBUitls ");
        DaoMaster daoMaster = App.getInstance().daoMaster;
        DaoSession session = daoMaster.newSession();
        newsJumpBeanDao = session.getNewsJumpBeanDao();
        jsonbeanDao = session.getJsonbeanDao();
        collectionDBUitls = session.getNewsItemBeanForCollectionDao();
        albumDBUitls = session.getAlbumBeanDBDao();
        videoDBUitls = session.getVideoItemBeanDbDao();
        newsList = session.getNewsBeanDBDao();
        zhuantiList = session.getZhuantiBeanDBDao();
        log = session.getUserLogDao();
        pushList = session.getPushBeanDBDao();
        channel = session.getNewsChannelBeanDBDao();
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (context instanceof Activity) {
            context = context.getApplicationContext();
        }
        if (null == instance) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    public static synchronized void setInstance(Context context) {
        instance = new DBHelper(context);
    }

    public void clear() {
        newsList.deleteAll();
        albumDBUitls.deleteAll();
        videoDBUitls.deleteAll();
        zhuantiList.deleteAll();
        collectionDBUitls.deleteAll();
        jsonbeanDao.deleteAll();
        newsJumpBeanDao.deleteAll();

    }

    public NewsJumpBeanDao getNewsJumpBeanDao() {
        return newsJumpBeanDao;
    }

    public JsonbeanDao getJsonbeanDao() {
        return jsonbeanDao;
    }

    public AlbumBeanDBDao getAlbumDBUitls() {
        return albumDBUitls;
    }

    public VideoItemBeanDbDao getVideoDBUitls() {
        return videoDBUitls;
    }

    public NewsBeanDBDao getNewsList() {
        return newsList;
    }

    public ZhuantiBeanDBDao getZhuantiList() {
        return zhuantiList;
    }

    public UserLogDao getLog() {
        return log;
    }

    public PushBeanDBDao getPushList() {
        return pushList;
    }

    public NewsChannelBeanDBDao getChannel() {
        return channel;
    }

    public NewsItemBeanForCollectionDao getCollectionDBUitls() {
        return collectionDBUitls;
    }
}
