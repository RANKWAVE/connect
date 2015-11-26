package com.rankwave.connect.sdk;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rankwave.connect.sdk.core.ConnectReceiver;

public class NotificationActivity extends Activity{

	public NotificationActivity() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		try{
			String jsonStr = getIntent().getStringExtra("json");
			json = new JSONObject(jsonStr);
			if(json.has("image_url")){
				String image_url = json.getString("image_url");
				if(image_url != null && !image_url.equals("")){
					new BitmapFromURL().execute(image_url);
				}else{
					showPopup(null);
				}
			}else{
				showPopup(null);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		try{
			String jsonStr = intent.getStringExtra("json");
			json = new JSONObject(jsonStr);
			if(json.has("image_url")){
				String image_url = json.getString("image_url");
				if(image_url != null && !image_url.equals("")){
					new BitmapFromURL().execute(image_url);
				}else{
					showPopup(null);
				}
			}else{
				showPopup(null);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private JSONObject json = null;
	public void showPopup(Bitmap image){
		try{
			setContentView(R.layout.notification);

			String title = json.getString("title");
			String message = json.getString("message");

			TextView tv_title = (TextView) findViewById(R.id.tv_title);
			TextView tv_message = (TextView)findViewById(R.id.tv_message);
			tv_title.setText(title);
			tv_message.setText(message);


			int popup_style = 0;
			if(json.has("popup_style") && !json.isNull("popup_style")){
				popup_style = json.getInt("popup_style");
			}


			int icon = 0x1080093;
			ApplicationInfo ai = getApplicationContext().getPackageManager()
					.getApplicationInfo(getApplicationContext().getPackageName(), 0);
			icon = ai.icon;

			//icon 정보가 메타데이터에 있으면 적용하고 없으면 기본 앱 아이콘
			ApplicationInfo aiMeta = getApplicationContext().getPackageManager().getApplicationInfo(
					getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
			int large = aiMeta.metaData.getInt(Connect.PROPERTY_NOTIFICATION_LARGE_ICON);

			if(large == 0){
				large = icon;
			}

			ImageView iv_icon_image = (ImageView)findViewById(R.id.iv_icon);
			iv_icon_image.setImageResource(large);

			ImageView iv_noti_image = (ImageView)findViewById(R.id.iv_noti_image);
			LinearLayout btn_layout = (LinearLayout) findViewById(R.id.btn_layout);
			if(popup_style == 2){
				//이미지 처리
				if(image != null){
					iv_noti_image.setImageBitmap(image);
					iv_noti_image.setVisibility(View.VISIBLE);
				}else{
					iv_noti_image.setVisibility(View.GONE);
				}
			}


			btn_layout.setVisibility(View.VISIBLE);

			//닫기 버튼
			Button btn_close = (Button)findViewById(R.id.btn_close);
			btn_close.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					moveTaskToBack(true);
					finish();
					android.os.Process.killProcess(android.os.Process.myPid());
					//overridePendingTransition(0,0);
				}
			});

			//확인 버튼
			Button btn_confirm = (Button)findViewById(R.id.btn_confirm);
			btn_confirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try{

						String payload = "";
						String cmn = "";
						String push_seq = "";
						String open_url = "";

						if (json.has("payload") == true) {
							payload = json.getString("payload");
						}

						if (json.has("cmn") == true) {
							cmn = json.getString("cmn");
						}

						if (json.has("push_seq") == true) {
							push_seq = json.getString("push_seq");
						}

						if(json.has("open_url")){
							open_url = json.getString("open_url");
						}


						Intent notificationIntent = new Intent(getApplicationContext(), ConnectReceiver.class).setAction(Connect.ACTION_PUSH_CLICK);
						notificationIntent.putExtra(Connect.INTENT_PUSH_PAYLOAD, payload);
						notificationIntent.putExtra(Connect.INTENT_PUSH_CMN, cmn);
						notificationIntent.putExtra(Connect.INTENT_PUSH_SEQ, push_seq);
						notificationIntent.putExtra(Connect.INTENT_PUSH_OPEN_URL, open_url);

						sendBroadcast(notificationIntent);

						finish();
					}catch(Exception e){
						e.printStackTrace();
					}

				}
			});

		}catch(Exception e){
			e.printStackTrace();
		}


	}
	
	
	private class BitmapFromURL extends AsyncTask<String, Void, Bitmap>{
		
		public BitmapFromURL(){
			super();
		
		}
		@Override
        protected Bitmap doInBackground(String... params) {
            try {

                Bitmap bitmap = Util.getBitmapFromURL(params[0]);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            } 
            return null;
        }
		
		
		@Override
        protected void onPostExecute(Bitmap result) {

            super.onPostExecute(result);
            try {
            	showPopup(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}
	
	@Override
	protected void onUserLeaveHint() {
		// TODO Auto-generated method stub
		super.onUserLeaveHint();
		
		finish();
		
	}
}
