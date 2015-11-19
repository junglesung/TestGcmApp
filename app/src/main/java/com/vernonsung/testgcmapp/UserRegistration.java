package com.vernonsung.testgcmapp;

import org.json.JSONException;
import org.json.JSONObject;

// Send to APP server to update registration token
public class UserRegistration {
    private String registrationToken;
    private String instanceId;
    private String newRegistrationToken;
    private String lastUpdateTime;

    // Default constructor
    public UserRegistration() {
    }

    // Constructor
    public UserRegistration(String registrationToken,
                            String instanceId,
                            String newRegistrationToken,
                            String lastUpdateTime) {
        this.registrationToken = registrationToken;
        this.instanceId = instanceId;
        this.newRegistrationToken = newRegistrationToken;
        this.lastUpdateTime = lastUpdateTime;
    }

    // Getter and setter
    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getNewRegistrationToken() {
        return newRegistrationToken;
    }

    public void setNewRegistrationToken(String newRegistrationToken) {
        this.newRegistrationToken = newRegistrationToken;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    // Android org.json is not as powerful as standard JAVA org.json.
    // There is no parser constructor for class object in JSONObject.
    // So create a transform function.
    public JSONObject toJSONObject() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("registrationtoken", registrationToken);
        // InstanceId is sent in URL. So don't put it in here.
        j.put("newregistrationtoken", newRegistrationToken);
        // LastUpdateTime are determined by server. So don't put it in here.
        return j;
    }

    @Override
    public String toString() {
        return "UserRegistration{" +
                "registrationToken='" + registrationToken + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", newRegistrationToken='" + newRegistrationToken + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                '}';
    }
}
