package com.comp3200.lubo.context_data_analysis;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Storage for geofence values, implemented in SharedPreferences.
 * Code heavily based on Google's examples
 */
public class SimpleGeofenceStore {
    // The SharedPreferences object in which geofences are stored
    private final SharedPreferences mPrefs;
    // The name of the SharedPreferences
    private static final String SHARED_PREFERENCES = "GeoStorePrefs";

    public SimpleGeofenceStore(Context context) {
        // Create the SharedPreferences storage with private access only
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    // Returns a stored geofence by its id, or returns null if it's not found.
    public SimpleGeofence getGeofence(String id) {
        // Get the geofence's paramters; return invalid values if unsuccessful
        double lat = mPrefs.getFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE), GeofenceUtils.INVALID_FLOAT_VALUE);
        double lng = mPrefs.getFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE), GeofenceUtils.INVALID_FLOAT_VALUE);
        float radius = mPrefs.getFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS), GeofenceUtils.INVALID_FLOAT_VALUE);
        long expirationDuration = mPrefs.getLong(getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION), GeofenceUtils.INVALID_LONG_VALUE);
        int transitionType = mPrefs.getInt(getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE), GeofenceUtils.INVALID_INT_VALUE);
        // If none of the values is incorrect, return the geofence object
        if (
                lat != GeofenceUtils.INVALID_FLOAT_VALUE &&
                        lng != GeofenceUtils.INVALID_FLOAT_VALUE &&
                        radius != GeofenceUtils.INVALID_FLOAT_VALUE &&
                        expirationDuration != GeofenceUtils.INVALID_LONG_VALUE &&
                        transitionType != GeofenceUtils.INVALID_INT_VALUE) {

            // Return a true Geofence object
            return new SimpleGeofence(id, lat, lng, radius, expirationDuration, transitionType);
            // Otherwise, return null.
        } else {
            return null;
        }
    }
    // Save a geofence
    public void setGeofence(String id, SimpleGeofence geofence) {
        // Get a SharedPreferences editor instance
        SharedPreferences.Editor editor = mPrefs.edit();
        // Write the Geofence values to SharedPreferences
        editor.putFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE),(float) geofence.getLatitude());
        editor.putFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE),(float) geofence.getLongitude());
        editor.putFloat(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS), geofence.getRadius());
        editor.putLong(getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION), geofence.getExpirationDuration());
        editor.putInt(getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE), geofence.getTransitionType());
        // Commit the changes
        editor.commit();
    }
    public void clearGeofence(String id) {
        // Remove a flattened geofence object from storage by removing all of its keys
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE));
        editor.commit();
    }
    // Given a Geofence object's ID and the name of a field, return the key name of the object's values in SharedPreferences.
    private String getGeofenceFieldKey(String id, String fieldName) {
        return GeofenceUtils.KEY_PREFIX + "_" + id + "_" + fieldName;
    }

    // Check if a geofence with the given id exists in the store
    public boolean checkGeoStore(String id){
        if(mPrefs.getString(id,null) != null){
            return true;
        }
        return false;
    }
}