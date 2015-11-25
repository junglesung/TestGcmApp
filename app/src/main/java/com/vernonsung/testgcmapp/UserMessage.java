package com.vernonsung.testgcmapp;

// HTTP body of sending a message to a user
// Since it's going to convert to JSON format with GSON library parser, use lowercase property names.
public class UserMessage {
    // To authenticate a valid sender
    private String instanceid;
    private String registrationtoken;
    // The message to the target user
    private String message;

    // Default constructor
    public UserMessage() {
    }

    // Initialization constructor
    public UserMessage(String instanceid, String registrationtoken, String message) {
        this.instanceid = instanceid;
        this.registrationtoken = registrationtoken;
        this.message = message;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public String getRegistrationtoken() {
        return registrationtoken;
    }

    public void setRegistrationtoken(String registrationtoken) {
        this.registrationtoken = registrationtoken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
