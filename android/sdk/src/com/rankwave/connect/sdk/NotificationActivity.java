package com.rankwave.connect.sdk;

import com.rankwave.connect.sdk.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class NotificationActivity extends Activity{

	public NotificationActivity() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Translucent);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.notification);
		 		
		String title = getIntent().getStringExtra("title");
		String message = getIntent().getStringExtra("message");
				
				
		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		TextView tv_message = (TextView)findViewById(R.id.tv_message);
		
		tv_title.setText(title);
		tv_message.setText(message);
		
		Button btn_close = (Button)findViewById(R.id.btn_close);
		btn_close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();		
				overridePendingTransition(0,0);
			}
		});
	}
}
