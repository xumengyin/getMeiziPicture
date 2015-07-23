package com.example.getmeizi;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class getDataService extends IntentService{
	public static int pageSize=10;
	public final static String Tag="getDataService";
	static final String URL="http://gank.io";
	private RequestQueue queue;
	private ObjectStore<Pic>store =new ObjectStore<Pic>(this);
	List<Pic>data =new ArrayList<Pic>();
	public getDataService() {
		super("getDataService");
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int picNum=0;
		//picNum =getPic("/",pageSize);
		queue=Volley.newRequestQueue(getApplicationContext());
		sendRequest("", 10);
	}
	private int getPic(String path,int count)
	{
		int num=0;
		
		
		return num;
	}

	private void sendRequest(final String path,int count)
	{
		StringRequest request =new StringRequest(URL+path,new Listener<String>() {

			@Override
			public void onResponse(String response) {
				//Log.e(Tag, response);
				Pic pic=ParserHtml.parser(response);
				data.add(pic);
				Log.d("xumengyin", "pic url:"+pic.getUrl()+"path:"+URL+""+path);
				if(pic.getPreUrl()!=null)
				{
					store.insertOneObject("pic", pic);
					sendRequest(pic.getPreUrl(), 5);
				}
				else
				{
					showPic();
				}
				//成功的回调
				//sendBroadcast(new Intent());				
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				//失败的回调
				Log.e(Tag, error.toString());
				showPic();
			}
		});
		queue.add(request);
	}
	private void showPic()
	{
		for(Pic pic: data)
		{
			Log.d(Tag, "pic:"+pic.getUrl());
		}
	}
}
