package com.rankwave.connect.sdk;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ConnectSession {
	private static final String PROPERTY_CONNECT_TOKEN = "connectToken";
	private static final String PROPERTY_SAVED_CONNECT_TOKEN = "savedConnectToken";
	
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
		if (connectToken.equalsIgnoreCase("")) {
			Log.i(Connect.TAG, "ConnectToken not found.");
			return "";
		}

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
		if (connectToken.equalsIgnoreCase("")) {
			Log.i(Connect.TAG, "SavedConnectToken not found.");
			return "";
		}

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
	
	
	public void connectSessionClear(){
		connect_token = null;
		
		connectSessionState = ConnectSessionState.READY;
		
		if(user != null){
			user = null;
		}
		user = new User();
	}
	
}
