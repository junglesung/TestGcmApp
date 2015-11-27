package com.vernonsung.testgcmapp;

public class MyConstants {
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_TOKEN = "registrationToken";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String UNREGISTRATION_COMPLETE = "unregistrationComplete";
    public static final String SUBSCRIPTION_COMPLETE = "subscriptionComplete";
    public static final String UNSUBSCRIBING_COMPLETE = "unsubscribingComplete";
    public static final String USER_ID = "userId";
    public static final String TOPIC = "topic";
    public static final String ACTION_GET_TOKEN = "actionGetToken";
    public static final String ACTION_DELETE_TOKEN = "actionDeleteToken";
    public static final String ACTION_SUBSCRIBE_TOPIC = "actionSubscribeTopic";
    public static final String ACTION_UNSUBSCRIBE_TOPIC = "actionUnsubscribeTopic";

    // URL
    public static final String APP_SERVER_URL_BASE = "https://testgcmserver-1120.appspot.com/api/0.1";
    public static final String USER_REGISTRATION_URL = APP_SERVER_URL_BASE + "/myself";
    public static final String USER_MESSAGE_URL = APP_SERVER_URL_BASE + "/user-messages";
    public static final String TOPIC_MESSAGE_URL = APP_SERVER_URL_BASE + "/topic-messages";

    // Values
    public static final int URL_CONNECTION_READ_TIMEOUT = 10000;  // milliseconds
    public static final int URL_CONNECTION_CONNECT_TIMEOUT = 15000;  // milliseconds
}
