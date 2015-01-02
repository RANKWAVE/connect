package com.rankwave.connect.sdk.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.rankwave.connect.sdk.Connect;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NetworkThread implements Runnable {
	
	public static int E_SUCCESS = 0;
	public static int E_FAILED_NETWORK_CONNECTION = 1;
	public static int E_INVALID_JSON_OBJECT = 2;
	public static int E_CLIENT_PROTOCOL_EXCEPTION = 3;
	public static int E_IO_EXCEPTION = 4;
	public static int E_UNKNOWN_EXCEPTION = 5;
		
	private Handler mainHandler;
	private int TRID; 
	
	private Context context;
	private String url;
		
	List<NameValuePair> params;
	
	Object callbackObj;
	
	Runnable finallyRunner;
	byte [] resBytes;
			
	public void setUrl(String url) {
		this.url = url;
	}
	
	public NetworkThread(Context context) {
		this(context, null, null, 0);
	}
	
	public NetworkThread(Context context, String url, Handler h, int TRID ) {
		this.context = context;
		this.url = url;
		this.mainHandler = h;
		this.TRID = TRID;
	}

	public NetworkThread(Context context, String url, Handler h, int TRID, List<NameValuePair> params) {
		this.context = context;
		this.url = url;
		this.mainHandler = h;
		this.TRID = TRID;
		this.params = params;
	}
	
	public void setCallbackObj(Object callbackObj) {
		this.callbackObj = callbackObj;
	}
			
	@Override
	public void run() {
						
		int retCd = E_UNKNOWN_EXCEPTION;
		JSONObject json = null;
		final ArrayList<Object> arrResponse = new ArrayList<Object>();
		try {
			
			if(checkNetworkConnection(context) == false)
			{
				arrResponse.add(Integer.valueOf(E_FAILED_NETWORK_CONNECTION));
				arrResponse.add("network connection is invalid");
				arrResponse.add(callbackObj);
				
				Message msg = Message.obtain();
				msg.what = TRID;
				msg.obj = arrResponse;
				
				if( mainHandler != null )
					mainHandler.sendMessage(msg);
				
				return;
			}
			
			String requestMessage = url;
			
			Log.d(Connect.TAG, requestMessage);
			
			String resultMessage = "";
			HttpPost httpPost = new HttpPost(requestMessage);
			//httpPost.addHeader("Interface_version", "1.0");
		
			if( params != null )
			{
				Log.d(Connect.TAG, params.toString());
				httpPost.setEntity(new UrlEncodedFormEntity(params));
			}

			DefaultHttpClient client = new DefaultHttpClient();
			
			client.getParams().setParameter("http.protocol.expect-continue", false);
			client.getParams().setParameter("http.connection.timeout", 20000);
			client.getParams().setParameter("http.socket.timeout", 20000);
			
			HttpResponse response = client.execute(httpPost);
			final StringBuilder sb = new StringBuilder();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			while( true ) {
				String line = br.readLine();
				
				if( line == null )
					break;
				
				sb.append(line + '\n');
			}
			
			br.close();
			
			resultMessage = sb.toString();

			Log.d(Connect.TAG, resultMessage);
			
			try {
				json = new JSONObject(resultMessage);
				retCd = E_SUCCESS;
				
				arrResponse.add(Integer.valueOf(retCd));
				arrResponse.add(json);
				arrResponse.add(callbackObj);
				
			} catch (JSONException e) {

				e.printStackTrace();
				retCd = E_INVALID_JSON_OBJECT;
				
				arrResponse.add(Integer.valueOf(retCd));
				arrResponse.add(e.getMessage());
				arrResponse.add(callbackObj);
			}

		} catch (Exception e) {
			e.printStackTrace();
			retCd = E_CLIENT_PROTOCOL_EXCEPTION;
			arrResponse.add(Integer.valueOf(retCd));
			arrResponse.add(e.getMessage());
			arrResponse.add(callbackObj);
		} 
		finally {
			
			Message msg = Message.obtain();
			msg.what = TRID;
			msg.obj = arrResponse;
			
			if( mainHandler != null )
				mainHandler.sendMessage(msg);
		}
	}
	
	public static boolean checkNetworkConnection(Context context) {
		
		ConnectivityManager connect = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiInfo = connect
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileInfo = connect
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (!(wifiInfo.isConnected() || mobileInfo.isConnected())) {
			return false;
		}

		return true;
	}
}
