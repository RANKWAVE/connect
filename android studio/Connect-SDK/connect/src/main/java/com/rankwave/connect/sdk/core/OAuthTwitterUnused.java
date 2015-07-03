package com.rankwave.connect.sdk.core;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.rankwave.connect.sdk.ConnectCallback;

public class OAuthTwitterUnused {

	public static final int OAUTH_TWITTER_REQUEST_CODE = 5001;
	public static final String PROPERTY_TWITTER_CONSUMER_KEY = "com.rankwave.connect.sdk.TwitterConsumerKey";
	public static final String PROPERTY_TWITTER_CONSUMER_SECRET = "com.rankwave.connect.sdk.TwitterConsumerSecret";
	public static final Uri TWIT_CALLBACK_URL = Uri.parse("http://api.rankwave.com/login/mobileTwitterCallback.do"); 
	
		
	private static OAuthTwitter instance = null;
	public static OAuthTwitter getInstance() {
		
		if(instance == null){
			
			synchronized (OAuthTwitter.class) {
				instance = new OAuthTwitter();				
			}
		}
		
		return instance;
	}
	
	public OAuthTwitterUnused() {

	}
	
	
	public void connect(Activity activity, ConnectCallback<com.rankwave.connect.sdk.ConnectSession> callback) {
					
	}
	
	public void getToken(Activity currentActivity, int requestCode, int resultCode, Intent data) {

		
	}
	
	
	public void close() {
		
	}

}
