package com.sdis.g0102.dsmn.fcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sdis.g0102.dsmn.EventDetailsActivity;
import com.sdis.g0102.dsmn.MainActivity;
import com.sdis.g0102.dsmn.R;
import com.sdis.g0102.dsmn.RecentEventView;
import com.sdis.g0102.dsmn.api.domain.StreetEvent;

/**
 * Created by André on 28/05/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    public MyFirebaseMessagingService() {}

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        String notification_body = remoteMessage.getNotification().getBody();
        Log.d(TAG, "Notification Message Body: " + notification_body);

        int test_id = 0;
        StreetEvent.Type test_type = StreetEvent.Type.CAR_CRASH;
        String test_description = "I'm just testing the event description.";
        sendNotification(test_id, test_type,test_description);
    }

    private void sendNotification(int event_id, StreetEvent.Type event_type, String event_description) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        /*switch(event_type) {
            case CAR_CRASH:
                mBuilder.setSmallIcon(R.drawable.notification_crash);
                break;
            case TRAFFIC_STOP:
                mBuilder.setSmallIcon(R.drawable.notification_stop);
                break;
            case HIGH_TRAFFIC:
                mBuilder.setSmallIcon(R.drawable.notification_traffic);
                break;
            case SPEED_RADAR:
                mBuilder.setSmallIcon(R.drawable.notification_camera);
                break;
        }*/
        mBuilder.setContentTitle("New event in your area.");
        mBuilder.setContentText(event_description);

        //Bundle b = this.getIntent().getExtras();
        //event_id = b.getInt(RecentEventView.EVENT_ID);
        //my_event = b.getBoolean(RecentEventView.EVENT_IS_MINE);

        Intent resultIntent = new Intent(this, EventDetailsActivity.class);
        resultIntent.putExtra(RecentEventView.EVENT_ID, event_id);
        resultIntent.putExtra(RecentEventView.EVENT_IS_MINE, false);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = event_id;
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

//        Intent intent = new Intent(act, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(act, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
//        //NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//        notificationBuilder.setContentTitle("FCM Message");
//        notificationBuilder.setContentText(event_description);
//        notificationBuilder.setAutoCancel(true);
//        notificationBuilder.setSound(defaultSoundUri);
//        notificationBuilder.setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_stat_ic_notification)
//                .setContentTitle("FCM Message")
//                .setContentText(messageBody)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
