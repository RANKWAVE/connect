package com.rankwave.connect.sdk;

public enum IdType {
	ID_TYPE_SNS,
	ID_TYPE_EMAIL,
	ID_TYPE_ANONYMOUS;
	
	public static String toString(IdType type) {
		if(type == ID_TYPE_SNS)
			return "sns";
		else if(type == ID_TYPE_EMAIL)
			return "email";
		else if(type == ID_TYPE_ANONYMOUS)
			return "anonymous";
		return "";
	}
}