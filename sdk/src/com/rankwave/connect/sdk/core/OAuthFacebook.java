package com.rankwave.connect.sdk.core;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.Session;
import com.facebook.SessionState;
import com.rankwave.connect.sdk.Connect;
import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.ConnectManager;
import com.rankwave.connect.sdk.ConnectSession;
import com.rankwave.connect.sdk.SnsType;

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

	public static com.facebook.Session.StatusCallback fb_session_callback = new com.facebook.Session.StatusCallback() {

		@Override
		public void call(com.facebook.Session session,
				com.facebook.SessionState state, Exception exception) {

			if (state == SessionState.OPENED
					|| state == SessionState.OPENED_TOKEN_UPDATED) {

				//Log.i(Connect.TAG, session.getAccessToken());

				final String access_token = session.getAccessToken();
				
				Connect.getActiveConnectSession().getUser().getSnsInfo().clearInfo();
				Connect.getActiveConnectSession().getUser().getSnsInfo().setSnsType(SnsType.SNS_TYPE_FACEBOOK);
				Connect.getActiveConnectSession().getUser().getSnsInfo().setAccessToken(access_token);
				ConnectManager.setFacebookToken(access_token, getInstance().connectCallback);
								
			} else if (state == SessionState.CLOSED_LOGIN_FAILED) {
				
				if (getInstance().connectCallback != null) {
					getInstance().connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL,
							new Exception("failed to login to facebook"));
				}
			} 
		}
	};

	public void connect(Activity activity, List<String> permissions,
			ConnectCallback<ConnectSession> callback) {
        
		getInstance().connectCallback = callback;

		if (permissions == null) {
			com.facebook.Session.openActiveSession(activity, true,
					fb_session_callback);
		} else {

			com.facebook.Session session = com.facebook.Session
					.getActiveSession();
			if (session == null) {
				session = new com.facebook.Session(activity);
				com.facebook.Session.setActiveSession(session);

				session.openForRead(new com.facebook.Session.OpenRequest(
						activity).setCallback(fb_session_callback)
						.setPermissions(permissions));
			} else {

				if (session.isOpened() == false && session.isClosed() == false) {
					session.openForRead(new com.facebook.Session.OpenRequest(
							activity).setCallback(fb_session_callback)
							.setPermissions(permissions));
				} else {
					com.facebook.Session.openActiveSession(activity, true,
							fb_session_callback);
				}
			}
		}
	}

	public void getToken(Activity currentActivity, int requestCode,
			int resultCode, Intent data) {

		if (com.facebook.Session.getActiveSession() != null)
			com.facebook.Session.getActiveSession().onActivityResult(
					currentActivity, requestCode, resultCode, data);
	}
	
	public void close() {
		
		Session session = Session.getActiveSession();
		
        if (session != null && !session.isClosed()) 
        {
            session.closeAndClearTokenInformation();
        }
	}

	
}
