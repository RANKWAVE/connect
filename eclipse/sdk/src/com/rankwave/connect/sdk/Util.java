package com.rankwave.connect.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;

public class Util {

	public Util() {

	}

	public static String getDeviceModel() {
		
		return android.os.Build.MODEL;
	}
	
	public static String getOsVersion() {
		return android.os.Build.VERSION.RELEASE;
	}
	
	public static String getAppVersion(Context context) {
		
		PackageInfo pInfo = null;

		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			
			return pInfo.versionName;
			
		} catch (NameNotFoundException e) {

			e.printStackTrace();
		}
		
		return "";
	}
	
	public static int getAppVersionCode(Context context) {
		
		PackageInfo pInfo = null;

		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			
			return pInfo.versionCode;
			
		} catch (NameNotFoundException e) {

			e.printStackTrace();
		}
		
		return -1;
	}

	
	@SuppressLint("DefaultLocale") public static String getDeviceId(Context context) {

		// 1 compute DEVICE ID
		String m_szDevIDShort = "35"
				+ // we make this look like a valid IMEI
				Build.BOARD.length() % 10 + Build.BRAND.length() % 10
				+ Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
				+ Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
				+ Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
				+ Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
				+ Build.TAGS.length() % 10 + Build.TYPE.length() % 10
				+ Build.USER.length() % 10; // 13 digits
		
		// 3 android ID - unreliable
		String m_szAndroidID = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);

		// 4 wifi manager, read MAC address - requires
		// android.permission.ACCESS_WIFI_STATE or comes as null
		WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();

		
		// 6 SUM THE IDs
		String m_szLongID =  m_szDevIDShort + m_szAndroidID
				+ m_szWLANMAC;
		
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
		byte p_md5Data[] = m.digest();

		String m_szUniqueID = new String();
		for (int i = 0; i < p_md5Data.length; i++) {
			int b = (0xFF & p_md5Data[i]);
			// if it is a single digit, make sure it have 0 in front (proper
			// padding)
			if (b <= 0xF)
				m_szUniqueID += "0";
			// add number to string
			m_szUniqueID += Integer.toHexString(b);
		}
	
		m_szUniqueID = m_szUniqueID.toUpperCase();
		
		return m_szUniqueID;
	}
	
	public static boolean isForeground(Context context) {
		
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		
		
		List<ActivityManager.RunningAppProcessInfo> process = manager.getRunningAppProcesses();
		
		for(ActivityManager.RunningAppProcessInfo app : process) {
			
			if(app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				
				if(context.getPackageName().equals(app.processName)) {
					return true;
				}
			}
		}
		
		return false;	
	}
	
	
	public static Bitmap getBitmapFromURL(String src) {
	      HttpURLConnection connection = null;
	      try {
	          URL url = new URL(src);
	          connection = (HttpURLConnection) url.openConnection();
	          connection.setDoInput(true);
	          connection.connect();
	          InputStream input = connection.getInputStream();
	          Bitmap myBitmap = BitmapFactory.decodeStream(input);
	          return myBitmap;
	      } catch (IOException e) {
	          e.printStackTrace();
	          return null;
	      }finally{
	        if(connection!=null)connection.disconnect();
	      }
	    }
}
