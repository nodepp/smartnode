package com.nodepp.smartnode.utils;

import android.content.Context;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;

/**
 * Created by yuyue on 2016/9/20.
 */
public class DBUtil {
    private static DbUtils dbUtils;

    /**
     * 获取dbUtils单例，数据库升级
     * @param context
     * @return
     */
    public static DbUtils getInstance(final Context context) {
        if (dbUtils == null) {
            synchronized (DBUtil.class) {
                if (dbUtils == null) {
                    //数据库版本更新
                    dbUtils = DbUtils.create(context.getApplicationContext(),Constant.DB_NAME, Constant.DB_VERSION, new DbUtils.DbUpgradeListener() {
                        @Override
                        public void onUpgrade(DbUtils dbUtils, int oldVersion, int newVersion) {

                            if (newVersion != oldVersion) {  //sqlite数据库不支持删除已经存在表的现有字段，只支持新增加字段
                                try {
//                                    if (dbUtils.tableIsExist(TimeTask.class)) {
//                                        dbUtils.dropTable(TimeTask.class);//如果增加了字段，之前旧数据就会为空就会出问题，不调用直接增加字段的方法，把旧的表格删除
                                    if (oldVersion < 17) {
//                                        addFieldToTable(dbUtils, "tb_device", "String", "clientKey");//添加列名clientKey的String字段
//                                        dbUtils.execNonQuery("alter table com_nodepp_smartnode_model_Device rename to tb_device ");//修改数据库表名
                                        addFieldToTable(dbUtils, "tb_device", "int", "firmwareLevel");//添加列名firmwareLevel的int字段
                                    }
                                     if (oldVersion <18){
                                         addFieldToTable(dbUtils, "tb_device", "int", "firmwareVersion");//添加列名firmwareVersion的int字段
                                     }
                                    if (oldVersion <19){
                                        addFieldToTable(dbUtils, "tb_device", "int", "deviceMode");//deviceMode int类型
                                    }
                                    if (oldVersion <20){
                                        addFieldToTable(dbUtils, "tb_time_task", "int", "operateIndex");//operateIndex int类型
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    DbUtils.DaoConfig daoConfig = dbUtils.getDaoConfig();
                    Log.i("daoConfig", "daoConfig.getDbName()----" + daoConfig.getDbName());
                    Log.i("daoConfig", "daoConfig.getDbVersion()----" + daoConfig.getDbVersion());
                    dbUtils.configAllowTransaction(true);// 开启事务
                }
            }
        }
        return dbUtils;
    }

    /**
     * 用于给现有的数据表添加字段的方法
     * @param dbUtils
     * @param tableName
     * @param fieldType
     * @param newFile
     */
    public static void addFieldToTable(DbUtils dbUtils, String tableName, String fieldType, String newFile) {
        try {
            if (fieldType.equals("int") || fieldType.equals("long") || fieldType.equals("boolean")) {
                dbUtils.execNonQuery("alter table " + tableName + " add " + newFile + " INTEGER ");//aler table只能用于表重命名和给表添加字段
            }else {
                dbUtils.execNonQuery("alter table " + tableName + " add " + newFile + " TEXT ");
            }

        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
