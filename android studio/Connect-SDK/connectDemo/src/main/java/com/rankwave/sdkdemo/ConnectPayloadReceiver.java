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
			//사용자가 정의한 데이터 얻기
			String payload = intent.getStringExtra(Connect.INTENT_PUSH_PAYLOAD);
			Log.i("ConnectPayloadReceiver", "payload :" + payload);
			
			//사용자 정의 데이터(payload)를 이용하여 원하는 로직을 구현 합니다.
		}
		
		if(isOrderedBroadcast())
			setResult(-1, null, null);
	}

}
