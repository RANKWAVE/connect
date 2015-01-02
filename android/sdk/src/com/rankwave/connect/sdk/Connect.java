package com.rankwave.connect.sdk;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
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
	
	public static final String SDK_VERISON = "0.0.1";
	public static final String INTENT_PUSH_PAYLOAD = "com.rankwave.connect.sdk.pushpayload";
	public static final String INTENT_PUSH_SEQ = "com.rankwave.connect.sdk.pushseq";
	public static final String INTENT_PUSH_CMN = "com.rankwave.connect.sdk.pushcmn";
	
	public static final String CONNECT_DOMAIN = "api.rank-cloud.com";
	//public static final String CONNECT_DOMAIN = "54.176.29.228";	//dev
	
	
	private static Context context;
	private static String connect_id;
	private static String facebook_appid;
	private static String twitter_consumer_key;
	private static String twitter_consumer_secret;
	private static String sender_id;
		
	public static ConnectCallback<Session> user_connect_callback = null;
	
	
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
	public static void sdkInitialize(Context context, ConnectCallback<Session> connectCallback) {
		if(context == null) {
			Log.e(TAG, "context can not be null.");
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("context can not be null."));
			return;
		}
		
		Connect.context = context;
		Connect.loadFromMetaData(context);		//metaData Load
		
		Log.i(TAG, "sdkInitialize Load MetaData : " +
				"connect_id[" + connect_id + "]facebook_appid[" + facebook_appid + "]twitter_consumer_key[" + twitter_consumer_key + 
				"]twitter_consumer_secret[" + twitter_consumer_secret + "]sender_id[" + sender_id + "]");
		
		if(connect_id == null || connect_id.length() == 0) {
			Log.e(TAG, "connect_id can not be empty.");
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("connect_id can not be empty."));
			return;
		}
		
		if(sender_id != null && !sender_id.equals("")){
			GCMManager.getInstance().init(context);
		}
		
		DeviceInfo.getInstance().init(context);		//DeviceInfo Load
		
		ConnectService.initialize(connectCallback);
		
		//push click action upload
		final SharedPreferences prefs = context.getSharedPreferences(Connect.SDK_PREFERENCES, Context.MODE_PRIVATE);
		String push_seq = prefs.getString("push_seq", "");
		
		if(!push_seq.equals("")){
			try{
				JSONObject etc = new JSONObject();
				etc.put(Connect.INTENT_PUSH_SEQ, push_seq);
				etc.put("os_type", DeviceInfo.getInstance().getOs_type());
				
				ConnectService.action(null, "PUSH", 1, "APP", etc);
				
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Connect.INTENT_PUSH_SEQ, "");
				editor.commit();
				
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
	}
	
	static void loadFromMetaData(Context context) {		  
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
        	connect_id = ai.metaData.getString(PROPERTY_CONNECT_ID);
        }
        if (facebook_appid == null) {
        	facebook_appid = ai.metaData.getString(PROPERTY_FACEBOOK_APPID);
        }
        if (twitter_consumer_key == null) {
        	twitter_consumer_key = ai.metaData.getString(PROPERTY_TWITTER_CONSUMER_KEY);
        }
        if (twitter_consumer_secret == null) {
        	twitter_consumer_secret = ai.metaData.getString(PROPERTY_TWITTER_CONSUMER_SECRET);
        }
        if (sender_id == null) {
        	sender_id = ai.metaData.getString(PROPERTY_SENDER_ID);
        }
    }
	
	
	
	/**
	 * anonymousLogin
	 * 
	 * @param connectCallback
	 */
	public static void anonymousLogin(ConnectCallback<Session> connectCallback) {
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.READY && session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		user_connect_callback = connectCallback;
		
		ConnectService.token(IdType.ID_TYPE_ANONYMOUS, null, null, new ConnectCallback<Session>(){
			@Override
        	public void onSuccess(Session session){
				Boolean join = session.getUser().getJoined();
				if(join){
					ConnectService.login(new ConnectCallback<Session>(){
						@Override
						public void onSuccess(Session session){
							user_connect_callback.onSuccess(session);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							user_connect_callback.onFail(result, exception);
						}
					});
				}else{
					join(null, user_connect_callback);
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				user_connect_callback.onFail(result, exception);
			}
		});
	}
	
	/**
	 * facebookLogin
	 * 
	 * @param activity
	 * @param permissions
	 * @param connectCallback
	 */
	public static void facebookLogin(Activity activity, ConnectCallback<Session> connectCallback) {
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.READY && session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		if(getFacebook_appid() == null || getFacebook_appid().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("facebook id can not be empty."));
			return;
		}
		
		OAuthFacebook.getInstance().connecnt(activity, null, connectCallback);
	}
	
	
	/**
	 * setFacebookToken
	 *  
	 * @param faceook_access_token 
	 * @param connectCallback 
	 */
	public static void setFacebookToken(String faceook_access_token, ConnectCallback<com.rankwave.connect.sdk.Session> connectCallback) {
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.READY && session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		if(getFacebook_appid() == null || getFacebook_appid().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Facebook App Id is null"));
			return;
		}
		
		user_connect_callback = connectCallback;
		
		HashMap<String, Object> sns_info = new HashMap<String, Object>();
		sns_info.put("facebook_access_token", faceook_access_token);
		
		ConnectService.token(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_FACEBOOK, sns_info, new ConnectCallback<Session>(){
			@Override
        	public void onSuccess(Session session){
				Boolean join = session.getUser().getJoined();
				if(join){
					ConnectService.login(new ConnectCallback<Session>(){
						@Override
						public void onSuccess(Session session){
							user_connect_callback.onSuccess(session);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							user_connect_callback.onFail(result, exception);
						}
					});
				}else{
					user_connect_callback.onSuccess(session);
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				user_connect_callback.onFail(result, exception);
			}
		});
	}
	
	/**
	 * twitterLogin
	 * 
	 * @param activity
	 * @param connectCallback
	 */
	public static void twitterLogin(Activity activity, ConnectCallback<Session> connectCallback) {
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.READY && session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		if(getTwitter_consumer_key() == null || getTwitter_consumer_key().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Twitter Consumer Key is null"));
			return;
		}
		if(getTwitter_consumer_secret() == null || getTwitter_consumer_secret().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Twitter Consumer Secret is null"));
			return;
		}
		
		OAuthTwitter.getInstance().connect(activity, connectCallback);
	}
	
	
	/**
	 * setTwitterToken
	 *  
	 * @param twitter_access_token
	 * @param twitter_token_secret
	 * @param connectCallback 
	 */
	public static void setTwitterToken(String twitter_access_token, String twitter_token_secret, ConnectCallback<Session> connectCallback) {
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.READY && session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		if(getTwitter_consumer_key() == null || getTwitter_consumer_key().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Twitter Consumer Key is null"));
			return;
		}
		if(getTwitter_consumer_secret() == null || getTwitter_consumer_secret().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Twitter Consumer Secret is null"));
			return;
		}
		
		
		user_connect_callback = connectCallback;
		
		HashMap<String, Object> sns_info = new HashMap<String, Object>();
		sns_info.put("twitter_access_token", twitter_access_token);
		sns_info.put("twitter_token_secret", twitter_token_secret);
		
		ConnectService.token(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_TWITTER, sns_info, new ConnectCallback<Session>(){
			@Override
        	public void onSuccess(Session session){
				Boolean join = session.getUser().getJoined();
				if(join){
					ConnectService.login(new ConnectCallback<Session>(){
						@Override
						public void onSuccess(Session session){
							user_connect_callback.onSuccess(session);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							user_connect_callback.onFail(result, exception);
						}
					});
				}else{
					user_connect_callback.onSuccess(session);
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				user_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	/**
	 * join
	 * 
	 * @param profile
	 * @param connectCallback
	 */
	public static void join(Profile profile, ConnectCallback<Session> connectCallback) {
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		user_connect_callback = connectCallback;
		ConnectService.join(profile, new ConnectCallback<Session>(){
			@Override
			public void onSuccess(Session session){
				ConnectService.login(new ConnectCallback<Session>(){
					@Override
					public void onSuccess(Session session){
						user_connect_callback.onSuccess(session);
					}
					
					@Override
					public void onFail(FuncResult result, Exception exception){
						user_connect_callback.onFail(result, exception);
					}
				});
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				user_connect_callback.onFail(result, exception);
			}
			
		});
	}

	
	/**
	 * logout
	 * 
	 * @param connectCallback
	 */
	public static void logout(ConnectCallback<Session> connectCallback){
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		ConnectService.logout(connectCallback);
	}
	
	
	/**
	 * leave
	 * 
	 * @param connectCallback
	 */
	public static void leave(ConnectCallback<Session> connectCallback){
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		ConnectService.leave(connectCallback);
	}
	
	
	/**
	 * profileUpdate
	 * 
	 * @param profile
	 * @param connectCallback
	 */
	public static void profileUpdate(Profile profile, ConnectCallback<Session> connectCallback){
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		ConnectService.profileUpdate(profile, connectCallback);
	}
	
	
	/**
	 * profileGet
	 * 
	 * @param profile
	 * @param connectCallback
	 */
	public static void profileGet(ConnectCallback<Profile> connectCallback){
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		ConnectService.profileGet(connectCallback);
	}
	
	/**
	 * pushRegister
	 * 
	 * @param connectCallback
	 */
	public static void registerGCMRregistrationId(ConnectCallback<Session> connectCallback){
		user_connect_callback = connectCallback;
		
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		String regid = GCMManager.getInstance().getRegistrationId(context);
		if (regid == null || regid.length() == 0) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {

					ConnectService.registerGCMRregistrationId(user_connect_callback);
				}
			}, 1000);
		}else{
			ConnectService.registerGCMRregistrationId(connectCallback);
		}
	}
	
	
	/**
	 * pushUnRegister
	 * 
	 * @param connectCallback
	 */
	public static void unregisterGCMRregistrationId(ConnectCallback<Session> connectCallback){
		Session session = getActiveSession();
		if(session != null){
			if(session.getState() != SessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session is null"));
			return;
		}
		
		ConnectService.unregisterGCMRregistrationId(connectCallback);
	}
	
	
	public static void action(){
		
	}
	
	
	
	/**
	 * getVersion
	 * 
	 * @return SDK Version
	 */
	public static String getVersion() {
		
		return SDK_VERISON;
	}	
	
	
	
	public static String getConnectId() {
		return connect_id;
	}
		
	
	public static Session getActiveSession(){
		return Session.getInstance();
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
}
