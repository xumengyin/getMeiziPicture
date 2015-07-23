/*
 * 文 件 名:  StbDataHelper.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  数据库操作助手类，通过SQLiteOpenHelper查询，删除，增加，修改，批量处理数据库。
 * 修 改 人:  y63586
 * 修改时间:  2012-10-18
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.example.getmeizi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库操作助手类，通过SQLiteOpenHelper查询，删除，增加，修改，批量处理数据库。
 * @author  b00198710
 * @version  [1.0, 2013-06-18]
 * @since  [1.0]
 */
public final class IptvDataHelper extends SQLiteOpenHelper
{
    /**
     * 数据库版本。
     */
    
    private static int databaseVersion = 15;
    
    public static void setVersion(int currentVersion)
    {
        databaseVersion = currentVersion;
    }
    
    /**
     * 数据库的名字。
     */
    private static final String DB_NAME = "iptv.db";
    
    private static IptvDataHelper dataHelper = null;
    
    public static synchronized IptvDataHelper getInstance(Context context)
    {
        if (dataHelper == null)
        {
            dataHelper = new IptvDataHelper(context);
        }
        return dataHelper;
    }
    
    public void deleteTable(String tableName)
    {
        synchronized (this)
        {
            SQLiteDatabase db = null;
            try
            {
                db = getWritableDatabase();
                db.execSQL("delete from " + tableName);
            }
            finally
            {
                if (db != null)
                {
                    db.close();
                }
            }
        }
    }
    
    private IptvDataHelper(Context context)
    {
        super(context, DB_NAME, null, databaseVersion);
    }
    
    private static final String[] objectClass = new String[] {"pic"};
    
    /**
     * 创建数据库。
     * @param db SQLiteDatabase对象。
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        synchronized (this)
        {
            
            for (String className : objectClass)
            {
                creatObjectTable(db, className);
            }
        }
        
    }
    
    /**
     * 升级数据库。
     * @param db SQLiteDatabase对象。
     * @param oldVersion 旧数据库版本
     * @param newVersion 新数据库版本
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        synchronized (this)
        {
           
            
            for (String className : objectClass)
            {
                dropTable(db, className);
            }
        }
    }
    
    private void createChannelsTable(final SQLiteDatabase db)
    {
        db.execSQL("create table channels (" + "id string primary key," + "data blob" + ")");
        db.execSQL("create index channels_id_idx on channels(id)");
    }
    
    private void createPlaybillsTable(final SQLiteDatabase db)
    {
        db.execSQL("create table playbills (" + "id string primary key," + "channel_id string, " + "start_time date, "
            + "end_time date, " + "data blob" + ")");
        db.execSQL("create index playbills_channel_id_idx on playbills(channel_id)");
        db.execSQL("create index playbills_start_time_idx on playbills(start_time)");
        db.execSQL("create index playbills_end_time_idx on playbills(end_time)");
    }
    
    private void createChannelExInfoTable(final SQLiteDatabase db)
    {
        db.execSQL("create table channel_ex_info (channel_id string,content_type string,category_id string)");
        db.execSQL("create index channel_ex_info_content_type on channel_ex_info(content_type)");
        db.execSQL("create index channel_ex_info_category_id on channel_ex_info(category_id)");
    }
    
    private void createHeartBitResponseTable(final SQLiteDatabase db)
    {
        db.execSQL("create table HeartBitResponse (data blob)");
    }
    
    private void createQueryTable(SQLiteDatabase db)
    {
        db.execSQL("create table if not exists " + "MemTable"
            + "( queryStr string primary key,responseData blob,time date )");
    }
    
    /**
     * 根据对象创建对应的表。
     * @param db  SQLiteDatabase对象。
     * @param clazz 需要生成的表对象。
     */
    private void creatObjectTable(SQLiteDatabase db, String tableName)
    {
        
        db.execSQL("create table if not exists " + tableName + "(_id integer primary key autoincrement,   data blob )");
        
    }
    
    /**
     * 根据对象删除对应的表。
     * @param db  SQLiteDatabase对象。
     * @param clazz 需要删除的表对象。
     */
    private void dropTable(SQLiteDatabase db, String className)
    {
        synchronized (this)
        {
            String sql = "drop table if exists " + className;
            db.execSQL(sql);
        }
    }
    
    private void dropChannelTables(SQLiteDatabase db)
    {
        db.execSQL("drop table if exists channels");
    }
    
    private void dropPlayBillTables(SQLiteDatabase db)
    {
        db.execSQL("drop table if exists playbills");
        db.execSQL("drop index if exists playbills_channel_id_idx");
        db.execSQL("drop index if exists playbills_start_time_idx");
        db.execSQL("drop index if exists playbills_end_time_idx");
    }
    
    private void dropChannelExInfoTable(SQLiteDatabase db)
    {
        db.execSQL("drop table if exists channel_ex_info");
        db.execSQL("drop index if exists channel_ex_info_content_type");
        db.execSQL("drop index if exists channel_ex_info_category_id");
    }
    
    private void dropHeartBitResponseTable(SQLiteDatabase db)
    {
        db.execSQL("drop table if exists HeartBitResponse");
    }
    
}
