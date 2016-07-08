package com.rankwave.sdkdemo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImageDownloadThread implements Runnable {

	Handler mainHandler;
	String mUrlPath;
	String mSavePath;
	Object mCallbackObj;
	boolean mAdjustExpireCache;
	
	public static final long LOCAL_IMAGE_CACHE_TIME	= 3600*24*3;
	
	
	public void setmAdjustExpireCache(boolean mAdjustExpireCache) {
		this.mAdjustExpireCache = mAdjustExpireCache;
	}

	public ImageDownloadThread(Handler h, String url, String savePath,
			Object cbObj) {
		this.mainHandler = h;

		Log.i("DEMO", "downlod url-" + url);

		this.mUrlPath = url;
		this.mSavePath = savePath;
		this.mCallbackObj = cbObj;
		this.mAdjustExpireCache = false;
	}

	@Override
	public void run() {

		
		boolean check = true;

		File file = new File(mSavePath);
		
		if( mAdjustExpireCache == true && file.exists() ) {
			
			Date lastDate = new Date(file.lastModified());
			Date nowDate = new Date();
			
			long timeIntervel = (nowDate.getTime() - lastDate.getTime())/1000;
			
			Log.i("DEMO", "time interval=" + timeIntervel + " file=" +  mSavePath );
			
			if( timeIntervel > LOCAL_IMAGE_CACHE_TIME )
			{
				file.delete();
				
				Log.i("DEMO", "Delete file cache:" + mSavePath );
			}		
		}

		if (file.exists() == false) {
			
			if( mUrlPath == null || mUrlPath.length() == 0 )
				return;
			
			try {
				
				String requestMessage = mUrlPath;
				
				HttpGet httpGet = new HttpGet(requestMessage);
				httpGet.addHeader("Interface_version", "1.0");

				DefaultHttpClient client = new DefaultHttpClient();

				client.getParams().setParameter(
						"http.protocol.expect-continue", false);
				client.getParams().setParameter("http.connection.timeout",
						10000);
				client.getParams().setParameter("http.socket.timeout", 10000);

				HttpResponse response = client.execute(httpGet);

				BufferedInputStream bis = new BufferedInputStream(response
						.getEntity().getContent());
				ByteArrayBuffer baf = new ByteArrayBuffer(500);

				int current = 0;

				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}
				
				File dir = new File(file.getParent());
				if (dir.exists() == false) {
					if (dir.mkdirs() == false) {
						Log.i("DEMO", "Failed to create folder" + dir.getPath());
					}
				}
				
				FileOutputStream fos = new FileOutputStream(mSavePath);
				fos.write(baf.toByteArray());
				fos.close();

			} catch (ClientProtocolException e) {

				e.printStackTrace();
				Log.i("DEMO",
						"ClientProtocolException: " + e.toString());
				check = false;

			} catch (IOException e) {

				e.printStackTrace();
				Log.i("DEMO", "IOException: " + e.toString());
				
				check = false;
			} 
		}
		
		ArrayList<Object> arr = new ArrayList<Object>();
		arr.add(check);
		arr.add(file.getAbsolutePath());
		arr.add(mCallbackObj);

		Message msg = Message.obtain();
		msg.what = 1;
		msg.obj = arr;

		mainHandler.sendMessage(msg);
 
	}
}
