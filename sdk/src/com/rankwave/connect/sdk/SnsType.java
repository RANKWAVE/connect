package com.rankwave.connect.sdk;

public enum SnsType {
	SNS_TYPE_FACEBOOK,
	SNS_TYPE_TWITTER;
	
	
	public static String toString(SnsType type) {
		if(type == SNS_TYPE_FACEBOOK)
			return "FB";
		else if(type == SNS_TYPE_TWITTER)
			return "TW";
		return "";
	}
}