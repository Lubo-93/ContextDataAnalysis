package com.comp3200.lubo.context_data_analysis;

/**
 * Created by Lubo on 9.1.2015.
 */

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for connecting to Location Services and requesting geofences.
 *
 * To use a GeofenceRequester, instantiate it and call AddGeofence(). Everything else is done
 * automatically.
 *
 * Code heavily based on Google's examples
 */
public class GeofenceRequester implements
        LocationClient.OnAddGeofencesResultListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    // Storage for a reference to the calling client
    private final Activity mActivity;
    // Stores the PendingIntent used to send geofence transitions back to the app
    private PendingIntent mGeofencePendingIntent;
    // Stores the current list of geofences
    private ArrayList<Geofence> mCurrentGeofences;
    // Stores the current instantiation of the location client
    private LocationClient mLocationClient;
    // Flag that indicates whether an add or remove request is underway
    private boolean mInProgress;
    // Tag for logs
    private final String APPTAG = "GeofenceRequester";

    public GeofenceRequester(Activity activityContext) {
        // Save the context
        mActivity = activityContext;
        // Initialize the globals to null
        mGeofencePendingIntent = null;
        mLocationClient = null;
        mInProgress = false;
    }


    // Set the "in progress" flag from a caller. This allows callers to re-set a request that failed but was later fixed
    public void setInProgressFlag(boolean flag) {
        mInProgress = flag;
    }

    // Get the current in progress status.
    public boolean getInProgressFlag() {
        return mInProgress;
    }

    // Returns the current PendingIntent to the caller.
    public PendingIntent getRequestPendingIntent() {
        return createRequestPendingIntent();
    }

    // Start adding geofences. Save the geofences, then start adding them by requesting a connection
    public void addGeofences(List<Geofence> geofences) throws UnsupportedOperationException {
        // Save the geofences so that they can be sent to Location Services once the connection is available
        mCurrentGeofences = (ArrayList<Geofence>) geofences;
        // If a request is not already in progress, toggle the flag and request a connection to Location Services
        if (!mInProgress) {
            mInProgress = true;
            requestConnection();
            // If a request is in progress, throw an exception and stop the request
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        getLocationClient().connect();
    }

    // Get the current location client, or create a new one if necessary
    private GooglePlayServicesClient getLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(mActivity, this, this);
        }
        return mLocationClient;
    }

    // Once the connection is available, send a request to add the Geofences
    private void continueAddGeofences() {
        // Get a PendingIntent that Location Services issues when a geofence transition occurs
        mGeofencePendingIntent = createRequestPendingIntent();
        // Send a request to add the current geofences
        mLocationClient.addGeofences(mCurrentGeofences, mGeofencePendingIntent, this);

    }

    // Handle the result of adding the geofences
    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        // Create a broadcast Intent that notifies other components of success or failure
        Intent broadcastIntent = new Intent();
        // Temp storage for messages
        String msg;
        // If adding the geocodes was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            // Create a message containing all the geofence IDs added.
            msg = mActivity.getString(R.string.add_geofences_result_success, Arrays.toString(geofenceRequestIds));
            // In debug mode, log the result
            Log.d(APPTAG, msg);
            // Create an Intent to broadcast to the app
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_ADDED)
                    .addCategory(GeofenceUtils.CATEGORY_CONTEXT_ANALYSIS)
                    .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
            // If adding the geofences failed
        } else {
            // Create a message containing the error code and the list of geofence IDs you tried to add
            msg = mActivity.getString(R.string.add_geofences_result_failure, statusCode,
                                         Arrays.toString(geofenceRequestIds)
            );
            // Log an error
            Log.e(APPTAG, msg);
            // Create an Intent to broadcast to the app
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR).addCategory(GeofenceUtils.CATEGORY_CONTEXT_ANALYSIS)
                                                                           .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
        }
        // Broadcast whichever result occurred
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(broadcastIntent);
        // Disconnect the location client
        requestDisconnection();
    }


    // Get a location client and disconnect from Location Services
    private void requestDisconnection() {
        // A request is no longer in progress
        mInProgress = false;
        getLocationClient().disconnect();
    }


    // Once the location client is connected, add the requested geofences.
    @Override
    public void onConnected(Bundle arg0) {
        // If debugging, log the connection
        Log.d(APPTAG, mActivity.getString(R.string.connected));
        continueAddGeofences();
    }

    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // In debug mode, log the disconnection
        Log.d(APPTAG, mActivity.getString(R.string.disconnected));
        // Destroy the current location client
        mLocationClient = null;
    }

    /**
     * Get a PendingIntent to send with the request to add Geofences. Location Services issues
     * the Intent inside this PendingIntent whenever a geofence transition occurs for the current
     * list of geofences.
     */
    private PendingIntent createRequestPendingIntent() {

        // If the PendingIntent already exists
        if (null != mGeofencePendingIntent) {
            // Return the existing intent
            return mGeofencePendingIntent;
            // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService
            Intent intent = new Intent(mActivity, ReceiveTransitionsIntentService.class);
            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
            return PendingIntent.getService(
                    mActivity,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
    //If a connection or disconnection request fails, report the error
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Turn off the request flag
        mInProgress = false;
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(mActivity, GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // Thrown if Google Play services canceled the original PendingIntent
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        /*
         * If no resolution is available, put the error code in an error Intent
         * and broadcast it back to the main Activity.
         * The Activity then displays an error dialog.
         */
        } else {
            Intent errorBroadcastIntent = new Intent(GeofenceUtils.ACTION_CONNECTION_ERROR);
            errorBroadcastIntent.addCategory(GeofenceUtils.CATEGORY_CONTEXT_ANALYSIS).putExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE,
                    connectionResult.getErrorCode());
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(errorBroadcastIntent);
        }
    }
}
