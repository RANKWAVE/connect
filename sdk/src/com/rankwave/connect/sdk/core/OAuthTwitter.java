package com.rankwave.connect.sdk.core;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.rankwave.connect.sdk.Connect;
import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.ConnectSession;
import com.rankwave.connect.sdk.OAuthLoginActivity;
import com.rankwave.connect.sdk.SnsType;

public class OAuthTwitter {

	public static final int OAUTH_TWITTER_REQUEST_CODE = 5001;
	public static final String PROPERTY_TWITTER_CONSUMER_KEY = "com.rankwave.connect.sdk.TwitterConsumerKey";
	public static final String PROPERTY_TWITTER_CONSUMER_SECRET = "com.rankwave.connect.sdk.TwitterConsumerSecret";
	public static final Uri TWIT_CALLBACK_URL = Uri.parse("http://api.rank-cloud.com/login/mobileTwitterCallback.do"); 
	
	ConnectCallback<ConnectSession> connectCallback = null;
	
	private Twitter twitter;
	private twitter4j.auth.RequestToken twitter_request_token;
		
	private static OAuthTwitter instance = null;
	public static OAuthTwitter getInstance() {
		
		if(instance == null){
			
			synchronized (OAuthTwitter.class) {
				instance = new OAuthTwitter();				
			}
		}
		
		return instance;
	}
	
	public OAuthTwitter() {

	}
	
	
	public void connect(Activity activity, ConnectCallback<ConnectSession> callback) {
		getInstance().connectCallback = callback;
		
		new AsyncTask<Object, Integer, String>() {

			@Override
			protected String doInBackground(Object... params) {
				
				Activity activity = (Activity)params[0];
				String twitter_consumer_key = Connect.getTwitter_consumer_key();
				String twitter_consumer_secret = Connect.getTwitter_consumer_secret();
				
				try {
					ConfigurationBuilder builder = new ConfigurationBuilder();
					builder.setOAuthConsumerKey(twitter_consumer_key);
					builder.setOAuthConsumerSecret(twitter_consumer_secret);
										
					TwitterFactory factory = new TwitterFactory(builder.build());
					twitter = factory.getInstance();
										
					getInstance().twitter_request_token = twitter.getOAuthRequestToken(TWIT_CALLBACK_URL.toString());
								
					Intent intent = new Intent(activity, OAuthLoginActivity.class);
					intent.putExtra("authUrl", getInstance().twitter_request_token.getAuthorizationURL());
				
					activity.startActivityForResult(intent, OAuthTwitter.OAUTH_TWITTER_REQUEST_CODE);
					
				} catch (Exception ex) {

					Log.i(Connect.TAG, "exception connectTwitter");
					ex.printStackTrace();
					
					if(getInstance().connectCallback != null){
						getInstance().connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL,
								new Exception("failed to login to facebook"));
					}	
				}
				
				return "";
			}
		
		}.execute(activity, null);		
	}
	
	public void getToken(Activity currentActivity, int requestCode, int resultCode, Intent data) {

		if(resultCode == Activity.RESULT_OK) { 
			String oauth_verifier = data.getStringExtra("oauthVerifier");
			
			new  AsyncTask<String, Integer, Exception>() {
	
				@Override
				protected Exception doInBackground(String... params) {
					
					String oauth_verifier = params[0];
	
					try {
						
						twitter4j.auth.AccessToken twitter_access_token = twitter.getOAuthAccessToken(getInstance().twitter_request_token, oauth_verifier);
						
						String twitter_token = twitter_access_token.getToken(); 
						String twitter_token_secret = twitter_access_token.getTokenSecret();
						
						twitter4j.User user = twitter.showUser(twitter.getId());
						String sns_id = String.valueOf(twitter.getId());
						String profile_url = user.getProfileImageURL();
						String name = twitter.getScreenName();
						
						
						Connect.getActiveConnectSession().getUser().getSnsInfo().clearInfo();
						Connect.getActiveConnectSession().getUser().getSnsInfo().setSnsId(sns_id);
						Connect.getActiveConnectSession().getUser().getSnsInfo().setName(name);
						Connect.getActiveConnectSession().getUser().getSnsInfo().setProfileUrl(profile_url);
						Connect.getActiveConnectSession().getUser().getSnsInfo().setSnsType(SnsType.SNS_TYPE_TWITTER);
						Connect.getActiveConnectSession().getUser().getSnsInfo().setAccessToken(twitter_token);
						Connect.getActiveConnectSession().getUser().getSnsInfo().setTokenSecret(twitter_token_secret);
						Connect.getActiveConnectSession().getUser().getSnsInfo().setBirthday("");
						
						return null;
	
					} catch (Exception ex) {
						ex.printStackTrace();
						
						return ex;
					}					
					
				}
				
				@Override
				protected void onPostExecute(Exception result) {
					
					if(result == null) {
					
						Connect.setTwitterToken(Connect.getActiveConnectSession().getUser().getSnsInfo().getAccessToken(),
								Connect.getActiveConnectSession().getUser().getSnsInfo().getTokenSecret(),
								getInstance().connectCallback);
					} else {
						
						if(getInstance().connectCallback != null){
							getInstance().connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, result);
						}
					}
				};
				
			}.execute(oauth_verifier);
		} else if(resultCode == Activity.RESULT_CANCELED) {
			
			// notify to connection fail.
			if(getInstance().connectCallback != null){
				getInstance().connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL, new Exception("oauth canceled"));
			}	
		}
	}
	
	
	public void close() {
		
	}

}
