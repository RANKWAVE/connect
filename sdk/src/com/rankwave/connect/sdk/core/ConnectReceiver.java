package com.rankwave.connect.sdk.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.rankwave.connect.sdk.Connect;

public class ConnectReceiver extends BroadcastReceiver{

	public ConnectReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		
		Log.i("ConnectReceiver", "OnReceive :" + action);
		
		if("com.rankwave.connect.sdk.PUSH_CLICK".equals(action) == true) {
			
			String payload = intent.getStringExtra(Connect.INTENT_PUSH_PAYLOAD);
			String push_seq = intent.getStringExtra(Connect.INTENT_PUSH_SEQ);
			
			if(push_seq != null && push_seq.length() > 0) {
				//GCMManager.getInstance().uploadPushAction(push_seq);
				
				final SharedPreferences prefs = context.getSharedPreferences(Connect.SDK_PREFERENCES, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Connect.INTENT_PUSH_SEQ, push_seq);
				editor.commit();
				
				Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
				launchIntent.putExtra(Connect.INTENT_PUSH_PAYLOAD, payload);
			
				context.startActivity(launchIntent);
			}
		}
		
		if(isOrderedBroadcast())
			setResult(-1, null, null);
	}

}
