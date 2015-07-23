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
			edit_name.setText(ConnectSession.getInstance().getUser().getProfile().getName());
			edit_birthday.setText(ConnectSession.getInstance().getUser().getProfile().getBirthday());
			
		}else if(requestCode == REQUEST_CODE_UPDATE){
			
		}
		
		Button btn_ok = (Button)findViewById(R.id.btn_ok);
		btn_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String email = edit_email.getText().toString();
				String name = edit_name.getText().toString();
				String birthday = edit_birthday.getText().toString();
				
				/*
				 * 회원 가입시 추가로 입력 가능한 Profile 객체를 셋팅할수 있습니다.
				 */
				Profile profile = new Profile();
				profile.setEmail(email);
				profile.setBirthday(birthday);
				profile.setName(name);

				/*
				profile.setLocale("ko_kr");
				profile.setTimezone("9.5");
				*/
				
				if(requestCode == REQUEST_CODE_JOIN){
					/*
					 * 회원 가입하는 함수입니다.
					 */
					Connect.join(profile, new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							Log.i(AppConst.LOG_TAG, "========================================");
			        		Log.i(AppConst.LOG_TAG, "join Success.");
			        		Log.i(AppConst.LOG_TAG, "========================================");
			        		
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
							Log.e(AppConst.LOG_TAG, "========================================");
			        		Log.e(AppConst.LOG_TAG, "join Fail.");
			        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
			        		Log.e(AppConst.LOG_TAG, exception.toString());
			        		Log.e(AppConst.LOG_TAG, "========================================");
						}
					});
				}else if(requestCode == REQUEST_CODE_UPDATE){
					/*
					 * Profile 를 수정하는 함수입니다.
					 */
					Connect.profileUpdate(profile, new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							Log.i(AppConst.LOG_TAG, "========================================");
			        		Log.i(AppConst.LOG_TAG, "profileUpdate Success.");
			        		Log.i(AppConst.LOG_TAG, "========================================");

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
							Log.e(AppConst.LOG_TAG, "========================================");
			        		Log.e(AppConst.LOG_TAG, "profileUpdate Fail.");
			        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
			        		Log.e(AppConst.LOG_TAG, exception.toString());
			        		Log.e(AppConst.LOG_TAG, "========================================");
						}
					});
				}
			}
		});
	}
	

}
