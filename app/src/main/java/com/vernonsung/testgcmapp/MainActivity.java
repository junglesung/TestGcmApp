package com.vernonsung.testgcmapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    // Threads
    CreateMemberTask mCreateMemberTask;
    HelloServerTask mHelloServerTask;

    // UI
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Button buttonGetToken;
    private Button buttonDeleteToken;
    private Button buttonSendMessage;
    private Button buttonSubscribeTopic;
    private Button buttonUnsubscribeTopic;
    private Button buttonJoinGroup;
    private Button buttonLeaveGroup;
    private EditText editTextMessage;
    private EditText editTextTopic;
    private EditText editTextGroup;
    private TextView textViewInfo;

    // Constants
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String LOG_TAG = "testGood";
    private final String CREATE_MEMBER_URL = "https://testgcmserver-1120.appspot.com/api/0.1/members";
    private final String ECHO_URL = "https://testgcmserver-1120.appspot.com/api/0.1/tokens";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Get UI components
        buttonGetToken = (Button) findViewById(R.id.buttonGetToken);
        buttonDeleteToken = (Button) findViewById(R.id.buttonDeleteToken);
        buttonSendMessage = (Button) findViewById(R.id.buttonSendMessage);
        buttonSubscribeTopic = (Button) findViewById(R.id.buttonSubscribeTopic);
        buttonUnsubscribeTopic = (Button) findViewById(R.id.buttonUnsubscribeTopic);
        buttonJoinGroup = (Button) findViewById(R.id.buttonJoinGroup);
        buttonLeaveGroup = (Button) findViewById(R.id.buttonLeaveGroup);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        editTextTopic = (EditText) findViewById(R.id.editTextTopic);
        editTextGroup = (EditText) findViewById(R.id.editTextGroup);
        textViewInfo = (TextView) findViewById(R.id.textViewInfo);

        // Receive token
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showToken();
            }
        };

        buttonGetToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchToken();
            }
        });
        buttonDeleteToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteToken();
            }
        });

        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                talkToServer();
            }
        });

        showToken();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(MyConstants.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(MyConstants.UNREGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void showToken() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String token = sharedPreferences.getString(MyConstants.REGISTRATION_TOKEN, "Start here~ Get a token first!");
        textViewInfo.setText(token);
    }

    private void fetchToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private void deleteToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, UnregistrationIntentService.class);
            startService(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void talkToServer() {
        // Check network connection ability and then access Google Cloud Storage
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_network_connection_available), Toast.LENGTH_LONG).show();
            return;
        }
        // Prepare data
        String message = editTextMessage.getText().toString();
        // Vernon debug
        Log.d(LOG_TAG, "Say: " + message);
        // Execute uploading thread
        if (mHelloServerTask != null && mHelloServerTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(LOG_TAG, "Last message is still sending");
            Toast.makeText(this, "Last message is still sending, please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        mHelloServerTask = new HelloServerTask();
        mHelloServerTask.execute(message);
    }

    /**
     * Implementation of AsyncTask, to send a member to the server in the background away from
     * the UI thread and get the item URL generated by the server.
     */
    private class HelloServerTask extends AsyncTask<String, Void, Void> {

        // Return the item URL generated by the server.
        // Return null when failed
        @Override
        protected Void doInBackground(String... args) {
            // Deal with one message at a time
            if (args.length < 1) {
                Log.e(LOG_TAG, "No specified message");
                return null;
            }

            try {
                // Upload
                sayHello(args[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, getString(R.string.connection_error));
            }

            return null;
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(Void arg) {
            Log.d(LOG_TAG, "Finish talking to server");
        }

        // Send a message to the server in Googld APP engine
        private void sayHello(String message) throws IOException {
            // Get token from shared preferences
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String token = sharedPreferences.getString(MyConstants.REGISTRATION_TOKEN, "");
            if (token.isEmpty()) {
                Log.d(LOG_TAG, "Please get token first");
                return;
            }

            // Prepare URL https://aaa.appspot.com/api/0.1/tokens/xxxxxx/messages
            URL url = new URL(ECHO_URL + "/" + token + "/messages");
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            int size;
            byte[] data;
            OutputStream out;
            // Set content type
            urlConnection.setRequestProperty("Content-Type", "application/json");

            try {
                // To upload data to a web server, configure the connection for output using setDoOutput(true). It will use POST if setDoOutput(true) has been called.
                urlConnection.setDoOutput(true);

                // Convert item to JSON string
                data = new JSONObject().put("message", message).toString().getBytes();

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
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);

                // Send and get response
                // getResponseCode() will automatically trigger connect()
                int responseCode = urlConnection.getResponseCode();
                String responseMsg = urlConnection.getResponseMessage();
                Log.d(LOG_TAG, "Response " + responseCode + " " + responseMsg);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     */
    private void sendRegistrationToServer() {
        // Get token from shared preferences
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String token = sharedPreferences.getString(MyConstants.REGISTRATION_TOKEN, "");
        if (token.isEmpty()) {
            Log.d(LOG_TAG, "Please get token first");
            return;
        }

        // Check network connection ability and then access Google Cloud Storage
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_network_connection_available), Toast.LENGTH_LONG).show();
            return;
        }
        // Prepare data
        // Member.id and Member.createtime are determined by the server. So just set an empty string.
        Member member = new Member("", token, "Hello", "");
        // Execute uploading thread
        mCreateMemberTask = new CreateMemberTask();
        mCreateMemberTask.execute(member);
    }

    /**
     * Implementation of AsyncTask, to send a member to the server in the background away from
     * the UI thread and get the item URL generated by the server.
     */
    private class CreateMemberTask extends AsyncTask<Member, Void, String> {

        // Return the item URL generated by the server.
        // Return null when failed
        @Override
        protected String doInBackground(Member... members) {
            // Deal with one image at a time
            if (members.length < 1) {
                Log.e(LOG_TAG, "No specified member");
                return null;
            }
            try {
                // Upload
                String id = sendMember(members[0]);
                if (id == null) {
                    Log.e(LOG_TAG, "Create the item failed");
                    return null;
                }
                return id;
            } catch (IOException e) {
                Log.e(LOG_TAG, getString(R.string.connection_error));
                return null;
            }
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(String url) {
            if (url == null) {
                Toast.makeText(getApplicationContext(), R.string.create_failed_warning, Toast.LENGTH_LONG).show();
            } else {
                // Create item successfully
                Log.d(LOG_TAG, "Finish creating a member");
            }
        }

        // Send an item to Google App Engine
        // Return the URL of the uploaded item
        // Return null if failed
        private String sendMember(Member member) throws IOException {
            URL url = new URL(CREATE_MEMBER_URL);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            int size;
            byte[] data;
            OutputStream out;
            String memberUrl = null;

            // Set content type
            urlConnection.setRequestProperty("Content-Type", "application/json");

            try {
                // To upload data to a web server, configure the connection for output using setDoOutput(true). It will use POST if setDoOutput(true) has been called.
                urlConnection.setDoOutput(true);

                // Convert item to JSON string
                data = member.toJSONObject().toString().getBytes();
//                data = new Gson().toJson(item).getBytes();

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
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);

                // Send and get response
                // getResponseCode() will automatically trigger connect()
                int responseCode = urlConnection.getResponseCode();
                String responseMsg = urlConnection.getResponseMessage();
                Log.d(LOG_TAG, "Response " + responseCode + " " + responseMsg);
                if (responseCode != HttpURLConnection.HTTP_CREATED) {
                    return null;
                }

                // Get image URL
                memberUrl = urlConnection.getHeaderField("Location");
                Log.d(LOG_TAG, "Item URL " + memberUrl);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return memberUrl;
        }
    }
}
