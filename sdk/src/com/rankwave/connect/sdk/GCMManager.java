package com.rankwave.connect.sdk;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

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

		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
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

			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			JSONObject json = new JSONObject(data);
			String message = "";
			String title = "";
			String payload = "";
			String cmn = "";
			String push_seq = "";
			int is_play_sound = 0;
			int is_process_foreground = 0;

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

			if (json.has("foreground") == true) {
				is_process_foreground = json.getInt("foreground");
			}

			if (json.has("cmn") == true) {
				cmn = json.getString("cmn");
			}

			if (json.has("push_seq") == true) {
				push_seq = json.getString("push_seq");
			}

			int icon = 0x1080093;
			try {
				ApplicationInfo ai = context.getPackageManager()
						.getApplicationInfo(context.getPackageName(), 0);
				icon = ai.icon;
			} catch (PackageManager.NameNotFoundException e) {
				// if we can't find it in the manifest, just return null
			}

			Notification note = new Notification(icon, message,
					System.currentTimeMillis());

			Intent notificationIntent = new Intent(
					context.getApplicationContext(), ConnectReceiver.class)
					.setAction("com.rankwave.connect.sdk.PUSH_CLICK");
			notificationIntent.putExtra(Connect.INTENT_PUSH_PAYLOAD, payload);
			notificationIntent.putExtra(Connect.INTENT_PUSH_CMN, cmn);
			notificationIntent.putExtra(Connect.INTENT_PUSH_SEQ, push_seq);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					context.getApplicationContext(), 0, notificationIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			notificationIntent.setAction(String.valueOf(System
					.currentTimeMillis()));

			note.setLatestEventInfo(context, title, message, pendingIntent);

			note.number = 0;
			if (is_play_sound == 1)
				note.defaults |= Notification.DEFAULT_SOUND;

			note.defaults |= Notification.DEFAULT_VIBRATE;
			note.defaults |= Notification.DEFAULT_LIGHTS;
			note.flags |= Notification.FLAG_AUTO_CANCEL;

			if (is_process_foreground == 0) {

				new AsyncTask<Object, Integer, String>() {

					@Override
					protected String doInBackground(Object... params) {

						Context context = (Context) params[0];
						NotificationManager manager = (NotificationManager) params[1];
						Notification note = (Notification) params[2];

						if (Util.isForeground(context) == false) {

							Log.i(Connect.TAG, "app is background!!");

							manager.notify(0, note);

						} else {
							Log.i(Connect.TAG, "app is foreground!!");
						}

						return null;
					}

				}.execute(context, notificationManager, note, null);

				return true;
			} else {
				notificationManager.notify(0, note);
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
					//uploadRegistrationId();
				}
				super.onPostExecute(result);
			}

		}.execute(GCMManager.getInstance(), null, null);
	}

	public void uploadRegistrationId() {

		if (Connect.getActiveConnectSession().getConnectSessionState() == ConnectSessionState.OPENED) {
			ConnectService.setGCMRegistrationId(null);

		} else {

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {

					uploadRegistrationId();
				}
			}, 1000);
		}
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

	public void popupNotification(Context context, String title, String message) {

		Intent intent = new Intent(context.getApplicationContext(),
				NotificationActivity.class);
		intent.putExtra("title", title);
		intent.putExtra("message", message);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
				| Intent.FLAG_ACTIVITY_NO_ANIMATION);
		context.startActivity(intent);
	}

}
