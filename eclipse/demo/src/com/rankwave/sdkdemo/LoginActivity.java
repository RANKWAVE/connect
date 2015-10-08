package com.rankwave.sdkdemo;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.rankwave.connect.sdk.Connect;
import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.ConnectSession;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

public class LoginActivity extends Activity {
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	TwitterAuthClient mTwitterAuthClient;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login);
		
		
		/*
		 * SDK 내부적으로 필요한 리소스를 초기화 하고, SDK 를 이용할 수 있게 준비하는 작업을 수행합니다.
		 */
		Connect.sdkInitialize(getApplicationContext(), new ConnectCallback<ConnectSession>() {
			@Override
        	public void onSuccess(ConnectSession connectSession){
				Log.i("Connect", "========================================");
        		Log.i("Connect", "SDK Initialized.");
        		Log.i("Connect", "========================================");
        		
        		Connect.pushWebLink();
			}
			
			@Override
        	public void onFail(FuncResult funcResult, Exception ex){
				Log.e("Connect", "========================================");
        		Log.e("Connect", "sdkInitialize Fail.");
        		Log.e("Connect", "----------------------------------------");
        		Log.e("Connect", funcResult.toString() + " : " + ex.toString());
        		Log.e("Connect", "========================================");
        	}
		});
		
		
		Button btn_login_facebook = (Button) findViewById(R.id.btn_facebook_login);
		
		btn_login_facebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				showLoading(true);
				
				List<String> permissions = Arrays.asList(
						"public_profile", "email", "user_friends");
				
				/*
				 * start facebook oauth login
				 */
				Session session = Session.getActiveSession();
				if (session == null) {
					Log.i(AppConst.LOG_TAG, "Facebook Session is null");

					session = new Session(getApplicationContext());
					
					Session.setActiveSession(session);

					session.openForRead(new Session.OpenRequest(LoginActivity.this).setCallback(
							statusCallback).setPermissions(permissions));

				} else {
					Log.i(AppConst.LOG_TAG, "Facebook Session is not null");
					if (!session.isOpened() && !session.isClosed()) {
						session.openForRead(new Session.OpenRequest(LoginActivity.this).setCallback(
								statusCallback).setPermissions(permissions));

					} else {
						Log.i(AppConst.LOG_TAG, "Facebook Session.openActiveSession");
						Session.openActiveSession(LoginActivity.this, true, statusCallback);
					}
				}
				
				showLoading(false);
			}

		});
		
		
		Button btn_login_twitter = (Button) findViewById(R.id.btn_twitter_login);

		btn_login_twitter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLoading(true);

				mTwitterAuthClient = new TwitterAuthClient();

				mTwitterAuthClient.authorize(LoginActivity.this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

					@Override
					public void success(Result<TwitterSession> twitterSessionResult) {
						// Success
						TwitterSession session = Twitter.getSessionManager().getActiveSession();
						TwitterAuthToken authToken = session.getAuthToken();
						String token = authToken.token;
						String secret = authToken.secret;

						Connect.twitterLogin(String.valueOf(session.getId()), token, secret, new ConnectCallback<ConnectSession>() {
							@Override
							public void onSuccess(ConnectSession connectSession) {
								Log.i(AppConst.LOG_TAG, "========================================");
								Log.i(AppConst.LOG_TAG, "twitterLogin Success.");
								Log.i(AppConst.LOG_TAG, "========================================");
							}

							@Override
							public void onFail(FuncResult funcResult, Exception ex) {
								Log.e(AppConst.LOG_TAG, "========================================");
								Log.e(AppConst.LOG_TAG, "twitterLogin Fail.");
								Log.e(AppConst.LOG_TAG, "----------------------------------------");
								Log.e(AppConst.LOG_TAG, ex.toString());
								Log.e(AppConst.LOG_TAG, "========================================");
							}
						});

/*
						 * 앱 시나리오에 맞는 UI 를 구성하시면 됩니다.
						 * Demo 앱에서는 Sns 정보를 객체에 담아 MainActivity 로 화면 전환을 하고 있습니다.
						 */

						TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
						twitterApiClient.getAccountService().verifyCredentials(false, false, new Callback<User>() {
							@Override
							public void success(Result<User> userResult) {
								TwitterSession session = Twitter.getSessionManager().getActiveSession();
								TwitterAuthToken authToken = session.getAuthToken();

								String name = userResult.data.name;
								String profileurl = userResult.data.profileImageUrl;

								SnsUser snsUser = new SnsUser();
								snsUser.setName(name);
								snsUser.setSns_id(String.valueOf(session.getId()));
								snsUser.setAccess_token(authToken.token);
								snsUser.setToken_secret(authToken.secret);
								snsUser.setId_type("sns");
								snsUser.setSns_type("TW");
								snsUser.setProfile_image(profileurl);

								Intent intent = new Intent(LoginActivity.this, MainActivity.class);
								intent.putExtra("snsUser", snsUser);
								startActivity(intent);
							}

							@Override
							public void failure(TwitterException e) {

							}
						});
					}

					@Override
					public void failure(TwitterException e) {
						e.printStackTrace();
					}
				});

				showLoading(false);
			}
		});
		
		
		showLoading(false);

		ImageView iv_loading = (ImageView) findViewById(R.id.iv_loading);
		rotateAnimation(1000, iv_loading);
		
	}
	
	
	public void goMainActivity(){
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
	}
	
	
	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {

			Log.i(AppConst.LOG_TAG, "Call updateView form SessionStatusCallback()" + state.toString());
			
			if (state == SessionState.OPENED || state == SessionState.OPENED_TOKEN_UPDATED) {

				// make request to the /me API
                Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                    // callback after Graph API response with user object
                    @Override
                    public void onCompleted(GraphUser user, Response response) {                    	
                        if (user != null) {
                        	/*
                        	 * facebook의 accesstoken 및 id 를 얻은 후 Connect SDK 의 facebookLogin을 호출 하면 됩니다.
                        	 */
            				String facebook_access_token = Session.getActiveSession().getAccessToken();
            				String id = user.getId();
            				
            				Connect.facebookLogin(id, facebook_access_token, new ConnectCallback<ConnectSession>() {
            					@Override
            					public void onSuccess(ConnectSession connectSession) {
            						Log.i(AppConst.LOG_TAG, "========================================");
            						Log.i(AppConst.LOG_TAG, "facebookLogin Success.");
            						Log.i(AppConst.LOG_TAG, "========================================");
            					}
            					
            					@Override
            					public void onFail(FuncResult funcResult, Exception ex) {
            						Log.e(AppConst.LOG_TAG, "========================================");
            						Log.e(AppConst.LOG_TAG, "facebookLogin Fail.");
            						Log.e(AppConst.LOG_TAG, "----------------------------------------");
            						Log.e(AppConst.LOG_TAG, ex.toString());
            						Log.e(AppConst.LOG_TAG, "========================================");
            					}
            				});
            				
            				
            				/*
                        	 * 앱 시나리오에 맞는 UI 를 구성하시면 됩니다.
                        	 * Demo 앱에서는 Sns 정보를 객체에 담아 MainActivity 로 화면 전환을 하고 있습니다.
                        	 */
            				SnsUser snsUser = new SnsUser();
            				snsUser.setName(user.getName());
            				snsUser.setSns_id(user.getId());
            				snsUser.setAccess_token(Session.getActiveSession().getAccessToken());
            				snsUser.setId_type("sns");
            				snsUser.setSns_type("FB");
            				snsUser.setProfile_image("https://graph.facebook.com/" + user.getId() + "/picture?type=large");
            				
            				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            				intent.putExtra("snsUser", snsUser);
            				startActivity(intent);
            				
                        }
                    }
                });
			}
		}
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (Session.getActiveSession() != null) {
			Log.i("Connect-Demo",
					"Session.getActiveSession().onActivityResult()");

			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
		}
		
		mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
	}
	
	public void showLoading(boolean show) {

		LinearLayout layout_login = (LinearLayout) findViewById(R.id.layout_login);
		RelativeLayout layout_loading = (RelativeLayout) findViewById(R.id.layout_loading);

		if (show) {

			layout_login.setVisibility(View.INVISIBLE);
			layout_loading.setVisibility(View.VISIBLE);

		} else {

			layout_login.setVisibility(View.VISIBLE);
			layout_loading.setVisibility(View.INVISIBLE);
		}
	}

	public void rotateAnimation(int duration, View view) {

		final RotateAnimation animRotate = new RotateAnimation(0.0f, 360.0f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);

		animRotate.setDuration(duration);
		animRotate.setFillAfter(true);
		animRotate.setRepeatMode(Animation.INFINITE);
		animRotate.setRepeatCount(Animation.INFINITE);

		view.startAnimation(animRotate);

	}
	
}
