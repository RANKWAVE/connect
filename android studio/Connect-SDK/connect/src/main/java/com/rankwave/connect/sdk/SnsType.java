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
	
	public static SnsType toEnum(String type) {
		if(type.equals("FB"))
			return SnsType.SNS_TYPE_FACEBOOK;
		else if(type.equals("TW"))
			return SnsType.SNS_TYPE_TWITTER;
		return null;
	}
}