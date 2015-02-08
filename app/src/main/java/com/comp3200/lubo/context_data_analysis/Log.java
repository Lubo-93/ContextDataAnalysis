package com.comp3200.lubo.context_data_analysis;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.AbstractSet;
import java.util.Map;
import java.util.TreeSet;


public class Log extends ActionBarActivity {

    // Local instance of the mLogger
    private Logger mLog;
    // The set of messages stored in persistent storage
    private Map<String, ?> mMessages;
    // A handle to the LinearLayout in the UI that will contain the messsages
    private LinearLayout mLogLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        // Instantiate the mLogger
        mLog = new Logger(this);
        // Get the set of messages stored in Shared Preferences
        mMessages = mLog.getSavedMessages();
        // Get the handle to the layout
        mLogLayout = (LinearLayout) findViewById(R.id.log_layout);
        // Display the log
        displayMessages();
    }

    // Display the log of messages in the UI
    private void displayMessages(){
        // The TextView to be added to the UI
        TextView temp;
        // Sort the keys in the map
        AbstractSet<String> keys = new TreeSet<String>(mMessages.keySet());
        // Iterate over the sorted set of messages and display its contents in a TextView
        for(String k : keys) {
            temp = new TextView(this);
            Gson gson = new Gson();
            String json = (String) mMessages.get(k);
            Message message = gson.fromJson(json, Message.class);
            temp.setText(k + " FROM: " + message.getIssuer() + " MESSAGE: " + message.getContent());
            mLogLayout.addView(temp);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log, menu);
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
}
