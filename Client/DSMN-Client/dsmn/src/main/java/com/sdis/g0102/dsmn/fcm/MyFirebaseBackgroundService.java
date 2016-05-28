package com.sdis.g0102.dsmn.fcm;

/**
 * Created by André on 28/05/2016.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sdis.g0102.dsmn.MainActivity;
import com.sdis.g0102.dsmn.R;

public class MyFirebaseBackgroundService extends Service {

    private ValueEventListener handler;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot arg0) {
                postNotif(arg0.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

            public void onCancelled() {
            }
        };
    }

    private void postNotif(String notifString) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = R.drawable.notification_stop;
        Notification notification = new Notification(icon, "Firebase" + Math.random(), System.currentTimeMillis());
//		notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Context context = getApplicationContext();
        CharSequence contentTitle = "Background" + Math.random();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        //notification.setLatestEventInfo(context, contentTitle, notifString, contentIntent);
        mNotificationManager.notify(1, notification);
    }
}
