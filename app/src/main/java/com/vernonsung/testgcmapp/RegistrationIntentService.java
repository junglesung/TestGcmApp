package com.vernonsung.testgcmapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * Get Google Cloud Messaging registration token and send to the APP server.
 */
public class RegistrationIntentService extends IntentService {

    private static final String LOG_TAG = "testGood";

    public RegistrationIntentService() {
        super("RegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Identify action
        String action = intent.getAction();
        switch (action) {
            case MyConstants.ACTION_GET_TOKEN:
                getToken();
            case MyConstants.ACTION_DELETE_TOKEN:
                deleteToken();
            default:
                Log.d(LOG_TAG, "Registration service received an unrecognized action");
        }
    }

    private void getToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);

            // gcm_defaultSenderId comes from google-services.json automatically through Android Studio compiler
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(LOG_TAG, "GCM Registration Token: " + token);


            // Now it can receive messages

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(MyConstants.SENT_TOKEN_TO_SERVER, true).apply();
            // Store token
            sharedPreferences.edit().putString(MyConstants.REGISTRATION_TOKEN, token).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(MyConstants.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void deleteToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START unregister_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START delete_token]
            InstanceID instanceID = InstanceID.getInstance(this);

            // gcm_defaultSenderId comes from google-services.json automatically through Android Studio compiler
            instanceID.deleteToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            // [END delete_token]


            // Now it won't receive messages

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false).apply();
            // Store token
            sharedPreferences.edit().remove(MyConstants.REGISTRATION_TOKEN).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token deleting", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent unregistrationComplete = new Intent(MyConstants.UNREGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(unregistrationComplete);
    }
}
