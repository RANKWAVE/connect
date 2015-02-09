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
	
	public static final String CONNECT_DOMAIN = "api.rankwave.com";
	//public static final String CONNECT_DOMAIN = "54.176.29.228";	//dev
	
	
	private static Context context;
	private static String connect_id;
	private static String facebook_appid;
	private static String twitter_consumer_key;
	private static String twitter_consumer_secret;
	private static String sender_id;
		
	public static ConnectCallback<ConnectSession> login_connect_callback = null;
	public static ConnectCallback<ConnectSession> join_connect_callback = null;
	public static ConnectCallback<ConnectSession> gcm_connect_callback = null;
	
	private static Boolean auto_join_flag;
	
	
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
		String push_seq = prefs.getString(Connect.INTENT_PUSH_SEQ, "");
		
		if(!push_seq.equals("")){
			try{
				JSONObject etc = new JSONObject();
				etc.put(Connect.INTENT_PUSH_SEQ, push_seq);
				etc.put("os_type", DeviceInfo.getInstance().getOs_type());
				
				ConnectService.action(null, "PUSH", 1, "CONNECT SDK", etc);
				
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
	public static void anonymousLogin(ConnectCallback<ConnectSession> connectCallback) {
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.READY && connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("Session's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		login_connect_callback = connectCallback;
		
		ConnectService.token(IdType.ID_TYPE_ANONYMOUS, null, null, new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				Boolean join = connectSession.getUser().getJoined();
				if(join){
					ConnectService.login(new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							login_connect_callback.onSuccess(connectSession);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							login_connect_callback.onFail(result, exception);
						}
					});
				}else{
					join(null, login_connect_callback);
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
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
	public static void facebookLogin(Activity activity, List<String> permissions, Boolean autoJoinFlag, ConnectCallback<ConnectSession> connectCallback) {
		auto_join_flag = autoJoinFlag;
		
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.READY && connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		if(getFacebook_appid() == null || getFacebook_appid().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("facebook id can not be empty."));
			return;
		}
		
		OAuthFacebook.getInstance().connecnt(activity, permissions, connectCallback);
	}
	
	
	/**
	 * setFacebookToken
	 *  
	 * @param faceook_access_token 
	 * @param connectCallback 
	 */
	public static void setFacebookToken(String faceook_access_token, ConnectCallback<ConnectSession> connectCallback) {
		setFacebookToken(faceook_access_token, getAuto_join_flag(), connectCallback);
	}
	
	/**
	 * setFacebookToken
	 *  
	 * @param faceook_access_token 
	 * @param connectCallback 
	 * @param autoJoinFlag
	 */
	public static void setFacebookToken(String faceook_access_token, Boolean autoJoinFlag, ConnectCallback<ConnectSession> connectCallback) {
		auto_join_flag = autoJoinFlag;
		
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.READY && connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		if(getFacebook_appid() == null || getFacebook_appid().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Facebook App Id is null"));
			return;
		}
		
		login_connect_callback = connectCallback;
		
		HashMap<String, Object> sns_info = new HashMap<String, Object>();
		sns_info.put("facebook_access_token", faceook_access_token);
		
		ConnectService.token(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_FACEBOOK, sns_info, new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				Boolean joined = connectSession.getUser().getJoined();
				if(joined){
					ConnectService.login(new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							ConnectService.setDeviceInfo(new ConnectCallback<ConnectSession>(){
								@Override
								public void onSuccess(ConnectSession connectSession){
									ConnectService.profileGet(new ConnectCallback<Profile>(){
										@Override
										public void onSuccess(Profile profile){
											ConnectSession connectSession = getConnectSession();
											connectSession.getUser().setProfile(profile);
											
											login_connect_callback.onSuccess(connectSession);
										}
										
										@Override
										public void onFail(FuncResult result, Exception exception){
											login_connect_callback.onFail(result, exception);
										}
									});
								}
								@Override
								public void onFail(FuncResult result, Exception exception){
									login_connect_callback.onFail(result, exception);
								}
							});
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							login_connect_callback.onFail(result, exception);
						}
					});
				}else{
					if(getAuto_join_flag() == null || getAuto_join_flag()){
						join(null, new ConnectCallback<ConnectSession>(){
							@Override
							public void onSuccess(ConnectSession connectSession){
								ConnectService.profileGet(new ConnectCallback<Profile>(){
									@Override
									public void onSuccess(Profile profile){
										ConnectSession connectSession = getConnectSession();
										connectSession.getUser().setProfile(profile);
										
										login_connect_callback.onSuccess(connectSession);
									}
									
									@Override
									public void onFail(FuncResult result, Exception exception){
										login_connect_callback.onFail(result, exception);
									}
								});
							}
							
							@Override
							public void onFail(FuncResult result, Exception exception){
								login_connect_callback.onFail(result, exception);
							}
						});
					}else{
						login_connect_callback.onSuccess(connectSession);
					}
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
			}
		});
	}
	
		
	/**
	 * twitterLogin
	 * 
	 * @param activity
	 * @param connectCallback
	 */
	public static void twitterLogin(Activity activity, Boolean autoJoinFlag, ConnectCallback<ConnectSession> connectCallback) {
		auto_join_flag = autoJoinFlag;
		
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.READY && connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
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
	public static void setTwitterToken(String twitter_access_token, String twitter_token_secret, ConnectCallback<ConnectSession> connectCallback) {
		setTwitterToken(twitter_access_token, twitter_token_secret, getAuto_join_flag(), connectCallback);
	}
	
	/**
	 * setTwitterToken
	 *  
	 * @param twitter_access_token
	 * @param twitter_token_secret
	 * @param connectCallback
	 * @param autoJoinFlag 
	 */
	public static void setTwitterToken(String twitter_access_token, String twitter_token_secret, Boolean autoJoinFlag, ConnectCallback<ConnectSession> connectCallback) {
		auto_join_flag = autoJoinFlag;
		
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.READY && connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		
		login_connect_callback = connectCallback;
		
		HashMap<String, Object> sns_info = new HashMap<String, Object>();
		sns_info.put("twitter_access_token", twitter_access_token);
		sns_info.put("twitter_token_secret", twitter_token_secret);
		
		ConnectService.token(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_TWITTER, sns_info, new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				Boolean join = connectSession.getUser().getJoined();
				if(join){
					ConnectService.login(new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							ConnectService.setDeviceInfo(new ConnectCallback<ConnectSession>(){
								@Override
								public void onSuccess(ConnectSession connectSession){
									ConnectService.profileGet(new ConnectCallback<Profile>(){
										@Override
										public void onSuccess(Profile profile){
											ConnectSession connectSession = getConnectSession();
											connectSession.getUser().setProfile(profile);
											
											login_connect_callback.onSuccess(connectSession);
										}
										
										@Override
										public void onFail(FuncResult result, Exception exception){
											login_connect_callback.onFail(result, exception);
										}
									});
								}
							
								@Override
								public void onFail(FuncResult result, Exception exception){
									login_connect_callback.onFail(result, exception);
								}
							});
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							login_connect_callback.onFail(result, exception);
						}
					});
				}else{
					if(getAuto_join_flag() == null || getAuto_join_flag()){
						join(null, new ConnectCallback<ConnectSession>(){
							@Override
							public void onSuccess(ConnectSession connectSession){
								ConnectService.profileGet(new ConnectCallback<Profile>(){
									@Override
									public void onSuccess(Profile profile){
										ConnectSession connectSession = getConnectSession();
										connectSession.getUser().setProfile(profile);
										
										login_connect_callback.onSuccess(connectSession);
									}
									
									@Override
									public void onFail(FuncResult result, Exception exception){
										login_connect_callback.onFail(result, exception);
									}
								});
							}
							
							@Override
							public void onFail(FuncResult result, Exception exception){
								login_connect_callback.onFail(result, exception);
							}
						});
					}else{
						login_connect_callback.onSuccess(connectSession);
					}
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	/**
	 * join
	 * 
	 * @param profile
	 * @param connectCallback
	 */
	public static void join(Profile profile, ConnectCallback<ConnectSession> connectCallback) {
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		join_connect_callback = connectCallback;
		ConnectService.join(profile, new ConnectCallback<ConnectSession>(){
			@Override
			public void onSuccess(ConnectSession connectSession){
				ConnectService.login(new ConnectCallback<ConnectSession>(){
					@Override
					public void onSuccess(ConnectSession connectSession){
						ConnectService.setDeviceInfo(new ConnectCallback<ConnectSession>(){
							@Override
							public void onSuccess(ConnectSession connectSession){
								ConnectService.profileGet(new ConnectCallback<Profile>(){
									@Override
									public void onSuccess(Profile profile){
										ConnectSession connectSession = getConnectSession();
										connectSession.getUser().setProfile(profile);
										
										join_connect_callback.onSuccess(connectSession);
									}
									
									@Override
									public void onFail(FuncResult result, Exception exception){
										join_connect_callback.onFail(result, exception);
									}
								});
							}
							@Override
							public void onFail(FuncResult result, Exception exception){
								join_connect_callback.onFail(result, exception);
							}
						});
						
					}
					
					@Override
					public void onFail(FuncResult result, Exception exception){
						join_connect_callback.onFail(result, exception);
					}
				});
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				join_connect_callback.onFail(result, exception);
			}
			
		});
	}

	
	/**
	 * logout
	 * 
	 * @param connectCallback
	 */
	public static void logout(ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		ConnectService.logout(connectCallback);
	}
	
	
	/**
	 * leave
	 * 
	 * @param connectCallback
	 */
	public static void leave(ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
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
	public static void profileUpdate(Profile profile, ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		ConnectService.profileUpdate(profile, connectCallback);
	}
	
	
	/**
	 * setGCMRegistrationId
	 * 
	 * @param connectCallback
	 */
	public static void setGCMRegistrationId(ConnectCallback<ConnectSession> connectCallback){
		gcm_connect_callback = connectCallback;
		
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		if(getSender_id() == null || getSender_id().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("sender id can not be empty."));
			return;
		}
		
		String regid = GCMManager.getInstance().getRegistrationId(context);
		if (regid == null || regid.length() == 0) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {

					ConnectService.setGCMRegistrationId(gcm_connect_callback);
				}
			}, 1000);
		}else{
			ConnectService.setGCMRegistrationId(connectCallback);
		}
	}
	
	
	/**
	 * unsetGCMRegistrationId
	 * 
	 * @param connectCallback
	 */
	public static void unsetGCMRegistrationId(ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		ConnectService.unsetGCMRegistrationId(connectCallback);
	}
	
	//comming soon
	public static void action(){
		
	}
	
	public static void getConnectSession(ConnectCallback<ConnectSession> connectCallback){
		ConnectSession connectSession = getConnectSession();
		if(connectSession != null){
			connectCallback.onSuccess(connectSession);
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
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
}
