package com.hzpd.modle.db;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "userlog")
public class UserLog extends BaseDB {

    @Column(column = "newsId")
    String newsId;

    @Column(column = "time")
    String time;

    public UserLog() {
        super();
    }

    public UserLog(String newsId, String time) {
        super();
        this.newsId = newsId;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }
}
