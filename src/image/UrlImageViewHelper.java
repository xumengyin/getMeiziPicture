package image;

import image.UrlDownloader.UrlLoadCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import com.example.getmeizi.App;

import junit.framework.Assert;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;


public final class UrlImageViewHelper
{
    public static final int HONEYCOMB = 11;
    
    public static final boolean LOG_ENABLED = false; //set to True to enable verbose logging
    
    public static final int CACHE_DURATION_INFINITE = Integer.MAX_VALUE;
    
    public static final int CACHE_DURATION_ONE_DAY = 1000 * 60 * 60 * 24;
    
    public static final int CACHE_DURATION_THREE_DAYS = CACHE_DURATION_ONE_DAY * 3;
    
    public static final int CACHE_DURATION_ONE_WEEK = CACHE_DURATION_ONE_DAY * 7;
    
    private static final String TAG = "UrlImageViewHelper";
    
    private static int screenWidth;
    
    private static int screenHeight;
    
    private static List<UrlDownloader> mDownloaders = new ArrayList<UrlDownloader>();
    
    private static LruBitmapCache mDeadCache = LruBitmapCache.newInstance();
    
    private static Map<ImageView, String> mPendingViews = new Hashtable<ImageView, String>();
    
    public static final Map<String, List<ImageView>> mPendingDownloads = new Hashtable<String, List<ImageView>>();
    
    static
    {
        caculateScreen(App.instance);
        mDownloaders.add(new HttpUrlDownloader());
    }
    
    private static void caculateScreen(Context context)
    {
        final int tw;
        final int th;
        
        DisplayMetrics mMetrics = new DisplayMetrics();
        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mMetrics);
        tw = mMetrics.widthPixels;
        th = mMetrics.heightPixels;
        
        screenWidth = tw <= 0 ? Integer.MAX_VALUE : tw;
        screenHeight = th <= 0 ? Integer.MAX_VALUE : th;
    }
    
    public static void executeTask(final AsyncTask<Void, Void, Void> task)
    {
        if (Build.VERSION.SDK_INT < HONEYCOMB)
        {
            task.execute();
        }
        else
        {
            executeTaskHoneycomb(task);
        }
    }
    
    @TargetApi(HONEYCOMB)
    private static void executeTaskHoneycomb(final AsyncTask<Void, Void, Void> task)
    {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    /**
     * Download and shrink an Image located at a specified URL, and display it in the provided {@link
     * android.widget.ImageView}.
     *
     * @param imageView       The {@link android.widget.ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link android.graphics.drawable.Drawable} that
     *                        should be displayed while the image is being downloaded.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource)
    {
        Drawable defaultDrawble = getDefaultDrawable(imageView, defaultResource);
        if (isNullOrEmpty(url))
        {
            mPendingViews.remove(imageView);
            imageView.setImageDrawable(defaultDrawble);
            return;
        }
        UrlImageViewHelper helper = new UrlImageViewHelper(imageView, url, defaultDrawble);
        helper.doTheJob();
    }
    
    private static Drawable getDefaultDrawable(final ImageView imageView, final int defaultResource)
    {
        Drawable defaultDrawable = null;
        if (defaultResource > 0)
        {
            String id = "RES:" + defaultResource;
            Bitmap bitmap = mDeadCache.get(id);
            Resources resource = imageView.getResources();
            if (bitmap == null)
            {
                bitmap = BitmapFactory.decodeResource(resource, defaultResource);
                if (bitmap != null)
                {
                    mDeadCache.put(id, bitmap);
                }
            }
            if (bitmap != null)
            {
                defaultDrawable = new BitmapDrawable(resource, bitmap);
            }
        }
        return defaultDrawable;
    }
    
    /**
     * Clear out all cached images older than a week. The same as calling cleanup(context,
     * CACHE_DURATION_ONE_WEEK);
     */
    private static void cleanup(final Context context)
    {
        ImageCommon.cleanup(context, CACHE_DURATION_ONE_WEEK);
    }
    
    private static boolean checkCacheDuration(File file, long cacheDurationMs)
    {
        return cacheDurationMs == CACHE_DURATION_INFINITE
            || System.currentTimeMillis() < file.lastModified() + cacheDurationMs;
    }
    
    /**
     * Download and shrink an Image located at a specified URL, and display it in the provided {@link
     * android.widget.ImageView}.
     *
     * @param context         A {@link android.content.Context} to allow setUrlDrawable to load and
     *                        save files.
     * @param imageView       The {@link android.widget.ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link android.graphics.drawable.Drawable} that
     *                        should be displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this image should be cached
     *                        locally.
     * @param callback        An instance of {@link com.koushikdutta.urlimageviewhelper.UrlImageViewCallback}
     *                        that is called when the image successfully finishes loading. This value
     *                        can be null.
     */
    
    private static boolean isNullOrEmpty(final String s)
    {
        return TextUtils.isEmpty(s) || "null".equalsIgnoreCase(s);
    }
    
    public static String getFilenameForUrl(final String url)
    {
        if (null == url)
        {
            return "urlimage";
        }
        return url.hashCode() + ".urlimage";
    }
    
    private final Context context;
    
    private final ImageView imageView;
    
    private final String url;
    
    private final Drawable defaultDrawable;
    
    private long cacheDurationMs = CACHE_DURATION_THREE_DAYS;
    
    private UrlImageViewCallback callback;
    
    private UrlImageViewReturnInterface returnCall;
    
    public void setCacheDurationMs(long cacheDurationMs)
    {
        this.cacheDurationMs = cacheDurationMs;
    }
    
    public void setCallback(UrlImageViewCallback callback)
    {
        this.callback = callback;
    }
    
    public void setReturnCall(UrlImageViewReturnInterface returnCall)
    {
        this.returnCall = returnCall;
    }
    
    private UrlImageViewHelper(final ImageView imageView, final String url, final Drawable defaultDrawable)
    {
        this.context = imageView.getContext();
        this.imageView = imageView;
        this.url = url;
        this.defaultDrawable = defaultDrawable;
        
    }
    
    private void doTheJob()
    {
        if (imageView == null)
        {
            return;
        }
        Assert.assertTrue("setUrlDrawable and loadUrlDrawable should only be called from the main thread.",
            Looper.getMainLooper().getThread() == Thread.currentThread());
        cleanup(context);
        
        Bitmap bitmap = mDeadCache.get(url);
        if (bitmap != null)
        {
            if (returnCall != null)
            {
                returnCall.onReturn(imageView, bitmap, true);
            }
            else
            {
                imageView.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
                if (callback != null)
                {
                    callback.onLoaded(imageView, url, true);
                }
            }
            mPendingViews.remove(imageView);
            return;
        }
        else
        {
            
            mPendingViews.put(imageView, url);
            
            final List<ImageView> currentDownload = mPendingDownloads.get(url);
            if (currentDownload != null)
            {
                currentDownload.add(imageView);
                return;
            }
            
            final List<ImageView> downloads = new ArrayList<ImageView>();
            downloads.add(imageView);
            mPendingDownloads.put(url, downloads);
            
            final String filename = context.getFileStreamPath(getFilenameForUrl(url)).getAbsolutePath();
            Rect rect = findMostSurtableRect(screenWidth, screenHeight);
            StreamLoader loader = new StreamLoader(imageView.getResources(), url, filename, rect);
            LoadCompletionRunnable completion = new LoadCompletionRunnable(loader, downloads);
            boolean fileExist = loadBitmapFromFile(filename, loader, completion);
            if (fileExist)
            {
                return;
            }
            else
            {
                //download File from network
                imageView.setImageDrawable(defaultDrawable);
                for (UrlDownloader downloader : mDownloaders)
                {
                    if (downloader.canDownloadUrl(url))
                    {
                        downloader.download(context, url, filename, loader, completion);
                        return;
                    }
                }
            }
            
            //  imageView.setImageDrawable(defaultDrawable);
        }
    }
    
    private Rect findMostSurtableRect(final int targetWidth, final int targetHeight)
    {
        final List<ImageView> downloads = UrlImageViewHelper.mPendingDownloads.get(url);
        Rect maxRect = ImageCommon.getMaxRectOfImages(downloads);
        int decodeWidth = 0;
        int decodeHeight = 0;
        if (maxRect.bottom > 0 && maxRect.right > 0)
        {
            decodeWidth = targetWidth < maxRect.right ? targetWidth : maxRect.right;
            decodeHeight = targetHeight < maxRect.bottom ? targetHeight : maxRect.bottom;
        }
        else
        {
            decodeWidth = targetWidth;
            decodeHeight = targetHeight;
        }
        
        return new Rect(0, 0, decodeWidth, decodeHeight);
    }
    
    class LoadCompletionRunnable implements UrlLoadCallback
    {
        StreamLoader loader;
        
        List<ImageView> downloads;
        
        public LoadCompletionRunnable(StreamLoader loader, List<ImageView> downloads)
        {
            this.loader = loader;
            this.downloads = downloads;
        }
        
        @Override
        public void onLoadComplete()
        {
            //  Assert.assertEquals(Looper.myLooper(), Looper.getMainLooper());
            
            Drawable usableResult = loader.getDrawable();
            if (usableResult == null)
            {
                Bitmap bitmap = loader.getBitmap();
                if (bitmap != null)
                {
                    usableResult = new BitmapDrawable(context.getResources(), bitmap);
                    mDeadCache.put(url, bitmap);
                }
                if (usableResult == null)
                {
                   // DebugLog.verbose(TAG, "No usable result, defaulting " + url);
                    usableResult = defaultDrawable;
                }
            }
            
            mPendingDownloads.remove(url);
            
            int waitingCount = 0;
            for (final ImageView iv : downloads)
            {
                // validate the url it is waiting for
                final String pendingUrl = mPendingViews.get(iv);
                if (!url.equals(pendingUrl))
                {
//                    DebugLog.verbose(TAG, "Ignoring out of date request to update view for " + url + " " + pendingUrl
//                        + " " + iv);
                    continue;
                }
                waitingCount++;
                mPendingViews.remove(iv);
                if (returnCall != null)
                {
                    returnCall.onReturn(iv, loader.getBitmap(), false);
                }
                else
                {
                    if (usableResult != null)
                    {
                        iv.setImageDrawable(usableResult);
                    }
                    if (callback != null)
                    {
                        callback.onLoaded(iv, url, false);
                    }
                }
            }
           // DebugLog.verbose(TAG, "Populated: " + waitingCount);
        }
        
    }
    
    private boolean loadBitmapFromFile(final String filename, final StreamLoader loader,
        final UrlLoadCallback completion)
    {
        File file = new File(filename);
        if (file.exists())
        {
            if (checkCacheDuration(file, cacheDurationMs))
            {
                Log.d(TAG,
                    "File Cache hit on: " + url + ". " + (System.currentTimeMillis() - file.lastModified()) + "ms old.");
                
                final AsyncTask<Void, Void, Void> fileloader = new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(final Void... params)
                    {
                        loader.onDownloadComplete(null, null, filename);
                        return null;
                    }
                    
                    @Override
                    protected void onPostExecute(final Void result)
                    {
                        completion.onLoadComplete();
                    }
                };
                try
                {
                    executeTask(fileloader);
                }
                catch (RejectedExecutionException e)
                {
                   // DebugLog.printException(e);
                    completion.onLoadComplete();
                }
                
                return true;
            }
            else
            {
               // DebugLog.verbose(TAG, "File cache has expired. Refreshing.");
            }
        }
        return false;
    }
    
}
