package com.rankwave.connect.sdk.core;

import java.util.List;

import android.app.Activity;
import android.content.Intent;

import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.ConnectSession;

public class OAuthFacebook {

	private ConnectCallback<ConnectSession> connectCallback = null;

	private static OAuthFacebook instance = null;

	public static OAuthFacebook getInstance() {

		if (instance == null) {

			synchronized (OAuthFacebook.class) {
				instance = new OAuthFacebook();
			}
		}

		return instance;
	}

	public OAuthFacebook() {

	}

	public void connecnt(Activity activity, List<String> permissions,
			ConnectCallback<com.rankwave.connect.sdk.ConnectSession> callback) {
       
	}

	public void getToken(Activity currentActivity, int requestCode,
			int resultCode, Intent data) {


	}
	
	public void close() {
		

	}

}
