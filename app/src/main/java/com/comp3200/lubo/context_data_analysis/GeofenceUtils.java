package com.comp3200.lubo.context_data_analysis;

/**
 * Created by Lubo on 18.1.2015.
 * 
 * This class definec constants used by the app
 *
 * Code heavily based on Google's examples
 */
public class GeofenceUtils {

    // Used to track what type of geofence removal request was made.
    public enum REMOVE_TYPE {INTENT, LIST}

    // Used to track what type of request is in process
    public enum REQUEST_TYPE {ADD, REMOVE}

    // Intent actions
    public static final String ACTION_CONNECTION_ERROR = "com.lubo.3200.context_data_analysis.ACTION_CONNECTION_ERROR";

    public static final String ACTION_CONNECTION_SUCCESS = "com.lubo.3200.context_data_analysis.ACTION_CONNECTION_SUCCESS";

    public static final String ACTION_GEOFENCES_ADDED = "com.lubo.3200.context_data_analysis.ACTION_GEOFENCES_ADDED";

    public static final String ACTION_GEOFENCES_REMOVED = "com.lubo.3200.context_data_analysis.ACTION_GEOFENCES_DELETED";

    public static final String ACTION_GEOFENCE_ERROR = "com.lubo.3200.context_data_analysis.ACTION_GEOFENCES_ERROR";

    public static final String ACTION_GEOFENCE_TRANSITION = "com.lubo.3200.context_data_analysis.ACTION_GEOFENCE_TRANSITION";

    public static final String ACTION_GEOFENCE_TRANSITION_ERROR = "com.lubo.3200.context_data_analysis.ACTION_GEOFENCE_TRANSITION_ERROR";

    // The Intent category used by the app
    public static final String CATEGORY_CONTEXT_ANALYSIS = "com.lubo.3200.context_data_analysis.CATEGORY_CONTEXT_ANALYSIS";

    // Keys for extended data in Intents
    public static final String EXTRA_CONNECTION_CODE = "com.example.android.EXTRA_CONNECTION_CODE";

    public static final String EXTRA_CONNECTION_ERROR_CODE = "com.lubo.3200.context_data_analysis.EXTRA_CONNECTION_ERROR_CODE";

    public static final String EXTRA_CONNECTION_ERROR_MESSAGE = "com.lubo.3200.context_data_analysis.EXTRA_CONNECTION_ERROR_MESSAGE";

    public static final String EXTRA_GEOFENCE_STATUS = "com.lubo.3200.context_data_analysis.EXTRA_GEOFENCE_STATUS";


    // Keys for flattened geofences stored in SharedPreferences
    public static final String KEY_LATITUDE = "com.lubo.3200.context_data_analysis.KEY_LATITUDE";

    public static final String KEY_LONGITUDE = "com.lubo.3200.context_data_analysis.KEY_LONGITUDE";

    public static final String KEY_RADIUS = "com.lubo.3200.context_data_analysis.KEY_RADIUS";

    public static final String KEY_EXPIRATION_DURATION = "com.lubo.3200.context_data_analysis.KEY_EXPIRATION_DURATION";

    public static final String KEY_TRANSITION_TYPE = "com.lubo.3200.context_data_analysis.KEY_TRANSITION_TYPE";

    // The prefix for flattened geofence keys
    public static final String KEY_PREFIX = "com.lubo.3200.context_data_analysis.KEY";

    // Invalid values, used to test geofence storage when retrieving geofences
    public static final long INVALID_LONG_VALUE = -999l;

    public static final float INVALID_FLOAT_VALUE = -999.0f;

    public static final int INVALID_INT_VALUE = -999;


    // Constants used in verifying the correctness of input values
    public static final double MAX_LATITUDE = 90.d;

    public static final double MIN_LATITUDE = -90.d;

    public static final double MAX_LONGITUDE = 180.d;

    public static final double MIN_LONGITUDE = -180.d;

    public static final float MIN_RADIUS = 1f;

    // Request code to send to Google Play services
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

}
