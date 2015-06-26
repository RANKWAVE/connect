package com.rankwave.sdkdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rankwave.connect.sdk.Connect;

public class ConnectPayloadReceiver extends BroadcastReceiver{

	public ConnectPayloadReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		
		Log.i("ConnectPayloadReceiver", "OnReceive :" + action);
		if(Connect.ACTION_SEND_PAYLOAD.equals(action) == true) {
			
			String payload = intent.getStringExtra(Connect.INTENT_PUSH_PAYLOAD);
			Log.i("ConnectPayloadReceiver", "payload :" + payload);
			
		}
		
		if(isOrderedBroadcast())
			setResult(-1, null, null);
	}

}
