package com.comp3200.lubo.context_data_analysis;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;


public class Main extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    // Base URL for REST calls to the Weather API
    private static String BASE_URL = "http://api.wunderground.com/api/";
    // Key needed to access the Weather API
    private static String API_KEY = "292e0ed8aab9e924/";
    // URL for weather condition query
    private static String CONDITIONS_QUERY = "conditions/q/";
    // An instance of Location Client
    private LocationClient mLocationClient;
    // Holds the current location
    private Location mCurrentLocation;
    // An instance of calendar used to get the current time
    private Calendar mCalendar;
    // Apache HttpClient and Response for Weather API REST calls
    private HttpClient mHttpClient;
    private HttpResponse mHttpResponse;
    // An instance of the custom logger
    private Logger mLog;
    // Handle to the label the displays the weather information in the UI
    private TextView mWeatherText;
    // Handle to the label that display the current time in the UI
    private TextView mTimeText;
    // Tag for logs
    private final String APPTAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Instantiate the LocationClient
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
        // Instantiate the HttpClient
        mHttpClient = new DefaultHttpClient();
        // Instantiate the logger
        mLog = new Logger(this);
        // Get an instance of the locale calendar
        mCalendar = Calendar.getInstance();
        // Get the handles to the editor fields in the UI
        mTimeText = (TextView) findViewById(R.id.time);
        // Display the current time in the UI
        mTimeText.setText(mCalendar.getTime().toString());
    }

    // Called when the user clicks to get location updates
    public void getUpdates(View view){
        // Intent to start LocationUpdates activity
        Intent intent = new Intent(this, LocationUpdates.class);
        startActivity(intent);
    }

    // Called when the user clicks to go to geofencing
    public void getGeofencing(View view) {
        // Intent to start Geofencing activity
        Intent intent = new Intent(this, Geofencing.class);
        startActivity(intent);
    }

    // Called when the user clicks on the Log button
    public void getLog(View view) {
        // Intent to start Log activity
        Intent intent = new Intent(this, Log.class);
        startActivity(intent);
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
    public void onConnected(Bundle bundle) {
        // Log the connection status
        Message mStatus = new Message(APPTAG, "Location client connected");
        mLog.addMessage(mStatus);
        TextView mLocationText = (TextView) findViewById(R.id.current_location);
        mWeatherText = (TextView) findViewById(R.id.weather);
        // Get the last known location's latitude and longitude
        mCurrentLocation = mLocationClient.getLastLocation();
        Double lat = mCurrentLocation.getLatitude();
        Double lon = mCurrentLocation.getLongitude();
        // Display it in the text view
        mLocationText.setText("Lat: " + lat + "\n" + "Lon: " + lon);
        // URL for REST call to get the weather conditions at the current location
        String mRestURL = BASE_URL + API_KEY + CONDITIONS_QUERY + lat + "," + lon + ".json";
        RequestWeather mRequestWeather = new RequestWeather();
        mRequestWeather.execute(mRestURL);

    }

    @Override
    public void onDisconnected() {
        // Log the connection status
        Message mStatus = new Message(APPTAG, "Location client disconnected");
        mLog.addMessage(mStatus);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Log the connection status
        Message mStatus = new Message(APPTAG, "Location client failed to connect");
        mLog.addMessage(mStatus);
    }

    class RequestWeather extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... urls) {
            try {
                // Create the HttpClient
                HttpClient mHttpClient = new DefaultHttpClient();
                // Execute the GET request
                HttpResponse mHttpResponse = mHttpClient.execute(new HttpGet(urls[0]));
                // If the request is successful display the weather information in the UI
                if (mHttpResponse.getStatusLine().getStatusCode() == 200) {
                    // Get the JSON returned by the API as an InputStream
                    InputStream mInputStream = mHttpResponse.getEntity().getContent();
                    BufferedReader mReader = new BufferedReader(new InputStreamReader(mInputStream, "UTF-8"));
                    // Build a String from the InputStream
                    String result = "";
                    String mLine;
                    while ((mLine = mReader.readLine()) != null) {
                        result += mLine;
                    }
                    // Create a JSON object from the result String
                    JSONObject mJSONResponse = new JSONObject(result);
                    // Traverse the JSON stream
                    JSONObject mJSONSubObj = new JSONObject(mJSONResponse.getString("current_observation"));
                    // Get the temperature and weather
                    String mTemperature = mJSONSubObj.getString("temp_c");
                    String mWeather = mJSONSubObj.getString("weather");
                    String data = mTemperature + "C, " + mWeather;
                    return data;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mWeatherText.setText(result);
            // Log the success
            Message mSuccess = new Message(APPTAG, "Weather information retrieved successfully");
            mLog.addMessage(mSuccess);
        }
    }
}
