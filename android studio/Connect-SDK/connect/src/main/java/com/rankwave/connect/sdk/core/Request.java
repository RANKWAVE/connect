package com.rankwave.connect.sdk.core;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.rankwave.connect.sdk.Connect;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Request {

    private String interface_name;
    private List<NameValuePair> params;
    private Callback callback = null;
    private Object user_object = null;
    @SuppressLint("HandlerLeak")
    private Handler httpHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0) {

                @SuppressWarnings("unchecked")
                ArrayList<Object> arr = (ArrayList<Object>) msg.obj;
                int retCd = ((Integer) arr.get(0)).intValue();

                Response response = new Response();
                response.error_code = retCd;
                response.user_obejct = user_object;

                if (retCd == NetworkThread.E_SUCCESS) {
                    JSONObject json = (JSONObject) arr.get(1);
                    response.error = "OK";
                    response.setJsonObject(json);

                    // check json type string
//					try {
//						json.getString("returnCode");
//						json.getString("resultMessage");
//
//					} catch (JSONException e) {
//
//						response.error = "invalid response";
//						response.setJsonObject(null);
//					}

                    if (callback != null)
                        callback.onCompleted(response);

                } else {

                    String error = (String) arr.get(1);
                    response.error = error;
                    response.setJsonObject(null);

                    if (callback != null) {
                        callback.onCompleted(response);
                    }
                }
            }
        }
    };

    ;

    public Request(String interface_name, List<NameValuePair> params, Callback callback, Object user_obejct) {
        this.interface_name = interface_name;
        this.params = params;
        this.callback = callback;
        this.user_object = user_obejct;
    }

    public void execute() {

        NetworkThread query = new NetworkThread(Connect.getContext(), interface_name,
                httpHandler, 0, params);
        Thread thread = new Thread(query);
        thread.setDaemon(true);
        thread.start();
    }

    public interface Callback {

        void onCompleted(Response response);
    }
}
