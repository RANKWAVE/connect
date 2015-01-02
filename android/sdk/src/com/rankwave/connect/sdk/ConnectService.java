package com.rankwave.connect.sdk;

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
		
	public static final String CONNECT_PUSH_REGISTER_DEVICE_PATH = PREFIX_PUSH_PATH + "/register_device.do";
	
	public static final String CONNECT_PUSH_UNREGISTER_DEVICE_PATH = PREFIX_PUSH_PATH + "/unregister_device.do";
	
	public static final String CONNECT_ACTION_PATH = PREFIX_APP_PATH + "/action.do";
	
	
	private static final String CONNECT_HTTP_FORMAT = "http://%s%s";
	private static final String CONNECT_HTTPS_FORMAT = "https://%s%s";
	

	public static String getHttpUrl(String path) {
		return String.format(CONNECT_HTTP_FORMAT, Connect.CONNECT_DOMAIN, path);
	}
	public static String getHttpsUrl(String path) {
		return String.format(CONNECT_HTTPS_FORMAT, Connect.CONNECT_DOMAIN, path);
	}
	
	
	@SuppressWarnings("unchecked")
	public static void initialize(ConnectCallback<Session> callback){
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
		
		new Request(getHttpUrl(CONNECT_INITIALIZE_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<Session> connectCallback = (ConnectCallback<Session>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						Boolean result = json.getBoolean("result");
						if(result){
							Session.getInstance().setState(SessionState.READY);
							
							if (connectCallback != null) {
								connectCallback.onSuccess(Session.getInstance());
							}
						}else{
							JSONObject error = json.getJSONObject("error");
							Log.e(Connect.TAG, error.toString());
							
							Session.getInstance().setState(SessionState.FAILED);
							
							if (connectCallback != null) {
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}
												
					} catch (JSONException e) {
						e.printStackTrace();
						
						Session.getInstance().setState(SessionState.FAILED);
						if(connectCallback != null){
							connectCallback.onFail(FuncResult.E_FAIL, new Exception(e.getMessage()));
						}
						
						return;
					}
				} else {
					Log.e(Connect.TAG, response.error);
					
					if(connectCallback != null){
						Session.getInstance().setState(SessionState.FAILED);
						connectCallback.onFail(FuncResult.E_FAIL, new Exception("fail to connect connection :: " + response.error));
					}
				}				
			}
		}, callback).execute();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static void token(IdType idType, SnsType snsType, HashMap<String, Object> snsInfo, ConnectCallback<Session> callback){
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		Session.getInstance().getUser().setSnsType(snsType);
		Session.getInstance().getUser().setIdType(idType);
		
		if(idType == IdType.ID_TYPE_SNS){
			if (snsType == SnsType.SNS_TYPE_FACEBOOK) {
				String faceook_access_token = (String) snsInfo.get("facebook_access_token");

				params.add(new BasicNameValuePair("access_token", faceook_access_token));
			} else if (snsType == SnsType.SNS_TYPE_TWITTER) {
				String twitter_access_token = (String) snsInfo.get("twitter_access_token");
				String twitter_token_secret = (String) snsInfo.get("twitter_token_secret");

				params.add(new BasicNameValuePair("access_token", twitter_access_token));
				params.add(new BasicNameValuePair("token_secret", twitter_token_secret));
			}
			
			params.add(new BasicNameValuePair("grant_type", "authorization_token"));
			params.add(new BasicNameValuePair("sns_type", SnsType.toString(snsType)));
			
		}else if(idType == IdType.ID_TYPE_EMAIL){
			params.add(new BasicNameValuePair("grant_type", "authorization_email "));
			return;
			
		}else if(idType == IdType.ID_TYPE_ANONYMOUS){
			params.add(new BasicNameValuePair("grant_type", "authorization_anonymous"));
			params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		}

		params.add(new BasicNameValuePair("id_type", IdType.toString(idType)));
		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		
		
		new Request(getHttpUrl(CONNECT_TOKEN_PATH), params, new Callback() {

			@Override
			public void onCompleted(Response response) {
				
				ConnectCallback<Session> connectCallback = (ConnectCallback<Session>)response.user_obejct;
				
				if (response.error_code == 0 && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							
							if (connectCallback != null) {
								connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else{
							Session session = Session.getInstance();
							User user = session.getUser();
							
							if(json.has("joined")){
								Boolean joined = json.getBoolean("joined");
								user.setJoined(joined);
							}else{
								user.setJoined(true);
							}
							session.setState(SessionState.OPENED);
							session.setConnect_token(json.getString("connect_token"));
							session.setExpires_in(json.getLong("expires_in"));
							
							if (connectCallback != null) {
								connectCallback.onSuccess(session);
							}
						}
						
					} catch (JSONException e) {
						e.printStackTrace();
						
						if (connectCallback != null) {
							connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, new Exception(e.getMessage()));
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
	public static void join(Profile profile, ConnectCallback<Session> callback){
		Session session = Session.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", session.getConnect_token()));
				
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
					params.add(new BasicNameValuePair("name", profile.getName()));
				}
			}
				
			if(profile.getBirthday() != null){
				if(profile.getBirthday().equals("")){
					params.add(new BasicNameValuePair("birthday", null));
				}else{
					params.add(new BasicNameValuePair("birthday", profile.getBirthday()));
				}
			}
			
			if(profile.getGender() != null){
				if(profile.getGender().equals("")){
					params.add(new BasicNameValuePair("gender", null));
				}else{
					params.add(new BasicNameValuePair("gender", profile.getGender()));
				}
			}
			
			if(profile.getResidence() != null){
				try{
					JSONObject residenceObject = new JSONObject();
					residenceObject.put("country", profile.getResidence().getCountry());
					residenceObject.put("states", profile.getResidence().getStates());
					residenceObject.put("city", profile.getResidence().getCity());
					residenceObject.put("street", profile.getResidence().getStreet());
					residenceObject.put("spot", profile.getResidence().getSpot());
					
					params.add(new BasicNameValuePair("residence", residenceObject.toString()));
				}catch(JSONException e){
					e.printStackTrace();
				}
			}
			
			if(profile.getHometown() != null){
				try{
					JSONObject hometownObject = new JSONObject();
					hometownObject.put("country", profile.getHometown().getCountry());
					hometownObject.put("states", profile.getHometown().getStates());
					hometownObject.put("city", profile.getHometown().getCity());
					hometownObject.put("street", profile.getHometown().getStreet());
					hometownObject.put("spot", profile.getHometown().getSpot());
					
					params.add(new BasicNameValuePair("hometown", hometownObject.toString()));
				}catch(JSONException e){
					e.printStackTrace();
				}
			}
			
			if(profile.getTimezone() != null){
				if(profile.getTimezone().equals("")){
					params.add(new BasicNameValuePair("timezone", null));
				}else{
					params.add(new BasicNameValuePair("timezone", profile.getTimezone()));
				}
			}
			
			if(profile.getLocale() != null){
				if(profile.getLocale().equals("")){
					params.add(new BasicNameValuePair("locale", null));
				}else{
					params.add(new BasicNameValuePair("locale", profile.getLocale()));
				}
			}
		}
		
		new Request(getHttpUrl(CONNECT_JOIN_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<Session> connectCallback = (ConnectCallback<Session>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						Boolean result = json.getBoolean("result");
						if(result){
							if (connectCallback != null) {
								connectCallback.onSuccess(Session.getInstance());
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
	public static void login(ConnectCallback<Session> callback){
		Session session = Session.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", session.getConnect_token()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpUrl(CONNECT_LOGIN_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<Session> connectCallback = (ConnectCallback<Session>)response.user_obejct;
				
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
								connectCallback.onSuccess(Session.getInstance());
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
	public static void logout(ConnectCallback<Session> callback){
		Session session = Session.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", session.getConnect_token()));
		
		new Request(getHttpUrl(CONNECT_LOGOUT_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				Session.getInstance().setState(SessionState.READY);
				
				ConnectCallback<Session> connectCallback = (ConnectCallback<Session>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
					
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
							
							if(connectCallback != null){
								connectCallback.onFail(FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else{							
							Session.getInstance().deleteConnectToken();
							if(connectCallback != null){
								connectCallback.onSuccess(Session.getInstance());
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
	public static void leave(ConnectCallback<Session> callback){
		Session session = Session.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", session.getConnect_token()));
		
		new Request(getHttpUrl(CONNECT_LEAVE_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				Session.getInstance().setState(SessionState.READY);
				
				ConnectCallback<Session> connectCallback = (ConnectCallback<Session>)response.user_obejct;
				
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
								connectCallback.onSuccess(Session.getInstance());
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
	public static void profileUpdate(Profile profile, ConnectCallback<Session> callback){
		Session session = Session.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", session.getConnect_token()));
				
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
					params.add(new BasicNameValuePair("name", profile.getName()));
				}
			}
				
			if(profile.getBirthday() != null){
				if(profile.getBirthday().equals("")){
					params.add(new BasicNameValuePair("birthday", null));
				}else{
					params.add(new BasicNameValuePair("birthday", profile.getBirthday()));
				}
			}
			
			if(profile.getGender() != null){
				if(profile.getGender().equals("")){
					params.add(new BasicNameValuePair("gender", null));
				}else{
					params.add(new BasicNameValuePair("gender", profile.getGender()));
				}
			}
			
			if(profile.getResidence() != null){
				try{
					JSONObject residenceObject = new JSONObject();
					residenceObject.put("country", profile.getResidence().getCountry());
					residenceObject.put("states", profile.getResidence().getStates());
					residenceObject.put("city", profile.getResidence().getCity());
					residenceObject.put("street", profile.getResidence().getStreet());
					residenceObject.put("spot", profile.getResidence().getSpot());
					
					params.add(new BasicNameValuePair("residence", residenceObject.toString()));
				}catch(JSONException e){
					
				}
			}
			
			if(profile.getHometown() != null){
				try{
					JSONObject hometownObject = new JSONObject();
					hometownObject.put("country", profile.getHometown().getCountry());
					hometownObject.put("states", profile.getHometown().getStates());
					hometownObject.put("city", profile.getHometown().getCity());
					hometownObject.put("street", profile.getHometown().getStreet());
					hometownObject.put("spot", profile.getHometown().getSpot());
					
					params.add(new BasicNameValuePair("hometown", hometownObject.toString()));
				}catch(JSONException e){
					
				}
			}
			
			if(profile.getTimezone() != null){
				if(profile.getTimezone().equals("")){
					params.add(new BasicNameValuePair("timezone", null));
				}else{
					params.add(new BasicNameValuePair("timezone", profile.getTimezone()));
				}
			}
			
			if(profile.getLocale() != null){
				if(profile.getLocale().equals("")){
					params.add(new BasicNameValuePair("locale", null));
				}else{
					params.add(new BasicNameValuePair("locale", profile.getLocale()));
				}
			}
		}
		
		new Request(getHttpUrl(CONNECT_PROFILE_UPDATE_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<Session> connectCallback = (ConnectCallback<Session>)response.user_obejct;
				
				if (response.error_code == NetworkThread.E_SUCCESS && response.error.equals("OK")) {
					JSONObject json = response.getJsonObject();
										
					try {
						if(json.has("error")){
							JSONObject error = json.getJSONObject("error");
													
							if(connectCallback != null){
								connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, new Exception(error.toString()));
							}
						}else if(json.has("result")){
							connectCallback.onSuccess(Session.getInstance());
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
	public static void profileGet(ConnectCallback<Profile> callback){
		Session session = Session.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", session.getConnect_token()));
		
		new Request(getHttpUrl(CONNECT_PROFILE_GET_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<Profile> connectCallback = (ConnectCallback<Profile>)response.user_obejct;
				
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
							
							if(profileObject.has("residence") && profileObject.getString("residence")  != null && !profileObject.getString("residence").equals("null")){
								JSONObject residencObject = profileObject.getJSONObject("residence");
								
								if(residencObject.has("country") && residencObject.getString("country")  != null && !residencObject.getString("country").equals("null"))
									profile.getResidence().setCountry(residencObject.getString("country"));
								
								if(residencObject.has("states") && residencObject.getString("states")  != null && !residencObject.getString("states").equals("null"))
									profile.getResidence().setStates(residencObject.getString("states"));
								
								if(residencObject.has("city") && residencObject.getString("city")  != null && !residencObject.getString("city").equals("null"))
									profile.getResidence().setCity(residencObject.getString("city"));
								
								if(residencObject.has("street") && residencObject.getString("street")  != null && !residencObject.getString("street").equals("null"))
									profile.getResidence().setStreet(residencObject.getString("street"));
								
								if(residencObject.has("spot") && residencObject.getString("spot")  != null && !residencObject.getString("spot").equals("null"))
									profile.getResidence().setSpot(residencObject.getString("spot"));
							}
							
							if(profileObject.has("hometown") && profileObject.getString("hometown")  != null && !profileObject.getString("hometown").equals("null")){
								JSONObject hometownObject = profileObject.getJSONObject("hometown");
								
								if(hometownObject.has("country") && hometownObject.getString("country")  != null && !hometownObject.getString("country").equals("null"))
									profile.getHometown().setCountry(hometownObject.getString("country"));
								
								if(hometownObject.has("states") && hometownObject.getString("states")  != null && !hometownObject.getString("states").equals("null"))
									profile.getHometown().setStates(hometownObject.getString("states"));
								
								if(hometownObject.has("city") && hometownObject.getString("city")  != null && !hometownObject.getString("city").equals("null"))
									profile.getHometown().setCity(hometownObject.getString("city"));
								
								if(hometownObject.has("street") && hometownObject.getString("street")  != null && !hometownObject.getString("street").equals("null"))
									profile.getHometown().setStreet(hometownObject.getString("street"));
								
								if(hometownObject.has("spot") && hometownObject.getString("spot")  != null && !hometownObject.getString("spot").equals("null"))
									profile.getHometown().setSpot(hometownObject.getString("spot"));
							}

							connectCallback.onSuccess(profile);
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
	public static void registerGCMRregistrationId(ConnectCallback<Session> callback){
		Session session = Session.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", session.getConnect_token()));
		params.add(new BasicNameValuePair("push_token", GCMManager.getInstance().getRegistrationId(Connect.getContext())));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpUrl(CONNECT_PUSH_REGISTER_DEVICE_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<Session> connectCallback = (ConnectCallback<Session>)response.user_obejct;
				
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
								connectCallback.onSuccess(Session.getInstance());
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
	public static void unregisterGCMRregistrationId(ConnectCallback<Session> callback){
		Session session = Session.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", session.getConnect_token()));
		params.add(new BasicNameValuePair("os_type", DeviceInfo.getInstance().getOs_type()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		
		new Request(getHttpUrl(CONNECT_PUSH_UNREGISTER_DEVICE_PATH), params, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				ConnectCallback<Session> connectCallback = (ConnectCallback<Session>)response.user_obejct;
				
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
								connectCallback.onSuccess(Session.getInstance());
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
		Session session = Session.getInstance();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("connect_id", Connect.getConnectId()));
		params.add(new BasicNameValuePair("connect_token", session.getConnect_token()));
		params.add(new BasicNameValuePair("device_id", DeviceInfo.getInstance().getDevice_id()));
		if(when != null)
			params.add(new BasicNameValuePair("when", String.valueOf(when.getTime())));
		
		params.add(new BasicNameValuePair("what", what));
		params.add(new BasicNameValuePair("how", String.valueOf(how)));
		params.add(new BasicNameValuePair("where", where));
		
		if(etc != null)
			params.add(new BasicNameValuePair("etc", etc.toString()));
		
		
		new Request(getHttpUrl(CONNECT_ACTION_PATH), params, new Request.Callback() {
			
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
