package com.rankwave.connect.sdk;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

public class ConnectPollingThread extends Thread{
	
	public void run(){
		try{
			while(true){
				//Log.d(Connect.TAG, "ConnectPollingThread :: ");
				//push click action upload
				final SharedPreferences prefs = Connect.getContext().getSharedPreferences(Connect.SDK_PREFERENCES, Context.MODE_PRIVATE);
				String push_seq = prefs.getString(Connect.INTENT_PUSH_SEQ, "");
				
				if(!push_seq.equals("")){
					
					//Action 을 연동하기 위해서 http 연동을 하는데 Thread 를 또 사용해야 하므로 아래와 같이 Handler 을 이용하여야 한다.
					new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
						@Override
						public void run() {
							try{
								String push_seq = prefs.getString(Connect.INTENT_PUSH_SEQ, "");
								SharedPreferences.Editor editor = prefs.edit();
								editor.putString(Connect.INTENT_PUSH_SEQ, "");
								editor.commit();
								
								JSONObject etc = new JSONObject();
								etc.put("push_seq", push_seq);
								etc.put("os_type", DeviceInfo.getInstance().getOs_type());
								
								ConnectService.action(null, "PUSH", 1, "CONNECT SDK", etc);
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}, 0);
				}
				
				
				String open_url = prefs.getString(Connect.INTENT_PUSH_OPEN_URL, "");
				
				if(!open_url.equals("")){
					new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
						@Override
						public void run() {
							try{
								String open_url = prefs.getString(Connect.INTENT_PUSH_OPEN_URL, "");
								
								SharedPreferences.Editor editor = prefs.edit();
								editor.putString(Connect.INTENT_PUSH_OPEN_URL, "");
								editor.commit();
								
								String connectToken = "";
								if(ConnectSession.getConnectSession() != null){
									connectToken = ConnectSession.getConnectSession().getConnect_token();
								}
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(open_url + "?connect_token=" + connectToken));
								browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								Connect.getContext().startActivity(browserIntent);
								
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}, 1000);
				}

				SystemClock.sleep(3000);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
