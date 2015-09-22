package com.rankwave.connect.sdk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.rankwave.connect.sdk.ConnectCallback.FuncResult;
import com.rankwave.connect.sdk.core.NetworkThread;
import com.rankwave.connect.sdk.core.Request;
import com.rankwave.connect.sdk.core.Request.Callback;
import com.rankwave.connect.sdk.core.Response;

public class ConnectService {
	
	public static final String PREFIX_APP_PATH = "/app";
	public static final String PREFIX_OAUTH_PATH = "/oauth";
	public static final String PREFIX_USER_PATH = "/user";
	public static final String PREFIX_PUSH_PATH = "/push";
	
	
	public static final String CONNECT_INITIALIZE_PATH = PREFIX_APP_PATH + "/initialize.do";
	
	public static final String CONNECT_TOKEN_PATH = PREFIX_OAUTH_PATH + "/token.do";
	
	public static final String CONNECT_JOIN_PATH = PREFIX_USER_PATH + "/join.do";
		
	public static final String CONNECT_LOGIN_PATH = PREFIX_USER_PATH + "/login.do";
	
	public static final String CONNECT_LOGOUT_PATH = PREFIX_USER_PATH + "/logout.do";
	
	public static final String CONNECT_LEAVE_PATH = PREFIX_USER_PATH + "/leave.do";
	
	public static final String CONNECT_PROFILE_UPDATE_PATH = PREFIX_USER_PATH + "/profile/update.do";
	
	public static final String CONNECT_PROFILE_GET_PATH = PREFIX_USER_PATH + "/profile/get.do";
	
	public static final String CONNECT_SNS_INFO_PATH = PREFIX_USER_PATH + "/getSnsInfo.do";
		
	public static final String CONNECT_PUSH_REGISTER_DEVICE_PATH = PREFIX_PUSH_PATH + "/register_device.do";
	
	public static final String CONNECT_PUSH_UNREGISTER_DEVICE_PATH = PREFIX_PUSH_PATH + "/unregister_device.do";
	
	public static final String CONNECT_PUSH_ON_PATH = PREFIX_PUSH_PATH + "/pushOn.do";
	
	public static final String CONNECT_PUSH_OFF_PATH = PREFIX_PUSH_PATH + "/pushOff.do";
	
	public static final String CONNECT_ACTION_PATH = PREFIX_APP_PATH + "/action.do";
	
	public static final String CONNECT_SET_DEVICE_INFO_PATH = PREFIX_APP_PATH + "/setDeviceInfo.do";
	
	
	private static final String CONNECT_HTTP_FORMAT = "http://%s%s";
	private static final String CONNECT_HTTPS_FORMAT = "https://%s%s";
	

	public static String getHttpUrl(String path) {
		return String.format(CONNECT_HTTP_FORMAT, Connect.CONNECT_DOMAIN, path);
	}
	public static String getHttpsUrl(String path) {
		return String.format(CONNECT_HTTPS_FORMAT, Connect.CONNECT_DOMAIN, path);
	}
	
	
	@SuppressWarnings("unchecked")
	public static void initialize(ConnectCallback<ConnectSession> callback){
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
		
		
		//20150922 추가
		params.add(new BasicNameValuePair("ad_id_type", DeviceInfo.getInstance().getOs_type()));	//현재는 device에 따라 수집하기 때문에 os_type 를 사용하지만 추후 facebook_user_id도 수집할 경우에는 수정이 필요. 
		params.add(new BasicNameValuePair("ad_id", DeviceInfo.getInstance().getAd_id()));
		String savedId = ConnectSession.getInstance().loadId();
		String savedIdType = ConnectSession.getInstance().loadIdType();
		String savedSnsType = ConnectSession.getInstance().loadSnsType();
		
		if(savedId != null && !"".equals(savedId)){
			params.add(new BasicNameValuePair("saved_id", savedId));
		}
		if(savedIdType != null && !"".equals(savedIdType)){
			params.add(new BasicNameValuePair("saved_id_type", savedIdType));
		}
		if(savedSnsType != null && !"".equals(savedSnsType)){
			params.add(new BasicNameValuePair("saved_sns_type", savedSnsType));
		}
		
		
		new Request(getHttpsUrl(CONNECT_INITIALIZE_PATH), params, new Request.Callback() {
			
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
							
							ConnectSession.getInstance().setConnectSessionState(ConnectSessionState.CLOSED);
							
							if (connectCallback != null) {
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}
												
					} catch (JSONException e) {
						e.printStackTrace();
						
						ConnectSession.getInstance().setConnectSessionState(ConnectSessionState.CLOSED);
						if(connectCallback != null){
							connectCallback.onFail(FuncResult.E_FAIL, new Exception(e.getMessage()));
						}
						
						return;
					}
				} else {
					Log.e(Connect.TAG, response.error);
					
					if(connectCallback != null){
						ConnectSession.getInstance().setConnectSessionState(ConnectSessionState.CLOSED);
						connectCallback.onFail(FuncResult.E_FAIL, new Exception("fail to connect connection :: " + response.error));
					}
				}				
			}
		}, callback).execute();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static void token(IdType idType, SnsType snsType, HashMap<String, Object> info, ConnectCallback<ConnectSession> callback){
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		ConnectSession.getInstance().getUser().setSnsType(snsType);
		ConnectSession.getInstance().getUser().setIdType(idType);
		
		if(idType == IdType.ID_TYPE_SNS){
			if (snsType == SnsType.SNS_TYPE_FACEBOOK) {
				String faceook_access_token = (String) info.get("facebook_access_token");

				params.add(new BasicNameValuePair("access_token", faceook_access_token));
			} else if (snsType == SnsType.SNS_TYPE_TWITTER) {
				String twitter_access_token = (String) info.get("twitter_access_token");
				String twitter_token_secret = (String) info.get("twitter_token_secret");

				params.add(new BasicNameValuePair("access_token", twitter_access_token));
				params.add(new BasicNameValuePair("token_secret", twitter_token_secret));
			}
			
			params.add(new BasicNameValuePair("grant_type", "authorization_token"));
			params.add(new BasicNameValuePair("sns_type", SnsType.toString(snsType)));
			
		}else if(idType == IdType.ID_TYPE_EMAIL){
			params.add(new BasicNameValuePair("grant_type", "authorization_email"));
			params.add(new BasicNameValuePair("email", (String)info.get("email")));
			
			ConnectSession.getInstance().getUser().setId((String)info.get("email"));
			
			
		}else if(idType == IdType.ID_TYPE_ANONYMOUS){
			params.add(new BasicNameValuePair("grant_type", "authorization_anonymous"));
			//params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
			
			//ANONYMOUS id : device_id 
			ConnectSession.getInstance().getUser().setId(DeviceInfo.getInstance().getDevice_id());
		}

		params.add(new BasicNameValuePair("id_type", IdType.toString(idType)));
		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		params.add(new BasicNameValuePair("ad_id_type", DeviceInfo.getInstance().getOs_type()));	//현재는 device에 따라 수집하기 때문에 os_type 를 사용하지만 추후 facebook_user_id도 수집할 경우에는 수정이 필요. 
		params.add(new BasicNameValuePair("ad_id", DeviceInfo.getInstance().getAd_id()));
		
		//20150922 추가
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		
		String savedId = ConnectSession.getInstance().loadId();
		String savedIdType = ConnectSession.getInstance().loadIdType();
		String savedSnsType = ConnectSession.getInstance().loadSnsType();
		
		if(savedId != null && !"".equals(savedId)){
			params.add(new BasicNameValuePair("saved_id", savedId));
		}
		if(savedIdType != null && !"".equals(savedIdType)){
			params.add(new BasicNameValuePair("saved_id_type", savedIdType));
		}
		if(savedSnsType != null && !"".equals(savedSnsType)){
			params.add(new BasicNameValuePair("saved_sns_type", savedSnsType));
		}
		
		
		
		new Request(getHttpsUrl(CONNECT_TOKEN_PATH), params, new Callback() {

			@Override
			public void onCompleted(Response response) {
				
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == 0 && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							
							if (connectCallback != null) {
								String code = error.getString("code");
								if(code.equals("403")){
									connectCallback.onFail(FuncResult.E_INVALID_SNS_TOKEN, new Exception(error.toString()));
								}else{
									connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
								}
								
							}
						}else{
							ConnectSession connectSession = ConnectSession.getInstance();
							connectSession.setConnect_token(json.getString("connect_token"));
							connectSession.setExpires_in(json.getLong("expires_in"));
							
							User user = connectSession.getUser();
							
							if(json.has("joined")){
								Boolean joined = json.getBoolean("joined");
								user.setJoined(joined);
							}else{
								user.setJoined(true);
							}
							
							if(json.has("user")){
								JSONObject userJson = json.getJSONObject("user");
								if(userJson.has("sns_id")){
									userJson.getString("sns_id");
								}
							}
							
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
	public static void join(Profile profile, ConnectCallback<ConnectSession> callback){
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
				
		if(profile != null){
			if(profile.getEmail() != null){
				if(profile.getEmail().equals("")){
					params.add(new BasicNameValuePair("email", null));
				}else{
					params.add(new BasicNameValuePair("email", profile.getEmail()));
				}
			}
			
			if(profile.getName() != null){
				if(profile.getName().equals("")){
					params.add(new BasicNameValuePair("name", null));
				}else{
					try{
						params.add(new BasicNameValuePair("name", URLEncoder.encode(profile.getName(), "UTF-8")));
					}catch(UnsupportedEncodingException ue){
						
					}
					
				}
			}
				
			if(profile.getBirthday() != null){
				if(profile.getBirthday().equals("")){
					params.add(new BasicNameValuePair("birthday", "null"));
				}else{
					params.add(new BasicNameValuePair("birthday", profile.getBirthday()));
				}
			}
			
			if(profile.getGender() != null){
				if(profile.getGender().equals("")){
					params.add(new BasicNameValuePair("gender", "null"));
				}else{
					params.add(new BasicNameValuePair("gender", profile.getGender()));
				}
			}
			
			
			if(profile.getTimezone() != null){
				if(profile.getTimezone().equals("")){
					params.add(new BasicNameValuePair("timezone", "null"));
				}else{
					params.add(new BasicNameValuePair("timezone", profile.getTimezone()));
				}
			}
			
			if(profile.getLocale() != null){
				if(profile.getLocale().equals("")){
					params.add(new BasicNameValuePair("locale", "null"));
				}else{
					params.add(new BasicNameValuePair("locale", profile.getLocale()));
				}
			}
		}
		
		new Request(getHttpsUrl(CONNECT_JOIN_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						Boolean result = json.getBoolean("result");
						if(result){
							ConnectSession.getInstance().getUser().setJoined(true);
							
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
	public static void login(ConnectCallback<ConnectSession> callback){
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpsUrl(CONNECT_LOGIN_PATH), params, new Request.Callback() {
			
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
							//login 성공하면 push_token 을 올린다.
							setGCMRegistrationId(null);
							
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
	public static void logout(ConnectCallback<ConnectSession> callback){
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
		
		new Request(getHttpsUrl(CONNECT_LOGOUT_PATH), params, new Request.Callback() {
			
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
							ConnectSession.getInstance().setConnectSessionState(ConnectSessionState.CLOSED);
							
							ConnectSession.getConnectSession().deleteSavedConnectToken();
							ConnectSession.getConnectSession().deleteSavedSession();
														
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
	public static void leave(ConnectCallback<ConnectSession> callback){
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
		
		new Request(getHttpsUrl(CONNECT_LEAVE_PATH), params, new Request.Callback() {
			
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
							ConnectSession.getInstance().setConnectSessionState(ConnectSessionState.CLOSED);
							
							ConnectSession.getConnectSession().deleteSavedConnectToken();
							ConnectSession.getConnectSession().deleteSavedSession();
							
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
	public static void profileUpdate(Profile profile, ConnectCallback<ConnectSession> callback){
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
				
		if(profile != null){
			if(profile.getEmail() != null){
				if(profile.getEmail().equals("")){
					params.add(new BasicNameValuePair("email", "null"));
				}else{
					params.add(new BasicNameValuePair("email", profile.getEmail()));
				}
			}
			
			if(profile.getName() != null){
				if(profile.getName().equals("")){
					params.add(new BasicNameValuePair("name", "null"));
				}else{
					try{
						params.add(new BasicNameValuePair("name", URLEncoder.encode(profile.getName(),"UTF-8")));
					}catch(UnsupportedEncodingException ue){
						ue.printStackTrace();
					}
					
				}
			}
				
			if(profile.getBirthday() != null){
				if(profile.getBirthday().equals("")){
					params.add(new BasicNameValuePair("birthday", "null"));
				}else{
					params.add(new BasicNameValuePair("birthday", profile.getBirthday()));
				}
			}
			
			if(profile.getGender() != null){
				if(profile.getGender().equals("")){
					params.add(new BasicNameValuePair("gender", "null"));
				}else{
					params.add(new BasicNameValuePair("gender", profile.getGender()));
				}
			}
		
			if(profile.getTimezone() != null){
				if(profile.getTimezone().equals("")){
					params.add(new BasicNameValuePair("timezone", "null"));
				}else{
					params.add(new BasicNameValuePair("timezone", profile.getTimezone()));
				}
			}
			
			if(profile.getLocale() != null){
				if(profile.getLocale().equals("")){
					params.add(new BasicNameValuePair("locale", "null"));
				}else{
					params.add(new BasicNameValuePair("locale", profile.getLocale()));
				}
			}
		}
		
		new Request(getHttpsUrl(CONNECT_PROFILE_UPDATE_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
										
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
													
							if(connectCallback != null){
								connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else if(json.has("result")){
							connectCallback.onSuccess(ConnectSession.getInstance());
						}
					} catch (JSONException e) {
						e.printStackTrace();
						
						if(connectCallback != null){
							connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, new Exception(e.getMessage()));
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
	public static void profileGet(ConnectCallback<User> callback){
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
		
		new Request(getHttpsUrl(CONNECT_PROFILE_GET_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<User> connectCallback = (ConnectCallback<User>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
										
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
													
							if(connectCallback != null){
								connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else if(json.has("result")){
							Profile profile = new Profile();
							
							JSONObject profileObject = json.getJSONObject("profile");
							if(profileObject.has("birthday") && profileObject.getString("birthday")  != null && !profileObject.getString("birthday").equals("null"))
								profile.setBirthday(profileObject.getString("birthday"));
							
							if(profileObject.has("name") && profileObject.getString("name")  != null && !profileObject.getString("name").equals("null"))
								profile.setName(profileObject.getString("name"));
							
							if(profileObject.has("email") && profileObject.getString("email")  != null && !profileObject.getString("email").equals("null"))
								profile.setEmail(profileObject.getString("email"));
							
							if(profileObject.has("gender") && profileObject.getString("gender")  != null && !profileObject.getString("gender").equals("null"))
								profile.setGender(profileObject.getString("gender"));
							
							if(profileObject.has("timezone") && profileObject.getString("timezone")  != null && !profileObject.getString("timezone").equals("null"))
								profile.setTimezone(profileObject.getString("timezone"));
							
							if(profileObject.has("locale") && profileObject.getString("locale")  != null && !profileObject.getString("locale").equals("null"))
								profile.setLocale(profileObject.getString("locale"));
						
							SnsInfo snsInfo = new SnsInfo();
							if(json.has("sns_info")){
								JSONObject snsInfoObject = json.getJSONObject("sns_info");
								
								if(snsInfoObject.has("sns_id") && snsInfoObject.getString("sns_id")  != null && !snsInfoObject.getString("sns_id").equals("null"))
									snsInfo.setSnsId(snsInfoObject.getString("sns_id"));
								
								if(snsInfoObject.has("sns_type") && snsInfoObject.getString("sns_type")  != null && !snsInfoObject.getString("sns_type").equals("null"))
									snsInfo.setSnsType(SnsType.toEnum(snsInfoObject.getString("sns_type")));
								
								if(snsInfoObject.has("access_token") && snsInfoObject.getString("access_token")  != null && !snsInfoObject.getString("access_token").equals("null"))
									snsInfo.setAccessToken(snsInfoObject.getString("access_token"));
								
								if(snsInfoObject.has("token_secret") && snsInfoObject.getString("token_secret")  != null && !snsInfoObject.getString("token_secret").equals("null"))
									snsInfo.setTokenSecret(snsInfoObject.getString("token_secret"));
								
								if(snsInfoObject.has("profile_url") && snsInfoObject.getString("profile_url")  != null && !snsInfoObject.getString("profile_url").equals("null"))
									snsInfo.setProfileUrl(snsInfoObject.getString("profile_url"));
								
							}
							
							User user = new User();
							
							user.setProfile(profile);
							user.setSnsInfo(snsInfo);
							
							String id = json.getString("id");
							String id_type = json.getString("id_type");
							String sns_type = json.getString("sns_type");
							Boolean emailVerify = json.getBoolean("email_verify");
							Boolean joined = json.getBoolean("joined");
							
							user.setId(id);
							user.setIdType(IdType.toEnum(id_type));
							user.setSnsType(SnsType.toEnum(sns_type));
							user.setEmailVerify(emailVerify);
							user.setJoined(joined);
							
							connectCallback.onSuccess(user);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						
						if(connectCallback != null){
							connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, new Exception(e.getMessage()));
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
	public static void setGCMRegistrationId(ConnectCallback<ConnectSession> callback){
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
		params.add(new BasicNameValuePair("push_token", GCMManager.getInstance().getRegistrationId(Connect.getContext())));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpsUrl(CONNECT_PUSH_REGISTER_DEVICE_PATH), params, new Request.Callback() {
			
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
	public static void unsetGCMRegistrationId(ConnectCallback<ConnectSession> callback){
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpsUrl(CONNECT_PUSH_UNREGISTER_DEVICE_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<ConnectSession> connectCallback = (ConnectCallback<ConnectSession>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");

							String code = error.getString("code");
							if(code != null && code.equals("701")){	//case by unregistered device
								if(connectCallback != null){
									connectCallback.onSuccess(ConnectSession.getInstance());
								}
							}else{
								if(connectCallback != null){
									connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
								}
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
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
		params.add(new BasicNameValuePair("push_token", GCMManager.getInstance().getRegistrationId(Connect.getContext())));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpsUrl(CONNECT_PUSH_ON_PATH), params, new Request.Callback() {
			
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
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpsUrl(CONNECT_PUSH_OFF_PATH), params, new Request.Callback() {
			
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
	
	public static void action(Timestamp when, String what, Integer how, String where, JSONObject etc){
		ConnectSession connectSession = ConnectSession.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", connectSession.getConnect_token()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		if(when != null)
			params.add(new BasicNameValuePair("when", String.valueOf(when.getTime())));
		
		params.add(new BasicNameValuePair("what", what));
		params.add(new BasicNameValuePair("how", String.valueOf(how)));
		params.add(new BasicNameValuePair("where", where));
		
		if(etc != null)
			params.add(new BasicNameValuePair("etc", etc.toString()));
		
		
		new Request(getHttpsUrl(CONNECT_ACTION_PATH), params, new Request.Callback() {
			
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
	
	
	@SuppressWarnings("unchecked")
	public static void getSnsInfo(SnsType snsType, HashMap<String, Object> snsInfo, ConnectCallback<ConnectSession> callback){
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("sns_type", SnsType.toString(snsType)));
		
		if (snsType == SnsType.SNS_TYPE_FACEBOOK) {
			String faceook_access_token = (String) snsInfo.get("facebook_access_token");

			params.add(new BasicNameValuePair("access_token", faceook_access_token));
		} else if (snsType == SnsType.SNS_TYPE_TWITTER) {
			String twitter_access_token = (String) snsInfo.get("twitter_access_token");
			String twitter_token_secret = (String) snsInfo.get("twitter_token_secret");

			params.add(new BasicNameValuePair("access_token", twitter_access_token));
			params.add(new BasicNameValuePair("token_secret", twitter_token_secret));
		}
				
		
		new Request(getHttpsUrl(CONNECT_SNS_INFO_PATH), params, new Request.Callback() {
			
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
							JSONObject snsInfoObject = json.getJSONObject("sns_info");
							
							if(snsInfoObject.has("sns_id") && snsInfoObject.getString("sns_id") != null && !snsInfoObject.getString("sns_id").equals("null")){
								Connect.getActiveConnectSession().getUser().getSnsInfo().setSnsId(snsInfoObject.getString("sns_id"));
								Connect.getActiveConnectSession().getUser().setId(snsInfoObject.getString("sns_id"));
							}
							
							if(snsInfoObject.has("name") && snsInfoObject.getString("name") != null && !snsInfoObject.getString("name").equals("null"))
								Connect.getActiveConnectSession().getUser().getProfile().setName(snsInfoObject.getString("name"));
							
							if(snsInfoObject.has("email") && snsInfoObject.getString("email") != null && !snsInfoObject.getString("email").equals("null"))
								Connect.getActiveConnectSession().getUser().getProfile().setEmail(snsInfoObject.getString("email"));
							
							if(snsInfoObject.has("profile_url") && snsInfoObject.getString("profile_url") != null && !snsInfoObject.getString("profile_url").equals("null"))
								Connect.getActiveConnectSession().getUser().getSnsInfo().setProfileUrl(snsInfoObject.getString("profile_url"));
							
							if(snsInfoObject.has("birthday") && snsInfoObject.getString("birthday") != null && !snsInfoObject.getString("birthday").equals("null"))
								Connect.getActiveConnectSession().getUser().getProfile().setBirthday(snsInfoObject.getString("birthday"));

							if(snsInfoObject.has("gender") && snsInfoObject.getString("gender") != null && !snsInfoObject.getString("gender").equals("null"))
								Connect.getActiveConnectSession().getUser().getProfile().setGender(snsInfoObject.getString("gender"));
							
							
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
	
}
