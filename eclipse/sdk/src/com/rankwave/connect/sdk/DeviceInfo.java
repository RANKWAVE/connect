package com.rankwave.connect.sdk;

import java.util.Locale;

import android.content.Context;
import android.telephony.TelephonyManager;

public class DeviceInfo {
	protected Context context;

	private final String os_type = "android";
	private String device_id;
	private String os_version;
	private String app_version;
	private String device_model;
	private String locale;
	private String location;
	private String network_operation_name;
	
	private String ad_id;
	
	
	private static DeviceInfo instance;
	
	public static DeviceInfo getInstance() {
		if(instance == null) {
			synchronized (DeviceInfo.class) {
				instance = new DeviceInfo();
			}
		} 

		return instance;
	}
	
	public DeviceInfo() {
		
	}
	
	public void init(Context context) {
		this.context = context;
		
		device_id = Util.getDeviceId(context);
		os_version = Util.getOsVersion();
		app_version = Util.getAppVersion(context);
		device_model = Util.getDeviceModel();
		
		Locale locale    = context.getResources().getConfiguration().locale;
		this.locale = locale.toString();
		
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	    String countryCode = tm.getSimCountryIso();
	    this.location = countryCode;
	    this.network_operation_name = tm.getNetworkOperatorName();
	    
	}
	
	
	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public String getOs_version() {
		return os_version;
	}

	public void setOs_version(String os_version) {
		this.os_version = os_version;
	}

	public String getApp_version() {
		return app_version;
	}

	public void setApp_version(String app_version) {
		this.app_version = app_version;
	}

	public String getDevice_model() {
		return device_model;
	}

	public void setDevice_model(String device_model) {
		this.device_model = device_model;
	}

	public String getOs_type() {
		return os_type;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getAd_id() {
		return ad_id;
	}

	public void setAd_id(String ad_id) {
		this.ad_id = ad_id;
	}

	public String getNetwork_operation_name() {
		return network_operation_name;
	}

	public void setNetwork_operation_name(String network_operation_name) {
		this.network_operation_name = network_operation_name;
	}

}
