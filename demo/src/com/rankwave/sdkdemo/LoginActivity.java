package com.rankwave.sdkdemo;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.rankwave.connect.sdk.Connect;
import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.ConnectSession;
import com.rankwave.connect.sdk.ConnectSessionState;
import com.rankwave.connect.sdk.User;

public class LoginActivity extends Activity {

	public static final int    REQUEST_CODE_JOIN = 1000;
	public static final int    REQUEST_CODE_EMAIL_LOGIN = 1001;
	
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
				
				/*
				 * SDK 를 이용할 수 있는 준비가 되었고 로그인 이벤트 시 SDK 에서 제공하는 facebookLogin,twitterLogin,anonymousLogin 을 이용하시면 됩니다.
				 */
				if(connectSession.getConnectSessionState() == ConnectSessionState.READY){
					Log.i(AppConst.LOG_TAG, "========================================");
	                Log.i(AppConst.LOG_TAG, "sdkInitialize success.");
	                Log.i(AppConst.LOG_TAG, "----------------------------------------");
	                Log.i(AppConst.LOG_TAG, "ConnectSessionState :: " + connectSession.getConnectSessionState());
	                Log.i(AppConst.LOG_TAG, "user :: " + connectSession.getUser().toString());
	                Log.i(AppConst.LOG_TAG, "========================================");
                /*
				 * SDK 에서 제공하는 Login시 sessionSaveFlag를 true로 설정 하였을 경우 다음 앱 실행 시 ConnectSessionState가 OPENED가 됩니다.(마지막으로 로그인한 Session 으로 처리됩니다.)
				 */
				}else if(connectSession.getConnectSessionState() == ConnectSessionState.OPENED){
					Log.i(AppConst.LOG_TAG, "========================================");
	                Log.i(AppConst.LOG_TAG, "sdkInitialize success.");
	                Log.i(AppConst.LOG_TAG, "----------------------------------------");
	                Log.i(AppConst.LOG_TAG, "ConnectSessionState :: " + connectSession.getConnectSessionState());
	                Log.i(AppConst.LOG_TAG, "user :: " + connectSession.getUser().toString());
	                Log.i(AppConst.LOG_TAG, "========================================");
	                
	                /*
					 * Login시 sessionSaveFlag를 true로 설정 하였을 시 다음 앱 실행 시 ConnectSessionState가 OPENED가 된경우 다음 앱 실행 시 Login 호출을 다시 안하는 경우(자동로그인 시)
					 * 즉, 가입시만 소셜 로그인을 처리 하는 경우 connectLogin 을 호출 하여 Login 되었다는 통계를 수집하기 위해 사용됩니다.
					 * 앱 실행시마다 소셜로그인을 호출하는 경우는 connectLogin 을 호출 안하셔도 됩니다.
					 */
					Connect.connectLogin(new ConnectCallback<ConnectSession>(){
	                	@Override
	                	public void onSuccess(ConnectSession connectSession){
	                		Log.i(AppConst.LOG_TAG, "========================================");
	                		Log.i(AppConst.LOG_TAG, "connectLogin Success.");
	                		Log.i(AppConst.LOG_TAG, "----------------------------------------");
	                		Log.i(AppConst.LOG_TAG, "SessionState :: " + connectSession.getConnectSessionState());
	                		Log.i(AppConst.LOG_TAG, "connect_token :: " + connectSession.getConnect_token());
	                		Log.i(AppConst.LOG_TAG, "user :: " + connectSession.getUser().toString());
	                		Log.i(AppConst.LOG_TAG, "========================================");
	                	}
	                	
	                	@Override
	                	public void onFail(FuncResult funcResult, Exception exception){
	                		Log.e(AppConst.LOG_TAG, "========================================");
	                		Log.e(AppConst.LOG_TAG, "connectLogin Fail.");
	                		Log.e(AppConst.LOG_TAG, "----------------------------------------");
	                		Log.e(AppConst.LOG_TAG, exception.toString());
	                		Log.e(AppConst.LOG_TAG, "========================================");
	                	}
	                });
				}else{
					Log.e(AppConst.LOG_TAG, "========================================");
	                Log.e(AppConst.LOG_TAG, "sdkInitialize Exception.");
	                Log.e(AppConst.LOG_TAG, "----------------------------------------");
	                Log.e(AppConst.LOG_TAG, "ConnectSessionState :: " + connectSession.getConnectSessionState());
	                Log.e(AppConst.LOG_TAG, "========================================");
				}
			}
			
			@Override
        	public void onFail(FuncResult funcResult, Exception exception){
				Log.e(AppConst.LOG_TAG, "========================================");
        		Log.e(AppConst.LOG_TAG, "sdkInitialize Fail.");
        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
        		Log.e(AppConst.LOG_TAG, exception.toString());
        		Log.e(AppConst.LOG_TAG, "========================================");
        	}
		});
		
		
		
		Button btn_login_facebook = (Button) findViewById(R.id.btn_facebook_login);
		
		btn_login_facebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				showLoading(true);
				
				/*
				 * permissions 변수는 페이스북에서 얻고자 하는 권한을 지정하는 변수입니다. 앱에서 필요로 하는 권한으로 수정 하시면 됩니다.
				 * 2014/04/30 이후에 생성된 App 의 경우, Facebook 의 승인을 얻지 못한 권한은 사용자에게 권한 승인 요청되지 않습니다. 
				 * 또한, 2015/04/30 이후 부터는 모든 Facebook App 이 기본 권한을 제외한 권한에 대해서는 Facebook 의 승인을 받아야만 합니다.
				 */
				List<String> permissions = Arrays.asList(
						"user_birthday", "user_photos", "user_status", "email");
				
				/*
				 * Facebook 로그인과 관련된 UI 를 구현하고, 로그인에 대한 Event handler 에서 facebookLogin 을 호출합니다.
				 * Activity activity : 현재 자신의 Activity
				 * List<String> permissions : 페이스북에서 얻고자 하는 변수
				 * Boolean sessionSaveFlag : 회원가입시만 로그인 하는 경우 ConnectSession을 저장하고 앱 실행시 connectLogin 을 호출 하여 로그인 통계를 수집할 경우 true 로 설정
				 * Boolean autoJoinFlag : 자동 회원가입을 하고자 할 경우 true로 설정하고 false 로 설정 할 시 user객체의 joined(user.getJoined)가 false 인 경우 별도 join 함수를 호출 하여야 합니다.하단(btn_facebook_profile 참고)
				 * ConnectCallback<ConnectSession> connectCallback
				 */
				Connect.facebookLogin(LoginActivity.this, permissions, true, true, new ConnectCallback<ConnectSession>() {
					@Override
					public void onSuccess(ConnectSession connectSession) {						
						User user = connectSession.getUser();
						
						Log.i(AppConst.LOG_TAG, "========================================");
		        		Log.i(AppConst.LOG_TAG, "facebookLogin Success.");
		        		Log.i(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.i(AppConst.LOG_TAG, user.toString());
		        		Log.i(AppConst.LOG_TAG, "========================================");
						
						/*
						 * 데모 프로젝트에서는 로그인 성공시 아래와 같이 Loading 처리 후 MainActivity 로 보내고 있습니다. 서비스에 맞게 처리 하시면 됩니다.
						 */
						goMainActivity();
						
						new Handler().postDelayed(new Runnable() {
							
							@Override
							public void run() {
								showLoading(false);											
							}
						}, 1000);
					}
					
					@Override
					public void onFail(FuncResult funcResult, Exception exception) {
						Log.e(AppConst.LOG_TAG, "========================================");
		        		Log.e(AppConst.LOG_TAG, "facebookLogin Fail.");
		        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.e(AppConst.LOG_TAG, exception.toString());
		        		Log.e(AppConst.LOG_TAG, "========================================");
						showLoading(false);
					}
				});
			}

		});
		

		Button btn_login_twitter = (Button) findViewById(R.id.btn_twitter_login);
		btn_login_twitter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				showLoading(true);
				
				/*
				 * Twitter 로그인과 관련된 UI 를 구현하고, 로그인에 대한 Event handler 에서 twitterLogin 을 호출합니다.
				 * Activity activity : 현재 자신의 Activity
				 * Boolean sessionSaveFlag : 회원가입시만 로그인 하는 경우 ConnectSession을 저장하고 앱 실행시 connectLogin 을 호출 하여 로그인 통계를 수집할 경우 true 로 설정
				 * Boolean autoJoinFlag : 자동 회원가입을 하고자 할 경우 true로 설정하고 false 로 설정 할 시 user객체의 joined(user.getJoined)가 false 인 경우 별도 join 함수를 호출 하여야 합니다.하단(btn_twitter_profile 참고)
				 * ConnectCallback<ConnectSession> connectCallback
				 */
				Connect.twitterLogin(LoginActivity.this, true, true, new ConnectCallback<ConnectSession>() {

					@Override
					public void onSuccess(ConnectSession connectSession) {
						User user = connectSession.getUser();
						
						Log.i(AppConst.LOG_TAG, "========================================");
		        		Log.i(AppConst.LOG_TAG, "twitterLogin Success.");
		        		Log.i(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.i(AppConst.LOG_TAG, user.toString());
		        		Log.i(AppConst.LOG_TAG, "========================================");
		        		
		        		/*
						 * 데모 프로젝트에서는 로그인 성공시 아래와 같이 Loading 처리 후 MainActivity 로 보내고 있습니다. 서비스에 맞게 처리 하시면 됩니다.
						 */
		        		goMainActivity();
						
						new Handler().postDelayed(new Runnable() {
							
							@Override
							public void run() {
								showLoading(false);											
							}
						}, 1000);
						
					}
					
					@Override
					public void onFail(FuncResult funcResult, Exception exception) {
						Log.e(AppConst.LOG_TAG, "========================================");
		        		Log.e(AppConst.LOG_TAG, "twitterLogin Fail.");
		        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.e(AppConst.LOG_TAG, exception.toString());
		        		Log.e(AppConst.LOG_TAG, "========================================");
		        		
						showLoading(false);
					}
				});
			}
		});
		
		Button btn_facebook_profile = (Button) findViewById(R.id.btn_facebook_profile_login);
		btn_facebook_profile.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				showLoading(true);

				/*
				 * permissions 변수는 페이스북에서 얻고자 하는 권한을 지정하는 변수입니다. 앱에서 필요로 하는 권한으로 수정 하시면 됩니다.
				 * 2014/04/30 이후에 생성된 App 의 경우, Facebook 의 승인을 얻지 못한 권한은 사용자에게 권한 승인 요청되지 않습니다. 
				 * 또한, 2015/04/30 이후 부터는 모든 Facebook App 이 기본 권한을 제외한 권한에 대해서는 Facebook 의 승인을 받아야만 합니다.
				 */
				List<String> permissions = Arrays.asList(
						"user_birthday", "user_photos", "user_status", "email");
				
				/*
				 * Facebook 로그인과 관련된 UI 를 구현하고, 로그인에 대한 Event handler 에서 facebookLogin 을 호출합니다.
				 * Activity activity : 현재 자신의 Activity
				 * List<String> permissions : 페이스북에서 얻고자 하는 변수
				 * Boolean sessionSaveFlag : 회원가입시만 로그인 하는 경우 ConnectSession을 저장하고 앱 실행시 connectLogin 을 호출 하여 로그인 통계를 수집할 경우 true 로 설정
				 * Boolean autoJoinFlag : 약관동의 또는 다른 프로필 정보를 받고 회원가입을 원할 경우 false 로 설정. user객체의 joined(user.getJoined)가 false 인 경우 별도 join 함수를 호출 하여야 합니다.
				 * ConnectCallback<ConnectSession> connectCallback
				 */
				Connect.facebookLogin(LoginActivity.this, permissions, true, false, new ConnectCallback<ConnectSession>() {

					@Override
					public void onSuccess(ConnectSession connectSession) {
						User user = connectSession.getUser();
						
						Log.i(AppConst.LOG_TAG, "========================================");
		        		Log.i(AppConst.LOG_TAG, "facebookLogin Success.");
		        		Log.i(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.i(AppConst.LOG_TAG, user.toString());
		        		Log.i(AppConst.LOG_TAG, "========================================");
		        		
		        		/*
		        		 * 데모 프로젝트에서는 로그인 성공시 회원가입이 되어 있는 경우 Loading 처리 후 MainActivity 로 보내고 있습니다. 서비스에 맞게 처리 하시면 됩니다.
		        		 */
						if(user.getJoined()){
							goMainActivity();
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									showLoading(false);											
								}
							}, 1000);
							
						/*
		        		 * 데모 프로젝트에서는 로그인 성공시 회원가입이 안되어 있는 경우 프로필 정보를 받기 위한 InputProfileActivity 로 보내고 있습니다. 
		        		 * InputProfileActivity 에 프로필정보를 받은 후 join 및 profile update 로직이 있으니 참고 하세요.
		        		 */
						}else{
							Intent intent = new Intent(LoginActivity.this, InputProfileActivity.class);
							intent.putExtra("requestCode", InputProfileActivity.REQUEST_CODE_JOIN);
							
							startActivity(intent);
							
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									showLoading(false);											
								}
							}, 1000);
						}
					}
					
					@Override
					public void onFail(FuncResult funcResult, Exception exception) {
						Log.e(AppConst.LOG_TAG, "========================================");
		        		Log.e(AppConst.LOG_TAG, "facebookLogin Fail.");
		        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.e(AppConst.LOG_TAG, exception.toString());
		        		Log.e(AppConst.LOG_TAG, "========================================");
		        		
						showLoading(false);
					}
				});
			}
		});
		
		Button btn_twitter_profile = (Button) findViewById(R.id.btn_twitter_profile_login);
		btn_twitter_profile.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showLoading(true);
				
				/*
				 * Twitter 로그인과 관련된 UI 를 구현하고, 로그인에 대한 Event handler 에서 twitterLogin 을 호출합니다.
				 * Activity activity : 현재 자신의 Activity
				 * Boolean sessionSaveFlag : 회원가입시만 로그인 하는 경우 ConnectSession을 저장하고 앱 실행시 connectLogin 을 호출 하여 로그인 통계를 수집할 경우 true 로 설정 
				 * Boolean autoJoinFlag : 약관동의 또는 다른 프로필 정보를 받고 회원가입을 원할 경우 false 로 설정. user객체의 joined(user.getJoined)가 false 인 경우 별도 join 함수를 호출 하여야 합니다.
				 * ConnectCallback<ConnectSession> connectCallback
				 */
				Connect.twitterLogin(LoginActivity.this, true, false, new ConnectCallback<ConnectSession>() {

					@Override
					public void onSuccess(ConnectSession connectSession) {
						User user = connectSession.getUser();
						
						Log.i(AppConst.LOG_TAG, "========================================");
		        		Log.i(AppConst.LOG_TAG, "twitterLogin Success.");
		        		Log.i(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.i(AppConst.LOG_TAG, user.toString());
		        		Log.i(AppConst.LOG_TAG, "========================================");
		        		
		        		/*
		        		 * 데모 프로젝트에서는 로그인 성공시 회원가입이 되어 있는 경우 Loading 처리 후 MainActivity 로 보내고 있습니다. 서비스에 맞게 처리 하시면 됩니다.
		        		 */
						if(user.getJoined()){
							goMainActivity();
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									showLoading(false);											
								}
							}, 1000);
							
						/*
		        		 * 데모 프로젝트에서는 로그인 성공시 회원가입이 안되어 있는 경우 프로필 정보를 받기 위한 InputProfileActivity 로 보내고 있습니다. 
		        		 * InputProfileActivity 에 프로필정보를 받은 후 join 및 profile update 로직이 있으니 참고 하세요.
		        		 */
						}else{
							Intent intent = new Intent(LoginActivity.this, InputProfileActivity.class);
							intent.putExtra("requestCode", InputProfileActivity.REQUEST_CODE_JOIN);
							
							startActivity(intent);
							
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									showLoading(false);											
								}
							}, 1000);
						}
					}
					
					@Override
					public void onFail(FuncResult funcResult, Exception exception) {
						Log.e(AppConst.LOG_TAG, "========================================");
		        		Log.e(AppConst.LOG_TAG, "twitterLogin Fail.");
		        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.e(AppConst.LOG_TAG, exception.toString());
		        		Log.e(AppConst.LOG_TAG, "========================================");
		        		
						showLoading(false);
					}
				});
			}
		});

		
		Button btn_login_anonymous = (Button) findViewById(R.id.btn_anonymous_login);
		
		btn_login_anonymous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				showLoading(true);
				
				/*
				 * 무인증 서비스를 위해, 제공되는 로그인 함수입니다. Device ID 로 사용자를 구분할 수 있게 해줍니다.
				 * Boolean sessionSaveFlag : 회원가입시만 로그인 하는 경우 ConnectSession을 저장하고 앱 실행시 connectLogin 을 호출 하여 로그인 통계를 수집할 경우 true 로 설정
				 * ConnectCallback<ConnectSession> connectCallback
				 */
				Connect.anonymousLogin(true, new ConnectCallback<ConnectSession>() {
					@Override
					public void onSuccess(ConnectSession connectSession) {						
						User user = connectSession.getUser();
						
						Log.i(AppConst.LOG_TAG, "========================================");
		        		Log.i(AppConst.LOG_TAG, "anonymousLogin Success.");
		        		Log.i(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.i(AppConst.LOG_TAG, user.toString());
		        		Log.i(AppConst.LOG_TAG, "========================================");
						
		        		/*
		        		 * 데모에서는 dialog 를 보여주고 있습니다. 서비스에 맞게 처리 하시면 됩니다.
		        		 */
						CommonAlertDialog.showDefaultDialog(LoginActivity.this,
								"anonymousLogin", "anonymousLogin success", "OK", null);
		        		
						showLoading(false);
					}
					
					@Override
					public void onFail(FuncResult funcResult, Exception exception) {
						Log.e(AppConst.LOG_TAG, "========================================");
		        		Log.e(AppConst.LOG_TAG, "anonymousLogin Fail.");
		        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
		        		Log.e(AppConst.LOG_TAG, exception.toString());
		        		Log.e(AppConst.LOG_TAG, "========================================");
						showLoading(false);
					}
				});
			}

		});
		
		showLoading(false);

		ImageView iv_loading = (ImageView) findViewById(R.id.iv_loading);
		rotateAnimation(1000, iv_loading);
		
	}
	
	
	public void goMainActivity(){
		/*
		 * Push 전송을 위해, 현재 Device 에 할당된 GCM registration ID 를 Connect 시스템에 등록합니다.
		 * 데모 프로젝트에서는 MainActivity로 이동할때 setGCMRegistrationId 를 호출 하고 있습니다.
		 * 등록 후 각 서비스에 따라 Push 전송을 안받아야 할 경우 설정에서 Push 해제 or Logout 시 안받고 싶은 경우에는
		 * unsetGCMRegistrationId 함수를 호출하면 됩니다.
		 */
		Connect.setGCMRegistrationId(new ConnectCallback<ConnectSession>(){
			@Override
			public void onSuccess(ConnectSession connectSession){
				Log.i(AppConst.LOG_TAG, "========================================");
        		Log.i(AppConst.LOG_TAG, "setGCMRegistrationId Success.");        		
        		Log.i(AppConst.LOG_TAG, "========================================");
			}
			
			@Override
			public void onFail(FuncResult funcResult, Exception exception){
				Log.e(AppConst.LOG_TAG, "========================================");
        		Log.e(AppConst.LOG_TAG, "setGCMRegistrationId Fail.");
        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
        		Log.e(AppConst.LOG_TAG, exception.toString());
        		Log.e(AppConst.LOG_TAG, "========================================");
			}
			
		});
		
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
	}

	/*
	 * SDK 에서 제공하는 social Login을 사용하는 경우에만 아래 함수를 Overriding 하여 아래 코드를 추가해 주어야 합니다.
	 * 자체 소셜로그인을 사용하여 token 을 넘기는 경우는 코드를 추가 안해도 된다. 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Connect.onActivityResult(this, requestCode, resultCode, data);
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
