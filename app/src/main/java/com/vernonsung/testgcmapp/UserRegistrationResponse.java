package com.vernonsung.testgcmapp;

// Receive from APP server of updating registration token
// Since it's going to convert to JSON format with GSON library parser, use lowercase property names.
public class UserRegistrationResponse {
    private String userid;  // Datastore key string of Google APP Engine

    public UserRegistrationResponse() {
    }

    public UserRegistrationResponse(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
