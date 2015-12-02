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
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    // Threads
    SendUserMessageTask mSendUserMessageTask;
    SendTopicMessageTask mSendTopicMessageTask;
    SendGroupMessageTask mSendGroupMessageTask;
    JoinGroupTask mJoinGroupTask;
    LeaveGroupTask mLeaveGroupTask;

    // UI
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Button buttonGetToken;
    private Button buttonDeleteToken;
    private Button buttonSendUserMessage;
    private Button buttonSendTopicMessage;
    private Button buttonSendGroupMessage;
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
        buttonSendUserMessage = (Button) findViewById(R.id.buttonSendUserMessage);
        buttonSendTopicMessage = (Button) findViewById(R.id.buttonSendTopicMessage);
        buttonSendGroupMessage = (Button) findViewById(R.id.buttonSendGroupMessage);
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
                // Identify action
                String action = intent.getAction();
                switch (action) {
                    case MyConstants.REGISTRATION_COMPLETE:
                        showToken();
                        break;
                    case MyConstants.UNREGISTRATION_COMPLETE:
                        showToken();
                        break;
                    case MyConstants.SUBSCRIPTION_COMPLETE:
                        Toast.makeText(MainActivity.this, "Subscribe topic successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case MyConstants.UNSUBSCRIBING_COMPLETE:
                        Toast.makeText(MainActivity.this, "Unsubscribe topic successfully", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Log.d(LOG_TAG, "Main activity received an unrecognized action");
                }
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
        buttonSendUserMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserMessage();
            }
        });
        buttonSendTopicMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTopicMessage();
            }
        });
        buttonSendGroupMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGroupMessage();
            }
        });
        buttonSubscribeTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeTopic();
            }
        });
        buttonUnsubscribeTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsubscribeTopic();
            }
        });
        buttonJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroup();
            }
        });
        buttonLeaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGroup();
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
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(MyConstants.SUBSCRIPTION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(MyConstants.UNSUBSCRIBING_COMPLETE));
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
        Log.d(LOG_TAG, "Token should be shown");
    }

    private void fetchToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.setAction(MyConstants.ACTION_GET_TOKEN);
            startService(intent);
        }
    }

    private void deleteToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = sharedPreferences.getString(MyConstants.REGISTRATION_TOKEN, "");
        Boolean flagSent = sharedPreferences.getBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false);

        if (token.isEmpty() || !flagSent) {
            // Server hasn't received registration token yet
            Log.d(LOG_TAG, "Server hasn't received registration token yet, it needn't unregister");
            Toast.makeText(this, "Server hasn't received registration token yet, it needn't unregister", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start the service to tell the server
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.setAction(MyConstants.ACTION_DELETE_TOKEN);
            startService(intent);
        }
    }

    private void subscribeTopic() {
        // Get topic name
        String topic = editTextTopic.getText().toString();
        if (topic.isEmpty()) {
            Toast.makeText(this, "Please give a topic name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check Google Cloud Messaging registration
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = sharedPreferences.getString(MyConstants.REGISTRATION_TOKEN, "");
        Boolean flagSent = sharedPreferences.getBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false);

        if (token.isEmpty() || !flagSent) {
            // Server hasn't received registration token yet
            Log.d(LOG_TAG, "Server hasn't received registration token yet, it needn't unregister");
            Toast.makeText(this, "Server hasn't received registration token yet, it needn't unregister", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start the service to tell the server
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.setAction(MyConstants.ACTION_SUBSCRIBE_TOPIC);
            intent.putExtra(MyConstants.TOPIC, topic);
            startService(intent);
        }
    }

    private void unsubscribeTopic() {
        // Get topic name
        String topic = editTextTopic.getText().toString();
        if (topic.isEmpty()) {
            Toast.makeText(this, "Please give a topic name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check Google Cloud Messaging registration
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = sharedPreferences.getString(MyConstants.REGISTRATION_TOKEN, "");
        Boolean flagSent = sharedPreferences.getBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false);

        if (token.isEmpty() || !flagSent) {
            // Server hasn't received registration token yet
            Log.d(LOG_TAG, "Server hasn't received registration token yet, it needn't unregister");
            Toast.makeText(this, "Server hasn't received registration token yet, it needn't unregister", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start the service to tell the server
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.setAction(MyConstants.ACTION_UNSUBSCRIBE_TOPIC);
            intent.putExtra(MyConstants.TOPIC, topic);
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

    private void sendUserMessage() {
        // Check network connection ability and then access Google Cloud Storage
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_network_connection_available), Toast.LENGTH_LONG).show();
            return;
        }

        // Prepare data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String instanceID = InstanceID.getInstance(this).getId();
        String userId = sharedPreferences.getString(MyConstants.USER_ID, "");
        String message = editTextMessage.getText().toString();
        boolean flagRegisteredServer = sharedPreferences.getBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false);
        UserMessage userMessage = new UserMessage(instanceID, userId, message);

        // Registration
        if (!flagRegisteredServer) {
            Toast.makeText(this, "Server is inaccessible, maybe network is down", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vernon debug
        Log.d(LOG_TAG, "Send \"" + message + "\" to " + userId);

        // Execute uploading thread
        if (mSendUserMessageTask != null && mSendUserMessageTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(LOG_TAG, "Last message is still sending");
            Toast.makeText(this, "Last message is still sending, please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        mSendUserMessageTask = new SendUserMessageTask();
        mSendUserMessageTask.execute(userMessage);
    }

    /**
     * Implementation of AsyncTask, to send a member to the server in the background away from
     * the UI thread and get the item URL generated by the server.
     */
    private class SendUserMessageTask extends AsyncTask<UserMessage, Void, Void> {

        // Return the item URL generated by the server.
        // Return null when failed
        @Override
        protected Void doInBackground(UserMessage... args) {
            // Deal with one message at a time
            if (args.length < 1) {
                Log.e(LOG_TAG, "No specified message");
                return null;
            }

            try {
                // Upload
                send(args[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, e + "in sending an user message");
            }

            return null;
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(Void arg) {
            Log.d(LOG_TAG, "Finish sending a message to a user");
            Toast.makeText(getApplicationContext(), "Finish sending a message to a user", Toast.LENGTH_SHORT).show();
        }

        // Send a message to the server in Google APP engine
        private void send(UserMessage userMessage) throws IOException {
            // Prepare URL https://aaa.appspot.com/api/0.1/user-messages
            // The target user is myself
            URL url = new URL(MyConstants.USER_MESSAGE_URL);
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
                data = new Gson().toJson(userMessage).getBytes();

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

    private void sendTopicMessage() {
        // Check network connection ability and then access Google Cloud Storage
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_network_connection_available), Toast.LENGTH_LONG).show();
            return;
        }

        // Prepare data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String instanceID = InstanceID.getInstance(this).getId();
        String topic = editTextTopic.getText().toString();
        String message = editTextMessage.getText().toString();
        boolean flagRegisteredServer = sharedPreferences.getBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false);
        TopicMessage topicMessage = new TopicMessage(instanceID, topic, message);

        // Registration
        if (!flagRegisteredServer) {
            Toast.makeText(this, "Server is inaccessible, maybe network is down", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vernon debug
        Log.d(LOG_TAG, "Send \"" + message + "\" to topic " + topic);

        // Execute uploading thread
        if (mSendTopicMessageTask != null && mSendTopicMessageTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(LOG_TAG, "Last message is still sending");
            Toast.makeText(this, "Last message is still sending, please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        mSendTopicMessageTask = new SendTopicMessageTask();
        mSendTopicMessageTask.execute(topicMessage);
    }

    /**
     * Implementation of AsyncTask, to send a member to the server in the background away from
     * the UI thread and get the item URL generated by the server.
     */
    private class SendTopicMessageTask extends AsyncTask<TopicMessage, Void, Void> {

        // Return the item URL generated by the server.
        // Return null when failed
        @Override
        protected Void doInBackground(TopicMessage... args) {
            // Deal with one message at a time
            if (args.length < 1) {
                Log.e(LOG_TAG, "No specified message");
                return null;
            }

            try {
                // Upload
                send(args[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, e + "in sending an topic message");
            }

            return null;
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(Void arg) {
            Log.d(LOG_TAG, "Finish sending a message to a topic");
            Toast.makeText(getApplicationContext(), "Finish sending a message to a topic", Toast.LENGTH_SHORT).show();
        }

        // Send a message to the server in Google APP engine
        private void send(TopicMessage topicMessage) throws IOException {
            // Prepare URL https://aaa.appspot.com/api/0.1/user-messages
            // The target user is myself
            URL url = new URL(MyConstants.TOPIC_MESSAGE_URL);
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
                data = new Gson().toJson(topicMessage).getBytes();

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

    private void sendGroupMessage() {
        // Check network connection ability and then access Google Cloud Storage
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_network_connection_available), Toast.LENGTH_LONG).show();
            return;
        }

        // Prepare data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String instanceID = InstanceID.getInstance(this).getId();
        String groupName = editTextGroup.getText().toString();
        String message = editTextMessage.getText().toString();
        boolean flagRegisteredServer = sharedPreferences.getBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false);
        GroupMessage groupMessage = new GroupMessage(instanceID, groupName, message);

        // Registration
        if (!flagRegisteredServer) {
            Toast.makeText(this, "Server is inaccessible, maybe network is down", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vernon debug
        Log.d(LOG_TAG, "Send \"" + message + "\" to group " + groupName);

        // Execute uploading thread
        if (mSendGroupMessageTask != null && mSendGroupMessageTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(LOG_TAG, "Last message is still sending");
            Toast.makeText(this, "Last message is still sending, please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        mSendGroupMessageTask = new SendGroupMessageTask();
        mSendGroupMessageTask.execute(groupMessage);
    }

    /**
     * Implementation of AsyncTask, to send a member to the server in the background away from
     * the UI thread and get the item URL generated by the server.
     */
    private class SendGroupMessageTask extends AsyncTask<GroupMessage, Void, Void> {

        // Return the item URL generated by the server.
        // Return null when failed
        @Override
        protected Void doInBackground(GroupMessage... args) {
            // Deal with one message at a time
            if (args.length < 1) {
                Log.e(LOG_TAG, "No specified message");
                return null;
            }

            try {
                // Upload
                send(args[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, e + "in sending an group message");
            }

            return null;
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(Void arg) {
            Log.d(LOG_TAG, "Finish sending a message to a group");
            Toast.makeText(getApplicationContext(), "Finish sending a message to a group", Toast.LENGTH_SHORT).show();
        }

        // Send a message to the server in Google APP engine
        private void send(GroupMessage groupMessage) throws IOException {
            // Prepare URL https://aaa.appspot.com/api/0.1/group-messages
            // The target user is myself
            URL url = new URL(MyConstants.GROUP_MESSAGE_URL);
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
                data = new Gson().toJson(groupMessage).getBytes();

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

    private void joinGroup() {
        // Check network connection ability and then access Google Cloud Storage
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_network_connection_available), Toast.LENGTH_LONG).show();
            return;
        }

        // Prepare data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String instanceID = InstanceID.getInstance(this).getId();
        String groupName = editTextGroup.getText().toString();
        boolean flagRegisteredServer = sharedPreferences.getBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false);
        GroupUser user = new GroupUser(instanceID, groupName);

        // Registration
        if (!flagRegisteredServer) {
            Toast.makeText(this, "Server is inaccessible, maybe network is down", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vernon debug
        Log.d(LOG_TAG, instanceID+" joins group "+groupName);

        // Execute uploading thread
        if (mJoinGroupTask != null && mJoinGroupTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(LOG_TAG, "Last message is still sending");
            Toast.makeText(this, "Last message is still sending, please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        mJoinGroupTask = new JoinGroupTask();
        mJoinGroupTask.execute(user);
    }

    /**
     * Implementation of AsyncTask, to send a member to the server in the background away from
     * the UI thread and get the item URL generated by the server.
     */
    private class JoinGroupTask extends AsyncTask<GroupUser, Void, String> {

        // Return the item URL generated by the server.
        // Return null when failed
        @Override
        protected String doInBackground(GroupUser... args) {
            // Deal with one message at a time
            if (args.length < 1) {
                Log.e(LOG_TAG, "No specified group");
                return null;
            }

            try {
                // Upload
                return send(args[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, e + "in joining group " + args[0].getGroupname());
            }

            return null;
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(String arg) {
            if (arg == null) {
                Log.d(LOG_TAG, "Successfully join a group");
                Toast.makeText(getApplicationContext(), "Finish joining a group", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(LOG_TAG, "Failed to join a group because " + arg);
                Toast.makeText(getApplicationContext(), arg, Toast.LENGTH_SHORT).show();
            }
        }

        // Send a message to the server in Google APP engine
        private String send(GroupUser user) throws IOException {
            // Prepare URL https://aaa.appspot.com/api/0.1/user-messages
            // The target user is myself
            URL url = new URL(MyConstants.GROUP_URL);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            int size;
            byte[] data;
            OutputStream out;
            // Set content type
            urlConnection.setRequestProperty("Content-Type", "application/json");

            try {
                // Set HTTP method
                urlConnection.setRequestMethod("PUT");

                // To upload data to a web server, configure the connection for output using setDoOutput(true). It will use POST if setDoOutput(true) has been called.
                urlConnection.setDoOutput(true);

                // Convert item to JSON string
                data = new Gson().toJson(user).getBytes();

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

                if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    return responseMsg;
                }
                // Get group URL
                String groupUrl = urlConnection.getHeaderField("Location");
                Log.d(LOG_TAG, "Group URL " + groupUrl);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return "Exception";
        }
    }

    private void leaveGroup() {
        // Check network connection ability and then access Google Cloud Storage
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_network_connection_available), Toast.LENGTH_LONG).show();
            return;
        }

        // Prepare data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String instanceID = InstanceID.getInstance(this).getId();
        String groupName = editTextGroup.getText().toString();
        boolean flagRegisteredServer = sharedPreferences.getBoolean(MyConstants.SENT_TOKEN_TO_SERVER, false);
        GroupUser user = new GroupUser(instanceID, groupName);

        // Registration
        if (!flagRegisteredServer) {
            Toast.makeText(this, "Server is inaccessible, maybe network is down", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vernon debug
        Log.d(LOG_TAG, instanceID + " leaves group " + groupName);

        // Execute uploading thread
        if (mLeaveGroupTask != null && mLeaveGroupTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(LOG_TAG, "Last message is still sending");
            Toast.makeText(this, "Last message is still sending, please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        mLeaveGroupTask = new LeaveGroupTask();
        mLeaveGroupTask.execute(user);
    }

    /**
     * Implementation of AsyncTask, to send a member to the server in the background away from
     * the UI thread and get the item URL generated by the server.
     */
    private class LeaveGroupTask extends AsyncTask<GroupUser, Void, String> {

        // Return the item URL generated by the server.
        // Return null when failed
        @Override
        protected String doInBackground(GroupUser... args) {
            // Deal with one message at a time
            if (args.length < 1) {
                Log.e(LOG_TAG, "No specified group");
                return null;
            }

            try {
                // Upload
                send(args[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, e + "in leaving group " + args[0].getGroupname());
            }

            return null;
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(String arg) {
            if (arg == null) {
                Log.d(LOG_TAG, "Successfully leave a group");
                Toast.makeText(getApplicationContext(), "Successfully leave a group", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(LOG_TAG, "Failed to leave a group because " + arg);
                Toast.makeText(getApplicationContext(), arg, Toast.LENGTH_SHORT).show();
            }
        }

        // Send a message to the server in Google APP engine
        private String send(GroupUser user) throws IOException {
            HttpsURLConnection urlConnection = null;
            try {
                // Prepare URL https://aaa.appspot.com/api/0.1/groups/xxx, xxx is the group name
                // Since it's not allowed to send HTTP BODY in DELETE method. Give group name in URL and give instance ID in header instead.
                URL url = new URL(MyConstants.GROUP_URL + "/" + user.getGroupname());
                urlConnection = (HttpsURLConnection) url.openConnection();

                // Set HTTP method
                urlConnection.setRequestMethod("DELETE");

                // Set instance ID in HTTP header
                urlConnection.setRequestProperty(MyConstants.HTTP_HEADER_INSTANCE_ID, user.getInstanceid());

                // Set timeout
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);

                // Send and get response
                // getResponseCode() will automatically trigger connect()
                int responseCode = urlConnection.getResponseCode();
                String responseMsg = urlConnection.getResponseMessage();
                Log.d(LOG_TAG, "Response " + responseCode + " " + responseMsg);

                if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    return responseMsg;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return "Exception";
        }
    }
}
