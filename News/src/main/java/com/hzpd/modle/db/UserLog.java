package com.hzpd.modle.db;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "userlog")
public class UserLog extends BaseDB {

    @Column(column = "newsId")
    String newsId;

    @Column(column = "time")
    String time;

    @Column(column = "reading_time")
    int active_time;

    public UserLog() {
        super();
    }

    public UserLog(String newsId, String time, int retentionTime) {
        super();
        this.newsId = newsId;
        this.time = time;
        this.active_time = retentionTime;
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

    public int getActive_time() {
        return active_time;
    }

    public void setActive_time(int active_time) {
        this.active_time = active_time;
    }
}
