package com.rankwave.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class InputAccountActivity extends Activity{

	EditText edit_email;
	EditText edit_password;
	
	public InputAccountActivity() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.input_account);
		
		edit_email = (EditText)findViewById(R.id.edit_email);
		edit_password = (EditText)findViewById(R.id.edit_password);
		
		Button btn_ok = (Button)findViewById(R.id.btn_ok);
		btn_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String email = edit_email.getText().toString();
				String password = edit_password.getText().toString();
				
				Intent resultIntent = new Intent(); 
                resultIntent.putExtra("email", email); 
                resultIntent.putExtra("password", password); 
				
				setResult(RESULT_OK, resultIntent);
				finish(); 
			}
		});
	}
	

}
