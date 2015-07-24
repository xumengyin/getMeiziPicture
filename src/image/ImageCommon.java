package image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.widget.ImageView;


public final class ImageCommon
{
    public static final String TAG = "ImageCommon";
    
    private static final boolean USE_BITMAP_SCALE = true;
    
    private ImageCommon()
    {
        
    }
    
    public static Bitmap loadDrawableFromStream(final String url, Resources resource, final String filename,
        Rect maxRect)
    {
        FileInputStream stream = null;
        
       // DebugLog.info(TAG, "Decoding: " + filename);
        int decodeWidth = maxRect.width();
        int decodeHeight = maxRect.height();
        try
        {
            try
            {
                Options options = null;
                if (USE_BITMAP_SCALE)
                {
                    int scale = 0;
                    //we could mark all this block to avoid shrink Bitmap
                    
                    options = new Options();
                    options.inJustDecodeBounds = true;
                    stream = new FileInputStream(filename);
                    BitmapFactory.decodeStream(stream, null, options);
                    if (stream != null)
                    {
                        stream.close();
                    }
                    
                    while ((options.outWidth >> scale) > decodeWidth || (options.outHeight >> scale) > decodeHeight)
                    {
                        scale++;
                    }
                    
                    options = new Options();
                    options.inPreferredConfig = LruBitmapCache.BITMAP_COLOR_CONFIG;//picture color default is rgb_565; max for Config.ARGB_8888   min for //Config.ALPHA_8;
                    options.inPurgeable = true; // when the vm is need for memory ,allow system to release the bitmap memory ,and realloc when need
                    options.inInputShareable = true;// share the copy from inut data such ass inputstream ,byte array
                    options.inSampleSize = 1 << scale;
                    
                }
                else
                {
                    options = new Options();
                    options.inPreferredConfig = LruBitmapCache.BITMAP_COLOR_CONFIG;//picture color default is rgb_565; max for Config.ARGB_8888   min for //Config.ALPHA_8;
                    options.inPurgeable = true; // when the vm is need for memory ,allow system to release the bitmap memory ,and realloc when need
                    options.inInputShareable = true;// share the copy from inut data such ass inputstream ,byte array
                }
                stream = new FileInputStream(filename);
                
                return BitmapFactory.decodeStream(stream, null, options);
            }
            finally
            {
                if (stream != null)
                {
                    stream.close();
                }
            }
        }
        catch (IOException e)
        {
           e.printStackTrace();
            return null;
        }
    }
    
    public static int copyStream(final InputStream input, final OutputStream output)
        throws IOException
    {
        final byte[] stuff = new byte[1024 * 200];
        int read = 0;
        int total = 0;
        while ((read = input.read(stuff)) != -1)
        {
            output.write(stuff, 0, read);
            total += read;
        }
        return total;
    }
    
    private static boolean mHasCleaned = false;
    
    public static void cleanup(final Context context, long age)
    {
        if (mHasCleaned)
        {
            return;
        }
        mHasCleaned = true;
        try
        {
            // purge any *.urlimage files over a week old
            final String[] files = context.getFilesDir().list();
            if (files == null)
            {
                return;
            }
            for (final String file : files)
            {
                if (!file.endsWith(".urlimage"))
                {
                    continue;
                }
                
                String fileName = context.getFilesDir().getAbsolutePath() + '/' + file;
                final File f = new File(fileName);
                if (System.currentTimeMillis() > f.lastModified() + age)
                {
                    boolean deleteSucceed = f.delete();
                    if (!deleteSucceed)
                    {
                       // DebugLog.error(TAG, "delete file in UrlImageViewHelper.cleanup faile :" + fileName);
                    }
                }
            }
        }
        catch (final Exception e)
        {
           e.printStackTrace();
        }
    }
    
    public static Rect getMaxRectOfImages(final List<ImageView> downloads)
    {
        
        Rect rect = new Rect();
        if (downloads != null)
        {
            int maxHeight = 0;
            int maxWidth = 0;
            for (ImageView view : downloads)
            {
                if (view.getHeight() > maxHeight)
                {
                    maxHeight = view.getHeight();
                }
                if (view.getWidth() > maxWidth)
                {
                    maxWidth = view.getWidth();
                }
            }
            rect.set(0, 0, maxWidth, maxHeight);
        }
        return rect;
    }
}
