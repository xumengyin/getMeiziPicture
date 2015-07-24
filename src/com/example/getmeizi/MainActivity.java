package com.example.getmeizi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v4.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.huewu.pla.lib.MultiColumnListView;

public class MainActivity extends Activity {

	String Tag="main";
	private RequestQueue queue;
	private MultiColumnListView list;
	private SwipeRefreshLayout reFresh;
	private List<String>data=new ArrayList<String>();
	ListAdapter adapter;
	static final String URL="http://gank.io/2015/07/17";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		list=(MultiColumnListView) findViewById(R.id.list);
		reFresh=(SwipeRefreshLayout) findViewById(R.id.swipe_fresh);
		adapter=new ListAdapter(data, this);
		list.setAdapter(adapter);
		getPic();
		//queue=Volley.newRequestQueue(this);
		//setRequest();
	}
	private void getPic()
	{
		Cursor cur=getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		if(cur!=null)
		{
			if(cur.moveToFirst())
			{
				do{
				long id=cur.getLong(cur.getColumnIndex(Images.Media._ID));
				Uri uri=Uri.parse(Images.Media.EXTERNAL_CONTENT_URI+"/"+id);
				data.add(uri.toString());		
				}while(cur.moveToNext());
			}
		}
		cur.close();
		adapter.notifyDataSetChanged();
	}
	@Override
	protected void onResume() {
		super.onResume();
		//startService(new Intent(this, getDataService.class));
	}
//	private void setRequest()
//	{
//		StringRequest request =new StringRequest(URL,new Listener<String>() {
//
//			@Override
//			public void onResponse(String response) {
//				Log.e(Tag, response);
//				Pic pic=ParserHtml.parser(response);
//				
//				//成功的回调
//				//sendBroadcast(new Intent());				
//			}
//		}, new ErrorListener() {
//
//			@Override
//			public void onErrorResponse(VolleyError error) {
//				//失败的回调
//				Log.e(Tag, error.toString());
//			}
//		});
//		queue.add(request);
//	}
}
