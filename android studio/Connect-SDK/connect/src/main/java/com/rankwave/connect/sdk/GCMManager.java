package com.rankwave.connect.sdk;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rankwave.connect.sdk.core.ConnectReceiver;

public class GCMManager {

	private static final String SDK_PREFERENCES = "com.rankave.connect.sdk.gcmmanager";
	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	public static GCMManager instance;

	protected GoogleCloudMessaging gcm;
	protected String sender_id;
	protected Context context;

	public static GCMManager getInstance() {

		if (instance == null) {

			synchronized (GCMManager.class) {

				instance = new GCMManager();
			}
		}

		return instance;
	}

	public GCMManager() {

	}

	public void init(Context context) {

		this.context = context;
		this.sender_id = Connect.getSender_id();
		this.gcm = GoogleCloudMessaging.getInstance(context);
		String regid = getRegistrationId(context);
		
		if (regid == null || regid.length() == 0) {
			registerInBackground();
		} else {
			//uploadRegistrationId();
		}

	}

	public String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.equalsIgnoreCase("")) {
			Log.i(Connect.TAG, "RegistrationId not found.");
			return "";
		}

		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = Util.getAppVersionCode(context);
		if (registeredVersion != currentVersion) {
			Log.i(Connect.TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = Util.getAppVersionCode(context);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private SharedPreferences getGCMPreferences(Context context) {

		return context.getSharedPreferences(SDK_PREFERENCES,
				Context.MODE_PRIVATE);
	}

	public boolean onReceive(Context context, Intent intent) {
		if (intent.hasExtra("rankwave") == false) {
			return false;
		}

		String data = intent.getStringExtra("rankwave");
		Log.i(Connect.TAG, "push notification : " + data);

		try {
			JSONObject json = new JSONObject(data);
			
			int is_process_foreground = 0;
			if (json.has("foreground") == true) {
				is_process_foreground = json.getInt("foreground");
			}
			
			if (is_process_foreground == 0) {

				new AsyncTask<Object, Integer, String>() {

					@Override
					protected String doInBackground(Object... params) {

						Context context = (Context) params[0];
						JSONObject json = (JSONObject) params[1];
						
						if (Util.isForeground(context) == false) {

							Log.i(Connect.TAG, "app is background!!");

							if(json.has("image_url")){
								new BitmapFromURL(context, json).execute();
							}else{
								notification(context, json, null);
							}

						} else {
							Log.i(Connect.TAG, "app is foreground!!");
						}

						return null;
					}

				}.execute(context, json);

				return true;
			} else {
				if(json.has("image_url")){
					new BitmapFromURL(context, json).execute();
				}else{
					notification(context, json, null);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private void registerInBackground() {

		new AsyncTask<GCMManager, Integer, String>() {
			@Override
			protected String doInBackground(GCMManager... params) {

				GCMManager gcm_manager = params[0];
				String msg = "";
				try {
					if (gcm_manager.gcm == null) {
						gcm_manager.gcm = GoogleCloudMessaging
								.getInstance(gcm_manager.context);
					}

					String regid = gcm_manager.gcm
							.register(gcm_manager.sender_id);
					
					// Persist the regID - no need to register again.
					storeRegistrationId(gcm_manager.context, regid);

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String result) {

				if (result.length() > 0) {

					Log.e(Connect.TAG, result);

					// retry
					registerInBackground();
				} else {
					// upload regID
					uploadRegistrationId();
				}
				super.onPostExecute(result);
			}

		}.execute(GCMManager.getInstance(), null, null);
	}

	public void uploadRegistrationId() {
		ConnectService.pushRegisterDevice(null);
	}

	public void uploadPushAction(String push_seq) {
		try{
			JSONObject etc = new JSONObject();
			etc.put("push_seq", push_seq);
			etc.put("os_type", DeviceInfo.getInstance().getOs_type());
			
			ConnectService.action(null, "PUSH", 1, "APP", etc);
			
		}catch(JSONException e){
			e.printStackTrace();
		}
	}

	public void popupNotification(Context context, JSONObject json) {
		try{
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			if(pm.isScreenOn()){

				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.notification, null);

				layout.setPadding(0, 0, 0, 0);

				TextView tv_title = (TextView) layout.findViewById(R.id.tv_title);
				TextView tv_message = (TextView) layout.findViewById(R.id.tv_message);
				tv_title.setText(json.getString("title"));
				tv_message.setText(json.getString("message"));


				tv_title.setSingleLine(true);
				tv_title.setEllipsize(TruncateAt.END);
				tv_message.setSingleLine(true);
				tv_message.setEllipsize(TruncateAt.END);

				int icon = 0x1080093;
				ApplicationInfo ai = context.getPackageManager()
						.getApplicationInfo(context.getPackageName(), 0);
				icon = ai.icon;

				//icon 정보가 메타데이터에 있으면 적용하고 없으면 기본 앱 아이콘
				ApplicationInfo aiMeta = context.getPackageManager().getApplicationInfo(
						context.getPackageName(), PackageManager.GET_META_DATA);
				int large = aiMeta.metaData.getInt(Connect.PROPERTY_NOTIFICATION_LARGE_ICON);

				if(large == 0){
					large = icon;
				}

				ImageView iv_icon_image = (ImageView)layout.findViewById(R.id.iv_icon);
				iv_icon_image.setImageResource(large);

				ImageView iv_noti_image = (ImageView)layout.findViewById(R.id.iv_noti_image);
				LinearLayout btn_layout = (LinearLayout)layout.findViewById(R.id.btn_layout);

				iv_noti_image.setVisibility(View.GONE);
				btn_layout.setVisibility(View.GONE);

				Toast toast = new Toast(context);

				toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setView(layout);

				toast.show();
			}else{
				Intent intent = new Intent(context,
						NotificationActivity.class);

				intent.putExtra("json", json.toString());
				//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}


	private void notification(Context context, JSONObject json, Bitmap bigPicture){
		//icon
		int icon = 0x1080093;
		try {
			ApplicationInfo ai = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(), 0);
			icon = ai.icon;

			String message = "";
			String title = "";
			String payload = "";
			String cmn = "";
			String push_seq = "";
			int is_play_sound = 0;
			int noti_priority = 0;
			String open_url = "";
			int noti_style = 0;
			int popup_style = 0;


			if (json.has("message") == true) {
				message = json.getString("message");
			}

			if (json.has("title") == true) {
				title = json.getString("title");
			}

			if (json.has("payload") == true) {
				payload = json.getString("payload");
			}

			if (json.has("sound") == true) {
				is_play_sound = json.getInt("sound");
			}

			if (json.has("cmn") == true) {
				cmn = json.getString("cmn");
			}

			if (json.has("push_seq") == true) {
				push_seq = json.getString("push_seq");
			}

			if(json.has("noti_priority")){
				noti_priority = json.getInt("noti_priority");
			}

			if(json.has("open_url")){
				open_url = json.getString("open_url");
			}

			if(json.has("noti_style")){
				noti_style = json.getInt("noti_style");
			}
			if(json.has("popup_style")){
				popup_style = json.getInt("popup_style");
			}

			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			Intent notificationIntent = new Intent(
					context.getApplicationContext(), ConnectReceiver.class)
					.setAction(Connect.ACTION_PUSH_CLICK);
			notificationIntent.putExtra(Connect.INTENT_PUSH_PAYLOAD, payload);
			notificationIntent.putExtra(Connect.INTENT_PUSH_CMN, cmn);
			notificationIntent.putExtra(Connect.INTENT_PUSH_SEQ, push_seq);
			notificationIntent.putExtra(Connect.INTENT_PUSH_OPEN_URL, open_url);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					context.getApplicationContext(), 0, notificationIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			notificationIntent.setAction(String.valueOf(System
					.currentTimeMillis()));


			//icon 정보가 메타데이터에 있으면 적용하고 없으면 기본 앱 아이콘
			ApplicationInfo aiMeta = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			int large = aiMeta.metaData.getInt(Connect.PROPERTY_NOTIFICATION_LARGE_ICON);
			int small = aiMeta.metaData.getInt(Connect.PROPERTY_NOTIFICATION_SMALL_ICON);

			if(large == 0){
				large = icon;
			}

			if(small == 0){
				small = icon;
			}

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			builder.setContentTitle(title)
					.setContentText(message)
					.setTicker(title + " " + message)
					.setWhen(System.currentTimeMillis())
					.setProgress(0, 0, false)
					.setAutoCancel(true)
					.setSmallIcon(small)
					.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), large))
					.setContentIntent(pendingIntent);

			//big picture
			if(noti_style == 1){
				Bitmap bigLargeIcon = BitmapFactory.decodeResource(context.getResources(), large);	//확대시 왼쪽에 나오는 아이콘

				builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).bigLargeIcon(bigLargeIcon)
						.setSummaryText(message));

				//long text
			}else if(noti_style == 2){
				builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message).setBigContentTitle(title));
			}

			//priority
			if(noti_priority == 1)
				builder.setPriority(NotificationCompat.PRIORITY_MAX);



			int notificationDefault = NotificationCompat.DEFAULT_VIBRATE;
			notificationDefault = notificationDefault | NotificationCompat.DEFAULT_LIGHTS;
			notificationDefault = notificationDefault | NotificationCompat.FLAG_AUTO_CANCEL;
			if (is_play_sound == 1)
				notificationDefault = notificationDefault | NotificationCompat.DEFAULT_SOUND;


			builder.setDefaults(notificationDefault);

			notificationManager.notify(0, builder.build());


			//popup
			if(popup_style == 1 || popup_style == 2){
				popupNotification(context, json);
			}

		} catch (PackageManager.NameNotFoundException e) {
			// if we can't find it in the manifest, just return null
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class BitmapFromURL extends AsyncTask<String, Void, Bitmap>{
		Context context;
		JSONObject json;

		public BitmapFromURL(Context context, JSONObject json){
			super();
			this.context = context;
			this.json = json;
		}
		@Override
		protected Bitmap doInBackground(String... params) {
			try {

				Bitmap bitmap = Util.getBitmapFromURL(json.getString("image_url"));
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
				notification(context, json, result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}