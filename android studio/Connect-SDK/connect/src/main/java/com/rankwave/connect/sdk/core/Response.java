package com.rankwave.connect.sdk.core;

import org.json.JSONObject;

public class Response {

	public int error_code;
	public String error;
	private JSONObject json_object;
	public Object user_obejct = null;
	
	public Response() {
	
	}
	
	public JSONObject getJsonObject() {
		return json_object;
	}
	
	public void setJsonObject(JSONObject json_object) {
		this.json_object = json_object;
	}
}
