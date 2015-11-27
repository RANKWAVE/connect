package com.rankwave.connect.sdk;

import android.util.Log;

import com.rankwave.connect.sdk.ConnectCallback.FuncResult;
import com.rankwave.connect.sdk.core.NetworkThread;
import com.rankwave.connect.sdk.core.Request;
import com.rankwave.connect.sdk.core.Request.Callback;
import com.rankwave.connect.sdk.core.Response;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectService {
	public static final String PREFIX_APP_PATH = "/2.0/app";
	public static final String PREFIX_USER_PATH = "/2.0/user";
	public static final String PREFIX_PUSH_PATH = "/2.0/push";
	
	
	public static final String APP_CONNECT_PATH = PREFIX_APP_PATH + "/connect.do";
	
	public static final String USER_CONNECT_PATH = PREFIX_USER_PATH + "/connect.do";
	
	public static final String USER_DISCONNECT_PATH = PREFIX_USER_PATH + "/disconnect.do";
	
	public static final String USER_DELETE_PATH = PREFIX_USER_PATH + "/delete.do";
		
	public static final String PUSH_REGISTER_DEVICE_PATH = PREFIX_PUSH_PATH + "/registerDevice.do";
	
	public static final String PUSH_ON_PATH = PREFIX_PUSH_PATH + "/pushOn.do";
	
	public static final String PUSH_OFF_PATH = PREFIX_PUSH_PATH + "/pushOff.do";
	
	public static final String APP_ACTION_PATH = PREFIX_APP_PATH + "/action.do";
		
	private static final String CONNECT_HTTP_FORMAT = "http://%s%s";
	private static final String CONNECT_HTTPS_FORMAT = "https://%s%s";
	

	public static String getHttpUrl(String path) {
		return String.format(CONNECT_HTTP_FORMAT, Connect.CONNECT_DOMAIN, path);
	}
	public static String getHttpsUrl(String path) {
		return String.format(CONNECT_HTTPS_FORMAT, Connect.CONNECT_DOMAIN, path);
	}
	
	
	@SuppressWarnings("unchecked")
	public static void appConnect(ConnectCallback<ConnectSession> callback){
		DeviceInfo deviceInfo = DeviceInfo.getInstance();
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("device_id", deviceInfo.getDevice_id()));
		params.add(new BasicNameValuePair("os_type", deviceInfo.getOs_type()));
		params.add(new BasicNameValuePair("os_version", deviceInfo.getOs_version()));
		params.add(new BasicNameValuePair("app_version", deviceInfo.getApp_version()));
		params.add(new BasicNameValuePair("model", deviceInfo.getDevice_model()));
		params.add(new BasicNameValuePair("locale", deviceInfo.getLocale()));
		params.add(new BasicNameValuePair("location", deviceInfo.getLocation()));
		params.add(new BasicNameValuePair("mobile_carrier_name", deviceInfo.getNetwork_operation_name()));
		params.add(new BasicNameValuePair("ad_id", deviceInfo.getAd_id()));
		params.add(new BasicNameValuePair("ad_id_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("saved_id", ConnectSession.getConnectSession().loadId()));
		params.add(new BasicNameValuePair("saved_id_type", ConnectSession.getConnectSession().loadIdType()));
		params.add(new BasicNameValuePair("saved_sns_type", ConnectSession.getConnectSession().loadSnsType()));
		params.add(new BasicNameValuePair("push_token", GCMManager.getInstance().getRegistrationId(Connect.getContext())));
		
		new Request(getHttpsUrl(APP_CONNECT_PATH), params, new Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						Boolean result = json.getBoolean("result");
						if(result){
							if (connectCallback != null) {
								connectCallback.onSuccess(ConnectSession.getInstance());
							}
						}else{
							JSONObject error = json.getJSONObject("error");
							Log.e(Connect.TAG, error.toString());
														
							if (connectCallback != null) {
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						
						if(connectCallback != null){
							connectCallback.onFail(FuncResult.E_FAIL, new Exception(e.getMessage()));
						}
						
						return;
					}
				} else {
					Log.e(Connect.TAG, response.error);
					
					if(connectCallback != null){
						connectCallback.onFail(FuncResult.E_FAIL, new Exception("fail to connect connection :: " + response.error));
					}
				}				
			}
		}, callback).execute();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static void userConnect(IdType idType, SnsType snsType, HashMap<String, Object> info, ConnectCallback<ConnectSession> callback){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		if(idType == IdType.ID_TYPE_SNS){
			if (snsType == SnsType.SNS_TYPE_FACEBOOK) {
				String faceook_access_token = (String) info.get("facebook_access_token");
				String sns_id = (String) info.get("sns_id");

				params.add(new BasicNameValuePair("id", sns_id));
				params.add(new BasicNameValuePair("token", faceook_access_token));
			} else if (snsType == SnsType.SNS_TYPE_TWITTER) {
				String twitter_access_token = (String) info.get("twitter_access_token");
				String twitter_token_secret = (String) info.get("twitter_token_secret");
				
				String sns_id = (String) info.get("sns_id");

				params.add(new BasicNameValuePair("id", sns_id));
				params.add(new BasicNameValuePair("token", twitter_access_token));
				params.add(new BasicNameValuePair("token_secret", twitter_token_secret));
			}else if (snsType == SnsType.SNS_TYPE_KAKAO){
				String kakao_access_token = (String) info.get("kakao_access_token");
				String sns_id = (String) info.get("sns_id");

				params.add(new BasicNameValuePair("id", sns_id));
				params.add(new BasicNameValuePair("token", kakao_access_token));
			}
			
			params.add(new BasicNameValuePair("sns_type", SnsType.toString(snsType)));
			
		}else if(idType == IdType.ID_TYPE_EMAIL){
			params.add(new BasicNameValuePair("email", (String)info.get("email")));
		}

		params.add(new BasicNameValuePair("id_type", IdType.toString(idType)));
		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		
		params.add(new BasicNameValuePair("ad_id_type", DeviceInfo.getInstance().getOs_type()));	//현재는 device에 따라 수집하기 때문에 os_type 를 사용하지만 추후 facebook_user_id도 수집할 경우에는 수정이 필요. 
		params.add(new BasicNameValuePair("ad_id", DeviceInfo.getInstance().getAd_id()));
		params.add(new BasicNameValuePair("saved_id", ConnectSession.getConnectSession().loadId()));
		params.add(new BasicNameValuePair("saved_id_type", ConnectSession.getConnectSession().loadIdType()));
		params.add(new BasicNameValuePair("saved_sns_type", ConnectSession.getConnectSession().loadSnsType()));
		params.add(new BasicNameValuePair("push_token", GCMManager.getInstance().getRegistrationId(Connect.getContext())));
		
		new Request(getHttpsUrl(USER_CONNECT_PATH), params, new Callback() {

			@Override
			public void onCompleted(Response response) {
				
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == 0 && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							
							if (connectCallback != null) {
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else{
							ConnectSession connectSession = ConnectSession.getInstance();
														
							if (connectCallback != null) {
								connectCallback.onSuccess(connectSession);
							}
						}
						
					} catch (JSONException e) {
						e.printStackTrace();
						
						if (connectCallback != null) {
							connectCallback.onFail(FuncResult.E_FAIL, new Exception(e.getMessage()));
						}
					}
				} else {
					Log.e(Connect.TAG, response.error);
					
					if (connectCallback != null) {
						connectCallback.onFail(FuncResult.E_FAIL, new Exception("fail to connect connection :: " + response.error));
					}
				}
			}
		}, (Object)callback).execute();
	}
	
	
	@SuppressWarnings("unchecked")
	public static void userDisconnect(ConnectCallback<ConnectSession> callback){
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("id", ConnectSession.getConnectSession().loadId()));
		params.add(new BasicNameValuePair("id_type", ConnectSession.getConnectSession().loadIdType()));
		params.add(new BasicNameValuePair("sns_type", ConnectSession.getConnectSession().loadSnsType()));
		
		new Request(getHttpsUrl(USER_DISCONNECT_PATH), params, new Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							
							if(connectCallback != null){
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else{														
							if(connectCallback != null){
								connectCallback.onSuccess(ConnectSession.getInstance());
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						
						if(connectCallback != null){
							connectCallback.onFail(FuncResult.E_FAIL, new Exception(e.getMessage()));
						}
						
						return;
					}
				} else {
					Log.e(Connect.TAG, response.error);
					
					if(connectCallback != null){
						connectCallback.onFail(FuncResult.E_FAIL, new Exception("fail to connect connection :: " + response.error));
					}
				}				
			}
		}, callback).execute();
	}
	
	
	@SuppressWarnings("unchecked")
	public static void userDelete(ConnectCallback<ConnectSession> callback){
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("id", ConnectSession.getConnectSession().loadId()));
		params.add(new BasicNameValuePair("id_type", ConnectSession.getConnectSession().loadIdType()));
		params.add(new BasicNameValuePair("sns_type", ConnectSession.getConnectSession().loadSnsType()));
		
		new Request(getHttpsUrl(USER_DELETE_PATH), params, new Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
										
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							
							if(connectCallback != null){
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else{						
							if(connectCallback != null){
								connectCallback.onSuccess(ConnectSession.getInstance());
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						
						if(connectCallback != null){
							connectCallback.onFail(FuncResult.E_FAIL, new Exception(e.getMessage()));
						}
						
						return;
					}
				} else {
					Log.e(Connect.TAG, response.error);
					
					if(connectCallback != null){
						connectCallback.onFail(FuncResult.E_FAIL, new Exception("fail to connect connection :: " + response.error));
					}
				}				
			}
		}, callback).execute();
	}
	

	
	@SuppressWarnings("unchecked")
	public static void pushRegisterDevice(ConnectCallback<ConnectSession> callback){
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("push_token", GCMManager.getInstance().getRegistrationId(Connect.getContext())));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpsUrl(PUSH_REGISTER_DEVICE_PATH), params, new Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							
							if(connectCallback != null){
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else{
							if(connectCallback != null){
								connectCallback.onSuccess(ConnectSession.getInstance());
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						if(connectCallback != null){
							connectCallback.onFail(FuncResult.E_FAIL, new Exception(e.getMessage()));
						}
						
						return;
					}
				} else {
					Log.e(Connect.TAG, response.error);
					
					if(connectCallback != null){
						connectCallback.onFail(FuncResult.E_FAIL, new Exception("fail to connect connection :: " + response.error));
					}
				}
			}
		}, callback).execute();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static void pushOn(ConnectCallback<ConnectSession> callback){
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("id", ConnectSession.getConnectSession().loadId()));
		params.add(new BasicNameValuePair("id_type", ConnectSession.getConnectSession().loadIdType()));
		params.add(new BasicNameValuePair("sns_type", ConnectSession.getConnectSession().loadSnsType()));

		params.add(new BasicNameValuePair("push_token", GCMManager.getInstance().getRegistrationId(Connect.getContext())));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpsUrl(PUSH_ON_PATH), params, new Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							
							if(connectCallback != null){
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else{
							if(connectCallback != null){
								connectCallback.onSuccess(ConnectSession.getInstance());
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						if(connectCallback != null){
							connectCallback.onFail(FuncResult.E_FAIL, new Exception(e.getMessage()));
						}
						
						return;
					}
				} else {
					Log.e(Connect.TAG, response.error);
					
					if(connectCallback != null){
						connectCallback.onFail(FuncResult.E_FAIL, new Exception("fail to connect connection :: " + response.error));
					}
				}
			}
		}, callback).execute();
	}
	
	@SuppressWarnings("unchecked")
	public static void pushOff(ConnectCallback<ConnectSession> callback){		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("id", ConnectSession.getConnectSession().loadId()));
		params.add(new BasicNameValuePair("id_type", ConnectSession.getConnectSession().loadIdType()));
		params.add(new BasicNameValuePair("sns_type", ConnectSession.getConnectSession().loadSnsType()));

		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpsUrl(PUSH_OFF_PATH), params, new Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							
							if(connectCallback != null){
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else{
							if(connectCallback != null){
								connectCallback.onSuccess(ConnectSession.getInstance());
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						if(connectCallback != null){
							connectCallback.onFail(FuncResult.E_FAIL, new Exception(e.getMessage()));
						}
						
						return;
					}
				} else {
					Log.e(Connect.TAG, response.error);
					
					if(connectCallback != null){
						connectCallback.onFail(FuncResult.E_FAIL, new Exception("fail to connect connection :: " + response.error));
					}
				}
			}
		}, callback).execute();
	}
	
	public static void action(Timestamp when, String what, Integer how, String where, JSONObject object){
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("id", ConnectSession.getConnectSession().loadId()));
		params.add(new BasicNameValuePair("id_type", ConnectSession.getConnectSession().loadIdType()));
		params.add(new BasicNameValuePair("sns_type", ConnectSession.getConnectSession().loadSnsType()));

		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		if(when != null)
			params.add(new BasicNameValuePair("when", String.valueOf(when.getTime())));
		
		params.add(new BasicNameValuePair("what", what));
		params.add(new BasicNameValuePair("how", String.valueOf(how)));
		params.add(new BasicNameValuePair("where", where));
		
		if(object != null)
			params.add(new BasicNameValuePair("object", object.toString()));
		
		
		new Request(getHttpsUrl(APP_ACTION_PATH), params, new Callback() {
			
			@Override
			public void onCompleted(Response response) {
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
										
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							Log.e(Connect.TAG, error.toString());
						}else{
							
						}
					} catch (JSONException e) {
						e.printStackTrace();
						
						return;
					}
				} else {
					Log.i(Connect.TAG, response.error);
				}
			}
		}, null).execute();
	}
	
	
}
