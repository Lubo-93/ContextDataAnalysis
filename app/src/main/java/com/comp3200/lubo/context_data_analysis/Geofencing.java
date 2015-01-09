package com.comp3200.lubo.context_data_analysis;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
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
    List<Geofence> mGeofenceList;
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        displayGeofences();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            }catch (Exception e){
                Log.e(APPTAG, "AddressConversionError", e);
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
}
