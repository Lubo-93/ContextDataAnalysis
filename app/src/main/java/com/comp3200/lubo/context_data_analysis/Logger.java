package com.comp3200.lubo.context_data_analysis;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.Map;

/**
 * Created by Lubo on 19.1.2015.
 *
 * A custom logger class to which activities and methods of the app can send message objects to.
 * The messages can be saved to persistent storage using SharedPreferences.
 * Messages can be sent to a server.
 */
public class Logger {

    // SharedPreferences object for writing messages to persistent storage
    private SharedPreferences mPrefs;
    // The name for the SharedPreferences
    private final String SHARED_PREFERENCES = "LoggerPrefs";
    // Http client and response for sending the logs to a server
    private HttpClient mClient;
    private HttpResponse mResponse;



    public Logger(Context context) {
        // Create the SharedPreferences storage with private access only
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        // Instantiate the HttpClient
        mClient = new DefaultHttpClient();
    }

    // Save the messages to SharedPreferences
    public void addMessage(Message message) {
        // Get a SharedPreferences editor instance
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(getLoggerFieldKey(message), getLoggerFieldValue(message));
        // Commit the changes
        editor.commit();
    }

    // Given a message, create a key for storing it to SharedPreferences
    private String getLoggerFieldKey(Message message) {
        String dayMonth = message.getDayMonth();
        String time = message.getTime();
        return dayMonth + "_" + time;
    }

    // Given a message, convert it to a JSON string so it can be stored in SharedPreferences
    private String getLoggerFieldValue(Message message) {
        Gson gson = new Gson();
        String converted = gson.toJson(message);
        return converted;
    }

    // Return all messages stored in SharedPreferences
    public Map<String, ?> getSavedMessages() {
        return mPrefs.getAll();
    }

    // Clear the storage
    public void clearStore() {
        // Get a SharedPreferences editor instance
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.clear();
        editor.commit();
    }

    // Sends all the messages in the set to a server



}
