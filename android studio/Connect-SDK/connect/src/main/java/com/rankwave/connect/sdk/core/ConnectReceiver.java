package com.rankwave.connect.sdk.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.rankwave.connect.sdk.Connect;

public class ConnectReceiver extends BroadcastReceiver {

    public ConnectReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.i("ConnectReceiver", "OnReceive :" + action);

        if (Connect.ACTION_PUSH_CLICK.equals(action)) {

            String payload = intent.getStringExtra(Connect.INTENT_PUSH_PAYLOAD);
            String push_seq = intent.getStringExtra(Connect.INTENT_PUSH_SEQ);
            String open_url = intent.getStringExtra(Connect.INTENT_PUSH_OPEN_URL);

            if (push_seq != null && push_seq.length() > 0) {
                final SharedPreferences prefs = context.getSharedPreferences(Connect.SDK_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Connect.INTENT_PUSH_SEQ, push_seq);
                editor.commit();
            }

            if (open_url != null && !open_url.equals("")) {
                final SharedPreferences prefs = context.getSharedPreferences(Connect.SDK_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Connect.INTENT_PUSH_OPEN_URL, open_url);
                editor.commit();
            }

            //payload 값을 broadcast
            Intent notificationIntent = new Intent(Connect.ACTION_SEND_PAYLOAD);
            notificationIntent.putExtra(Connect.INTENT_PUSH_PAYLOAD, payload);
            context.sendBroadcast(notificationIntent);


            //APP 실행
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            launchIntent.putExtra(Connect.INTENT_PUSH_PAYLOAD, payload);

            context.startActivity(launchIntent);


        }

        if (isOrderedBroadcast())
            setResult(-1, null, null);
    }

}
