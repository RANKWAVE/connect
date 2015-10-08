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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rankwave.connect.sdk.Connect;
import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.ConnectSession;

public class MainActivity extends Activity {

	private ImageView profilePictureView;
	private TextView userNameView;
	
	public ConnectSession connectSession;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		
		Intent intent = getIntent();
		
		SnsUser snsUser = (SnsUser)intent.getSerializableExtra("snsUser");
		
		System.out.println(snsUser.getProfile_image());
		System.out.println(snsUser.getSns_id());
		System.out.println(snsUser.getSns_type());
		
		// Find the user's profile picture custom view
		profilePictureView = (ImageView) findViewById(R.id.iv_profile_pic);
		displayProfileImage(snsUser.getProfile_image(), snsUser.getSns_id(), snsUser.getSns_type());
		
		// Find the user's name view
		userNameView = (TextView) findViewById(R.id.tv_user_name);
		userNameView.setText(snsUser.getName());
		
		findViewById(R.id.btn_push_on).setOnClickListener(onPushOn);
		findViewById(R.id.btn_push_off).setOnClickListener(onPushOff);
		
		findViewById(R.id.btn_logout).setOnClickListener(onLogout);
		findViewById(R.id.btn_leave).setOnClickListener(onLeave);
		
	}

	
	View.OnClickListener onLogout = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			/*
			 * 로그아웃을 수행하는 함수입니다. 
			 */
			Connect.logout(new ConnectCallback<ConnectSession>() {
				@Override
				public void onSuccess(ConnectSession connectSession){
					Log.i(AppConst.LOG_TAG, "========================================");
	        		Log.i(AppConst.LOG_TAG, "logout Success.");
	        		Log.i(AppConst.LOG_TAG, "========================================");
	        		
	        		/*
	        		 * 데모에서는 Dialog 를 보여주고 있습니다.
	        		 */
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
					Log.e(AppConst.LOG_TAG, "========================================");
	        		Log.e(AppConst.LOG_TAG, "logout Fail.");
	        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
	        		Log.e(AppConst.LOG_TAG, exception.toString());
	        		Log.e(AppConst.LOG_TAG, "========================================");
	        		
					/*
	        		 * 데모에서는 dialog 를 보여주고 있습니다.
	        		 */
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Logout", "logout fail :: " + exception.getMessage(), "OK", null);
				}
			});
		}
	};
	
	View.OnClickListener onPushOff = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			/*
			 * Push 전송 해지를 위해 GCM registration ID 를 등록 해제하는 함수입니다.
			 */
			Connect.pushOff(new ConnectCallback<ConnectSession>(){
				@Override
				public void onSuccess(ConnectSession connectSession){
					Log.i(AppConst.LOG_TAG, "========================================");
	        		Log.i(AppConst.LOG_TAG, "pushOff Success.");
	        		Log.i(AppConst.LOG_TAG, "========================================");
					
					/*
	        		 * 데모에서는 dialog 를 보여주고 있습니다.
	        		 */
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"pushOff", "pushOff success", "OK", null);
				}
				@Override
				public void onFail(FuncResult funcResult, Exception exception){
					Log.e(AppConst.LOG_TAG, "========================================");
	        		Log.e(AppConst.LOG_TAG, "pushOff Fail.");
	        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
	        		Log.e(AppConst.LOG_TAG, exception.toString());
	        		Log.e(AppConst.LOG_TAG, "========================================");
	        		
	        		/*
	        		 * 데모에서는 dialog 를 보여주고 있습니다.
	        		 */
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"pushOff", "pushOff fail :: " + exception.getMessage(), "OK", null);
				}
			});
		}
	};
	
	View.OnClickListener onPushOn = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			/*
			 * Push를 받기위해 GCM registration ID 를 등록 함수입니다.
			 */
			Connect.pushOn(new ConnectCallback<ConnectSession>(){
				@Override
				public void onSuccess(ConnectSession connectSession){
					Log.i(AppConst.LOG_TAG, "========================================");
	        		Log.i(AppConst.LOG_TAG, "pushOn Success.");
	        		Log.i(AppConst.LOG_TAG, "========================================");
					
					/*
	        		 * 데모에서는 dialog 를 보여주고 있습니다.
	        		 */
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"pushOn", "pushOn success", "OK", null);
				}	
				@Override
				public void onFail(FuncResult funcResult, Exception exception){
					Log.e(AppConst.LOG_TAG, "========================================");
	        		Log.e(AppConst.LOG_TAG, "pushOn Fail.");
	        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
	        		Log.e(AppConst.LOG_TAG, exception.toString());
	        		Log.e(AppConst.LOG_TAG, "========================================");
	        		
	        		/*
	        		 * 데모에서는 dialog 를 보여주고 있습니다.
	        		 */
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"pushOn", "pushOn fail :: " + exception.getMessage(), "OK", null);
				}
			});
		}
	};

	View.OnClickListener onLeave = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			/*
    		 * 회원 탈퇴시 사용되는 함수입니다.
    		 */
			Connect.leave(new ConnectCallback<ConnectSession>() {
				@Override
				public void onSuccess(ConnectSession connectSession){
					Log.i(AppConst.LOG_TAG, "========================================");
	        		Log.i(AppConst.LOG_TAG, "leave Success.");
	        		Log.i(AppConst.LOG_TAG, "========================================");
	        		
	        		/*
	        		 * 데모에서는 dialog 를 보여주고 있습니다.
	        		 */
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Leave", "leave success", "OK",
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
					Log.e(AppConst.LOG_TAG, "========================================");
	        		Log.e(AppConst.LOG_TAG, "leave Fail.");
	        		Log.e(AppConst.LOG_TAG, "----------------------------------------");
	        		Log.e(AppConst.LOG_TAG, exception.toString());
	        		Log.e(AppConst.LOG_TAG, "========================================");
					
					/*
	        		 * 데모에서는 dialog 를 보여주고 있습니다.
	        		 */
					CommonAlertDialog.showDefaultDialog(MainActivity.this,
							"Leave", "leave fail :: " + exception.getMessage(), "OK", null);
				}
			});
		}
	};


	public void displayProfileImage(String profile_url, String id, String snsType) {
		if(profile_url == null || profile_url.equals("")){
			profilePictureView.setImageResource(R.drawable.loading_logo);
		}else{
			String cacheDir = cacheDirPath(MainActivity.this);
			String savePath = cacheDir + snsType + id;
							
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

					Bitmap targetBitmap = makeCircleImage(bitmap, maskBitmap, 200,
							200);

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
							200, 200);

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
