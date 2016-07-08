package com.rankwave.sdkdemo;

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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
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

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends Activity {
	private CallbackManager callbackManager;

	TwitterAuthClient mTwitterAuthClient;

	private SessionCallback callback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		FacebookSdk.sdkInitialize(LoginActivity.this);

		setContentView(R.layout.login);

/*
		DemoApplication.setCurrentActivity(LoginActivity.this);
*/
		callback = new SessionCallback();
		Session.getCurrentSession().addCallback(callback);
		Session.getCurrentSession().checkAndImplicitOpen();



		/*
		 * SDK 내부적으로 필요한 리소스를 초기화 하고, SDK 를 이용할 수 있게 준비하는 작업을 수행합니다.
		 */
		Connect.sdkInitialize(getApplicationContext(), new ConnectCallback<ConnectSession>() {
			@Override
			public void onSuccess(ConnectSession connectSession) {
				Log.i("Connect", "========================================");
				Log.i("Connect", "SDK Initialized.");
				Log.i("Connect", "========================================");

				Connect.pushWebLink();
			}

			@Override
			public void onFail(FuncResult funcResult, Exception exception) {
				Log.e("Connect", "========================================");
				Log.e("Connect", "sdkInitialize Fail.");
				Log.e("Connect", "----------------------------------------");
				Log.e("Connect", funcResult.toString() + " : " + exception.toString());
				Log.e("Connect", "========================================");
			}
		});
		
		
		Button btn_login_facebook = (Button) findViewById(R.id.btn_facebook_login);
		
		btn_login_facebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLoading(true);

				callbackManager = CallbackManager.Factory.create();

				List<String> permissions = Arrays.asList(
						"public_profile", "email", "user_friends");
				LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, permissions);

				LoginManager.getInstance().registerCallback(callbackManager,
						new FacebookCallback<LoginResult>() {
							@Override
							public void onSuccess(LoginResult loginResult) {
								// App code
							/*
                        	 * facebook의 accesstoken 및 id 를 얻은 후 Connect SDK 의 facebookLogin을 호출 하면 됩니다.
                        	 */
								String facebook_access_token = loginResult.getAccessToken().getToken();
								String id = loginResult.getAccessToken().getUserId();

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
								new GraphRequest(
										AccessToken.getCurrentAccessToken(),
										id,
										null,
										HttpMethod.GET,
										new GraphRequest.Callback() {
											public void onCompleted(GraphResponse response) {
           										 /* handle the result */
												try {
													JSONObject jsonObject = response.getJSONObject();
													SnsUser snsUser = new SnsUser();
													snsUser.setName(jsonObject.getString("name"));
													snsUser.setSns_id(jsonObject.getString("id"));
													snsUser.setAccess_token(AccessToken.getCurrentAccessToken().getToken());
													snsUser.setId_type("sns");
													snsUser.setSns_type("FB");
													snsUser.setProfile_image("https://graph.facebook.com/" + jsonObject.getString("id") + "/picture?type=large");

													Intent intent = new Intent(LoginActivity.this, MainActivity.class);
													intent.putExtra("snsUser", snsUser);
													startActivity(intent);

												} catch (Exception e) {
													e.printStackTrace();
												}

											}
										}
								).executeAsync();
							}

							@Override
							public void onCancel() {
								// App code
							}

							@Override
							public void onError(FacebookException exception) {
								// App code

							}
						});


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
						String id = String.valueOf(session.getId());

						Connect.twitterLogin(id, token, secret, new ConnectCallback<ConnectSession>() {
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



//		Button btn_login_kakao = (Button) findViewById(R.id.btn_kakao_login);
//
//		btn_login_kakao.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				showLoading(true);
//
//				DemoApplication.setCurrentActivity(LoginActivity.this);
//				callback = new SessionCallback();
//				Session.getCurrentSession().addCallback(callback);
//				Session.getCurrentSession().checkAndImplicitOpen();
//
//				showLoading(false);
//			}
//		});

		showLoading(false);

		ImageView iv_loading = (ImageView) findViewById(R.id.iv_loading);
		rotateAnimation(1000, iv_loading);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Session.getCurrentSession().removeCallback(callback);
	}

	private class SessionCallback implements ISessionCallback {

		@Override
		public void onSessionOpened() {
			UserManagement.requestMe(new MeResponseCallback() {
				@Override
				public void onSuccess(UserProfile userProfile) {
					String id = String.valueOf(userProfile.getId());

					String token = Session.getCurrentSession().getAccessToken();
					String refreshToken = Session.getCurrentSession().getRefreshToken();

					Connect.kakaoLogin(id, token, refreshToken, new ConnectCallback<ConnectSession>() {
						@Override
						public void onSuccess(ConnectSession connectSession) {
							Log.i(AppConst.LOG_TAG, "========================================");
							Log.i(AppConst.LOG_TAG, "kakaoLogin Success.");
							Log.i(AppConst.LOG_TAG, "========================================");
						}

						@Override
						public void onFail(FuncResult funcResult, Exception ex) {
							Log.e(AppConst.LOG_TAG, "========================================");
							Log.e(AppConst.LOG_TAG, "kakaoLogin Fail.");
							Log.e(AppConst.LOG_TAG, "----------------------------------------");
							Log.e(AppConst.LOG_TAG, ex.toString());
							Log.e(AppConst.LOG_TAG, "========================================");
						}
					});


					SnsUser snsUser = new SnsUser();
					snsUser.setName(userProfile.getNickname());
					snsUser.setSns_id(String.valueOf(userProfile.getId()));
					snsUser.setAccess_token(token);
					snsUser.setToken_secret("");
					snsUser.setId_type("sns");
					snsUser.setSns_type("KO");
					snsUser.setProfile_image(userProfile.getProfileImagePath());

					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					intent.putExtra("snsUser", snsUser);
					startActivity(intent);
				}

				@Override
				public void onFailure(ErrorResult errorResult) {

				}

				@Override
				public void onSessionClosed(ErrorResult errorResult) {

				}

				@Override
				public void onNotSignedUp() {

				}
			});



		}

		@Override
		public void onSessionOpenFailed(KakaoException exception) {
			if(exception != null) {
				Logger.e(exception);
			}
		}
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(callbackManager != null)
			callbackManager.onActivityResult(requestCode, resultCode, data);

		if(mTwitterAuthClient != null){
			mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
		}

		if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
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
