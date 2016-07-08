package com.rankwave.connect.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import org.json.JSONObject;

public class ConnectPollingThread extends Thread {

    public void run() {
        try {
            while (true) {
                //Log.d(Connect.TAG, "ConnectPollingThread :: ");
                //push click action upload
                final SharedPreferences prefs = Connect.getContext().getSharedPreferences(Connect.SDK_PREFERENCES, Context.MODE_PRIVATE);
                String push_seq = prefs.getString(Connect.INTENT_PUSH_SEQ, "");

                if (!push_seq.equals("")) {

                    //Action 을 연동하기 위해서 http 연동을 하는데 Thread 를 또 사용해야 하므로 아래와 같이 Handler 을 이용하여야 한다.
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String push_seq = prefs.getString(Connect.INTENT_PUSH_SEQ, "");
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(Connect.INTENT_PUSH_SEQ, "");
                                editor.commit();

                                JSONObject object = new JSONObject();
                                object.put("push_seq", push_seq);

                                ConnectService.action(null, "PUSH EVENT CLICK", 1, "CONNECT SDK", object);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0);
                }


                SystemClock.sleep(3000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
