package com.vernonsung.testgcmapp;

import org.json.JSONException;
import org.json.JSONObject;

public class Member {
    private String id;
    private String token;
    private String message;
    private String createtime; // RCF 3339 format "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"

    // Default constructor
    public Member() {
        //
    }

    public Member(String id, String token, String message, String createtime) {
        this.id = id;
        this.token = token;
        this.message = message;
        this.createtime = createtime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    // Android org.json is not as powerful as standard JAVA org.json.
    // There is no parser constructor for class object in JSONObject.
    // So create a transform function.
    public JSONObject toJSONObject() throws JSONException {
        JSONObject j = new JSONObject();
        // ID and CreateTime are determined by server. So just put other properties.
        j.put("id", id);
        j.put("token", token);
        j.put("message", message);
        // Never send CreateTime to the server because it's determined by the server.
        return j;
    }
}
