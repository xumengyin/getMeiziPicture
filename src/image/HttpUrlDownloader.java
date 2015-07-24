package image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.NameValuePair;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;


public class HttpUrlDownloader implements UrlDownloader
{
    private static final String TAG = "HttpUrlDownloader";
    
    public static interface RequestPropertiesCallback
    {
        public List<NameValuePair> getHeadersForRequest(Context context, String url);
    }
    
    private RequestPropertiesCallback mRequestPropertiesCallback;
    
    public RequestPropertiesCallback getRequestPropertiesCallback()
    {
        return mRequestPropertiesCallback;
    }
    
    public void setRequestPropertiesCallback(final RequestPropertiesCallback callback)
    {
        mRequestPropertiesCallback = callback;
    }
    
    @Override
    public void download(final Context context, final String url, final String filename,
        final UrlDownloaderCallback callback, final UrlLoadCallback completion)
    {
        final AsyncTask<Void, Void, Void> downloader = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(final Void... params)
            {
                InputStream is = null;
                try
                {
                    String thisUrl = url;
                    /********************/
                   Uri uri= Uri.parse(thisUrl);
                    final String scheme = uri.getScheme();
                    if ( scheme == null || ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            			//bitmap = decodeFile(new File(url));
                    	File file=new File(url);
                    	is =new FileInputStream(file);
                    	callback.onDownloadComplete(HttpUrlDownloader.this, is, null);
                    	return null;
            		} else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            			BitmapFactory.Options options = new BitmapFactory.Options();  
            	        options.inDither = false;
            	        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            	       is = getCustomThumbnail(uri,200,context);
            	       callback.onDownloadComplete(HttpUrlDownloader.this, is, null);
//            	        if (isUseMediaStoreThumbnails) {
//            	        	bitmap = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), ContentUris.parseId(uri), Images.Thumbnails.MICRO_KIND,options);
//            	        } else {
//            	        	bitmap = getCustomThumbnail(uri,requiredSize);
//            	        }
            	    	return null;
            		}else {
                    /********************/
                    HttpURLConnection urlConnection;
                    int responseCode = 0;
                    while (true)
                    {
                        final URL u = new URL(thisUrl);
                        urlConnection = (HttpURLConnection)u.openConnection();
                        urlConnection.setInstanceFollowRedirects(true);
                        urlConnection.setUseCaches(true);
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setReadTimeout(5000);
                        
                        if (mRequestPropertiesCallback != null)
                        {
                            final List<NameValuePair> props =
                                mRequestPropertiesCallback.getHeadersForRequest(context, url);
                            if (props != null)
                            {
                                for (final NameValuePair pair : props)
                                {
                                    urlConnection.addRequestProperty(pair.getName(), pair.getValue());
                                }
                            }
                        }
                        responseCode = urlConnection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP
                            && responseCode != HttpURLConnection.HTTP_MOVED_PERM)
                        {
                            break;
                        }
                        thisUrl = urlConnection.getHeaderField("Location");
                    }
                    
                    if (responseCode != HttpURLConnection.HTTP_OK)
                    {
                       // DebugLog.error(TAG, "Response Code: " + responseCode + "  at url:" + url);
                        return null;
                    }
                    is = urlConnection.getInputStream();
                    callback.onDownloadComplete(HttpUrlDownloader.this, is, null);
                    
                    return null;
                }
                }
                catch (final Exception e)
                {
                 //   DebugLog.printException(TAG, e);
                    return null;
                }
                finally
                {
                    if (is != null)
                    {
                        try
                        {
                            is.close();
                        }
                        catch (IOException e)
                        {
                           // DebugLog.printException(TAG, e);
                        }
                    }
                    
                }
            }
            
            @Override
            protected void onPostExecute(final Void result)
            {
                if (completion != null)
                {
                    completion.onLoadComplete();
                }
            }
        };
        
        try
        {
            UrlImageViewHelper.executeTask(downloader);
        }
        catch (RejectedExecutionException e)
        {
            //DebugLog.printException(e);
        	e.printStackTrace();
            completion.onLoadComplete();
        }
        
    }
    
    @Override
    public boolean allowCache()
    {
        return true;
    }
    
    @Override
    public boolean canDownloadUrl(String url)
    {
        return url.startsWith("http");
    }
    //直接从图库中获得数据流
    private  InputStream getCustomThumbnail(Uri uri,int size,Context mContext){
        try {
	        InputStream input = mContext.getContentResolver().openInputStream(uri);
	        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
	        onlyBoundsOptions.inJustDecodeBounds = true;
	        onlyBoundsOptions.inDither=true;//optional
	        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
	        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
	        input.close();
	        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
	            return null;
	        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;
	        double ratio = (originalSize > size) ? (originalSize / size) : 1.0;
	        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
	        bitmapOptions.inDither=true;//optional
	        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
	        input = mContext.getContentResolver().openInputStream(uri);
	       // Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
	       // input.close();
	        return input;
        }catch (FileNotFoundException e) {
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }
}
