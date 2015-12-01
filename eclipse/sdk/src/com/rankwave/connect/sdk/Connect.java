package com.rankwave.connect.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.rankwave.connect.sdk.ConnectCallback.FuncResult;


public final class Connect {
	public static final String TAG = "Connect";
	
	public static final String SDK_PREFERENCES = "com.rankave.connect.sdk";
		
	public static final String PROPERTY_CONNECT_ID = "com.rankwave.connect.sdk.ConnectId";
	public static final String PROPERTY_CONNECT_SECRET = "com.rankwave.connect.sdk.ConnectSecret";
	public static final String PROPERTY_SENDER_ID = "com.rankwave.connect.sdk.SenderId";
	
	public static final String PROPERTY_NOTIFICATION_LARGE_ICON = "com.rankwave.notification.large_icon";
	public static final String PROPERTY_NOTIFICATION_SMALL_ICON = "com.rankwave.notification.small_icon";
	
	public static final String SDK_VERISON = "2.1.1";
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
	private static String sender_id;
	
	
	/**
	 * Constructor
	 */
	private Connect(){
	}
	
	/**
	 * Connect SDK Initialize
	 * 
	 * @param context : Context
	 * @param connectCallback : ConnectCallback
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
				"connect_id[" + connect_id + "]sender_id[" + sender_id + "]");
		
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
		
		//ConnectSession instance
		ConnectSession.getInstance();
		
		//DeviceInfo Load
		DeviceInfo.getInstance().init(context);
				
		ConnectManager.sdkInitialize(connectCallback);
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
        if (sender_id == null) {
        	sender_id = ai.metaData.getString(Connect.PROPERTY_SENDER_ID);
        }
    }
	
	
	/**
	 * facebookLogin
	 * @param sns_id : sns_id
	 * @param faceook_access_token :  faceook_access_token
	 * @param connectCallback : ConnectCallback
	 */
	public static void facebookLogin(String sns_id, String faceook_access_token, ConnectCallback<ConnectSession> connectCallback) {
		ConnectSession connectSession = getConnectSession();
		
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(sns_id == null || sns_id.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("facebook id can not be empty."));
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
		
		ConnectManager.facebookLogin(sns_id, faceook_access_token, connectCallback);
	}
	

	
	/**
	 * twitterLogin
	 * @param : sns_id : sns_id
	 * @param twitter_access_token : twitter_access_token
	 * @param twitter_token_secret : twitter_token_secret
	 * @param connectCallback : ConnectCallback
	 */
	public static void twitterLogin(String sns_id, String twitter_access_token, String twitter_token_secret, ConnectCallback<ConnectSession> connectCallback) {
		ConnectSession connectSession = getConnectSession();
		
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(sns_id == null || sns_id.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("twitter id can not be empty."));
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
		
		ConnectManager.twitterLogin(sns_id, twitter_access_token, twitter_token_secret, connectCallback);
	}
	
	
	/**
	 * kakaoLogin
	 * @param sns_id : sns_id
	 * @param kakao_access_token :  kakao_access_token
	 * @param kakao_refresh_token :  kakao_refresh_token
	 * @param connectCallback : ConnectCallback
	 */
	public static void kakaoLogin(String sns_id, String kakao_access_token, String kakao_refresh_token, ConnectCallback<ConnectSession> connectCallback) {
		ConnectSession connectSession = getConnectSession();

		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");

			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}

		if(sns_id == null || sns_id.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("kakao id can not be empty."));
			}
			return;
		}

		if(kakao_access_token == null || kakao_access_token.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("kakao_access_token can not be empty."));
			}
			return;
		}
		//session clear
		connectSession.connectSessionClear();

		ConnectManager.kakaoLogin(sns_id, kakao_access_token, kakao_refresh_token, connectCallback);
	}
	
	/**
	 * logout
	 * 
	 * @param connectCallback : ConnectCallback
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
		
		if(connectSession.loadId().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		ConnectManager.userDisconnect(connectCallback);
	}
	
	
	/**
	 * leave
	 * 
	 * @param connectCallback : ConnectCallback
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
		
		if(connectSession.loadId().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		ConnectManager.userDelete(connectCallback);
	}
	
	
	/**
	 * pushOn
	 * 
	 * @param connectCallback : ConnectCallback
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
		
		if(connectSession.loadId().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
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
	 * @param connectCallback : ConnectCallback
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
		
		if(connectSession.loadId().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		ConnectManager.pushOff(connectCallback);
	}
	
	
	//comming soon
	public static void action(){
		
	}
	
	public static void pushWebLink(){
		final SharedPreferences prefs = Connect.getContext().getSharedPreferences(Connect.SDK_PREFERENCES, Context.MODE_PRIVATE);
		
		String open_url = prefs.getString(Connect.INTENT_PUSH_OPEN_URL, "");
		
		if(!open_url.equals("")){
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(Connect.INTENT_PUSH_OPEN_URL, "");
			editor.commit();
			
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(open_url));
			browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Connect.getContext().startActivity(browserIntent);
		}
		
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
	
	public static Context getContext() {
		return context;
	}
	public static String getConnect_id() {
		return connect_id;
	}

	public static String getSender_id() {
		return sender_id;
	}
	
	
}
