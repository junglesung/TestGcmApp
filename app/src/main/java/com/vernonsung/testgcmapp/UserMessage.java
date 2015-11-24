package com.vernonsung.testgcmapp;

// HTTP body of sending a message to a user
// Since it's going to convert to JSON format
public class UserMessage {
    private String instanceId;
    private String registrationToken;
    private String message;
}
