package com.vernonsung.testgcmapp;

// HTTP body of sending a message to a group
// Since it's going to convert to JSON format with GSON library parser, use lowercase property names.
public class GroupMessage {
    // To authenticate a valid sender
    private String instanceid;
    // The message to the target user
    private String groupName;
    private String message;

    // Default constructor
    public GroupMessage() {
    }

    // Initialization constructor
    public GroupMessage(String instanceid, String groupName, String message) {
        this.instanceid = instanceid;
        this.groupName = groupName;
        this.message = message;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
