package image;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface UrlImageViewReturnInterface
{
    
    void onReturn(ImageView imageView, Bitmap bitmap, boolean loadedFromCache);
}
