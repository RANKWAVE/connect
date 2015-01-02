package com.rankwave.sdkdemo;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rankwave.connect.sdk.Connect;
import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.IdType;
import com.rankwave.connect.sdk.Session;
import com.rankwave.connect.sdk.SnsType;

public class MainActivity extends Activity {

	private ImageView profilePictureView;
	private TextView userNameView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Find the user's profile picture custom view
		profilePictureView = (ImageView) findViewById(R.id.iv_profile_pic);

		// Find the user's name view
		userNameView = (TextView) findViewById(R.id.tv_user_name);
		userNameView.setText(Connect.getActiveSession().getUser().getSnsInfo().getName());
		
		String profile_url = Connect.getActiveSession().getUser().getSnsInfo().getProfileUrl();
		displayProfileImage(profile_url);

		findViewById(R.id.btn_get_connect_token).setOnClickListener(onGetUserInfo);
		findViewById(R.id.btn_get_access_token).setOnClickListener(onGetUserInfo);
		findViewById(R.id.btn_get_sns_id).setOnClickListener(onGetUserInfo);
		
		findViewById(R.id.btn_get_login_type).setOnClickListener(onGetUserInfo);
		findViewById(R.id.btn_get_name).setOnClickListener(onGetUserInfo);
		findViewById(R.id.btn_get_profile_url).setOnClickListener(onGetUserInfo);
		
		findViewById(R.id.btn_profile_update).setOnClickListener(onProfileUpdate);
		findViewById(R.id.btn_logout).setOnClickListener(onLogout);
		findViewById(R.id.btn_unregist).setOnClickListener(onUnregist);
		
		findViewById(R.id.btn_unregister_gcm_id).setOnClickListener(onUnregisterGCMRregistrationId);
		findViewById(R.id.btn_register_gcm_id).setOnClickListener(onRegisterGCMRregistrationId);
		
	}

	View.OnClickListener onProfileUpdate = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, InputProfileActivity.class);
			intent.putExtra("requestCode", InputProfileActivity.REQUEST_CODE_UPDATE);
			startActivity(intent);
		}
	};
	
	View.OnClickListener onLogout = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			Connect.logout(new ConnectCallback<Session>() {
				@Override
				public void onSuccess(Session session){
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Logout", "logout success", "OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							});
				}
				
				@Override
				public void onFail(FuncResult funcResult, Exception exception) {
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Logout", "logout fail :: " + exception.getMessage(), "OK", null);
				}
			});
		}
	};

	View.OnClickListener onUnregisterGCMRregistrationId = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Connect.unregisterGCMRregistrationId(new ConnectCallback<Session>(){
				@Override
				public void onSuccess(Session session){
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"unregisterGCMRregistrationId", "unregisterGCMRregistrationId success", "OK", null);
				}	
				@Override
				public void onFail(FuncResult funcResult, Exception exception){
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"unregisterGCMRregistrationId", "unregisterGCMRregistrationId fail :: " + exception.getMessage(), "OK", null);
				}
			});
		}
	};
	
	View.OnClickListener onRegisterGCMRregistrationId = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Connect.registerGCMRregistrationId(new ConnectCallback<Session>(){
				@Override
				public void onSuccess(Session session){
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"registerGCMRregistrationId", "registerGCMRregistrationId success", "OK", null);
				}	
				@Override
				public void onFail(FuncResult funcResult, Exception exception){
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"registerGCMRregistrationId", "registerGCMRregistrationId fail :: " + exception.getMessage(), "OK", null);
				}
			});
		}
	};

	View.OnClickListener onUnregist = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Connect.leave(new ConnectCallback<Session>() {
				@Override
				public void onSuccess(Session session){
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Logout", "leave success", "OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							});
				}
				
				@Override
				public void onFail(FuncResult funcResult, Exception exception) {
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Logout", "leave fail :: " + exception.getMessage(), "OK", null);
				}
			});
		}
	};

	View.OnClickListener onGetUserInfo = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.btn_get_login_type: {
				IdType id_type = Connect.getActiveSession()
						.getUser().getIdType();
				
				if(id_type == IdType.ID_TYPE_SNS){
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Login TYPE", IdType.toString(id_type) + " : " + SnsType.toString(Connect.getActiveSession().getUser().getSnsType()), "OK", null);
				}else{
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Login TYPE", IdType.toString(id_type), "OK", null);
				}
				

				break;
			}

			case R.id.btn_get_name: {
				String name = Connect.getActiveSession().getUser().getSnsInfo().getName();
				CommonAlertDialog.showDefaultDialog(MainActivity.this, "NAME",
						name, "OK", null);
				break;
			}

			case R.id.btn_get_profile_url: {
				String profile_url = Connect.getActiveSession().getUser()
						.getSnsInfo().getProfileUrl();
				CommonAlertDialog.showDefaultDialog(MainActivity.this,
						"Profile Image URL", profile_url, "OK", null);
				break;
			}

			case R.id.btn_get_connect_token: {
				String connect_token = Connect.getActiveSession().getConnect_token();
				CommonAlertDialog.showDefaultDialog(MainActivity.this, "Connect Token",
						connect_token, "OK", null);
				break;
			}
			
			case R.id.btn_get_access_token: {
				IdType id_type = Connect.getActiveSession()
						.getUser().getIdType();
				
				if(id_type == IdType.ID_TYPE_SNS){
					String access_token = Connect.getActiveSession().getUser()
							.getSnsInfo().getAccessToken();
					
					SnsType sns_type = Connect.getActiveSession().getUser().getSnsType();
					
					if(sns_type == SnsType.SNS_TYPE_FACEBOOK){
						CommonAlertDialog.showDefaultDialog(MainActivity.this,
								"Access Token", access_token, "OK", null);
					}else if(sns_type == SnsType.SNS_TYPE_TWITTER){
						String token_secret = Connect.getActiveSession().getUser()
								.getSnsInfo().getTokenSecret();

						CommonAlertDialog.showDefaultDialog(MainActivity.this,
								"Access Token + Token Secret", access_token + "\n"
										+ token_secret, "OK", null);
					}
				}else{
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Access Token", "Not logged in SNS", "OK",
							null);
				}
				
				break;
			}
			
			case R.id.btn_get_sns_id: {
				String sns_id = Connect.getActiveSession().getUser().getSnsInfo().getSnsId();
				CommonAlertDialog.showDefaultDialog(MainActivity.this, "SNS ID",
						sns_id, "OK", null);
				break;
			}
			
			}
			
			

		}
	};

	public void displayProfileImage(String profile_url) {

		String cacheDir = cacheDirPath(MainActivity.this);
		String savePath = cacheDir
				+ Connect.getActiveSession().getUser().getSnsType() + Connect.getActiveSession().getUser().getSnsInfo().getSnsId();
						
		File local = new File(savePath);

		if (local.exists() && local.length() > 0) {

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(savePath, options);

			if (options.outWidth < 0 || options.outHeight < 0) {

				local.delete();

				ImageDownloadThread downThread = new ImageDownloadThread(
						imageDownloadHandler, profile_url, savePath, "");

				Thread thread = new Thread(downThread);
				thread.setDaemon(true);
				thread.start();

			} else {

				Bitmap bitmap = BitmapFactory.decodeFile(savePath);
				Bitmap maskBitmap = BitmapFactory.decodeResource(
						getResources(), R.drawable.alpha_mask);

				Bitmap targetBitmap = makeCircleImage(bitmap, maskBitmap, 150,
						150);

				profilePictureView.setImageBitmap(targetBitmap);
			}

		} else {

			if (local.exists())
				local.delete();

			ImageDownloadThread downThread = new ImageDownloadThread(
					imageDownloadHandler, profile_url, savePath, "");

			Thread thread = new Thread(downThread);
			thread.setDaemon(true);
			thread.start();
		}
	}

	@SuppressLint("HandlerLeak")
	Handler imageDownloadHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 1:
				@SuppressWarnings("unchecked")
				ArrayList<Object> arr = (ArrayList<Object>) msg.obj;
				boolean check = (Boolean) arr.get(0);

				String savePath = arr.get(1).toString();

				if (check) {
					Bitmap bitmap = BitmapFactory.decodeFile(savePath);

					Bitmap maskBitmap = BitmapFactory.decodeResource(
							getResources(), R.drawable.alpha_mask);

					Bitmap targetBitmap = makeCircleImage(bitmap, maskBitmap,
							150, 150);

					profilePictureView.setImageBitmap(targetBitmap);
				}
				break;
			}
		}
	};

	public String cacheDirPath(Context context) {

		File cacheDir;

		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			cacheDir = context.getExternalCacheDir();
		else
			cacheDir = context.getCacheDir();

		return cacheDir.getAbsolutePath() + "/";
	}

	public static Bitmap makeCircleImage(Bitmap sourceBitmap,
			Bitmap maskBitmap, int targetWidth, int targetHeight) {

		if (sourceBitmap == null)
			return null;

		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(targetBitmap);

		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),
				sourceBitmap.getHeight()), new Rect(0, 0, targetWidth,
				targetHeight), null);

		Paint p = new Paint();
		p.setFilterBitmap(false);
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

		canvas.drawBitmap(maskBitmap, new Rect(0, 0, maskBitmap.getWidth(),
				maskBitmap.getHeight()), new Rect(0, 0, targetWidth,
				targetHeight), p);

		p.setXfermode(null);

		return targetBitmap;
	}

}
