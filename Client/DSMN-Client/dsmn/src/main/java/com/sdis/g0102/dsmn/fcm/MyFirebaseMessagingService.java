package com.sdis.g0102.dsmn.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sdis.g0102.dsmn.EventDetailsActivity;
import com.sdis.g0102.dsmn.MainActivity;
import com.sdis.g0102.dsmn.R;
import com.sdis.g0102.dsmn.RecentEventView;
import com.sdis.g0102.dsmn.RecentEventsActivity;
import com.sdis.g0102.dsmn.api.domain.StreetEvent;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        Log.d(TAG, remoteMessage.getData().toString());
        try {
            StreetEvent event = new StreetEvent(new JSONObject(remoteMessage.getData().get("event")));
            sendNotification(event.id, event.description, event.type);
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // [END receive_message]

    private void sendNotification(int event_id, String event_description, StreetEvent.Type event_type) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra(RecentEventView.EVENT_ID, event_id);
        intent.putExtra(RecentEventView.EVENT_IS_MINE, false);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_stop)
                .setContentTitle("New event in your area.")
                .setContentText(event_description)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}