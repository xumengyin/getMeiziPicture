package image;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.example.getmeizi.App;

public class LruBitmapCache extends LruCache<String, Bitmap>
{
    
    public static final Config BITMAP_COLOR_CONFIG = Config.RGB_565;
    
    public static final int BYTE_COLOR_BITMAP = getByteoFBitmapConfig();
    
    public static LruBitmapCache newInstance()
    {
        int cacheSizeInMillon = 0;
        
        Context context = App.instance;
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        cacheSizeInMillon = activityManager.getMemoryClass();
        if (cacheSizeInMillon > 64)
        {
            cacheSizeInMillon = cacheSizeInMillon / 16;
        }
        else
        {
            cacheSizeInMillon = 4;
        }
        return new LruBitmapCache(cacheSizeInMillon * 1024 * 1024);
    }
    
    public LruBitmapCache(int maxSize)
    {
        super(maxSize);
       // DebugLog.error("baoyihu", "UrlImageView cache :" + maxSize);
    }
    
    @Override
    protected int sizeOf(String key, Bitmap value)
    {
        return value.getWidth() * value.getHeight() * BYTE_COLOR_BITMAP;
    }
    
    private static int getByteoFBitmapConfig()
    {
        if (BITMAP_COLOR_CONFIG.equals(Config.RGB_565))
        {
            return 2;
        }
        else if (BITMAP_COLOR_CONFIG.equals(Config.ARGB_8888))
        {
            return 4;
        }
        else if (BITMAP_COLOR_CONFIG.equals(Config.ARGB_4444))
        {
            return 2;
        }
        else
        {
            return 1;
        }
    }
}
