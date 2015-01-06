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
import com.rankwave.connect.sdk.Profile;
import com.rankwave.connect.sdk.Session;
import com.rankwave.connect.sdk.Profile.Hometown;

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
			edit_name.setText(Session.getInstance().getUser().getSnsInfo().getName());
			edit_birthday.setText(Session.getInstance().getUser().getSnsInfo().getBirthday());
			
		}else if(requestCode == REQUEST_CODE_UPDATE){
			Connect.profileGet(new ConnectCallback<Profile>(){
				@Override
				public void onSuccess(Profile profile){
					Log.i(AppConst.LOG_TAG, "profileGet success");
					Log.i(AppConst.LOG_TAG, profile.toString());
					
					if(profile != null){
						if(profile.getEmail() != null)
							edit_email.setText(profile.getEmail());
						
						if(profile.getName() != null)
							edit_name.setText(profile.getName());
						
						if(profile.getBirthday() != null)
							edit_birthday.setText(profile.getBirthday());
					}
				}
				
				@Override
				public void onFail(FuncResult funcResult, Exception exception){
					Log.i(AppConst.LOG_TAG, "profileGet fail :: " + funcResult);
					Log.i(AppConst.LOG_TAG, "profileGet fail :: " + exception.getMessage());
				}
			});
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
								
				Hometown hometown = profile.new Hometown();
				hometown.setCountry("seoul");
				profile.setHometown(hometown);
				
				
				if(requestCode == REQUEST_CODE_JOIN){
					Connect.join(profile, new ConnectCallback<Session>(){
						@Override
						public void onSuccess(Session session){
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
					Connect.profileUpdate(profile, new ConnectCallback<Session>(){
						@Override
						public void onSuccess(Session session){
							Log.i(AppConst.LOG_TAG, "pofile update success");
							
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
							Log.i(AppConst.LOG_TAG, "pofile update fail :: " + funcResult);
							Log.i(AppConst.LOG_TAG, "pofile update fail :: " + exception.getMessage());
						}
					});
				}
			}
		});
	}
	

}
