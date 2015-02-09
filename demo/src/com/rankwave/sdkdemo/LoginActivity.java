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
		
		Connect.sdkInitialize(getApplicationContext(), new ConnectCallback<ConnectSession>() {
			@Override
        	public void onSuccess(ConnectSession connectSession){
				if(connectSession.getConnectSessionState() == ConnectSessionState.READY) {
	                Log.i(AppConst.LOG_TAG, "SDK Initialized.");
	                Log.i(AppConst.LOG_TAG, "ConnectSessionState :: " + connectSession.getConnectSessionState());
	                
	                Connect.anonymousLogin(new ConnectCallback<ConnectSession>(){
	                	@Override
	                	public void onSuccess(ConnectSession connectSession){
	                		Log.i(AppConst.LOG_TAG, "AnonymousLogin Success.");
	                		Log.i(AppConst.LOG_TAG, "SessionState :: " + connectSession.getConnectSessionState());
	                		Log.i(AppConst.LOG_TAG, "connect_token :: " + connectSession.getConnect_token());
	                		Log.i(AppConst.LOG_TAG, "user :: " + connectSession.getUser().toString());
	                	}
	                	
	                	@Override
	                	public void onFail(FuncResult funcResult, Exception exception){
	                		Log.i(AppConst.LOG_TAG, "anonymousLogin Fail. : " + exception.toString());
	                	}
	                });
	                
	            } else {
	                Log.e(AppConst.LOG_TAG, "Failed to initialize SDK.");
	            }
			}
			
			@Override
        	public void onFail(FuncResult funcResult, Exception exception){
        		Log.i(AppConst.LOG_TAG, "Failed to initialize SDK. : " + exception.toString());
        	}
		});
		
		Button btn_login_facebook = (Button) findViewById(R.id.btn_facebook_login);

		btn_login_facebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				showLoading(true);
				
				List<String> permissions = Arrays.asList("user_about_me",
						"user_activities", "user_birthday", "user_checkins",
						"user_education_history", "user_groups",
						"user_hometown", "user_interests", "user_likes",
						"user_photos", "user_status", "email",
						"user_work_history", "friends_about_me",
						"friends_birthday", "friends_activities",
						"friends_likes", "friends_photos", "read_friendlists",
						"read_stream", "user_location", "user_relationships",
						"user_subscriptions", "friends_location",
						"friends_education_history", "friends_relationships");
				
				Connect.facebookLogin(LoginActivity.this, permissions, true, new ConnectCallback<ConnectSession>() {

					@Override
					public void onSuccess(ConnectSession connectSession) {
						Log.i(AppConst.LOG_TAG, "facebookLogin Success");
						
						User user = connectSession.getUser();
						Log.i(AppConst.LOG_TAG, user.toString());			
						if(user.getJoined()){
							goMainActivity();
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									showLoading(false);											
								}
							}, 1000);
						}else{
							Connect.join(null, new ConnectCallback<ConnectSession>(){
								@Override
								public void onSuccess(ConnectSession connectSession){
									Log.i(AppConst.LOG_TAG, "join success");
									
									goMainActivity();
									
									new Handler().postDelayed(new Runnable() {
										
										@Override
										public void run() {
											showLoading(false);											
										}
									}, 1000);
								}
								
								@Override
								public void onFail(FuncResult funcResult, Exception exception){
									Log.i(AppConst.LOG_TAG, "join fail :: " + funcResult);
									Log.i(AppConst.LOG_TAG, "join fail :: " + exception.getMessage());
								}
							});
						}
					}
					
					@Override
					public void onFail(FuncResult funcResult, Exception exception) {
						Log.i(AppConst.LOG_TAG, "callback fail :: " + exception.getMessage());
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
				
				Connect.twitterLogin(LoginActivity.this, true, new ConnectCallback<ConnectSession>() {

					@Override
					public void onSuccess(ConnectSession connectSession) {
						Log.i(AppConst.LOG_TAG, "twitterLogin Success");
						
						User user = connectSession.getUser();
						
						if(user.getJoined()){
							goMainActivity();
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									showLoading(false);											
								}
							}, 1000);
						}else{
							Connect.join(null, new ConnectCallback<ConnectSession>(){
								@Override
								public void onSuccess(ConnectSession connectSession){
									Log.i(AppConst.LOG_TAG, "join success");
									
									goMainActivity();
									
									new Handler().postDelayed(new Runnable() {
										
										@Override
										public void run() {
											showLoading(false);											
										}
									}, 1000);
								}
								
								@Override
								public void onFail(FuncResult funcResult, Exception exception){
									Log.i(AppConst.LOG_TAG, "join fail :: " + funcResult);
									Log.i(AppConst.LOG_TAG, "join fail :: " + exception.getMessage());
								}
							});
						}
					}
					
					@Override
					public void onFail(FuncResult funcResult, Exception exception) {
						Log.i(AppConst.LOG_TAG, "callback fail :: " + exception.getMessage());
						showLoading(false);
					}
				});
			}
		});
		
		Button btn_facebook__profile = (Button) findViewById(R.id.btn_facebook__profile_login);
		btn_facebook__profile.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				showLoading(true);

				List<String> permissions = Arrays.asList("user_about_me",
						"user_activities", "user_birthday", "user_checkins",
						"user_education_history", "user_groups",
						"user_hometown", "user_interests", "user_likes",
						"user_photos", "user_status", "email",
						"user_work_history", "friends_about_me",
						"friends_birthday", "friends_activities",
						"friends_likes", "friends_photos", "read_friendlists",
						"read_stream", "user_location", "user_relationships",
						"user_subscriptions", "friends_location",
						"friends_education_history", "friends_relationships");
				
				Connect.facebookLogin(LoginActivity.this, permissions, false, new ConnectCallback<ConnectSession>() {

					@Override
					public void onSuccess(ConnectSession connectSession) {
						Log.i(AppConst.LOG_TAG, "facebookLogin Success");
						
						User user = connectSession.getUser();
						
						if(user.getJoined()){
							goMainActivity();
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									showLoading(false);											
								}
							}, 1000);
							
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
						Log.i(AppConst.LOG_TAG, "callback fail :: " + exception.getMessage());
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
				
				Connect.twitterLogin(LoginActivity.this, false, new ConnectCallback<ConnectSession>() {

					@Override
					public void onSuccess(ConnectSession connectSession) {
						Log.i(AppConst.LOG_TAG, "twitterLogin Success");
						
						User user = connectSession.getUser();
						
						if(user.getJoined()){
							goMainActivity();
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									showLoading(false);											
								}
							}, 1000);
							
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
						Log.i(AppConst.LOG_TAG, "callback fail :: " + exception.getMessage());
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
		Connect.setGCMRegistrationId(new ConnectCallback<ConnectSession>(){
			@Override
			public void onSuccess(ConnectSession connectSession){
				Log.i(AppConst.LOG_TAG, "registerGCMRegistrationId Success");
			}
			
			@Override
			public void onFail(FuncResult funcResult, Exception exception){
				Log.i(AppConst.LOG_TAG, "registerGCMRegistrationId fail :: " + exception.getMessage());
			}
			
		});
		
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
	}

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
