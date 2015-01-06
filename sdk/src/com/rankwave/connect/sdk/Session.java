package com.rankwave.connect.sdk;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Session {
	private static final String PROPERTY_CONNECT_TOKEN = "connectToken";
	
	private SessionState sessionState = SessionState.CLOSED;
	
	private String connect_token;
	private Long expires_in;
	
	private User user;
		
	
	/**
	 *  Session singleton object
	 */
	private static Session instance = null;
	
	public static Session getInstance() {
		if(instance == null) {
			synchronized (Session.class) {
				instance = new Session();
			}
		}

		return instance;
	}
	
	public Session() {

		init();		
	}
	
	
	public static Session getSession(){
		return instance;
	}
	
	/**
	 * Session init
	 * 
	 * @Method Name   : init
	 */
	private void init() {
		user = new User();
	}
	
		
	/**
	 * 현재 Session의 상태를 반환한다.
	 * 
	 * @Method Name   : getState
	 * @return SessionState 값
	 */
	public SessionState getState() {
		return sessionState;
	}
	
	
	/**
	 * 현재 Session의 상태를 할당한다.
	 * 
	 * @Method Name   : setState
	 * @param state SessionState 값
	 */
	public void setState(SessionState sessionState) {
		this.sessionState = sessionState;
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
	
	
	
}
