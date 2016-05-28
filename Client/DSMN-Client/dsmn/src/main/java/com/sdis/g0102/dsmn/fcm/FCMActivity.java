package com.sdis.g0102.dsmn.fcm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sdis.g0102.dsmn.R;

public class FCMActivity extends AppCompatActivity {

    public final static String TAG = "FCMActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fcm);
    }

    public void f1(View v) {
        FirebaseMessaging.getInstance().subscribeToTopic("events");
        Log.d(TAG, "Subscribed to events topic");
    }

    public void f2(View v) {
        Log.d(TAG, "InstanceID token: " + FirebaseInstanceId.getInstance().getToken());
    }

    public void f3(View v) {

    }
}
