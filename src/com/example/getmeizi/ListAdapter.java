package com.example.getmeizi;

import image.UrlImageViewHelper;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.imageload.ImageLoader;

public class ListAdapter extends BaseAdapter{
	List<String>data;
	Context ctx;
	ImageLoader loader;
	public ListAdapter(List<String>data,Context ctx) {
		this.data=data;
		this.ctx=ctx;
		loader=new ImageLoader(ctx);
		 WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
	    int width = wm.getDefaultDisplay().getWidth();//屏幕宽度
	    loader.setIsUseMediaStoreThumbnails(false);
	    loader.setRequiredSize(width/3);
	}
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder=null;
		if(convertView==null)
		{
			holder=new Holder();
			convertView=LayoutInflater.from(ctx).inflate(R.layout.image_item, null);
			holder.image=(ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		}
		else
		{
			holder=(Holder) convertView.getTag();
		}
		loader.DisplayImage(data.get(position), holder.image);
		//UrlImageViewHelper.setUrlDrawable(holder.image, data.get(position), R.drawable.ic_launcher);
		return convertView;
	}
	class Holder 
	{
		ImageView image;
	}
}
