package com.rankwave.connect.sdk.core;


import com.rankwave.connect.sdk.GCMManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GCMReceiver extends BroadcastReceiver {

	public GCMReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		boolean flag = true;
		flag = GCMManager.getInstance().onReceive(context, intent);		
		
		if(isOrderedBroadcast()) {
			setResult(-1, null, null);
			if(flag)
				abortBroadcast();
		}
	}
}
