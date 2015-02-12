package com.comp3200.lubo.context_data_analysis;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

/**
 * Created by Lubo on 11.2.2015.
 *
 * A class that starts or stops the scanning of user activity
 */
public class ActivityRecognitionScan implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    // Reference to the calling activity
    private final Activity mActivity;
    // PendingIntent used to send updates about the activity
    private PendingIntent mActivityScanPendingIntent;
    // ActivityRecognition client used to determine the current activity
    private ActivityRecognitionClient mClient;
    // Custom logger
    private Logger log;
    // Tag for logs
    private final String APPTAG = "ActivityRecgonitionScan";

    public ActivityRecognitionScan(Activity activityContext){
        // Save the context
        mActivity = activityContext;
        // Instantiate logger
        log = new Logger(activityContext);
    }

    // Start the scanning of activity recognition - activity won't be recognised until the client has connected
    public void startActivityRecognitionScan() {
        // Instantiate the ActivityRecognitionClient and connect it to Location Services
        mClient = new ActivityRecognitionClient(mActivity, this, this);
        mClient.connect();
        Message status = new Message(APPTAG, "Activity recognition scanning started");
        log.addMessage(status);
    }

    // Stop activity recognition scanning
    public void stopActivityRecognitionScan(){
        try{
            mClient.removeActivityUpdates(mActivityScanPendingIntent);
            Message status = new Message(APPTAG, "Activity recognition scan stopped");
            log.addMessage(status);
        }catch (Exception e){
            // Most likely the scan was not set up
            String msg = "Error stoping activity recognition scan. Make sure the scan was started";
            Message status = new Message(APPTAG, msg);
            log.addMessage(status);
        }

    }

    // Once the connection has been established, make a request for activity updates
    @Override
    public void onConnected(Bundle bundle) {
        Message status = new Message(APPTAG, "Client connected");
        log.addMessage(status);
        Intent intent = new Intent(mActivity, ActivityRecognitionService.class);
        mActivityScanPendingIntent = PendingIntent.getService(mActivity,
                                                              0,
                                                              intent,
                                                              PendingIntent.FLAG_UPDATE_CURRENT);
        // TODO: change detection value and turn it into a parameter
        mClient.requestActivityUpdates(10000, mActivityScanPendingIntent);
        status = new Message(APPTAG, "Activity recognition updates requested");
        log.addMessage(status);
    }

    // When disconnected, log a message and destroy the client
    @Override
    public void onDisconnected() {
        Message status = new Message(APPTAG, "Client disconnected");
        log.addMessage(status);
        mClient = null;
    }

    // If a connection request fails, log the error and try to resovle it
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // If the error has a resolution, GooglePlay services can attempt to resolve it
        Message status;
        if(connectionResult.hasResolution()){
            try{
                // TODO: change int to a paramter
                connectionResult.startResolutionForResult(mActivity, 9002);
                status = new Message(APPTAG, "Connection error. Attempting resolution");
            } catch (IntentSender.SendIntentException e) {
                String error = Log.getStackTraceString(e);
                Log.d(APPTAG, error);
                status = new Message(APPTAG, "Connection error could not be resolved");
                status.setExtra(error);
                log.addMessage(status);
            }
        }
    }
}
