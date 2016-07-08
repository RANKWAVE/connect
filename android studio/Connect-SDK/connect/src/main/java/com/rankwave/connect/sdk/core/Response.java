package com.rankwave.connect.sdk.core;

import org.json.JSONObject;

public class Response {

    public int error_code;
    public String error;
    public Object user_obejct = null;
    private JSONObject json_object;

    public Response() {

    }

    public JSONObject getJsonObject() {
        return json_object;
    }

    public void setJsonObject(JSONObject json_object) {
        this.json_object = json_object;
    }
}
