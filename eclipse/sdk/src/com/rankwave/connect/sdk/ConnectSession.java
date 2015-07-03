package com.rankwave.connect.sdk;


import android.content.Context;
import android.content.SharedPreferences;

public class ConnectSession {
	private static final String PROPERTY_CONNECT_TOKEN = "connectToken";
	private static final String PROPERTY_SAVED_CONNECT_TOKEN = "savedConnectToken";
	
	private static final String PROPERTY_ID_TYPE = "idType";
	private static final String PROPERTY_SNS_TYPE = "snsType";
	private static final String PROPERTY_SNS_ACCESS_TOKEN = "snsAccessToken";
	private static final String PROPERTY_SNS_TOKEN_SECRET = "snsTokenSecret";
	private static final String PROPERTY_ID = "id";
	
	private ConnectSessionState connectSessionState = ConnectSessionState.CLOSED;
	
	private String connect_token;
	private Long expires_in;
	
	private User user;
		
	
	/**
	 *  Session singleton object
	 */
	private static ConnectSession instance = null;
	
	public static ConnectSession getInstance() {
		if(instance == null) {
			synchronized (ConnectSession.class) {
				instance = new ConnectSession();
			}
		}

		return instance;
	}
	
	public ConnectSession() {

		init();		
	}
	
	
	public static ConnectSession getConnectSession(){
		return instance;
	}
	
	/**
	 * ConnectSession init
	 * 
	 * @Method Name   : init
	 */
	private void init() {
		user = new User();
	}
	
		
	/**
	 * 현재 ConnectSession의 상태를 반환한다.
	 * 
	 * @Method Name   : getConnectSessionState
	 * @return SessionState 값
	 */
	public ConnectSessionState getConnectSessionState() {
		return connectSessionState;
	}
	
	
	/**
	 * 현재 Session의 상태를 할당한다.
	 * 
	 * @Method Name   : setState
	 * @param state SessionState 값
	 */
	public void setConnectSessionState(ConnectSessionState connectSessionState) {
		this.connectSessionState = connectSessionState;
	}
	
	
	
	
	public String getConnect_token() {
		connect_token = loadConnectToken();
		return connect_token;
	}



	public void setConnect_token(String connect_token) {
		this.connect_token = connect_token;
		storeConnectToken(connect_token);
	}



	public Long getExpires_in() {
		return expires_in;
	}



	public void setExpires_in(Long expires_in) {
		this.expires_in = expires_in;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	private SharedPreferences getUserPreferences(Context context) {

		return context.getSharedPreferences(Connect.SDK_PREFERENCES,
				Context.MODE_PRIVATE);
	}
	
	public String loadConnectToken() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		String connectToken = prefs.getString(PROPERTY_CONNECT_TOKEN, "");
		
		return connectToken;
	}

	public void storeConnectToken(String connectToken) {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_CONNECT_TOKEN, connectToken);
		editor.commit();
	}

	public void deleteConnectToken() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_CONNECT_TOKEN, "");
		editor.commit();
	}
	
	public String loadSavedConnectToken() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		String connectToken = prefs.getString(PROPERTY_SAVED_CONNECT_TOKEN, "");
		
		return connectToken;
	}

	public void storeSavedConnectToken(String connectToken) {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_SAVED_CONNECT_TOKEN, connectToken);
		editor.commit();
	}

	public void deleteSavedConnectToken() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_SAVED_CONNECT_TOKEN, "");
		editor.commit();
	}
	
	public String loadIdType() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		String idType = prefs.getString(PROPERTY_ID_TYPE, "");
		
		return idType;
	}

	public void storeIdType(String idType) {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_ID_TYPE, idType);
		editor.commit();
	}

	public void deleteIdType() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_ID_TYPE, "");
		editor.commit();
	}
	
	public String loadSnsType() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		String snsType = prefs.getString(PROPERTY_SNS_TYPE, "");
		
		return snsType;
	}

	public void storeSnsType(String snsType) {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_SNS_TYPE, snsType);
		editor.commit();
	}

	public void deleteSnsType() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_SNS_TYPE, "");
		editor.commit();
	}
	
	public String loadSnsAccessToken() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		String snsAccessToken = prefs.getString(PROPERTY_SNS_ACCESS_TOKEN, "");
		
		return snsAccessToken;
	}

	public void storeSnsAccessToken(String snsAccessToken) {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_SNS_ACCESS_TOKEN, snsAccessToken);
		editor.commit();
	}

	public void deleteSnsAccessToken() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_SNS_ACCESS_TOKEN, "");
		editor.commit();
	}

	public String loadSnsTokenSecret() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		String snsTokenSecret = prefs.getString(PROPERTY_SNS_TOKEN_SECRET, "");
		
		return snsTokenSecret;
	}

	public void storeSnsTokenSecret(String snsTokenSecret) {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_SNS_TOKEN_SECRET, snsTokenSecret);
		editor.commit();
	}

	public void deleteSnsTokenSecret() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_SNS_TOKEN_SECRET, "");
		editor.commit();
	}
	
	
	public String loadId() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		String id = prefs.getString(PROPERTY_ID, "");
		
		return id;
	}

	public void storeId(String id) {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_ID, id);
		editor.commit();
	}

	public void deleteId() {
		final SharedPreferences prefs = getUserPreferences(Connect.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_ID, "");
		editor.commit();
	}
	
	public void connectSessionClear(){
		connect_token = null;
		
		connectSessionState = ConnectSessionState.CLOSED;
		
		if(user != null){
			user = null;
		}
		
		user = new User();
	}
	
	public void deleteSavedSession(){
		ConnectSession.getInstance().deleteConnectToken();
		ConnectSession.getInstance().deleteSavedConnectToken();
		ConnectSession.getInstance().deleteIdType();
		ConnectSession.getInstance().deleteSnsType();
		ConnectSession.getInstance().deleteSnsAccessToken();
		ConnectSession.getInstance().deleteSnsTokenSecret();
		ConnectSession.getInstance().deleteId();
	}
}
