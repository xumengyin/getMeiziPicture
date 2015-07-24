package image;

import java.io.InputStream;

import android.content.Context;

public interface UrlDownloader
{
    
    public static interface UrlDownloaderCallback
    {
        
        public void onDownloadComplete(UrlDownloader downloader, InputStream in, String filename);
    }
    
    public static interface UrlLoadCallback
    {
        public void onLoadComplete();
    }
    
    public void download(Context context, String url, String filename, UrlDownloaderCallback callback,
        UrlLoadCallback completion);
    
    public boolean allowCache();
    
    public boolean canDownloadUrl(String url);
}