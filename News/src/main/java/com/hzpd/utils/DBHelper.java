package com.hzpd.utils;

import android.app.Activity;
import android.content.Context;

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
    private DbUtils pushListDbUtils;//新闻列表数据库
    private DbUtils channelDbUtils;//频道,TAG数据库

    private DbUtils logDbUtils;

    private static DBHelper instance;

    private String dbPath;
    private Context context;


    public DbUtils testDb;

    private DBHelper(Context mContext) {
        this.context = mContext;
        Log.e("DBHelper", "DBHelper collectionDBUitls ");
        dbPath = this.context.getDatabasePath("cms").getAbsolutePath();
        collectionDBUitls = DbUtils.create(context
                , dbPath, SPUtil.getCountry() + "" + App.collectiondbname);
        albumDBUitls = DbUtils.create(context
                , dbPath, SPUtil.getCountry() + "" + App.albumListDb);
        albumDBUitls.configAllowTransaction(true);
        videoDBUitls = DbUtils.create(context
                , dbPath, SPUtil.getCountry() + "" + App.videoListDb);
        videoDBUitls.configAllowTransaction(true);

        newsListDbUtils = DbUtils.create(context
                , dbPath, SPUtil.getCountry() + "" + App.newsListDb, 10, new DbUtils.DbUpgradeListener() {
            @Override
            public void onUpgrade(DbUtils db, int oldVersion, int newVersion) {
                try {
                    Log.e("DBHelper", "DbUtils " + oldVersion + ":" + newVersion);
                    if (newVersion > oldVersion) {
                        db.dropTable(NewsBeanDB.class);
                        Log.e("DBHelper", "DbUtils Update DropTable ");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });
        bianminListDbUtils = DbUtils.create(context
                , dbPath, SPUtil.getCountry() + "" + App.bianminListDb);
        zhuantiListDbUtils = DbUtils.create(context
                , dbPath, SPUtil.getCountry() + "" + App.zhuantiListDb);
        logDbUtils = DbUtils.create(context
                , dbPath, SPUtil.getCountry() + "" + App.userLogDb);
        pushListDbUtils = DbUtils.create(context
                , dbPath, SPUtil.getCountry() + "" + App.pushListDb);
        channelDbUtils = DbUtils.create(context
                , dbPath, SPUtil.getCountry() + "" + App.channelDb);
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

    public DbUtils getPushListDbUtils() {
        return pushListDbUtils;
    }

    public DbUtils getChannelDbUtils() {
        return channelDbUtils;
    }
}
