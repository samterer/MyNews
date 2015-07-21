package com.hzpd.modle.db;

import com.lidroid.xutils.db.annotation.Id;

public abstract class BaseDB {

	@Id // 如果主键没有命名名为id或_id的时，需要为主键添加此注解
	protected int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
