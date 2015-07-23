package com.example.getmeizi;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;






import org.apache.commons.lang.SerializationUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class ObjectStore<T>
{
    
    IptvDataHelper iptvDataHandler = null;
    
//    protected static final Serializer serializer;
//    
//    static
//    {
//        Strategy strategy = new AnnotationStrategy();
//        serializer = new Persister(strategy, new APIBooleanMatcher());
//    }
    
    public ObjectStore(Context context)
    {
        iptvDataHandler = IptvDataHelper.getInstance(context);
    }
    
    public Map<T, Long> getObjectList(Class<T> cls, String tableName)
    {
        final Map<T, Long> objectMap = new LinkedHashMap<T, Long>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        synchronized (iptvDataHandler)
        {
            
            try
            {
                db = iptvDataHandler.getReadableDatabase();
                cursor = db.rawQuery("select * from " + tableName, null);
                if (cursor.moveToFirst())
                {
                    byte[] byteArray = null;
                    do
                    {
                        byteArray = cursor.getBlob(cursor.getColumnIndex("data"));
                        if (byteArray == null)
                        {
                            continue;
                        }
//                        final T object = MemStore.fromBytes(byteArray, cls);
                        final T object = fromByte(byteArray);
                        if (object == null)
                        {
//                            DebugLog.error(tableName, "got object from database is null. ");
                        }
                        else
                        {
                            objectMap.put(object, cursor.getLong(cursor.getColumnIndex("_id")));
                        }
                    } while (cursor.moveToNext());
                }
            }
            finally
            {
                if (cursor != null)
                {
                    cursor.close();
                }
                if (db != null)
                {
                    db.close();
                }
            }
            
        }
      //  DebugLog.error(tableName, "got object list from database. " + objectMap.size());
        
        return objectMap;
    }
    
    public void deleteOneObject(Long id, String tableName)
    {
        synchronized (iptvDataHandler)
        {
            SQLiteDatabase db = null;
            try
            {
                db = iptvDataHandler.getWritableDatabase();
                db.delete(tableName, "_id = ?", new String[] {String.valueOf(id)});
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
    
    public void deleteTable(String tableName)
    {
        synchronized (iptvDataHandler)
        {
            SQLiteDatabase db = null;
            try
            {
                db = iptvDataHandler.getWritableDatabase();
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
    
    public void updateOneObject(String tableName, Long id, final T obj)
    {
        synchronized (iptvDataHandler)
        {
            SQLiteDatabase db = null;
            try
            {
                db = iptvDataHandler.getWritableDatabase();
                db.update(tableName, objectToContentValues(obj), "_id = ?", new String[] {String.valueOf(id)});
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
    
    public Long insertOneObject(String tableName, final T obj)
    {
        Long id = 0L;
        synchronized (iptvDataHandler)
        {
            SQLiteDatabase db = null;
            try
            {
                db = iptvDataHandler.getWritableDatabase();
                id = db.insert(tableName, null, objectToContentValues(obj));
            }
            finally
            {
                if (db != null)
                {
                    db.close();
                }
            }
        }
        return id;
    }
    
    public Map<T, Long> insertObjectList(String tableName, final List<T> objList)
    {
        Map<T, Long> ret = new HashMap<T, Long>();
        synchronized (iptvDataHandler)
        {
            SQLiteDatabase objectDb = null;
            objectDb = iptvDataHandler.getWritableDatabase();
            if (objectDb != null)
            {
                objectDb.beginTransaction();
                try
                {
                    for (final T object : objList)
                    {
                        Long id = objectDb.insert(tableName, null, objectToContentValues(object));
                        if (id > -1)
                        {
                            ret.put(object, id);
                        }
                        
                    }
                    objectDb.setTransactionSuccessful();
                }
                finally
                {
                    objectDb.endTransaction();
                    objectDb.close();
                }
            }
        }
        return ret;
    }
    
    private ContentValues objectToContentValues(final T obj)
    {
        ContentValues values = new ContentValues();
       // byte[] byteArray = MemStore.toBytes(obj);//SerializerService.toXml(obj);
        byte[] byteArray = SerializationUtils.serialize((java.io.Serializable)obj);
        values.put("data", byteArray);
        return values;
    }
    @SuppressWarnings("unchecked")
	private T fromByte(byte[]byteArray)
    {
    	return (T) SerializationUtils.deserialize(byteArray);
    }
}
