package com.rankwave.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.rankwave.connect.sdk.Connect;
import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.ConnectSession;
import com.rankwave.connect.sdk.Profile;

public class InputProfileActivity extends Activity{

	public static final int REQUEST_CODE_JOIN = 1000;
	public static final int REQUEST_CODE_UPDATE = 1001;
	
	EditText edit_email;
	EditText edit_name;
	EditText edit_birthday;
	
	int requestCode;
	
	public InputProfileActivity() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.input_profile);
		
		Intent receivedItent = getIntent();		
		requestCode = receivedItent.getIntExtra("requestCode", 0);
		
		edit_email = (EditText)findViewById(R.id.edit_email);
		edit_name = (EditText)findViewById(R.id.edit_name);
		edit_birthday = (EditText)findViewById(R.id.edit_birthday);

		if(requestCode == REQUEST_CODE_JOIN){			
			//got to set Connect(SNS Login)
			edit_name.setText(ConnectSession.getInstance().getUser().getSnsInfo().getName());
			edit_birthday.setText(ConnectSession.getInstance().getUser().getSnsInfo().getBirthday());
			
		}else if(requestCode == REQUEST_CODE_UPDATE){
			
		}
		
		Button btn_ok = (Button)findViewById(R.id.btn_ok);
		btn_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String email = edit_email.getText().toString();
				String name = edit_name.getText().toString();
				String birthday = edit_birthday.getText().toString();
				
				Profile profile = new Profile();
				profile.setEmail(email);
				profile.setBirthday(birthday);
				profile.setName(name);

				/*
				//Residence info sample
				Residence residence = profile.new Residence();
				residence.setCountry("대한민국");
				residence.setStates("서초구");
				residence.setCity("서울");
				residence.setStreet("방배로");
				residence.setSpot("사무실");
				profile.setResidence(residence);
				
				//Hometown info sample
				Hometown hometown = profile.new Hometown();
				hometown.setCountry("대한민국");
				hometown.setStates("남양주");
				hometown.setCity("경기도");
				hometown.setStreet("늘을길");
				hometown.setSpot("");
				profile.setHometown(hometown);
				
				profile.setLocale("ko_kr");
				profile.setTimezone("9.5");
				*/
				
				if(requestCode == REQUEST_CODE_JOIN){
					Connect.join(profile, new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							Log.i(AppConst.LOG_TAG, "join success");
							
							startActivity(new Intent(
									InputProfileActivity.this,
									MainActivity.class));
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									//showLoading(false);											
								}
							}, 1000);
							
							finish();
						}
						
						@Override
						public void onFail(FuncResult funcResult, Exception exception){
							Log.i(AppConst.LOG_TAG, "join fail :: " + funcResult);
							Log.i(AppConst.LOG_TAG, "join fail :: " + exception.getMessage());
						}
					});
				}else if(requestCode == REQUEST_CODE_UPDATE){
					Connect.profileUpdate(profile, new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							Log.i(AppConst.LOG_TAG, "pofile update success");

							finish();
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									//showLoading(false);											
								}
							}, 1000);
							
							finish();
						}
						
						@Override
						public void onFail(FuncResult funcResult, Exception exception){
							Log.i(AppConst.LOG_TAG, "pofile update fail :: " + funcResult);
							Log.i(AppConst.LOG_TAG, "pofile update fail :: " + exception.getMessage());
						}
					});
				}
			}
		});
	}
	

}
