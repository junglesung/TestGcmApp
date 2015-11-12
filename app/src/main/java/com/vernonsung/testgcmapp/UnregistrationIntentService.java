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
public class UnregistrationIntentService extends IntentService {

    private static final String LOG_TAG = "testGood";

    public UnregistrationIntentService() {
        super("UnregistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
