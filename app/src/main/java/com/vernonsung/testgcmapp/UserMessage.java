package com.vernonsung.testgcmapp;

// HTTP body of sending a message to a user
// Since it's going to convert to JSON format with GSON library parser, use lowercase property names.
public class UserMessage {
    // To authenticate a valid sender
    private String instanceid;
    // The message to the target user
    private String userid;
    private String message;

    // Default constructor
    public UserMessage() {
    }

    // Initialization constructor
    public UserMessage(String instanceid, String userid, String message) {
        this.instanceid = instanceid;
        this.userid = userid;
        this.message = message;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
