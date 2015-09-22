package com.rankwave.connect.sdk;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.rankwave.connect.sdk.ConnectCallback.FuncResult;
import com.rankwave.connect.sdk.core.OAuthFacebook;
import com.rankwave.connect.sdk.core.OAuthTwitter;


public final class Connect {
	public static final String TAG = "Connect";
	
	public static final String SDK_PREFERENCES = "com.rankave.connect.sdk";
		
	public static final String PROPERTY_CONNECT_ID = "com.rankwave.connect.sdk.ConnectId";
	public static final String PROPERTY_CONNECT_SECRET = "com.rankwave.connect.sdk.ConnectSecret";
	public static final String PROPERTY_FACEBOOK_APPID = "com.facebook.sdk.ApplicationId";
	public static final String PROPERTY_TWITTER_CONSUMER_KEY = "com.rankwave.connect.sdk.TwitterConsumerKey";
	public static final String PROPERTY_TWITTER_CONSUMER_SECRET = "com.rankwave.connect.sdk.TwitterConsumerSecret";
	public static final String PROPERTY_SENDER_ID = "com.rankwave.connect.sdk.SenderId";
	
	public static final String SDK_VERISON = "2.1.0";
	public static final String INTENT_PUSH_PAYLOAD = "com.rankwave.connect.sdk.pushpayload";
	public static final String INTENT_PUSH_SEQ = "com.rankwave.connect.sdk.pushseq";
	public static final String INTENT_PUSH_CMN = "com.rankwave.connect.sdk.pushcmn";
	public static final String INTENT_PUSH_OPEN_URL = "com.rankwave.connect.sdk.pushopenurl";
	
	
	public static final String ACTION_PUSH_CLICK = "com.rankwave.connect.sdk.PUSH_CLICK";
	public static final String ACTION_SEND_PAYLOAD = "com.rankwave.connect.sdk.SEND_PAYLOAD";
	
	public static final String CONNECT_DOMAIN = "api.rankwave.com";
	//public static final String CONNECT_DOMAIN = "54.176.29.228";	//dev
	
	private static Context context;
	private static String connect_id;
	private static String facebook_appid;
	private static String twitter_consumer_key;
	private static String twitter_consumer_secret;
	private static String sender_id;
	private static Boolean auto_join_flag;
	private static Boolean session_save_flag;
	
	
	/**
	 * Constructor
	 */
	private Connect(){
	}
	
	/**
	 * Connect SDK Initialize
	 * 
	 * @param context
	 * @param connectCallback
	 */
	public static void sdkInitialize(Context context, ConnectCallback<ConnectSession> connectCallback) {
		if(context == null) {
			Log.e(Connect.TAG, "context can not be null.");
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("context can not be null."));
			}
			
			return;
		}
		
		Connect.context = context;
		
		loadFromMetaData();		//metaData Load
		
		Log.i(Connect.TAG, "sdkInitialize Load MetaData : " +
				"connect_id[" + connect_id + "]facebook_appid[" + facebook_appid + "]twitter_consumer_key[" + twitter_consumer_key + 
				"]twitter_consumer_secret[" + twitter_consumer_secret + "]sender_id[" + sender_id + "]");
		
		if(connect_id == null || connect_id.length() == 0) {
			Log.e(Connect.TAG, "connect_id can not be empty.");
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("connect_id can not be empty."));
			}
			return;
		}
		
		if(sender_id != null && !sender_id.equals("")){
			GCMManager.getInstance().init(context);
		}
		
		//DeviceInfo Load
		DeviceInfo.getInstance().init(context);
		
		ConnectManager.sdkInitialize(connectCallback);	
		
		//기본 자동로그인 추가..
		autoLogin(null);
		
	}
	
	
	static void loadFromMetaData() {		  
        if (context == null) {
            return;
        }

        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        if (ai == null || ai.metaData == null) {
            return;
        }

        if (connect_id == null) {
        	connect_id = ai.metaData.getString(Connect.PROPERTY_CONNECT_ID);
        }
        if (facebook_appid == null) {
        	facebook_appid = ai.metaData.getString(Connect.PROPERTY_FACEBOOK_APPID);
        }
        if (twitter_consumer_key == null) {
        	twitter_consumer_key = ai.metaData.getString(Connect.PROPERTY_TWITTER_CONSUMER_KEY);
        }
        if (twitter_consumer_secret == null) {
        	twitter_consumer_secret = ai.metaData.getString(Connect.PROPERTY_TWITTER_CONSUMER_SECRET);
        }
        if (sender_id == null) {
        	sender_id = ai.metaData.getString(Connect.PROPERTY_SENDER_ID);
        }
    }
	
	
	/**
	 * autoLogin
	 * 
	 * @param connectCallback 
	 */
	public static void autoLogin(ConnectCallback<ConnectSession> connectCallback) {
		ConnectSession connectSession = getConnectSession();
		
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		//session clear
		connectSession.connectSessionClear();
		
		ConnectManager.autoLogin(connectCallback);
	}
	
	/**
	 * facebookLogin
	 * 
	 * @param activity
	 * @param permissions
	 * @param sessionSaveFlag
	 * @param autoJoinFlag
	 * @param connectCallback
	 */
	public static void facebookLogin(Activity activity, List<String> permissions, ConnectCallback<ConnectSession> connectCallback) {
		facebookLogin(activity, permissions, true, true, connectCallback);
	}
	
	/**
	 * facebookLogin
	 * 
	 * @param activity
	 * @param permissions
	 * @param connectCallback
	 */
	public static void facebookLogin(Activity activity, List<String> permissions, Boolean sessionSaveFlag, Boolean autoJoinFlag, ConnectCallback<ConnectSession> connectCallback) {
		session_save_flag = sessionSaveFlag;
		auto_join_flag = autoJoinFlag;
		
		ConnectSession connectSession = getConnectSession();
						
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(getFacebook_appid() == null || getFacebook_appid().equals("")){
			Log.e(Connect.TAG, "facebook id can not be empty.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("facebook id can not be empty."));
			}
			return;
		}
		
		//session clear
		connectSession.connectSessionClear();
		
		if(permissions == null){
			permissions = Arrays.asList(
					"public_profile", "email", "user_friends");
		}
		OAuthFacebook.getInstance().connect(activity, permissions, connectCallback);
	}
	
	
	/**
	 * facebookLogin
	 *  
	 * @param faceook_access_token 
	 * @param connectCallback 
	 */
	public static void facebookLogin(String faceook_access_token, ConnectCallback<ConnectSession> connectCallback) {
		facebookLogin(faceook_access_token, true, true, connectCallback);
	}
	
	/**
	 * facebookLogin
	 *  
	 * @param faceook_access_token 
	 * @param sessionSaveFlag
	 * @param autoJoinFlag
	 * @param connectCallback 
	 */
	public static void facebookLogin(String faceook_access_token, Boolean sessionSaveFlag, Boolean autoJoinFlag, ConnectCallback<ConnectSession> connectCallback) {
		session_save_flag = sessionSaveFlag;
		auto_join_flag = autoJoinFlag;
		
		ConnectSession connectSession = getConnectSession();
		
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(faceook_access_token == null || faceook_access_token.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("faceook_access_token can not be empty."));
			}
			return;
		}
		//session clear
		connectSession.connectSessionClear();
		
		ConnectManager.setFacebookToken(faceook_access_token, connectCallback);
	}
	
		
	/**
	 * twitterLogin
	 * 
	 * @param activity
	 * @param connectCallback
	 */
	public static void twitterLogin(Activity activity, ConnectCallback<ConnectSession> connectCallback) {
		twitterLogin(activity, true, true, connectCallback);
	}
	
	/**
	 * twitterLogin
	 * 
	 * @param activity
	 * @param sessionSaveFlag
	 * @param autoJoinFlag
	 * @param connectCallback
	 */
	public static void twitterLogin(Activity activity, Boolean sessionSaveFlag, Boolean autoJoinFlag, ConnectCallback<ConnectSession> connectCallback) {
		session_save_flag = sessionSaveFlag;
		auto_join_flag = autoJoinFlag;
		
		ConnectSession connectSession = getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(getTwitter_consumer_key() == null || getTwitter_consumer_key().equals("")){
			Log.e(Connect.TAG, "Twitter Consumer Key is null");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Twitter Consumer Key can not be empty."));
			}
			return;
		}
		if(getTwitter_consumer_secret() == null || getTwitter_consumer_secret().equals("")){
			Log.e(Connect.TAG, "Twitter Consumer Secret is null");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Twitter Consumer Secret can not be empty."));
			}
			return;
		}
		
		//session clear
		connectSession.connectSessionClear();
				
		OAuthTwitter.getInstance().connect(activity, connectCallback);
	}
	
	
	/**
	 * twitterLogin
	 *  
	 * @param twitter_access_token
	 * @param twitter_token_secret
	 * @param connectCallback 
	 */
	public static void twitterLogin(String twitter_access_token, String twitter_token_secret, ConnectCallback<ConnectSession> connectCallback) {
		twitterLogin(twitter_access_token, twitter_token_secret, true, true, connectCallback);
	}
	
	/**
	 * twitterLogin
	 *  
	 * @param twitter_access_token
	 * @param twitter_token_secret
	 * @param sessionSaveFlag
	 * @param autoJoinFlag
	 * @param connectCallback 
	 */
	public static void twitterLogin(String twitter_access_token, String twitter_token_secret, Boolean sessionSaveFlag, Boolean autoJoinFlag, ConnectCallback<ConnectSession> connectCallback) {
		session_save_flag = sessionSaveFlag;
		auto_join_flag = autoJoinFlag;
		
		ConnectSession connectSession = getConnectSession();
		
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(twitter_access_token == null || twitter_access_token.equals("")){
			Log.e(Connect.TAG, "Twitter Consumer Key is null");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("twitter_access_token can not be empty."));
			}
			return;
		}
		if(twitter_token_secret == null || twitter_token_secret.equals("")){
			Log.e(Connect.TAG, "Twitter Consumer Secret is null");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("twitter_token_secret can not be empty."));
			}
			return;
		}
		//session clear
		connectSession.connectSessionClear();
		
		ConnectManager.setTwitterToken(twitter_access_token, twitter_token_secret, connectCallback);
	}
	
	
	/**
	 * emailLogin
	 *  
	 * @param email 
	 * @param connectCallback 
	 */
	public static void emailLogin(String email, ConnectCallback<ConnectSession> connectCallback) {
		emailLogin(email, true, true, connectCallback);
	}
	
	/**
	 * emailLogin
	 *  
	 * @param email 
	 * @param sessionSaveFlag
	 * @param autoJoinFlag
	 * @param connectCallback 
	 */
	public static void emailLogin(String email, Boolean sessionSaveFlag, Boolean autoJoinFlag, ConnectCallback<ConnectSession> connectCallback) {
		session_save_flag = sessionSaveFlag;
		auto_join_flag = autoJoinFlag;
		
		ConnectSession connectSession = getConnectSession();
		
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(email == null || email.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("email can not be empty."));
			}
			return;
		}
		//session clear
		connectSession.connectSessionClear();
		
		ConnectManager.emailLogin(email, connectCallback);
	}
	
	/**
	 * anonymousLogin
	 * 
	 * @param sessionSaveFlag
	 * @param connectCallback
	 */
	public static void anonymousLogin(Boolean sessionSaveFlag, ConnectCallback<ConnectSession> connectCallback) {
		session_save_flag = sessionSaveFlag;
		
		ConnectSession connectSession = getConnectSession();
		
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		//session clear
		connectSession.connectSessionClear();
		
		ConnectManager.anonymousLogin(connectCallback);
	}
	
	
	/**
	 * join
	 * 
	 * @param profile
	 * @param connectCallback
	 */
	public static void join(Profile profile, ConnectCallback<ConnectSession> connectCallback) {
		ConnectSession connectSession = getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(connectSession.getConnect_token() == null){
			Log.e(Connect.TAG, "connectSession.getConnect_token() is null");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("connectSession.getConnect_token() is null"));
			}
			return;
		}
		
		ConnectManager.join(profile, connectCallback);
	}

	
	/**
	 * logout
	 * 
	 * @param connectCallback
	 */
	public static void logout(ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
			return;
		}
		
		ConnectManager.logout(connectCallback);
	}
	
	
	/**
	 * leave
	 * 
	 * @param connectCallback
	 */
	public static void leave(ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
			return;
		}
		
		ConnectManager.leave(connectCallback);
	}
	
	
	/**
	 * profileUpdate
	 * 
	 * @param profile
	 * @param connectCallback
	 */
	public static void profileUpdate(Profile profile, ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
			return;
		}
		
		ConnectManager.profileUpdate(profile, connectCallback);
	}
	
	
	/**
	 * pushOn
	 * 
	 * @param connectCallback
	 */
	public static void pushOn(ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
			return;
		}
		
		if(getSender_id() == null || getSender_id().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("sender id can not be empty."));
			return;
		}
		
		ConnectManager.pushOn(connectCallback);
	}
	
	
	/**
	 * pushOff
	 * 
	 * @param connectCallback
	 */
	public static void pushOff(ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
			return;
		}
		
		ConnectManager.pushOff(connectCallback);
	}
	
	
	//comming soon
	public static void action(){
		
	}
	
	public static void getConnectSession(ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
			
		connectCallback.onSuccess(connectSession);
	}
	
	
	/**
	 * getVersion
	 * 
	 * @return SDK Version
	 */
	public static String getVersion() {
		
		return Connect.SDK_VERISON;
	}	
	
	
	
	public static String getConnectId() {
		return connect_id;
	}
		
	
	public static ConnectSession getActiveConnectSession(){
		return ConnectSession.getInstance();
	}
	
	public static ConnectSession getConnectSession(){
		return ConnectSession.getConnectSession();
	}
	
	
	
	/**
	 * Facebook/Twitter Social login을 지원하기 위한 onActivityResult 함수
	 * 로그인을 호출하는 activity에서  onActivityResult를 오버라이딩하여 이를 호출해주어야 한다.
	 * 
	 * @Method Name   : onActivityResult
	 * @param currentActivity
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public static void onActivityResult(Activity currentActivity,
			int requestCode, int resultCode, Intent data) {

		if (requestCode == OAuthTwitter.OAUTH_TWITTER_REQUEST_CODE) {
			OAuthTwitter.getInstance().getToken(currentActivity, requestCode, resultCode, data);
		} else {
			OAuthFacebook.getInstance().getToken(currentActivity, requestCode, resultCode, data);
		}
	}

	
	public static Context getContext() {
		return context;
	}
	public static String getConnect_id() {
		return connect_id;
	}

	public static String getFacebook_appid() {
		return facebook_appid;
	}

	public static String getTwitter_consumer_key() {
		return twitter_consumer_key;
	}

	public static String getTwitter_consumer_secret() {
		return twitter_consumer_secret;
	}

	public static String getSender_id() {
		return sender_id;
	}
	
	public static Boolean getAuto_join_flag() {
		return auto_join_flag;
	}
	
	public static Boolean getSession_save_flag() {
		return session_save_flag;
	}
	
}
