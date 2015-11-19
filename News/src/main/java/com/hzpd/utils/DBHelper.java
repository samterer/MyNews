package com.hzpd.utils;

import android.app.Activity;
import android.content.Context;

import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.ui.App;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

public class DBHelper {
    private DbUtils collectionDBUitls;//本地收藏数据库
    private DbUtils albumDBUitls;//图集
    private DbUtils videoDBUitls;//视频

    private DbUtils newsListDbUtils;//新闻列表数据库
    private DbUtils bianminListDbUtils;//便民列表数据库
    private DbUtils zhuantiListDbUtils;//专题列表数据库

    private DbUtils logDbUtils;

    private static DBHelper instance;

    private String dbPath;
    private Context context;

    private DBHelper(Context mContext) {
        this.context = mContext;
//		dbPath= mContext.getCacheDir().getAbsolutePath();
        dbPath = this.context.getDatabasePath("hzpd").getAbsolutePath();
        collectionDBUitls = DbUtils.create(context
                , dbPath, App.collectiondbname, 4, new DbUtils.DbUpgradeListener() {
            @Override
            public void onUpgrade(DbUtils dbUtils, int i, int i1) {
                try {
                    dbUtils.dropTable(NewsItemBeanForCollection.class);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });
        albumDBUitls = DbUtils.create(context
                , dbPath, App.albumListDb);
        albumDBUitls.configAllowTransaction(true);
        videoDBUitls = DbUtils.create(context
                , dbPath, App.videoListDb);
        videoDBUitls.configAllowTransaction(true);

        newsListDbUtils = DbUtils.create(context
                , dbPath, App.newsListDb, 4, new DbUtils.DbUpgradeListener() {
            @Override
            public void onUpgrade(DbUtils dbUtils, int i, int i1) {
                try {
                    dbUtils.dropTable(NewsBeanDB.class);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });
        bianminListDbUtils = DbUtils.create(context
                , dbPath, App.bianminListDb);
        zhuantiListDbUtils = DbUtils.create(context
                , dbPath, App.zhuantiListDb);
        logDbUtils = DbUtils.create(context
                , dbPath, App.userLogDb);


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

    public DbUtils getCollectionDBUitls() {
        return collectionDBUitls;
    }

    public DbUtils getAlbumDBUitls() {
        return albumDBUitls;
    }

    public DbUtils getNewsListDbUtils() {
        return newsListDbUtils;
    }

    public DbUtils getVideoDBUitls() {
        return videoDBUitls;
    }

    public DbUtils getZhuantiListDbUtils() {
        return zhuantiListDbUtils;
    }

    public DbUtils getBianminListDbUtils() {
        return bianminListDbUtils;
    }

    public DbUtils getLogDbUtils() {
        return logDbUtils;
    }
}
