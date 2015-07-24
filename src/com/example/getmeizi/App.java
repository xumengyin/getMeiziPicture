package com.example.getmeizi;
import android.app.Application;


public class App extends Application{
	public static App instance;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		instance=this;
		
	}

}
