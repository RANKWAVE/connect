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
import com.rankwave.connect.sdk.Session;
import com.rankwave.connect.sdk.SessionState;
import com.rankwave.connect.sdk.User;

public class LoginActivity extends Activity {

	public static final int    REQUEST_CODE_JOIN = 1000;
	public static final int    REQUEST_CODE_EMAIL_LOGIN = 1001;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);
		
		Connect.sdkInitialize(getApplicationContext(), new ConnectCallback<Session>() {
			@Override
        	public void onSuccess(Session session){
				if(session.getState() == SessionState.READY) {
	                Log.i(AppConst.LOG_TAG, "SDK Initialized.");
	                Log.i(AppConst.LOG_TAG, "SessionState :: " + session.getState());
	                
	                Connect.anonymousLogin(new ConnectCallback<Session>(){
	                	@Override
	                	public void onSuccess(Session session){
	                		Log.i(AppConst.LOG_TAG, "AnonymousLogin Success.");
	                		Log.i(AppConst.LOG_TAG, "SessionState :: " + session.getState());
	                		Log.i(AppConst.LOG_TAG, "connect_token :: " + session.getConnect_token());
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
				
				Connect.facebookLogin(LoginActivity.this, new ConnectCallback<Session>() {

					@Override
					public void onSuccess(Session session) {
						Log.i(AppConst.LOG_TAG, "facebookLogin Success");
						
						User user = session.getUser();
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
							Connect.join(null, new ConnectCallback<Session>(){
								@Override
								public void onSuccess(Session session){
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
				
				Connect.twitterLogin(LoginActivity.this, new ConnectCallback<Session>() {

					@Override
					public void onSuccess(Session session) {
						Log.i(AppConst.LOG_TAG, "twitterLogin Success");
						
						User user = session.getUser();
						
						if(user.getJoined()){
							goMainActivity();
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									showLoading(false);											
								}
							}, 1000);
						}else{
							Connect.join(null, new ConnectCallback<Session>(){
								@Override
								public void onSuccess(Session session){
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

				Connect.facebookLogin(LoginActivity.this, new ConnectCallback<Session>() {

					@Override
					public void onSuccess(Session session) {
						Log.i(AppConst.LOG_TAG, "facebookLogin Success");
						
						User user = session.getUser();
						
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
				
				Connect.twitterLogin(LoginActivity.this, new ConnectCallback<Session>() {

					@Override
					public void onSuccess(Session session) {
						Log.i(AppConst.LOG_TAG, "twitterLogin Success");
						
						User user = session.getUser();
						
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
		
		
		String payload = getIntent().getStringExtra(Connect.INTENT_PUSH_PAYLOAD);
	    if (payload != null) {
	          Log.d(AppConst.LOG_TAG, "Connect push notification with payload " + payload);
	    }
	}
	
	
	public void goMainActivity(){
		Connect.registerGCMRregistrationId(new ConnectCallback<Session>(){
			@Override
			public void onSuccess(Session session){
				Log.i(AppConst.LOG_TAG, "registerGCMRregistrationId Success");
			}
			
			@Override
			public void onFail(FuncResult funcResult, Exception exception){
				Log.i(AppConst.LOG_TAG, "registerGCMRregistrationId fail :: " + exception.getMessage());
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
	
	
	 @Override
	  protected void onNewIntent(Intent intent) {
	      super.onNewIntent(intent);
	      String payload = getIntent().getStringExtra(Connect.INTENT_PUSH_PAYLOAD);
	      if (payload != null) {
	          Log.d(AppConst.LOG_TAG, "Connect push notification with payload " + payload);
	      }
	  }
}
