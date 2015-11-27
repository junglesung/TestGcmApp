package com.vernonsung.testgcmapp;

// HTTP body of sending a message to a user
// Since it's going to convert to JSON format with GSON library parser, use lowercase property names.
public class TopicMessage {
    // To authenticate a valid sender
    private String instanceid;
    // The message to the target user
    private String topic;
    private String message;

    // Default constructor
    public TopicMessage() {
    }

    // Initialization constructor
    public TopicMessage(String instanceid, String topic, String message) {
        this.instanceid = instanceid;
        this.topic = topic;
        this.message = message;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
