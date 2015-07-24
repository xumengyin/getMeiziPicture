package image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

import junit.framework.Assert;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;


class StreamLoader implements UrlDownloader.UrlDownloaderCallback
{
    private static final String TAG = "StreamLoader";
    
    private String fileName = null;
    
    private Drawable drawable;
    
    private Bitmap bitmap;
    
    private Rect rect = null;
    
    private String url = null;
    
    public Bitmap getBitmap()
    {
        return bitmap;
    }
    
    public Drawable getDrawable()
    {
        return drawable;
    }
    
    private Resources resource = null;
    
    StreamLoader(Resources resource, String url, String fileName, Rect rect)
    {
        this.fileName = fileName;
        this.rect = rect;
        this.url = url;
        this.resource = resource;
    }
    
    @Override
    public void onDownloadComplete(UrlDownloader downloader, InputStream in, String existingFilename)
    {
        FileOutputStream fout = null;
        try
        {
            try
            {
                Assert.assertTrue(in == null || existingFilename == null);
                if (in == null && existingFilename == null)
                {
                    return;
                }
                String targetFilename = fileName;
                if (in != null)
                {
                    fout = new FileOutputStream(fileName);
                    ImageCommon.copyStream(in, fout);
                    
                }
                else
                {
                    targetFilename = existingFilename;
                }
                
                if (url.toLowerCase(Locale.US).endsWith(".gif"))
                {
                   // drawable = new GifDrawable(targetFilename);
                }
                else
                {
                    bitmap = ImageCommon.loadDrawableFromStream(url, resource, targetFilename, rect);
                }
            }
            finally
            {
                // if we're not supposed to cache this thing, delete the temp file.
                if (fout != null)
                {
                    fout.close();
                }
                if (downloader != null && !downloader.allowCache())
                {
                    boolean deleteSucceed = new File(fileName).delete();
                    if (!deleteSucceed)
                    {
                       // DebugLog.error(TAG, "delete file in UrlImageViewHelper.onDownloadComplete faile :" + fileName);
                    }
                    fileName = null;
                }
            }
        }
        catch (final Exception ex)
        {
            // always delete busted files when we throw.
            if (fileName != null)
            {
                boolean deleteSucceed = new File(fileName).delete();
                if (!deleteSucceed)
                {
                    //DebugLog.error(TAG, "delete file in UrlImageViewHelper.onDownloadComplete 2 faile :" + fileName);
                }
            }
            
           // DebugLog.printException(TAG, ex);
            
        }
        
    }
}