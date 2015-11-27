package com.vernonsung.testgcmapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
                break;
            case MyConstants.ACTION_DELETE_TOKEN:
                deleteToken();
                break;
            default:
                Log.d(LOG_TAG, "Registration service received an unrecognized action");
        }
    }

    private void getToken() {
        // Get current token from shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String oldToken = sharedPreferences.getString(MyConstants.REGISTRATION_TOKEN, "");

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);

            // gcm_defaultSenderId comes from google-services.json automatically through Android Studio compiler
            String newToken = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(LOG_TAG, "GCM Registration Token: " + newToken);

            // Store new token
            if (newToken != oldToken) {
                sharedPreferences.edit().putString(MyConstants.REGISTRATION_TOKEN, newToken).apply();
            }

            // Update new token to server
            UserRegistration registration = new UserRegistration(instanceID.getId(), newToken);
            if (sendTokenToServer(registration) != 0) {
                throw new Exception("Send token to server failed");
            }
            // Vernon debug
            Log.d(LOG_TAG, "Successfully send registration token " + registration.toString());
            // Now it can receive messages

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(MyConstants.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token refresh because " + e.getMessage());
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

            // It's impractical to unregister from an APP server because it only happens when APP is uninstalled.
            // But there is no way to execute code when uninstalling happens.
            // APP servers should clean unused registration tokens periodically.

            // Now it won't receive messages

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().remove(MyConstants.SENT_TOKEN_TO_SERVER).apply();
            // Store token
            sharedPreferences.edit().remove(MyConstants.REGISTRATION_TOKEN).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token deleting", e);
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent unregistrationComplete = new Intent(MyConstants.UNREGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(unregistrationComplete);
    }

    // Send new registration token to APP server so that the server can send message to this device through Google Cloud Messaging
    private int sendTokenToServer(UserRegistration registration) {
        // Check network connection ability and then access Google Cloud Storage
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_network_connection_available), Toast.LENGTH_LONG).show();
            return -1;
        }

        // Send
        URL url;
        HttpsURLConnection urlConnection = null;
        int size;
        int ret = 0;
        byte[] data;
        OutputStream out;
        String userUrl;

        try {
            url = new URL(MyConstants.USER_REGISTRATION_URL);
            urlConnection = (HttpsURLConnection) url.openConnection();

            // Set HTTP method
            urlConnection.setRequestMethod("PUT");

            // Set content type
            urlConnection.setRequestProperty("Content-Type", "application/json");

            // To upload data to a web server, configure the connection for output using setDoOutput(true). It will use POST if setDoOutput(true) has been called.
            urlConnection.setDoOutput(true);

            // Convert item to JSON string
            data = new Gson().toJson(registration).getBytes();

            // For best performance, you should call either setFixedLengthStreamingMode(int) when the body length is known in advance, or setChunkedStreamingMode(int) when it is not. Otherwise HttpURLConnection will be forced to buffer the complete request body in memory before it is transmitted, wasting (and possibly exhausting) heap and increasing latency.
            size = data.length;
            if (size > 0) {
                urlConnection.setFixedLengthStreamingMode(size);
            } else {
                // Set default chunk size
                urlConnection.setChunkedStreamingMode(0);
            }

            // Get the OutputStream of HTTP client
            out = new BufferedOutputStream(urlConnection.getOutputStream());
            // Copy from file to the HTTP client
            out.write(data);
            // Make sure to close streams, otherwise "unexpected end of stream" error will happen
            out.close();

            // Set timeout
            urlConnection.setReadTimeout(MyConstants.URL_CONNECTION_READ_TIMEOUT);
            urlConnection.setConnectTimeout(MyConstants.URL_CONNECTION_CONNECT_TIMEOUT);

            // Send and get response
            // getResponseCode() will automatically trigger connect()
            int responseCode = urlConnection.getResponseCode();
            String responseMsg = urlConnection.getResponseMessage();
            Log.d(LOG_TAG, "Response " + responseCode + " " + responseMsg);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return -1;
            }

            // Get user ID from response body
            InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());
            UserRegistrationResponse responseBody = new Gson().fromJson(in, UserRegistrationResponse.class);
            String userId = responseBody.getUserid();
            Log.d(LOG_TAG, "User ID " + userId);

            // Save user ID to preference
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String oldId = sharedPreferences.getString(MyConstants.USER_ID, "");
            if (oldId != userId) {
                sharedPreferences.edit().putString(MyConstants.USER_ID, userId).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = -1;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return ret;
    }
}
