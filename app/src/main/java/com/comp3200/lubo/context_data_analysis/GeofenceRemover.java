package com.comp3200.lubo.context_data_analysis;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Lubo on 18.1.2015.
 *
 * Class for connecting to Location Services and removing geofences.
 *
 * To use a GeofenceRemover, instantiate it, then call either RemoveGeofencesById() or
 * RemoveGeofencesByIntent(). Everything else is done automatically.
 *
 * Code heavily based on Google's examples
 */
public class GeofenceRemover implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnRemoveGeofencesResultListener {

    // Storage for a context from the calling client
    private Context mContext;
    // Stores the current list of geofences
    private List<String> mCurrentGeofenceIds;
    // Stores the current instantiation of the location client
    private LocationClient mLocationClient;
    // The PendingIntent sent in removeGeofencesByIntent
    private PendingIntent mCurrentIntent;
    // Record the type of removal. This allows continueRemoveGeofences to call the appropriate removal request method
    private GeofenceUtils.REMOVE_TYPE mRequestType;
    // Flag that indicates whether an add or remove request is underway
    private boolean mInProgress;
    // Tag for logging
    private final String APPTAG = "GeofenceRemover";

    // Construct a GeofenceRemover for the current Context
    public GeofenceRemover(Context context) {
        // Save the context
        mContext = context;
        // Initialize the globals to null
        mCurrentGeofenceIds = null;
        mLocationClient = null;
        mInProgress = false;
    }

    // Set the "in progress" flag from a caller. This allows callers to re-set a request that failed but was later fixed.
    public void setInProgressFlag(boolean flag) {
        mInProgress = flag;
    }

    // Get the current in progress status.
    public boolean getInProgressFlag() {
        return mInProgress;
    }

    /**
     * Remove the geofences in a list of geofence IDs. To remove all current geofences associated
     * with a request, you can also call removeGeofencesByIntent.
     */
    public void removeGeofencesById(List<String> geofenceIds) throws IllegalArgumentException, UnsupportedOperationException {
        // If the List is empty or null, throw an error immediately
        if ((null == geofenceIds) || (geofenceIds.size() == 0)) {
            throw new IllegalArgumentException();
            // Set the request type, store the List, and request a location client connection.
        } else {
            // If a removal request is not already in progress, continue
            if (!mInProgress) {
                mRequestType = GeofenceUtils.REMOVE_TYPE.LIST;
                mCurrentGeofenceIds = geofenceIds;
                requestConnection();
                // If a removal request is in progress, throw an exception
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * Remove the geofences associated with a PendIntent. The PendingIntent is the one used
     * in the request to add the geofences; all geofences in that request are removed. To remove
     * a subset of those geofences, call removeGeofencesById().
     */
    public void removeGeofencesByIntent(PendingIntent requestIntent) {
        // If a removal request is not in progress, continue
        if (!mInProgress) {
            // Set the request type, store the List, and request a location client connection.
            mRequestType = GeofenceUtils.REMOVE_TYPE.INTENT;
            mCurrentIntent = requestIntent;
            requestConnection();
            // If a removal request is in progress, throw an exception
        } else {

            throw new UnsupportedOperationException();
        }
    }

    /**
     * Once the connection is available, send a request to remove the Geofences. The method
     * signature used depends on which type of remove request was originally received.
     */
    private void continueRemoveGeofences() {
        switch (mRequestType) {

            // If removeGeofencesByIntent was called
            case INTENT :
                mLocationClient.removeGeofences(mCurrentIntent, this);
                break;

            // If removeGeofencesById was called
            case LIST :
                mLocationClient.removeGeofences(mCurrentGeofenceIds, this);
                break;
        }
    }


    // Request a connection to Location Services
    private void requestConnection() {
        getLocationClient().connect();
    }

    // Get the current location client, or create a new one if necessary
    private GooglePlayServicesClient getLocationClient() {
        if (mLocationClient == null) {

            mLocationClient = new LocationClient(mContext, this, this);
        }
        return mLocationClient;
    }

    // When the request to remove geofences by PendingIntent returns, handle the result.
    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent requestIntent) {
        // Create a broadcast Intent that notifies other components of success or failure
        Intent broadcastIntent = new Intent();
        // If removing the geofences was successful
        if (statusCode == LocationStatusCodes.SUCCESS) {
            // In debug mode, log the result
            Log.d(APPTAG, mContext.getString(R.string.remove_geofences_intent_success));
            // Set the action and add the result message
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);
            broadcastIntent.putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, mContext.getString(R.string.remove_geofences_intent_success));
            // If removing the geocodes failed
        } else {
            // Always log the error
            Log.e(APPTAG, mContext.getString(R.string.remove_geofences_intent_failure, statusCode));
            // Set the action and add the result message
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
            broadcastIntent.putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, mContext.getString(R.string.remove_geofences_intent_failure, statusCode));
        }
        // Broadcast the Intent to all components in this app
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
        // Disconnect the location client
        requestDisconnection();
    }

    // When the request to remove geofences by IDs returns, handle the result.
    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
        // Create a broadcast Intent that notifies other components of success or failure
        Intent broadcastIntent = new Intent();
        // Temp storage for messages
        String msg;
        // If removing the geocodes was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            // Create a message containing all the geofence IDs removed.
            msg = mContext.getString(R.string.remove_geofences_id_success, Arrays.toString(geofenceRequestIds));
            // In debug mode, log the result
            Log.d(APPTAG, msg);
            // Create an Intent to broadcast to the app
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED).addCategory(GeofenceUtils.CATEGORY_CONTEXT_ANALYSIS)
                                                                               .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
            // If removing the geocodes failed
        } else {
            // Create a message containing the error code and the list of geofence IDs you tried to remove
            msg = mContext.getString(R.string.remove_geofences_id_failure,
                                      statusCode,
                                      Arrays.toString(geofenceRequestIds)
            );
            // Log an error
            Log.e(APPTAG, msg);
            // Create an Intent to broadcast to the app
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR).addCategory(GeofenceUtils.CATEGORY_CONTEXT_ANALYSIS)
                                                                             .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
        }
        // Broadcast whichever result occurred
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
        // Disconnect the location client
        requestDisconnection();
    }

    // Get a location client and disconnect from Location Services
    private void requestDisconnection() {
        // A request is no longer in progress
        mInProgress = false;
        getLocationClient().disconnect();
        /*
         * If the request was done by PendingIntent, cancel the Intent. This prevents problems if
         * the client gets disconnected before the disconnection request finishes; the location
         * updates will still be cancelled.
         */
        if (mRequestType == GeofenceUtils.REMOVE_TYPE.INTENT) {
            mCurrentIntent.cancel();
        }

    }

    // Once the location client is connected, remove the requested geofences
    @Override
    public void onConnected(Bundle arg0) {
        // If debugging, log the connection
        Log.d(APPTAG, mContext.getString(R.string.connected));
        // Continue the request to remove the geofences
        continueRemoveGeofences();
    }

    @Override
    public void onDisconnected() {
        // A request is no longer in progress
        mInProgress = false;
        // In debug mode, log the disconnection
        Log.d(APPTAG, mContext.getString(R.string.disconnected));
        // Destroy the current location client
        mLocationClient = null;
    }

    // If a connection or disconnection request fails, report the error
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // A request is no longer in progress
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
                connectionResult.startResolutionForResult((Activity) mContext, GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // Thrown if Google Play services canceled the original PendingIntent
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }

        /*
         * If no resolution is available, put the error code in
         * an error Intent and broadcast it back to the main Activity.
         * The Activity then displays an error dialog.
         */
        } else {
            Intent errorBroadcastIntent = new Intent(GeofenceUtils.ACTION_CONNECTION_ERROR);
            errorBroadcastIntent.addCategory(GeofenceUtils.CATEGORY_CONTEXT_ANALYSIS)
                                 .putExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE, connectionResult.getErrorCode());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(errorBroadcastIntent);
        }
    }
}
