package com.nodepp.smartnode.model;

import java.io.Serializable;

/**
 * Created by yuyue on 2017/8/24.
 */
public abstract class EntityBase implements Serializable {


    //@Id // 如果主键没有命名名为id或_id的时，需要为主键添加此注解
    //@NoAutoIncrement // int,long类型的id默认自增，不想使用自增时添加此注解
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
