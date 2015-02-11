package com.comp3200.lubo.context_data_analysis;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Lubo on 11.2.2015.
 *
 * A class that starts or stops the scanning of user activity
 */
public class ActivityRecognitionScan implements
        GoogleApiClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    // Reference to the calling activity
    private final Activity mActivity;
    // PendingIntent used to send updates about the activity
    private PendingIntent mActivityScanPendingIntent;
    
}
