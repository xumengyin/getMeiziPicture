package com.example.getmeizi;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	String Tag="main";
	private RequestQueue queue;
	static final String URL="http://gank.io/2015/07/17";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//queue=Volley.newRequestQueue(this);
		//setRequest();
	}
	@Override
	protected void onResume() {
		super.onResume();
		startService(new Intent(this, getDataService.class));
	}
	private void setRequest()
	{
		StringRequest request =new StringRequest(URL,new Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.e(Tag, response);
				Pic pic=ParserHtml.parser(response);
				
				//成功的回调
				//sendBroadcast(new Intent());				
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				//失败的回调
				Log.e(Tag, error.toString());
			}
		});
		queue.add(request);
	}
}
