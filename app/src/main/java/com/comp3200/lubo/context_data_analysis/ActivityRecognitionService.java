package com.comp3200.lubo.context_data_analysis;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by Lubo on 12.2.2015.
 *
 * Service that handles updates received from the ActivityRecognitionClient
 */
public class ActivityRecognitionService extends IntentService {

    // Tag for logs
    private final String APPTAG = "ActivityRecognitionService";
    // Custom logger
    private Logger mLog;

    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    // Handles activity recognition updates
    @Override
    protected void onHandleIntent(Intent intent) {
        // Instantiate the logger
        mLog = new Logger(this);
        // Extract the result if it exists
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity activity = result.getMostProbableActivity();
            Message status = new Message(APPTAG, "You are currently " + getActivity(activity.getType()) +
                                                    " certainty: " + activity.getConfidence());
            mLog.addMessage(status);
        }
    }

    // Converts the detected activity to a readable format
    private String getActivity(int activity){
        switch(activity){
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.ON_FOOT:
                return "on foot";
            case DetectedActivity.ON_BICYCLE:
                return "cycling";
            case DetectedActivity.IN_VEHICLE:
                return "driving";
            default:
                return "unknown";
        }
    }
}
