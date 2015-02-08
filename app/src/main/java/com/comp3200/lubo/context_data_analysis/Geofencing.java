package com.comp3200.lubo.context_data_analysis;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class Geofencing extends ActionBarActivity {

    // Geocoder for converting an address to latitude and longitude coordinates
    private Geocoder mCoder;
    // The home and work flat geofence objects
    private SimpleGeofence mHomeGeofence;
    private SimpleGeofence mWorkGeofence;
    // Local Geofence flat storage
    private SimpleGeofenceStore mGeoStore;
    // List of the LocationServices Geofence objects
    private List<Geofence> mGeofenceList;
    // Store the current type of geofence request
    private GeofenceUtils.REQUEST_TYPE mRequestType;
    // Store the current type of geofence removal
    private GeofenceUtils.REMOVE_TYPE mRemoveType;
    // Add geofences handler
    private GeofenceRequester mGeofenceRequester;
    // Remove geofences handler
    private GeofenceRemover mGeofenceRemover;
    // Receives updates from connection listeners and the geofence transition service
    private GeofenceReceiver mBroadcastReceiver;
    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;
    // Store the list of geofences to remove
    private List<String> mGeofenceIdsToRemove;

    // Handles to the home and work addresses in the UI
    private EditText mHomeAddress;
    private EditText mWorkAddress;
    // Handles to the home and work radiuses in the UI
    private EditText mHomeRadInput;
    private EditText mWorkRadInput;
    // Handles to the home geofence information in the UI
    private TextView mHomeIdView;
    private TextView mHomeLatView;
    private TextView mHomeLngView;
    private TextView mHomeRadView;
    // Handles to the work geofence information in the UI
    private TextView mWorkIdView;
    private TextView mWorkLatView;
    private TextView mWorkLngView;
    private TextView mWorkRadView;

    // Home geofence coordinates and radius
    private double mHomeLat;
    private double mHomeLng;
    private float mHomeRad;
    // Work geofence coordinates and radius
    private double mWorkLat;
    private double mWorkLng;
    private float mWorkRad;

    // Instance of the custom logger class
    private Logger log;
    // Tag for logs
    private static final String APPTAG = "Geofencing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the UI
        setContentView(R.layout.activity_geofencing);
        // Instantiate the Geocoder
        mCoder = new Geocoder(this, Locale.getDefault());
        // Instantiate the Geostore
        mGeoStore = new SimpleGeofenceStore(this);
        // Instantiate the Geofence list
        mGeofenceList = new ArrayList<Geofence>(2);
        // Instantiate the broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new GeofenceReceiver();
        // Instantiate the intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();
        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);
        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);
        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
        // Set the category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_CONTEXT_ANALYSIS);
        // Instantiate the Geofence requester and remover
        mGeofenceRequester = new GeofenceRequester(this);
        mGeofenceRemover = new GeofenceRemover(this);
        // Instantiate the logger
        log = new Logger(this);

        // Get the handles to the editor fields in the UI
        mHomeAddress = (EditText)findViewById(R.id.home_address);
        mWorkAddress = (EditText)findViewById(R.id.work_address);

        mHomeRadInput = (EditText)findViewById(R.id.home_rad_input);
        mWorkRadInput = (EditText)findViewById(R.id.work_rad_input);
        // Get the handles to the textview fields in the UI
        mHomeIdView = (TextView)findViewById(R.id.home_id);
        mHomeLatView = (TextView)findViewById(R.id.home_lat);
        mHomeLngView = (TextView)findViewById(R.id.home_lng);
        mHomeRadView = (TextView)findViewById(R.id.home_rad);

        mWorkIdView = (TextView)findViewById(R.id.work_id);
        mWorkLatView = (TextView)findViewById(R.id.work_lat);
        mWorkLngView = (TextView)findViewById(R.id.work_lng);
        mWorkRadView = (TextView)findViewById(R.id.work_rad);
    }

    // Save the current geofence settings in the geo store
    @Override
    protected void onPause() {
        super.onPause();
    }

    // Whenever the Activity resumes, reconnect the client to Location Services and reload the last geofences that were set
    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver to receive status updates
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
        // Display the geofences information in the UI, if any exist
        displayGeofences();
    }

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * GeofenceRemover and GeofenceRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     * calls
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {
            // If the request code matches the code sent in onConnectionFailed
            case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:
                        // If the request was to add geofences
                        if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {
                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);
                            // Restart the process of adding the current geofences
                            mGeofenceRequester.addGeofences(mGeofenceList);
                            // If the request was to remove geofences
                        } else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType ){
                            // Toggle the removal flag and send a new removal request
                            mGeofenceRemover.setInProgressFlag(false);
                            // If the removal was by Intent
                            if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {
                                // Restart the removal of all geofences for the PendingIntent
                                mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());
                                // If the removal was by a List of geofence IDs
                            } else {
                                // Restart the removal of the geofence list
                                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
                            }
                        }
                        break;
                    // If any other result was returned by Google Play services
                    default:
                        // Report that Google Play services was unable to resolve the problem.
                        Message mStatus = new Message(APPTAG, getString(R.string.no_resolution));
                        log.addMessage(mStatus);
                }
                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Message mStatus = new Message(APPTAG, getString(R.string.unknown_activity_request_code, requestCode));
                break;
        }
    }

    // Add geofences to the local store; called by the Add Geofences button in the UI
    public void addGeofences(View view) {
        // The addresses supplied by the user
        String[] addresses = new String[2];
        addresses[0] = mHomeAddress.getText().toString();
        addresses[1] = mWorkAddress.getText().toString();
        // The geofence radiuses supplied by the user
        mHomeRad = Float.parseFloat(mHomeRadInput.getText().toString());
        mWorkRad = Float.parseFloat(mWorkRadInput.getText().toString());
        // Convert them to geo coordinates, set the instance variables, add the geofences to the local store and display the results in the UI
        ConvertAddresses mConverter = new ConvertAddresses();
        mConverter.execute(addresses);
    }
    // Clears geofences from the local store; called by the Clear Geofences button in the UI
    public void clearGeofences(View view){
        mGeoStore.clearGeofence("home");
        mGeoStore.clearGeofence("work");
        mGeofenceList.clear();
        displayGeofences();
    }

    // Request to Location Servcies to monitor the geofences in the geo store; called by the Monitor Geofences button in the UI
    public void requestGeofences(View view) {
        /*
         * Record the request as an ADD. If a connection error occurs,
         * the app can automatically restart the add request if Google Play services
         * can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;
        // Start the request. Fail if there's already a request in progress
        try {
            // Try to add geofences
            mGeofenceRequester.addGeofences(mGeofenceList);
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, "Can't add geofences. Previous request hasn't finished", Toast.LENGTH_LONG).show();
        }
    }

    // Stop monitoring the geofences request by this Intent; called by the Unominitor Geofences button in the UI
    public void removeGeofences(View view) {
        // Record the removal as remove by Intent.
        mRemoveType = GeofenceUtils.REMOVE_TYPE.INTENT;
        // Try to make a removal request
        try {
        /*
         * Remove the geofences represented by the currently-active PendingIntent. If the
         * PendingIntent was removed for some reason, re-create it; since it's always
         * created with FLAG_UPDATE_CURRENT, an identical PendingIntent is always created.
         */
            mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, "Can't remove geofences. Previous request hasn't finished", Toast.LENGTH_LONG).show();
        }

    }

    // Stop monitoring the home geofence; called by the Unmonitor Home Geofence button in the UI
    public void removeHomeGeofence(View view) {
        // Remove the geofence by creating a List of geofences to remove and sending it to Location Services
        mGeofenceIdsToRemove = Collections.singletonList("home");
        // Record the removal as remove by list
        mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;
        // Try to remove the geofence
        try {
            mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
            // Catch errors with the provided geofence IDs
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, "Can't remove geofence. Previous request hasn't finished", Toast.LENGTH_LONG).show();
        }
    }

    // Stop monitoring the work geofence; called by the Unmonitor Work Geofence button in the UI
    public void removeWorkGeofence(View view) {
        // Remove the geofence by creating a List of geofences to remove and sending it to Location Services
        mGeofenceIdsToRemove = Collections.singletonList("work");
        // Record the removal as remove by list
        mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;
        // Try to remove the geofence
        try {
            mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
            // Catch errors with the provided geofence IDs
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, "Can't remove geofence. Previous request hasn't finished", Toast.LENGTH_LONG).show();
        }
    }

    // Display the parameters of the geofences in the store in the UI
    public void displayGeofences(){
        // Get the flat home geofence stored in the shared preferences
        SimpleGeofence mHomeGeofence = mGeoStore.getGeofence("home");
        // If it exists, display its parameters in the UI
        if (mHomeGeofence != null){
            mHomeIdView.setText(mHomeGeofence.getId());
            mHomeLatView.setText(String.valueOf(mHomeGeofence.getLatitude()));
            mHomeLngView.setText(String.valueOf(mHomeGeofence.getLongitude()));
            mHomeRadView.setText(String.valueOf(mHomeGeofence.getRadius()));
        // Otherwise inform the user the geofence is not present in the local store
        }else{
            mHomeIdView.setText("Geofence doesn't exist!");
            mHomeLatView.setText("");
            mHomeLngView.setText("");
            mHomeRadView.setText("");
        }
        // Do the same for the work geofence
        SimpleGeofence mWorkGeofence = mGeoStore.getGeofence("work");
        
        if (mWorkGeofence != null){
            mWorkIdView.setText(mWorkGeofence.getId());
            mWorkLatView.setText(String.valueOf(mWorkGeofence.getLatitude()));
            mWorkLngView.setText(String.valueOf(mWorkGeofence.getLongitude()));
            mWorkRadView.setText(String.valueOf(mWorkGeofence.getRadius()));
        }else{
            mWorkIdView.setText("Geofence doesn't exist!");
            mWorkLatView.setText("");
            mWorkLngView.setText("");
            mWorkRadView.setText("");
        }
        
    }

    // Inflate the app menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_geofencing2, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /*  Handle action bar item clicks here. The action bar will
        *  automatically handle clicks on the Home/Up button, so long
        *  as you specify a parent activity in AndroidManifest.xml.*/
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Gets the latitude and logitude coordinates from user suplied addresses
    class ConvertAddresses extends AsyncTask<String, Void, Void>{
        // Holds the addresses found by the geocoder
        List<Address> mConvertedHomeAddress;
        List<Address> mConvertedWorkAddress;

        @Override
        protected Void doInBackground(String... addresses) {
            try{
                // Convert home address string to an Address object
                mConvertedHomeAddress = mCoder.getFromLocationName(addresses[0], 1);
                // Do the same for the work address
                mConvertedWorkAddress = mCoder.getFromLocationName(addresses[1], 1);
                // TODO add check for empty array and inform the user of failure or success
                // Set the home location coordinates
                mHomeLat = mConvertedHomeAddress.get(0).getLatitude();
                mHomeLng = mConvertedHomeAddress.get(0).getLongitude();
                // Set the work location coordinates
                mWorkLat = mConvertedWorkAddress.get(0).getLatitude();
                mWorkLng = mConvertedWorkAddress.get(0).getLongitude();
            }catch (Exception e) {
                Message mStatus = new Message(APPTAG, "AddressConversionError");
                log.addMessage(mStatus);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Create the flat geofence objects and store them in the local geofence storage
            mHomeGeofence = new SimpleGeofence("home",
                                                mHomeLat,
                                                mHomeLng,
                                                mHomeRad,
                                                Geofence.NEVER_EXPIRE,
                                                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

            mGeoStore.setGeofence("home", mHomeGeofence);

            mWorkGeofence = new SimpleGeofence("work",
                                                mWorkLat,
                                                mWorkLng,
                                                mWorkRad,
                                                Geofence.NEVER_EXPIRE,
                                                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

            mGeoStore.setGeofence("work", mWorkGeofence);
            // Convert the flat geofences to LocationServices geofence objects and add them to the list
            mGeofenceList.add(mHomeGeofence.toGeofence());
            mGeofenceList.add(mWorkGeofence.toGeofence());
            // Display the geofences parameters in the UI
            displayGeofences();
        }
    }

    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    class GeofenceReceiver extends BroadcastReceiver {

        // This method is invoked when a broadcast Intent triggers the receiver
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check the action code and determine what to do
            String action = intent.getAction();
            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {
                handleGeofenceError(context, intent);
                // Intent contains information about successful addition or removal of geofences
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                            || TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {
                handleGeofenceStatus(context, intent);
                // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {
                handleGeofenceTransition(context, intent);
                // The Intent contained an invalid action
            } else {
                Message mStatus = new Message(APPTAG, getString(R.string.invalid_action_detail, action));
                log.addMessage(mStatus);
            }
        }

        // If you want to display a UI message about adding or removing geofences, put it here.
        private void handleGeofenceStatus(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Message mStatus = new Message(APPTAG, msg);
            log.addMessage(mStatus);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

        // Report geofence transitions to the UI
        private void handleGeofenceTransition(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Message mStatus = new Message(APPTAG, msg);
            log.addMessage(mStatus);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
        // Report addition or removal errors to the UI, using a Toast
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Message mStatus = new Message(APPTAG, msg);
            log.addMessage(mStatus);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
}
