package com.comp3200.lubo.context_data_analysis;

/**
 * Created by Lubo on 4.12.2014.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that triggered
 * the event.
 *
 * Code heavily based on Google's examples
 */
public class ReceiveTransitionsIntentService extends IntentService {

    // Tag for logs
    private final String APPTAG = "ReceiveTransitionsIntentService";
    // Instance of the logger class
    private Logger log;


    // Sets an identifier for this class' background thread
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");

    }

    /**
     * Handles incoming intents
     * @param intent The Intent sent by Location Services. This Intent is provided
     * to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Instantiate the logger
        log = new Logger(this);
        // Create a local broadcast Intent and set its category
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(GeofenceUtils.CATEGORY_CONTEXT_ANALYSIS);
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Message status = new Message(APPTAG, String.valueOf(errorCode));
            log.addMessage(status);
            // Set the action and error message for the broadcast intent
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorCode);
            // Broadcast the error *locally* to other components in this app
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
            // If there's no error, get the transition type and create a notification
        } else {
            // Get the type of transition (entry or exit)
            int transition = LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                // Post a notification
                List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
                String[] geofenceIds = new String[geofences.size()];
                for (int index = 0; index < geofences.size() ; index++) {
                    geofenceIds[index] = geofences.get(index).getRequestId();
                }
                String ids = TextUtils.join(", ",geofenceIds);
                String transitionType = getTransitionString(transition);

                sendNotification(transitionType, ids);

                // Log the transition type and a message
                Message status = new Message(APPTAG, getString(R.string.geofence_transition_notification_title, transitionType, ids));
                log.addMessage(status);
                status = new Message(APPTAG, getString(R.string.geofence_transition_notification_text));
                log.addMessage(status);

                // An invalid transition was reported
            } else {
                // Always log as an error
                Message status = new Message(APPTAG, getString(R.string.geofence_transition_invalid_type, transition));
                log.addMessage(status);

            }
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     */
    private void sendNotification(String transitionType, String ids) {

        // Create an explicit content Intent that starts the main Activity
        Intent notificationIntent = new Intent(getApplicationContext(),Main.class);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(Main.class);

        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(getString(R.string.geofence_transition_notification_title, transitionType, ids))
                                .setContentText(getString(R.string.geofence_transition_notification_text))
                                .setContentIntent(notificationPendingIntent);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    // Maps geofence transition types to their human-readable equivalents
    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
}
