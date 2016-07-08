package com.rankwave.connect.sdk.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rankwave.connect.sdk.Connect;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NetworkThread implements Runnable {

    public static int E_SUCCESS = 0;
    public static int E_FAILED_NETWORK_CONNECTION = 1;
    public static int E_INVALID_JSON_OBJECT = 2;
    public static int E_CLIENT_PROTOCOL_EXCEPTION = 3;

    @SuppressWarnings("unused")
    public static int E_IO_EXCEPTION = 4;
    public static int E_UNKNOWN_EXCEPTION = 5;
    List<NameValuePair> params;
    Object callbackObj;

    @SuppressWarnings("unused")
    Runnable finallyRunner;

    @SuppressWarnings("unused")
    byte[] resBytes;

    private Handler mainHandler;
    private int TRID;
    private Context context;
    private String url;

    @SuppressWarnings("unused")
    public NetworkThread(Context context) {
        this(context, null, null, 0);
    }

    public NetworkThread(Context context, String url, Handler h, int TRID) {
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

    public static boolean checkNetworkConnection(Context context) {

        ConnectivityManager connect = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connect.getActiveNetworkInfo();

        if (activeNetwork != null) {
            return true;
        }
        else {
            return false;
        }
    }

    @SuppressWarnings("unused")
    public void setUrl(String url) {
        this.url = url;
    }

    @SuppressWarnings("unused")
    public void setCallbackObj(Object callbackObj) {
        this.callbackObj = callbackObj;
    }

    @Override
    public void run() {
        int retCd;
        JSONObject json;
        final ArrayList<Object> arrResponse = new ArrayList<>();
        String requestMessage = "";

        try {

            if (!checkNetworkConnection(context)) {
                arrResponse.add(E_FAILED_NETWORK_CONNECTION);
                arrResponse.add("network connection is invalid");
                arrResponse.add(callbackObj);

                Message msg = Message.obtain();
                msg.what = TRID;
                msg.obj = arrResponse;

                if (mainHandler != null)
                    mainHandler.sendMessage(msg);

                return;
            }

            requestMessage = url;

            Log.d(Connect.TAG, requestMessage);

            String resultMessage = "";
            HttpPost httpPost = new HttpPost(requestMessage);
            //httpPost.addHeader("Interface_version", "1.0");

            if (params != null) {
                Log.d(Connect.TAG, params.toString());
                httpPost.setEntity(new UrlEncodedFormEntity(params));
            }

            //DefaultHttpClient client = new DefaultHttpClient();

            HttpClient client = getHttpClient();

            client.getParams().setParameter("http.protocol.expect-continue", false);
            client.getParams().setParameter("http.connection.timeout", 20000);
            client.getParams().setParameter("http.socket.timeout", 20000);

            HttpResponse response = client.execute(httpPost);
            final StringBuilder sb = new StringBuilder();

            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while (true) {
                String line = br.readLine();

                if (line == null)
                    break;

                sb.append(line).append('\n');
            }

            br.close();

            resultMessage = sb.toString();

            Log.d(Connect.TAG, resultMessage);

            try {
                json = new JSONObject(resultMessage);
                retCd = E_SUCCESS;

                arrResponse.add(retCd);
                arrResponse.add(json);
                arrResponse.add(callbackObj);

            } catch (JSONException e) {
                Log.e(Connect.TAG, requestMessage);
                Log.e(Connect.TAG, resultMessage);
                e.printStackTrace();
                retCd = E_INVALID_JSON_OBJECT;

                arrResponse.add(retCd);
                arrResponse.add(e.getMessage());
                arrResponse.add(callbackObj);
            }

        } catch (Exception e) {
            Log.e(Connect.TAG, requestMessage);
            e.printStackTrace();
            retCd = E_CLIENT_PROTOCOL_EXCEPTION;
            arrResponse.add(retCd);
            arrResponse.add(e.getMessage());
            arrResponse.add(callbackObj);
        } finally {

            Message msg = Message.obtain();
            msg.what = TRID;
            msg.obj = arrResponse;

            if (mainHandler != null)
                mainHandler.sendMessage(msg);
        }
    }

    private HttpClient getHttpClient() {

        try {
            HttpParams params = new BasicHttpParams();

            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);


            SchemeRegistry registry = new SchemeRegistry();

            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

            registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));


            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);

        } catch (Exception e) {

            return new DefaultHttpClient();

        }

    }

}
