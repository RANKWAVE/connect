package com.rankwave.connect.sdk;

import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.rankwave.connect.sdk.ConnectCallback.FuncResult;


public class ConnectManager {
	private static ConnectCallback<ConnectSession> initialize_connect_callback = null;
	private static ConnectCallback<ConnectSession> login_connect_callback = null;
	private static ConnectCallback<ConnectSession> gcm_connect_callback = null;
	
	private static Thread thread = null;
	
	static HashMap<String, Object> info = new HashMap<String, Object>();
	
	public static void sdkInitialize(ConnectCallback<ConnectSession> connectCallback) {
		initialize_connect_callback = connectCallback;
		
		//android ad_id를 먼저 구한다.
		new AsyncTask<DeviceInfo, Integer, Info>() {
			@Override
			protected Info doInBackground(DeviceInfo... params) {
				DeviceInfo deviceInfo = params[0];
			
				Info adInfo = null;
				try {
				    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(deviceInfo.context);
				    
				    if(adInfo != null){
				    	String id = adInfo.getId();
						Boolean isLAT = adInfo.isLimitAdTrackingEnabled();
						
						Log.d(Connect.TAG, adInfo.toString());
						if(!isLAT){
							DeviceInfo.getInstance().setAd_id(id);
						}
				    }
				    
				} catch (Exception e) {
				    // Unrecoverable error connecting to Google Play services (e.g.,
				    // the old version of the service doesn't support getting AdvertisingId).
					  e.printStackTrace();
					  
				}
				  
				return adInfo;
			}
			
			@Override
			protected void onPostExecute(Info adInfo) {
				//appconnect 를 호출한다.
				appConnect();
				
				if (adInfo == null) {
					// retry??
					
				} else {
					
				}
				super.onPostExecute(adInfo);
			}
		}.execute(DeviceInfo.getInstance(), null, null);
	}
	
	public static void appConnect() {
		ConnectService.appConnect(new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				initialize_connect_callback.onSuccess(connectSession);
				
				if(thread == null || !thread.isAlive()){
					thread = new ConnectPollingThread();
					thread.start();
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				initialize_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	/**
	 * setFacebookToken
	 *  
	 * @param sns_id : sns_id
	 * @param faceook_access_token : faceook_access_token 
	 * @param connectCallback : connectCallback
	 */
	public static void facebookLogin(String sns_id, String faceook_access_token, ConnectCallback<ConnectSession> connectCallback) {
		login_connect_callback = connectCallback;
		
		ConnectSession connectSession = Connect.getConnectSession();
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
		
		info.put("sns_id", sns_id);
		info.put("facebook_access_token", faceook_access_token);
		
		ConnectService.userConnect(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_FACEBOOK, info, new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				connectSession.connectSessionClear();
				
				connectSession.storeId((String)info.get("sns_id"));
				connectSession.storeIdType(IdType.toString(IdType.ID_TYPE_SNS));
				connectSession.storeSnsType(SnsType.toString(SnsType.SNS_TYPE_FACEBOOK));
				
				login_connect_callback.onSuccess(connectSession);
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	/**
	 * setTwitterToken
	 *  
	 * @param sns_id : sns_id
	 * @param twitter_access_token : twitter_access_token
	 * @param twitter_token_secret : twitter_token_secret
	 * @param connectCallback : connectCallback
	 */
	public static void twitterLogin(String sns_id, String twitter_access_token, String twitter_token_secret, ConnectCallback<ConnectSession> connectCallback) {
		login_connect_callback = connectCallback;
		
		ConnectSession connectSession = Connect.getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(twitter_access_token == null || twitter_access_token.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("twitter_access_token can not be empty."));
			}
			return;
		}
		
		if(twitter_token_secret == null || twitter_token_secret.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("twitter_token_secret can not be empty."));
			}
			return;
		}
				
		info.put("sns_id", sns_id);
		info.put("twitter_access_token", twitter_access_token);
		info.put("twitter_token_secret", twitter_token_secret);
		
		ConnectService.userConnect(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_TWITTER, info, new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				connectSession.connectSessionClear();
				
				connectSession.storeId((String)info.get("sns_id"));
				connectSession.storeIdType(IdType.toString(IdType.ID_TYPE_SNS));
				connectSession.storeSnsType(SnsType.toString(SnsType.SNS_TYPE_TWITTER));
				
				login_connect_callback.onSuccess(connectSession);
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	public static void userDisconnect(ConnectCallback<ConnectSession> connectCallback){
		ConnectService.userDisconnect(connectCallback);
		
		ConnectSession.getConnectSession().connectSessionClear();
	}
	
	public static void userDelete(ConnectCallback<ConnectSession> connectCallback){
		ConnectService.userDelete(connectCallback);
		
		ConnectSession.getConnectSession().connectSessionClear();
	}
	
	
	
	
	public static void pushOn(ConnectCallback<ConnectSession> connectCallback){
		gcm_connect_callback = connectCallback;
		
		String regid = GCMManager.getInstance().getRegistrationId(Connect.getContext());
		if (regid == null || regid.length() == 0) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					
					pushOn(gcm_connect_callback);
					
				}
			}, 1000);
		}else{
			ConnectService.pushOn(connectCallback);
		}
	}
	
	public static void pushOff(ConnectCallback<ConnectSession> connectCallback){
		ConnectService.pushOff(connectCallback);
	}
	
}
