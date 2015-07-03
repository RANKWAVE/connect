package com.rankwave.connect.sdk.core;

import android.app.Activity;
import android.content.Intent;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.rankwave.connect.sdk.Connect;
import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.ConnectManager;
import com.rankwave.connect.sdk.ConnectSession;
import com.rankwave.connect.sdk.SnsType;

import java.util.List;

public class OAuthFacebook {

	private ConnectCallback<ConnectSession> connectCallback = null;

	private static OAuthFacebook instance = null;

	private CallbackManager callbackManager;
	private LoginResult loginResult;



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



	public void connect(Activity activity, List<String> permissions,
						ConnectCallback<ConnectSession> callback) {

		getInstance().connectCallback = callback;

		FacebookSdk.sdkInitialize(activity);

		callbackManager = CallbackManager.Factory.create();
		LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				// App code
				final String access_token = loginResult.getAccessToken().getToken();

				Connect.getActiveConnectSession().getUser().getSnsInfo().clearInfo();
				Connect.getActiveConnectSession().getUser().getSnsInfo().setSnsType(SnsType.SNS_TYPE_FACEBOOK);
				Connect.getActiveConnectSession().getUser().getSnsInfo().setAccessToken(access_token);
				ConnectManager.setFacebookToken(access_token, getInstance().connectCallback);

			}

			@Override
			public void onCancel() {
				// App code

			}

			@Override
			public void onError(FacebookException exception) {
				// App code
				if(getInstance().connectCallback != null){
					getInstance().connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, new Exception(exception.getMessage()));
				}
			}
		});


		LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
	}

	public void getToken(Activity currentActivity, int requestCode,
						 int resultCode, Intent data) {

		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	public void close() {

	}


}
