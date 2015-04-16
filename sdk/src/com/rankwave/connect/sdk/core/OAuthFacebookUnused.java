package com.rankwave.connect.sdk.core;

import java.util.List;

import android.app.Activity;
import android.content.Intent;

import com.rankwave.connect.sdk.ConnectCallback;

public class OAuthFacebookUnused {

	private static OAuthFacebookUnused instance = null;

	public static OAuthFacebookUnused getInstance() {

		if (instance == null) {

			synchronized (OAuthFacebookUnused.class) {
				instance = new OAuthFacebookUnused();
			}
		}

		return instance;
	}

	public OAuthFacebookUnused() {

	}


	public void connect(Activity activity, List<String> permissions,
			ConnectCallback<com.rankwave.connect.sdk.ConnectSession> callback) {
       
	}

	public void getToken(Activity currentActivity, int requestCode,
			int resultCode, Intent data) {


	}
	
	public void close() {
		

	}

}
